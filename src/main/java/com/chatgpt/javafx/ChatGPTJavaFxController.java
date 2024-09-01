package com.chatgpt.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatGPTJavaFxController {

    @FXML
    private TextField textQuestion;

    @FXML
    private TextArea textareaAnswer;
    @FXML
    private TextArea textareaHistory;
    // Content history
    private List<String> contentHistory = new ArrayList<>();

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

    @FXML
    protected void onClearHistoryButtonClick() {
        // Clear the contents of textQuestion and textareaAnswer
        textQuestion.setText("");
        textareaAnswer.setText("");
        textareaHistory.setText("");
        contentHistory.clear();
    }

    private void fillAnswer() throws IOException {
        OpenAIClient aiClient = new OpenAIClient();
        String apiKey = aiClient.readApiKey();
        CloseableHttpClient client = aiClient.initOpenAIClient();

        String inputText = textQuestion.getText();
        String myAnswer = aiClient.getOpenAIResponseGpt4Mini(inputText, contentHistory, client, apiKey);
        if (contentHistory.size() > 0) {
            updateTextAreaHistory();
        }
        contentHistory.add("QUESTION");
        contentHistory.add(inputText);
        contentHistory.add("ANSWER");
        contentHistory.add(myAnswer);
        textareaAnswer.setText(myAnswer);
    }

    private void updateTextAreaHistory() {
        StringBuilder historyCombined = new StringBuilder();
        for (String entry : contentHistory) {
            historyCombined.append(entry).append("\n\n");
        }
        textareaHistory.setText(historyCombined.toString());
    }
}