package com.chatgpt.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Cross-platform Text-to-Speech using native OS tools:
 * - Windows: PowerShell + System.Speech
 * - macOS: say (selectable via -v <voice>)
 * - Linux: espeak-ng or espeak (selectable via -v <lang/voice>)
 *
 * Supports cancellation and language/voice selection.
 */
public class TextToSpeechService {
    private volatile Process ttsProcess;
    private final Object lock = new Object();

    // Optional configuration
    // Example language codes: "en-US", "en-GB", "de-DE", "fr-FR"
    private volatile String languageCode;
    // Example voice names:
    // - Windows: any installed System.Speech voice name
    // - macOS: voices like "Alex", "Daniel", "Anna", "Amelie", etc.
    // - Linux (espeak-ng/espeak): voices like "en-us", "en", "de", "fr", etc.
    private volatile String voiceName;

    /**
     * Set preferred language code, e.g. "en-US", "de-DE", "fr-FR".
     * If voiceName is also set, voiceName takes precedence.
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = (languageCode == null || languageCode.isBlank()) ? null : languageCode.trim();
    }

    /**
     * Set explicit voice name. If provided, this takes precedence over languageCode.
     */
    public void setVoiceName(String voiceName) {
        this.voiceName = (voiceName == null || voiceName.isBlank()) ? null : voiceName.trim();
    }

    /**
     * Convenience: speak with a one-off language code override (does not persist).
     */
    public void speakAsync(String text, String oneOffLanguageCode) throws IOException {
        String previous = this.languageCode;
        try {
            setLanguageCode(oneOffLanguageCode);
            speakAsync(text);
        } finally {
            setLanguageCode(previous);
        }
    }

    /**
     * Starts speaking asynchronously. If another TTS is running, it will be stopped first.
     */
    public void speakAsync(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) return;

        stop();

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            startWindowsTts(text);
        } else if (os.contains("mac")) {
            startMacTts(text);
        } else {
            startLinuxTts(text);
        }
    }

    private void startWindowsTts(String text) throws IOException {
        // Build PowerShell one-liner
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("Add-Type -AssemblyName System.Speech; ")
                  .append("$s=New-Object System.Speech.Synthesis.SpeechSynthesizer; ");

        if (voiceName != null) {
            String escapedVoice = voiceName.replace("'", "''");
            cmdBuilder.append("try{ $s.SelectVoice('").append(escapedVoice).append("') } catch{}; ");
        } else if (languageCode != null) {
            String escapedCulture = languageCode.replace("'", "''");
            cmdBuilder.append("try{ $s.SelectVoiceByHints([System.Speech.Synthesis.VoiceGender]::NotSet, ")
                      .append("[System.Speech.Synthesis.VoiceAge]::NotSet, 0, ")
                      .append("[System.Globalization.CultureInfo]::GetCultureInfo('").append(escapedCulture).append("')) } catch{}; ");
        }

        cmdBuilder.append("$s.Speak([Console]::In.ReadToEnd())");

        List<String> cmd = Arrays.asList(
            "powershell",
            "-NoProfile",
            "-Command",
            cmdBuilder.toString()
        );
        startProcessAndFeedStdin(cmd, text);
    }

    private void startMacTts(String text) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("say");

        String selectedVoice = voiceName;
        if ((selectedVoice == null || selectedVoice.isBlank()) && languageCode != null) {
            selectedVoice = mapMacVoiceForLanguage(languageCode);
        }

        if (selectedVoice != null && !selectedVoice.isBlank()) {
            command.add("-v");
            command.add(selectedVoice);
        }
        command.add(text);

        startProcess(command);
    }

    private void startLinuxTts(String text) throws IOException {
        // Prefer espeak-ng
        List<String> command = new ArrayList<>();
        String engine = "espeak-ng";
        boolean tryFallback = false;

        try {
            command = buildEspeakCommand("espeak-ng", text);
            startProcess(command);
        } catch (IOException first) {
            tryFallback = true;
            // fallback
            try {
                command = buildEspeakCommand("espeak", text);
                startProcess(command);
            } catch (IOException second) {
                IOException ex = new IOException("No TTS engine found. Install 'espeak-ng' or 'espeak'.");
                ex.addSuppressed(first);
                ex.addSuppressed(second);
                throw ex;
            }
        }
    }

    private List<String> buildEspeakCommand(String binary, String text) {
        List<String> command = new ArrayList<>();
        command.add(binary);

        String voice = (voiceName != null && !voiceName.isBlank())
                ? voiceName
                : normalizeLinuxLang(languageCode);

        if (voice != null && !voice.isBlank()) {
            command.add("-v");
            command.add(voice);
        }
        command.add(text);
        return command;
    }

    private String normalizeLinuxLang(String lang) {
        if (lang == null || lang.isBlank()) return null;
        // espeak typically prefers lowercase, dash-separated
        return lang.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private String mapMacVoiceForLanguage(String lang) {
        if (lang == null) return null;
        String code = lang.toLowerCase(Locale.ROOT);
        // Minimal, common defaults; users can override via setVoiceName()
        // en-US, en-GB, de-DE, fr-FR, it-IT, es-ES
        if (code.startsWith("en-us")) return "Alex";
        if (code.startsWith("en-gb")) return "Daniel";
        if (code.startsWith("de-de")) return "Anna";
        if (code.startsWith("fr-fr")) return "Amelie";
        if (code.startsWith("it-it")) return "Alice";
        if (code.startsWith("es-es")) return "Monica";
        // Fallback: let macOS pick default
        return null;
    }

    /**
     * Stops current speech if any.
     */
    public void stop() {
        synchronized (lock) {
            if (ttsProcess != null) {
                try {
                    ttsProcess.destroy();
                    if (!ttsProcess.waitFor(500, TimeUnit.MILLISECONDS)) {
                        ttsProcess.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    ttsProcess = null;
                }
            }
        }
    }

    private void startProcess(List<String> command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        synchronized (lock) {
            ttsProcess = p;
        }

        // Drain output to avoid blocking
        Thread drain = new Thread(() -> {
            try (InputStream is = p.getInputStream()) {
                is.transferTo(OutputStream.nullOutputStream());
            } catch (IOException ignore) { }
        }, "tts-drain");
        drain.setDaemon(true);
        drain.start();

        // Clear process when finished
        Thread waiter = new Thread(() -> {
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (lock) {
                if (ttsProcess == p) {
                    ttsProcess = null;
                }
            }
        }, "tts-wait");
        waiter.setDaemon(true);
        waiter.start();
    }

    private void startProcessAndFeedStdin(List<String> command, String text) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        synchronized (lock) {
            ttsProcess = p;
        }

        // Write the text to stdin (used for PowerShell)
        Thread writer = new Thread(() -> {
            try (OutputStream os = p.getOutputStream()) {
                os.write(text.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException ignore) { }
            try {
                p.getOutputStream().close();
            } catch (IOException ignore) { }
        }, "tts-feed");
        writer.setDaemon(true);
        writer.start();

        // Drain output to avoid blocking
        Thread drain = new Thread(() -> {
            try (InputStream is = p.getInputStream()) {
                is.transferTo(OutputStream.nullOutputStream());
            } catch (IOException ignore) { }
        }, "tts-drain");
        drain.setDaemon(true);
        drain.start();

        // Clear process when finished
        Thread waiter = new Thread(() -> {
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (lock) {
                if (ttsProcess == p) {
                    ttsProcess = null;
                }
            }
        }, "tts-wait");
        waiter.setDaemon(true);
        waiter.start();
    }
}
