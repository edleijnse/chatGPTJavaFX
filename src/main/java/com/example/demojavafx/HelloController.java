package com.example.demojavafx;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HelloController {

    @FXML
    private TextField textQuestion;

    @FXML
    private TextArea textareaAnswer;

    @FXML
    protected void onHelloButtonClick() {
        // Copy the content of textQuestion to textareaAnswer
        textareaAnswer.setText(textQuestion.getText());
    }

    @FXML
    protected void onCleanButtonClick() {
        // Clear the contents of textQuestion and textareaAnswer
        textQuestion.setText("");
        textareaAnswer.setText("");
    }
}