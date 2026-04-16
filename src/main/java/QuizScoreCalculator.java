public class QuizScoreCalculator {

    public int calculateIncorrectAnswers(int correctAnswers, int totalQuestions) {
        return totalQuestions - correctAnswers;
    }

    public int calculateScore(int correctAnswers) {
        return correctAnswers;
    }
}