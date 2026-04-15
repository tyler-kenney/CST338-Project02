import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Unit tests for DatabaseManager CRUD functionality
 *
 * @author Antonio Salmeron
 * @since 4/14/2026
 * @version 0.1.0
 */
public class DatabaseManagerTest {

    private DatabaseManager db;

    @BeforeEach
    void setUp() {
        db = new DatabaseManager("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void seedData_usersExist() {
        List<String> users = db.getAllUsernames();
        assertEquals(3, users.size());
        assertTrue(users.contains("Stewie"));
        assertTrue(users.contains("Brian"));
        assertTrue(users.contains("admin"));
    }

    @Test
    void seedData_categoriesExist() {
        List<String> categories = db.getAllCategories();
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Java"));
        assertTrue(categories.contains("C++"));
    }

    @Test
    void seedData_questionsExist() {
        int javaCatId = db.getCategoryId("Java");
        int cppCatId = db.getCategoryId("C++");
        assertEquals(5, db.getQuestionCount(javaCatId));
        assertEquals(5, db.getQuestionCount(cppCatId));
    }

    @Test
    void insertUser_thenCanLogin() {
        db.insertUserItem("newuser", "pass123", 0);
        assertTrue(db.isUsername("newuser"));
        assertTrue(db.isPassword("newuser", "pass123"));
        assertFalse(db.isAdmin("newuser", "pass123"));
    }

    @Test
    void insertUser_adminRole() {
        db.insertUserItem("bossuser", "adminpass", 1);
        assertTrue(db.isAdmin("bossuser", "adminpass"));
        assertEquals(1, db.getUsertype("bossuser", "adminpass"));
    }

    @Test
    void isUsername_returnsFalseForNonexistent() {
        assertFalse(db.isUsername("nobody"));
    }

    @Test
    void isPassword_wrongPasswordFails() {
        assertTrue(db.isPassword("Stewie", "FamilyGuy1"));
        assertFalse(db.isPassword("Stewie", "wrongpass"));
    }

    @Test
    void getUserId_returnsValidId() {
        int id = db.getUserId("Stewie");
        assertTrue(id > 0);
        assertEquals(-1, db.getUserId("nonexistent"));
    }

    @Test
    void updateUserPassword_newPasswordWorks() {
        db.insertUserItem("testguy", "oldpass", 0);
        assertTrue(db.isPassword("testguy", "oldpass"));

        db.updateUserPassword("testguy", "newpass");
        assertFalse(db.isPassword("testguy", "oldpass"));
        assertTrue(db.isPassword("testguy", "newpass"));
    }

    @Test
    void deleteUser_removesFromDatabase() {
        db.insertUserItem("deleteme", "pass", 0);
        assertTrue(db.isUsername("deleteme"));

        assertTrue(db.deleteUser("deleteme"));
        assertFalse(db.isUsername("deleteme"));
    }

    @Test
    void insertCategory_appearsInList() {
        int id = db.insertCategory("Python", "Python questions");
        assertTrue(id > 0);
        assertTrue(db.getAllCategories().contains("Python"));
    }

    @Test
    void getCategoryId_matchesByName() {
        int javaId = db.getCategoryId("Java");
        int cppId = db.getCategoryId("C++");
        assertTrue(javaId > 0);
        assertTrue(cppId > 0);
        assertNotEquals(javaId, cppId);
        assertEquals(-1, db.getCategoryId("Nonexistent"));
    }

    @Test
    void updateCategory_changesName() {
        int id = db.getCategoryId("Java");
        db.updateCategory(id, "Java SE", "Updated description");

        assertTrue(db.getAllCategories().contains("Java SE"));
        assertFalse(db.getAllCategories().contains("Java"));
    }

    @Test
    void deleteCategory_removesFromList() {
        int id = db.insertCategory("Temporary", "Will be deleted");
        assertTrue(db.deleteCategory(id));
        assertFalse(db.getAllCategories().contains("Temporary"));
    }

    @Test
    void insertQuestion_increasesCount() {
        int catId = db.getCategoryId("Java");
        int before = db.getQuestionCount(catId);

        int adminId = db.getUserId("admin");
        db.insertQuestion(catId, "What is JVM?",
                "Java Virtual Machine", "Java Visual Maker",
                "Java Version Manager", "Java Variable Map",
                1, adminId);

        assertEquals(before + 1, db.getQuestionCount(catId));
    }

    @Test
    void getQuestionsByCategory_returnsCorrectCount() throws SQLException {
        int catId = db.getCategoryId("Java");
        ResultSet rs = db.getQuestionsByCategory(catId, 3);

        int count = 0;
        while (rs != null && rs.next()) {
            count++;
            // verify each row has the expected columns
            assertNotNull(rs.getString("question"));
            assertNotNull(rs.getString("option_a"));
            assertTrue(rs.getInt("answer") >= 1 && rs.getInt("answer") <= 4);
        }
        assertEquals(3, count);
    }

    @Test
    void deleteQuestion_decreasesCount() {
        int catId = db.getCategoryId("C++");
        int before = db.getQuestionCount(catId);

        db.deleteQuestion(6);
        assertEquals(before - 1, db.getQuestionCount(catId));
    }

    @Test
    void updateQuestion_changesText() throws SQLException {
        db.updateQuestion(1, "UPDATED QUESTION?",
                "New A", "New B", "New C", "New D", 3);

        int catId = db.getCategoryId("Java");
        ResultSet rs = db.getQuestionsByCategory(catId, 5);
        boolean found = false;
        while (rs != null && rs.next()) {
            if (rs.getString("question").equals("UPDATED QUESTION?")) {
                found = true;
                assertEquals("New A", rs.getString("option_a"));
                assertEquals(3, rs.getInt("answer"));
            }
        }
        assertTrue(found);
    }

    @Test
    void fullQuizFlow_createAnswerCompleteAndLeaderboard() {
        int userId = db.getUserId("Stewie");
        int catId = db.getCategoryId("Java");

        // Step 1: start a session
        int attemptId = db.createQuizAttempt(userId, catId, 5);
        assertTrue(attemptId > 0);

        // Step 2: answer 5 questions — 3 right, 2 wrong
        db.insertQuestionAttempt(attemptId, 1, 2, true);   // correct
        db.insertQuestionAttempt(attemptId, 2, 1, true);   // correct
        db.insertQuestionAttempt(attemptId, 3, 2, true);   // correct
        db.insertQuestionAttempt(attemptId, 4, 4, false);  // wrong
        db.insertQuestionAttempt(attemptId, 5, 1, false);  // wrong

        // Step 3: verify correct count
        assertEquals(3, db.getCorrectCount(attemptId));

        // Step 4: complete the session
        assertTrue(db.completeQuizAttempt(attemptId, 3));

        // Step 5: verify it shows in leaderboard
        List<Leaderboard> leaders = db.getAllQuizes();
        assertFalse(leaders.isEmpty());
        assertEquals(3.0, leaders.get(0).getPoints());
    }

    @Test
    void incompleteQuiz_doesNotAppearInLeaderboard() {
        int userId = db.getUserId("Brian");
        int catId = db.getCategoryId("C++");

        // Start a session but never call completeQuizAttempt
        int attemptId = db.createQuizAttempt(userId, catId, 10);
        db.insertQuestionAttempt(attemptId, 6, 2, true);

        // Leaderboard only shows completed sessions
        List<Leaderboard> leaders = db.getAllQuizes();
        assertTrue(leaders.isEmpty());
    }

    @Test
    void deleteQuizAttempt_removesSession() {
        int userId = db.getUserId("admin");
        int catId = db.getCategoryId("Java");

        int attemptId = db.createQuizAttempt(userId, catId, 10);
        assertTrue(db.deleteQuizAttempt(attemptId));
    }

    @Test
    void deleteQuestionAttempts_removesAllAnswers() {
        int userId = db.getUserId("Stewie");
        int catId = db.getCategoryId("Java");
        int attemptId = db.createQuizAttempt(userId, catId, 2);

        db.insertQuestionAttempt(attemptId, 1, 2, true);
        db.insertQuestionAttempt(attemptId, 2, 1, true);
        assertEquals(2, db.getCorrectCount(attemptId));

        db.deleteQuestionAttempts(attemptId);
        assertEquals(0, db.getCorrectCount(attemptId));
    }
}