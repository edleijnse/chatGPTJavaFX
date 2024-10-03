package com.chatgpt.javafx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.lang3.StringEscapeUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public  class OpenAIClient {
    public static String escapeHtml(String input) {
        // Escape standard HTML characters
        String escaped = StringEscapeUtils.escapeHtml4(input);

        // Optionally replace special characters if needed
        // Here we show how you might ensure they are encoded properly
        // For example purposes, you can define your own replacements
        escaped = escaped.replace("Ü", "Ü").replace("ü", "ü");

        return escaped;
    }


    public String readApiKey() throws IOException {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");
        // Get the directory one level above the current working directory
        String parentDir = new File(currentDir).getParent();
        // Read the API key file
        byte[] keyBytes = Files.readAllBytes(Paths.get(parentDir, "api_key"));
        // Convert to string and remove the end of line characters
        return new String(keyBytes).trim();
    }

    public CloseableHttpClient initOpenAIClient() throws IOException {
        return HttpClients.createDefault();
    }

    public String getOpenAIResponseGpt4(String model, String inputText, List<String> contentHistory, CloseableHttpClient client, String apiKey) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Create the root node
        ObjectNode rootNode = mapper.createObjectNode();
        // Add the 'model' field
        rootNode.put("model", model);
        // Create the 'messages' array node
        ArrayNode messagesNode = rootNode.putArray("messages");

        // Add the content history messages
        for (String historicalContent : contentHistory) {
            ObjectNode historyMessage = mapper.createObjectNode();
            historyMessage.put("role", "user"); // Assuming historical messages are from the user
            historyMessage.put("content", escapeHtml(historicalContent));
            messagesNode.add(historyMessage);
        }

        // Create a new message object for the current inputText
        ObjectNode message1 = mapper.createObjectNode();
        message1.put("role", "user"); // Use the correct role
        message1.put("content", escapeHtml(inputText));
        // Add the message object to the 'messages' array
        messagesNode.add(message1);

        // Convert the rootNode to a JSON string
        String requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        HttpPost request = new HttpPost("https://api.openai.com/v1/chat/completions");
        request.setHeader("Authorization", "Bearer " + apiKey);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(requestBody));

        try (CloseableHttpResponse response = client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                String errorResponse = EntityUtils.toString(response.getEntity());
                System.err.println("Error: " + errorResponse);
                return "Error: " + errorResponse; // Or throw an exception
            }
            JsonNode responseData = mapper.readTree(response.getEntity().getContent());
            JsonNode choicesNode = responseData.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                String completion = choicesNode.get(0).path("message").path("content").asText();
                return completion;
            }
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            return "Error: " + e.getMessage(); // Or throw an exception
        }
        return "";
    }

    public void main(String[] args) {
        try {
            String apiKey = readApiKey();
            CloseableHttpClient client = initOpenAIClient();

            // Example usage
            String inputText = "Best things to see in Paris, in categories";
            String inputText2 = "Best things to see in Paris";
            // getOpenAIResponseGpt4(inputText, client, apiKey);
            // Content history
            List<String> contentHistory = new ArrayList<>();
            contentHistory.add("Hello!");
            contentHistory.add("How can you assist me today?");
            String response = getOpenAIResponseGpt4("gpt4-o-mini",inputText, contentHistory, client, apiKey);
            System.out.println("Answer: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}