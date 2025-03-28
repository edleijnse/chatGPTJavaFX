# Refactor To Do List

## File: refactor.md

This document outlines refactoring tasks for the JavaFX application, based on the provided FXML structure.  Each item includes a checkbox to track progress.

**1. Model Selection Logic Simplification:**

* [ ] **Problem:** The FXML uses separate CheckBoxes and Labels for each model, leading to repetitive code and making it harder to manage models.
* [ ] **Solution:** Replace individual CheckBoxes with a ChoiceBox or similar control for a more streamlined UI and simplified controller logic. Display the model description dynamically.

**2. Answer and History Area Consolidation:**

* [ ] **Problem:** Two separate TextAreas (`textareaAnswer`, `textareaHistory`) likely lead to duplicated code and unclear interaction.
* [ ] **Solution:** Merge answer and history into a single TextArea, appending new answers with clear separators or formatting.

**3. Controller Method Consolidation:**

* [ ] **Problem:** Individual event handlers (`onChkSimpleClicked`, etc.) likely contain redundant logic.
* [ ] **Solution:** Consolidate handlers into a single event handler for the new ChoiceBox, streamlining event handling.

**4. Improved Layout Management:**

* [ ] **Problem:** The GridPane for model selection seems overly complex and mixes layout concerns within controls.
* [ ] **Solution:**  Explore simpler layouts (VBox, HBox) for improved clarity and adaptability.

**5. Clearer Naming Conventions:**

* [ ] **Problem:** Control IDs like `chkSimple`, `lblSimpleModel` are not very descriptive.
* [ ] **Solution:** Use more meaningful names (e.g., `modelChoiceBox`, `chatHistoryTextArea`).


This list provides a starting point, and more items might be added during implementation. Remember to update the checkboxes as you complete each task!