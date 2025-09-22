package com.chatgpt.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.awt.GraphicsEnvironment; // Headless-Check

public class ChatGPTJavaFX extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatGPTJavaFX.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 580, 780);
        stage.setTitle("Chatbot JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Frühzeitiger Headless-Check mit klarer Diagnose statt QuantumRenderer-Crash.
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("[ERROR] Kein grafisches Display verfügbar (Headless-Umgebung).");
            System.err.println("Starte in einer Desktop-Session (X11/Wayland) oder nutze ein virtuelles Display, z. B.:");
            System.err.println("  xvfb-run -s \"-screen 0 1024x768x24\" java -jar deine-app.jar");
            System.exit(1);
        }

        // Robuster Fallback: Software-Rendering bevorzugen, falls GPU/GL nicht verfügbar ist.
        // Muss vor Application.launch() gesetzt sein.
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "true"); // optional: detaillierte Diagnose in der Konsole

        launch();
    }
}