import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Quiz.fxml and quiz gameplay
 *
 * @author Antonio Salmeron
 * @since 4/20/2026
 */
public class QuizController {

    @FXML private Label categoryTitle;
    @FXML private Label progressLabel;
    @FXML private Label questionLabel;
    @FXML private Label feedbackLabel;
    @FXML private RadioButton optA;
    @FXML private RadioButton optB;
    @FXML private RadioButton optC;
    @FXML private RadioButton optD;
    @FXML private Button nextButton;

    private ToggleGroup answerGroup;
    private DatabaseManager db;
    private Stage stage;

    private int categoryId;
    private String categoryName;
    private int quizSize;
    private int attemptId;

    private List<int[]> questionIds;      // [questionId, correctAnswer]
    private List<String[]> questionTexts; // [question, A, B, C, D]

    private int currentIndex = 0;
    private int score = 0;

    @FXML
    public void initialize() {
        answerGroup = new ToggleGroup();
        optA.setToggleGroup(answerGroup);
        optB.setToggleGroup(answerGroup);
        optC.setToggleGroup(answerGroup);
        optD.setToggleGroup(answerGroup);

        questionIds = new ArrayList<>();
        questionTexts = new ArrayList<>();
    }

    public void setDependencies(DatabaseManager db, Stage stage) {
        this.db = db;
        this.stage = stage;
    }

    /**
     * Called after setDependencies. Loads questions from DB,
     * creates a quiz_attempts session, and displays the first question.
     */
    public void startQuiz(int categoryId, String categoryName, int quizSize) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.quizSize = quizSize;

        categoryTitle.setText(categoryName + " Quiz");

        // Create session in database
        attemptId = db.createQuizAttempt(SceneFactory.Session.userId, categoryId, quizSize);

        // Load questions
        try {
            ResultSet rs = db.getQuestionsByCategory(categoryId, quizSize);
            while (rs != null && rs.next()) {
                questionIds.add(new int[]{rs.getInt("id"), rs.getInt("answer")});
                questionTexts.add(new String[]{
                        rs.getString("question"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d")
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to load questions: " + e.getMessage());
        }

        if (questionTexts.isEmpty()) {
            feedbackLabel.setText("Failed to load questions.");
            nextButton.setDisable(true);
            return;
        }

        // Display first question
        showQuestion(0);
    }

    private void showQuestion(int index) {
        String[] q = questionTexts.get(index);
        progressLabel.setText("Question " + (index + 1) + " of " + quizSize);
        questionLabel.setText(q[0]);
        optA.setText(q[1]);
        optB.setText(q[2]);
        optC.setText(q[3]);
        optD.setText(q[4]);
        answerGroup.selectToggle(null);
        feedbackLabel.setText("");

        if (index == quizSize - 1) {
            nextButton.setText("Finish Quiz");
        }
    }

    @FXML
    private void handleNext() {
        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
        if (selected == null) {
            feedbackLabel.setText("Please select an answer.");
            return;
        }

        // Determine which option was picked
        int selectedOption = 0;
        if (selected == optA) selectedOption = 1;
        else if (selected == optB) selectedOption = 2;
        else if (selected == optC) selectedOption = 3;
        else if (selected == optD) selectedOption = 4;

        // Check if correct
        int correctAnswer = questionIds.get(currentIndex)[1];
        boolean isCorrect = (selectedOption == correctAnswer);
        if (isCorrect) score++;

        // Record answer in database
        int questionId = questionIds.get(currentIndex)[0];
        db.insertQuestionAttempt(attemptId, questionId, selectedOption, isCorrect);

        // Advance
        currentIndex++;

        if (currentIndex >= quizSize) {
            db.completeQuizAttempt(attemptId, score);
            Scene results = SceneFactory.BuildQuizResults(stage, db, score, quizSize, categoryName);
            stage.setScene(results);
        } else {
            showQuestion(currentIndex);
        }
    }
}