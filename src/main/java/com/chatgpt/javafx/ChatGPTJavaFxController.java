package com.chatgpt.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class ChatGPTJavaFxController {

    @FXML
    private TextField textQuestion;

    @FXML
    private TextArea textareaAnswer;

    @FXML
    protected void onButtonAskClick() throws IOException {
        // communicate with ChatGPT
        try {
            fillAnswer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onCleanButtonClick() {
        // Clear the contents of textQuestion and textareaAnswer
        textQuestion.setText("");
        textareaAnswer.setText("");
    }
    private void fillAnswer() throws IOException {
        OpenAIClient aiClient = new OpenAIClient();
        String apiKey = aiClient.readApiKey();
        CloseableHttpClient client = aiClient.initOpenAIClient();

        String inputText = textQuestion.getText();
        String myAnswer = aiClient.getOpenAIResponseGpt4Mini(inputText, client, apiKey);

        textareaAnswer.setText(myAnswer);
    }
}