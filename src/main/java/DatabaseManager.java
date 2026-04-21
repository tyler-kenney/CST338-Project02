/**
 * This class will take inputted data and read, update, or delete the database.
 *
 * @ author: Tyler Kenney
 * @ author: Antonio Salmeron
 * @ since:  4/10/2026
 * @ version 0.2.0
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

  // " jdbc : sqlite :" tells JDBC which driver to use .
  // The path after it is the database file location .
  private static final String DB_URL = "jdbc:sqlite:mydb.db";
  private Connection connection;

  /**
   * Constructor sets up the database.
   */
  public DatabaseManager() {
    try {
      // Opens ( or creates ) app . db in the project root
      connection = DriverManager.getConnection(DB_URL);
      connection.createStatement().execute("PRAGMA foreign_keys = ON");
      System.out.println(" Database connected . ");
      createTables(); // set up schema on first run
      seedData();     // seed data only if tables are empty
    } catch (SQLException e) {
      System.out.println(" Connection failed : " + e.getMessage());
    }
  }

  // Pass "jdbc:sqlite::memory:" to get a fresh in-memory database
  // that vanishes when the connection closes. No file created.
  public DatabaseManager(String dbUrl) {
    try {
      connection = DriverManager.getConnection(dbUrl);
      connection.createStatement().execute("PRAGMA foreign_keys = ON");
      System.out.println(" Database connected . ");
      createTables();
      seedData();
    } catch (SQLException e) {
      System.out.println(" Connection failed : " + e.getMessage());
    }
  }

  /**
   * This method closes the database.
   */
  public void close() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method creates an SQL table if none exists.
   */
  private void createTables() {
    // Text blocks ( Java 15+) keep multi - line SQL readable
    //type = 0 or 1 (user or admin)
    String users_sql = """
        CREATE TABLE IF NOT EXISTS users
         (
             id       INTEGER PRIMARY KEY AUTOINCREMENT,
             username TEXT    NOT NULL UNIQUE,
             password TEXT    NOT NULL,
             usertype INTEGER NOT NULL
         )
        """;

    String categories_sql = """
        CREATE TABLE IF NOT EXISTS categories (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          category TEXT NOT NULL UNIQUE,
          description TEXT
        )
        """;

    String questions_sql = """
        CREATE TABLE IF NOT EXISTS questions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            category_id INTEGER NOT NULL REFERENCES categories(id), --foreign key
            question TEXT NOT NULL UNIQUE,
            option_a TEXT NOT NULL,
            option_b TEXT NOT NULL,
            option_c TEXT NOT NULL,
            option_d TEXT NOT NULL,
            answer INTEGER NOT NULL,    --1-4 (a-d)
            created DATETIME DEFAULT CURRENT_TIMESTAMP,
            user_id INTEGER REFERENCES users(id) --foreign key
        )
        """;

    String quiz_attempts_sql = """
        CREATE TABLE IF NOT EXISTS quiz_attempts
         (
             id              INTEGER PRIMARY KEY AUTOINCREMENT,
             user_id         INTEGER NOT NULL REFERENCES users (id),
             category_id     INTEGER NOT NULL REFERENCES categories (id),
             score           INTEGER DEFAULT 0,
             total_questions INTEGER DEFAULT 10,
             started_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
             completed_at    DATETIME
         )
        """;

    String question_attempts_sql = """
        CREATE TABLE IF NOT EXISTS question_attempts
         (
             id              INTEGER PRIMARY KEY AUTOINCREMENT,
             question_id     INTEGER NOT NULL REFERENCES questions (id),
             attempt_id      INTEGER NOT NULL REFERENCES quiz_attempts (id),
             selected_option INTEGER,
             is_correct      INTEGER NOT NULL DEFAULT 0 --0 for no, 1 for yes
         )
        """;

    try (Statement stmt = connection.createStatement()) {
      stmt.execute(users_sql);
      stmt.execute(categories_sql);
      stmt.execute(questions_sql);
      stmt.execute(quiz_attempts_sql);
      stmt.execute(question_attempts_sql);
    } catch (SQLException e) {
      System.out.println(" createTables failed : " + e.getMessage());
    }
  }

  private void seedData() {
    try (Statement stmt = connection.createStatement()) {
      ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
      if (rs.next() && rs.getInt(1) > 0) return;
    } catch (SQLException e) {
      System.out.println("seedData check failed: " + e.getMessage());
      return;
    }

    String insert_users = """
        INSERT OR IGNORE INTO users (username, password, usertype)
                           VALUES ('Stewie', 'FamilyGuy1', 0),
                                  ('Brian', 'FamilyGuy2', 1),
                                  ('admin', '123', 1)
        """;

    String insert_category_1 = """
        INSERT OR IGNORE INTO categories (category, description)
        VALUES ('Java', 'These are questions related to java programming')
        """;
    String insert_category_2 = """
        INSERT OR IGNORE INTO categories (category, description)
        VALUES ('C++', 'These are questions related to C++ programming')
        """;
    String insert_questions_sql = """
        INSERT OR IGNORE INTO questions (
          category_id, question, option_a, option_b, option_c, option_d, answer, user_id
        ) VALUES
          ((SELECT id FROM categories WHERE category = 'Java'),
           'Which of these is NOT a Java primitive type?',
           'int', 'String', 'boolean', 'double',
           2, (SELECT id FROM users WHERE username = 'admin')),

          ((SELECT id FROM categories WHERE category = 'Java'),
           'What is the default value of an int in Java?',
           '0', 'null', '1', 'undefined',
           1, (SELECT id FROM users WHERE username = 'admin')),

          ((SELECT id FROM categories WHERE category = 'Java'),
           'Which keyword is used to inherit a class in Java?',
           'implements', 'extends', 'inherits', 'super',
           2, (SELECT id FROM users WHERE username = 'admin')),

          ((SELECT id FROM categories WHERE category = 'Java'),
           'What does the final keyword prevent on a class?',
           'Instantiation', 'Subclassing', 'Method calls', 'Importing',
           2, (SELECT id FROM users WHERE username = 'admin')),

          ((SELECT id FROM categories WHERE category = 'Java'),
           'Which access modifier makes a field visible only within its own class?',
           'public', 'protected', 'private', 'default',
           3, (SELECT id FROM users WHERE username = 'admin')),

          ((SELECT id FROM categories WHERE category = 'C++'),
           'Which is a C++ primitive type that Java does NOT have?',
           'int', 'unsigned int', 'boolean', 'double',
           2, (SELECT id FROM users WHERE username = 'Brian')),

          ((SELECT id FROM categories WHERE category = 'C++'),
           'What does the -> operator do in C++?',
           'Compares two values', 'Accesses a member through a pointer', 'Creates a reference', 'Assigns a value',
           2, (SELECT id FROM users WHERE username = 'Brian')),

          ((SELECT id FROM categories WHERE category = 'C++'),
           'How do you allocate memory on the heap in C++?',
           'malloc only', 'new keyword', 'alloc()', 'heap()',
           2, (SELECT id FROM users WHERE username = 'Brian')),

          ((SELECT id FROM categories WHERE category = 'C++'),
           'What is a destructor in C++?',
           'A method that creates objects', 'A method called when an object is deleted',
           'A method that returns memory size', 'A static initializer',
           2, (SELECT id FROM users WHERE username = 'Brian')),

          ((SELECT id FROM categories WHERE category = 'C++'),
           'Which symbol denotes a reference variable in C++?',
           '*', '&', '#', '@',
           2, (SELECT id FROM users WHERE username = 'Brian'))
        """;

    try (Statement stmt = connection.createStatement()) {
      stmt.execute(insert_users);
      stmt.execute(insert_category_1);
      stmt.execute(insert_category_2);
      stmt.execute(insert_questions_sql);
    } catch (SQLException e) {
      System.out.println("seedData failed: " + e.getMessage());
    }
  }

  /**
   * CRUD - Create (insert). For Table users
   *
   * @param username
   */
  public void insertUserItem(String username, String password, int usertype) {
    if (username == null || password == null) {
      System.out.println(" Username and password are null . ");
      return;
    }
    String testSql = "INSERT INTO users (username, password, usertype) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(testSql)) {
      pstmt.setString(1, username);
      pstmt.setString(2, password);  // bind parameter 1 to 'name '
      pstmt.setInt(3, usertype);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println(" insertItem failed : " + e.getMessage());
    }
  }

  /**
   * This method checks if parameter is in database table users
   *
   * @param username
   * @return
   */
  public boolean isUsername(String username) {
    List<String> users = new ArrayList<>(getAllUsernames());
    for (String user : users) {
      if (user.equals(username)) {
        return true;
      }
    }
    return false;
  }

  /**
   * CRUD - Read (Select)
   *
   * @param username
   * @return
   */
  public String getPassword(String username) {
    String password = "";

    String sql = "SELECT * FROM users WHERE username = '" + username + "'";
    try (Statement statement = connection.createStatement()) {
      //statement.setString(1, username);
      ResultSet rs = statement.executeQuery(sql);
      password = (rs.getString("password"));
    } catch (SQLException e) {
      System.out.println(" getPassword failed : " + e.getMessage());
    }
    return password;
  }

  public Boolean isPassword(String username, String password) {
    return !getPassword(username).isEmpty() && password.equals(getPassword(username));
  }

  /**
   * CRUD - Read (Select)
   *
   * @param username
   * @param password
   * @return
   */
  public int getUsertype(String username, String password) {
    int usertype = 0;
    String sql = "SELECT usertype FROM users WHERE username = '"
            + username + "' AND password = '" + password + "'";
    try (Statement statement = connection.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      usertype = (rs.getInt("usertype"));
    } catch (SQLException e) {
      System.out.println("getUsertype failed : " + e.getMessage());
    }
    return usertype;
  }

  public Boolean isAdmin(String username, String password) {
    return getUsertype(username, password) == 1;
  }

  /**
   * CRUD - Read (Select)
   *
   * @return
   */
  public List<String> getAllUsernames() {
    List<String> users = new ArrayList<>();
    String sql = " SELECT username FROM users";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) { // move to next row
        users.add(rs.getString("username")); // read column by name
      }
    } catch (SQLException e) {
      System.out.println(" getAllUsernames failed : " + e.getMessage());
    }
    return users;
  }

  public List<Leaderboard> getAllQuizes() {
    List<Leaderboard> leaders = new ArrayList<>();
    String sql = """
        SELECT user_id, category_id, MAX(score) as score
        FROM quiz_attempts
        WHERE completed_at IS NOT NULL
        GROUP BY user_id, category_id
        ORDER BY score DESC
        LIMIT 10
        """;
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        int userID = (rs.getInt("user_id"));
        int categoryID = (rs.getInt("category_id"));
        double points = rs.getDouble("score");
        leaders.add(new Leaderboard(userID, categoryID, points));
      }
    } catch (SQLException e) {
      System.out.println(" getAllQuizes failed : " + e.getMessage());
    }
    return leaders;
  }

  /**
   * Get a user's primary key by username.
   *
   * @return primary key. -1 if not found.
   */
  public int getUserId(String username) {
    String sql = "SELECT id FROM users WHERE username = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, username);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return rs.getInt("id");
    } catch (SQLException e) {
      System.out.println("getUserId failed: " + e.getMessage());
    }
    return -1;
  }

  /** Update a user's password. */
  public boolean updateUserPassword(String username, String newPassword) {
    String sql = "UPDATE users SET password = ? WHERE username = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, newPassword);
      ps.setString(2, username);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("updateUserPassword failed: " + e.getMessage());
      return false;
    }
  }

  /** Delete a user by username. */
  public boolean deleteUser(String username) {
    String sql = "DELETE FROM users WHERE username = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, username);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("deleteUser failed: " + e.getMessage());
      return false;
    }
  }

  /** Get all category names. */
  public List<String> getAllCategories() {
    List<String> categories = new ArrayList<>();
    String sql = "SELECT category FROM categories";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        categories.add(rs.getString("category"));
      }
    } catch (SQLException e) {
      System.out.println("getAllCategories failed: " + e.getMessage());
    }
    return categories;
  }

  /** Get a category's ID by name. Returns -1 if not found. */
  public int getCategoryId(String categoryName) {
    String sql = "SELECT id FROM categories WHERE category = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, categoryName);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return rs.getInt("id");
    } catch (SQLException e) {
      System.out.println("getCategoryId failed: " + e.getMessage());
    }
    return -1;
  }

  /**
   * Insert a new category.
   *
   * @return category ID.
   */
  public int insertCategory(String category, String description) {
    String sql = "INSERT OR IGNORE INTO categories (category, description) VALUES (?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, category);
      ps.setString(2, description);
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) return keys.getInt(1);
    } catch (SQLException e) {
      System.out.println("insertCategory failed: " + e.getMessage());
    }
    return -1;
  }

  /** Update a category's name and description. */
  public boolean updateCategory(int id, String newName, String newDescription) {
    String sql = "UPDATE categories SET category = ?, description = ? WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, newName);
      ps.setString(2, newDescription);
      ps.setInt(3, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("updateCategory failed: " + e.getMessage());
      return false;
    }
  }

  /** Delete category using ID. */
  public boolean deleteCategory(int id) {
    String sql = "DELETE FROM categories WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("deleteCategory failed: " + e.getMessage());
      return false;
    }
  }

  /** Insert a question.
   *
   * @return question ID.
   */
  public int insertQuestion(int categoryId, String question,
                            String optA, String optB, String optC, String optD,
                            int answer, int userId) {
    String sql = """
        INSERT OR IGNORE INTO questions
        (category_id, question, option_a, option_b, option_c, option_d, answer, user_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, categoryId);
      ps.setString(2, question);
      ps.setString(3, optA);
      ps.setString(4, optB);
      ps.setString(5, optC);
      ps.setString(6, optD);
      ps.setInt(7, answer);
      ps.setInt(8, userId);
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) return keys.getInt(1);
    } catch (SQLException e) {
      System.out.println("insertQuestion failed: " + e.getMessage());
    }
    return -1;
  }

  /** Get random questions from a category */
  public ResultSet getQuestionsByCategory(int categoryId, int limit) {
    String sql = "SELECT * FROM questions WHERE category_id = ? ORDER BY RANDOM() LIMIT ?";
    try {
      PreparedStatement ps = connection.prepareStatement(sql);
      ps.setInt(1, categoryId);
      ps.setInt(2, limit);
      return ps.executeQuery();
    } catch (SQLException e) {
      System.out.println("getQuestionsByCategory failed: " + e.getMessage());
      return null;
    }
  }

  /** Count how many questions exist in a category. */
  public int getQuestionCount(int categoryId) {
    String sql = "SELECT COUNT(*) FROM questions WHERE category_id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, categoryId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      System.out.println("getQuestionCount failed: " + e.getMessage());
    }
    return 0;
  }

  /** Update a question's text, options, and answer. */
  public boolean updateQuestion(int id, String question,
                                String optA, String optB, String optC, String optD,
                                int answer) {
    String sql = """
        UPDATE questions SET question = ?, option_a = ?, option_b = ?,
        option_c = ?, option_d = ?, answer = ? WHERE id = ?
        """;
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, question);
      ps.setString(2, optA);
      ps.setString(3, optB);
      ps.setString(4, optC);
      ps.setString(5, optD);
      ps.setInt(6, answer);
      ps.setInt(7, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("updateQuestion failed: " + e.getMessage());
      return false;
    }
  }

  public List<String> getCategoryNames() {
    List<String> categories = new ArrayList<>();
    String sql = "SELECT category FROM categories ORDER BY id";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        categories.add(rs.getString("category"));
      }
    } catch (SQLException e) {
      System.out.println("getCategoryNames failed: " + e.getMessage());
    }
    return categories;
  }

  /** Delete a question by ID. */
  public boolean deleteQuestion(int id) {
    String sql = "DELETE FROM questions WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("deleteQuestion failed: " + e.getMessage());
      return false;
    }
  }

  /**
   * Start a new quiz session. Returns the session ID.
   * Call when user picks a category and begins a quiz.
   * completed_at stays NULL until completeQuizAttempt() is called.
   */
  public int createQuizAttempt(int userId, int categoryId, int totalQuestions) {
    String sql = "INSERT INTO quiz_attempts (user_id, category_id, total_questions) VALUES (?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, userId);
      ps.setInt(2, categoryId);
      ps.setInt(3, totalQuestions);
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) return keys.getInt(1);
    } catch (SQLException e) {
      System.out.println("createQuizAttempt failed: " + e.getMessage());
    }
    return -1;
  }

  /**
   * Finish a quiz session with the final score.
   * Sets completed_at to now so we know it's done.
   */
  public boolean completeQuizAttempt(int attemptId, int score) {
    String sql = "UPDATE quiz_attempts SET score = ?, completed_at = CURRENT_TIMESTAMP WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, score);
      ps.setInt(2, attemptId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("completeQuizAttempt failed: " + e.getMessage());
      return false;
    }
  }

  /** Delete a quiz session by ID. */
  public boolean deleteQuizAttempt(int attemptId) {
    String sql = "DELETE FROM quiz_attempts WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, attemptId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      System.out.println("deleteQuizAttempt failed: " + e.getMessage());
      return false;
    }
  }

  /**
   * Record one answer within a quiz session.
   * selectedOption: 1=A, 2=B, 3=C, 4=D (matches questions.answer format).
   */
  public int insertQuestionAttempt(int attemptId, int questionId,
                                   int selectedOption, boolean isCorrect) {
    String sql = "INSERT INTO question_attempts (attempt_id, question_id, selected_option, is_correct) VALUES (?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, attemptId);
      ps.setInt(2, questionId);
      ps.setInt(3, selectedOption);
      ps.setInt(4, isCorrect ? 1 : 0);
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) return keys.getInt(1);
    } catch (SQLException e) {
      System.out.println("insertQuestionAttempt failed: " + e.getMessage());
    }
    return -1;
  }

  /** Count how many questions the user got right in a session. */
  public int getCorrectCount(int attemptId) {
    String sql = "SELECT COUNT(*) FROM question_attempts WHERE attempt_id = ? AND is_correct = 1";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, attemptId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
      System.out.println("getCorrectCount failed: " + e.getMessage());
    }
    return 0;
  }

  /** Delete all question attempts for a session. */
  public boolean deleteQuestionAttempts(int attemptId) {
    String sql = "DELETE FROM question_attempts WHERE attempt_id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, attemptId);
      return ps.executeUpdate() >= 0;
    } catch (SQLException e) {
      System.out.println("deleteQuestionAttempts failed: " + e.getMessage());
      return false;
    }
  }

  //Recieves userID, and returs the username.
  public String getUsernameById(int userId) {
    String sql = "SELECT username FROM users WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("username");
      }
    } catch (SQLException e) {
      System.out.println("getUsernameById failed: " + e.getMessage());
    }
    return "Unknown User";
  }

  //Gets User's max quiz scores for each category.
  public List<Leaderboard> getLeaderboardByCategory(int categoryId) {
    List<Leaderboard> leaders = new ArrayList<>();
    String sql = """
        SELECT user_id, MAX(score) as score
        FROM quiz_attempts
        WHERE category_id = ? AND completed_at IS NOT NULL
        GROUP BY user_id
        ORDER BY score DESC
        """;
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, categoryId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        leaders.add(new Leaderboard(rs.getInt("user_id"), categoryId, rs.getDouble("score")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return leaders;
  }
}