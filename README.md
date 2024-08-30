CHATGPT Client written in JAVAFX   

Prerequisites:
For GUI development, download Gluon Scene Builder:
https://gluonhq.com/products/scene-builder/

add jvm options  
--module-path "C:\Program Files\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.fxml


You need an api_key, it will be red from the parent directory
To get an API key for OpenAI, follow these steps:
Go to OpenAI’s Platform website: Visit https://platform.openai.com.
Sign in or create an account: If you don’t have an account, you’ll need to sign up.
Access API keys: Click on your profile icon in the top-right corner and select "View API Keys"1.
Create a new key: Click “Create New Secret Key” to generate a new API key2.
Copy and store the key: Make sure to copy and store your API key in a safe place
  
To run your jar file (example) make a .bat or .sh file:  
java --module-path "C:\Program Files\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar -jar C:\chatgpt\client\chatGPTJavaFX-1.0-SNAPSHOT-shaded.jar
