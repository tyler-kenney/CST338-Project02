import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuizScoreCalculatorTest {

    @Test
    void calculateIncorrectAnswersReturnsCorrectValue() {
        QuizScoreCalculator calculator = new QuizScoreCalculator();
        assertEquals(2, calculator.calculateIncorrectAnswers(8, 10));
    }

    @Test
    void calculateIncorrectAnswersReturnsZeroWhenAllAreCorrect() {
        QuizScoreCalculator calculator = new QuizScoreCalculator();
        assertEquals(0, calculator.calculateIncorrectAnswers(10, 10));
    }
}