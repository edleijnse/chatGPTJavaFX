package com.chatgpt.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cross-platform Text-to-Speech using native OS tools:
 * - Windows: PowerShell + System.Speech
 * - macOS: say
 * - Linux: espeak-ng or espeak
 *
 * Runs processes in background and supports cancellation.
 */
public class TextToSpeechService {
    private volatile Process ttsProcess;
    private final Object lock = new Object();

    /**
     * Starts speaking asynchronously. If another TTS is running, it will be stopped first.
     */
    public void speakAsync(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) return;

        stop();

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            List<String> cmd = Arrays.asList(
                "powershell",
                "-NoProfile",
                "-Command",
                "Add-Type -AssemblyName System.Speech; $speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; $speak.Speak([Console]::In.ReadToEnd())"
            );
            startProcessAndFeedStdin(cmd, text);
        } else if (os.contains("mac")) {
            startProcess(Arrays.asList("say", text));
        } else {
            // Try espeak-ng, then espeak
            try {
                startProcess(Arrays.asList("espeak-ng", text));
            } catch (IOException first) {
                try {
                    startProcess(Arrays.asList("espeak", text));
                } catch (IOException second) {
                    IOException ex = new IOException("No TTS engine found. Install 'espeak-ng' or 'espeak'.");
                    ex.addSuppressed(first);
                    ex.addSuppressed(second);
                    throw ex;
                }
            }
        }
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
