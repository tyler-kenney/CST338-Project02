import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class CategorySelectionController {

    @FXML
    private ListView<String> categoryListView;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        categoryListView.setItems(FXCollections.observableArrayList(
                "Java Basics",
                "OOP",
                "Databases",
                "Data Structures"
        ));
    }

    @FXML
    private void handleStartQuiz() {
        String selected = categoryListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Please select a category.");
            return;
        }

        messageLabel.setText("Starting quiz: " + selected);

    }

    @FXML
    private void handleReturnToMenu() {
        messageLabel.setText("Returning to main menu...");


    }
}