package com.chatgpt.javafx;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatGPTJavaFxController implements Initializable {

    @FXML
    private CheckBox chkSimple;
    @FXML
    private CheckBox chkExtended;
    @FXML
    private Label lblSimpleModel;
    @FXML
    private Label lblExtendedModel;

    @FXML
    private TextField textQuestion;

    @FXML
    private TextArea textareaAnswer;
    @FXML
    private TextArea textareaHistory;
    // Content history
    private List<String> contentHistory = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Controller initialized");
        if (chkSimple != null) {
            System.out.println("Initial chkSimple state: " + chkSimple.isSelected());
        } else {
            System.out.println("chkSimple is null");
        }
        if (chkExtended != null) {
            System.out.println("Initial chkExtended state: " + chkExtended.isSelected());
        } else {
            System.out.println("chkExtended is null");
        }
    }

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
        String myModel = "";
        if (chkExtended.isSelected()){
            myModel = "gpt-4o";
            chkSimple.setSelected(false);
            lblExtendedModel.setText("model gpt4-o");
            lblExtendedModel.setTextFill(Color.GREEN);
            lblSimpleModel.setText("model gpt4-0-mini NOT USED");
            lblSimpleModel.setTextFill(Color.RED);
        } else {
            myModel = "gpt-4o-mini";
            chkSimple.setSelected(true);
            chkExtended.setSelected(false);
            lblExtendedModel.setText("model gpt4-o NOT USED");
            lblExtendedModel.setTextFill(Color.RED);
            lblSimpleModel.setText("model gpt4-0-mini");
            lblSimpleModel.setTextFill(Color.GREEN);
        }
        String myAnswer = aiClient.getOpenAIResponseGpt4(myModel, inputText, contentHistory, client, apiKey);
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

    @FXML
    public void onChkSimpleClicked(ActionEvent actionEvent) {
        System.out.println("Simple CheckBox clicked"); // Füge dieses Print-Statement hinzu
        if (chkSimple.isSelected()) {
            System.out.println("Simple model wird verwendet.");
            chkExtended.setSelected(false);
            lblExtendedModel.setText("model gpt4-o NOT USED");
            lblExtendedModel.setTextFill(Color.RED);
            lblSimpleModel.setText("model gpt4-0-mini");
            lblSimpleModel.setTextFill(Color.GREEN);

        } else {
            System.out.println("Simple model wird nicht verwendet.");
            lblExtendedModel.setBlendMode(BlendMode.EXCLUSION);
            chkExtended.setSelected(false);
        }
    }

    @FXML
    public void onChkExtendedClicked(ActionEvent actionEvent) {
        System.out.println("Extended CheckBox clicked"); // Füge dieses Print-Statement hinzu
        if (chkExtended.isSelected()) {
            System.out.println("Extended model wird verwendet.");
            chkSimple.setSelected(false);
            lblExtendedModel.setText("model gpt4-o");
            lblExtendedModel.setTextFill(Color.GREEN);
            lblSimpleModel.setText("model gpt4-0-mini NOT USED");
            lblSimpleModel.setTextFill(Color.RED);
        } else {
            System.out.println("Extended model wird nicht verwendet.");
            chkSimple.setSelected(true);
        }
    }
}