
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Unittests {

    @FXML
    private Label scoreLabel;

    @FXML
    private Label correctLabel;

    @FXML
    private Label incorrectLabel;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        setResults(8, 10);
    }

    public void setResults(int correctAnswers, int totalQuestions) {
        int incorrectAnswers = totalQuestions - correctAnswers;
        scoreLabel.setText("Score: " + correctAnswers + "/" + totalQuestions);
        correctLabel.setText("Correct Answers: " + correctAnswers);
        incorrectLabel.setText("Incorrect Answers: " + incorrectAnswers);
    }

    @FXML
    private void handleReturnToMenu() {
        messageLabel.setText("Returning to main menu...");
    }

    @FXML
    private void handleViewLeaderboard() {
        messageLabel.setText("Opening leaderboard...");
    }
}