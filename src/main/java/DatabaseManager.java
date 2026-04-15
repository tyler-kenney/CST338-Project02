/**
 * This class will take inputted data and read, update, or delete the database.
 *
 * @ author: Tyler Kenney
 * @ since:  4/10/2026
 * @ version 0.1.0
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
   * This Constructor sets up the database.
   */
  public DatabaseManager() {
    try {
      // Opens ( or creates ) app . db in the project root
      connection = DriverManager.getConnection(DB_URL);
      System.out.println(" Database connected . ");
      createTables(); // set up schema on first run
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
    //TODO:Refactor to multithread.
    String users_sql = """
        CREATE TABLE IF NOT EXISTS users
         (
             id       INTEGER PRIMARY KEY AUTOINCREMENT,
             username TEXT    NOT NULL UNIQUE,
             password TEXT    NOT NULL,
             usertype INTEGER NOT NULL
         )
        """;
    String insert_users = """
        INSERT OR IGNORE INTO users (username, password, usertype)
                           VALUES ('BartSimpson', 'EatMyShorts!', 0),
                                  ('SlimShady', 'PleaseStandUp?', 1),
                                  ('admin', '123', 1)
        """;

    String categories_sql = """
        CREATE TABLE IF NOT EXISTS categories (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          category TEXT NOT NULL UNIQUE,
          description TEXT
        )
        """;

    String insert_categories_sql = """
        INSERT OR IGNORE INTO categories (category, description)
        VALUES ('MidJourney Term', 'These are questions to prepare for the mid-Journey-term');
        INSERT OR IGNORE INTO categories (category)
        VALUES ('Wk01');
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
            user_id INTEGER NO NULL REFERENCES users(id), --foreign key
            attempts INTEGER DEFAULT 0
        )
        """;

    String insert_questions_sql = """
        INSERT OR IGNORE INTO questions (
          category_id,
          question,
          option_a,
          option_b,
          option_c,
          option_d,
          answer,
          user_id
        )
        VALUES (
        (SELECT id FROM categories WHERE category = 'MidJourney Term'),
        'Which is NOT a valid way to call a constructor?',
        'Too too = new car()',
        'this(42, noCake);',
        'Mine mine = new Mine();',
        'Yo Mama is a Constructor!',
        4,
        (SELECT id FROM users WHERE username = 'admin')
        ),
        ((Select id FROM categories WHERE category = 'Wk01'),
        'Which of the following is true about a method that has this declaration? \n \n public Karen getDepartmentManager(){...}',
        'it’s defined in the Karen class',
        'it returns an object of the Karen class',
        'it’s only accessible within its class',
        'it is an instance method of the Karen class that can access instance variables of Employee',
        2,
        (SELECT id FROM users WHERE username = 'SlimShady'))
        """;
//TODO:Refactor points to be total earned points. Update when necessary
    //TODO:add attribute for total points.
    String quiz_attempts_sql = """
        CREATE TABLE IF NOT EXISTS quiz_attempts
         (
             id          INTEGER PRIMARY KEY AUTOINCREMENT,
             user_id     INTEGER NOT NULL REFERENCES users (id),      --foreign key
             category_id INTEGER NOT NULL REFERENCES categories (id), --foreign key
             time_taken  DATETIME DEFAULT CURRENT_TIMESTAMP,
             points      DOUBLE  NOT NULL                       --1.0 if correct, 0.0 if incorrect,
         )
        """;

    String insert_quiz_attempts_sql = """
        INSERT INTO quiz_attempts (user_id, category_id, points)
           VALUES ((SELECT id FROM users WHERE username = 'BartSimpson'),
                   (SELECT id FROM categories WHERE category = 'MidJourney Term'),
                   0.0),
                  ((SELECT id FROM users WHERE username = 'BartSimpson'),
                  (SELECT id FROM categories WHERE category = 'MidJourney Term'),
                   1.0)
        """;

    String question_attempts_sql = """
        CREATE TABLE IF NOT EXISTS question_attempts
         (
             id          INTEGER PRIMARY KEY AUTOINCREMENT,
             question_id INTEGER NOT NULL REFERENCES questions (id),
             attempt_id  INTEGER NOT NULL REFERENCES quiz_attempts (id),
             is_correct  INTEGER NOT NULL --0 for no, 1 for yes
         )
        """;

    String insert_question_attempts_sql = """
        INSERT INTO question_attempts (question_id, attempt_id, is_correct)
         VALUES ((SELECT id FROM questions WHERE id = 1),
                 (SELECT id FROM quiz_attempts WHERE id = 1),
                 0),
                ((SELECT id FROM questions WHERE id = 2,
                 (SELECT id FROM quiz_attempts WHERE id = 2),
                 1)
        """;

    try (Statement stmt = connection.createStatement()) {
      stmt.execute("DROP TABLE users");
      stmt.execute("DROP TABLE categories");
      stmt.execute("DROP TABLE questions");
      stmt.execute("DROP TABLE quiz_attempts");
      stmt.execute("DROP TABLE question_attempts");

      stmt.execute(users_sql);
      stmt.execute(insert_users);

      stmt.execute(categories_sql);
      stmt.execute(insert_categories_sql);

      stmt.execute(questions_sql);
      stmt.execute(insert_questions_sql);

      stmt.execute(quiz_attempts_sql);
      stmt.execute(insert_quiz_attempts_sql);

      stmt.execute(question_attempts_sql);
      stmt.execute(insert_question_attempts_sql);


    } catch (SQLException e) {
      System.out.println(" createTables failed : " + e.getMessage());
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
    String sql = " INSERT INTO items ( name ) VALUES (?) ";
    try (PreparedStatement pstmt = connection.prepareStatement(testSql)) {
      pstmt.setString(1, username);
      pstmt.setString(2, password);  // bind parameter 1 to ’name ’
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
    String sql = " SELECT * FROM quiz_attempts ORDER BY user_id DESC";
    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) { // move to next row
        int userID = (rs.getInt("user_id"));
        int categoryID = (rs.getInt("category_id"));
        double points = rs.getDouble("points");
      }
    } catch (SQLException e) {
      System.out.println(" getAllQuizes failed : " + e.getMessage());
    }
    return leaders;
  }

  /**
   * CRUD - Read (Select)
   *
   * @return
   */
  public List<String> getAllItems() {
    List<String> items = new ArrayList<>();
    String sql = " SELECT name FROM items WHERE done = 0 ORDER BY created DESC ";
    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) { // move to next row
        items.add(rs.getString(" name ")); // read column by name
      }
    } catch (SQLException e) {
      System.out.println(" getAllItems failed : " + e.getMessage());
    }
    return items;
  }

  /**
   * CRUD - Update.
   *
   * @param id
   */
  public void markDone(int id) {
    String sql = " UPDATE items SET done = 1 WHERE id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println(" markDone failed : " + e.getMessage());
    }
    //TODO: update number of rows affected.
  }

  /**
   * CRUD - Delete.
   *
   * @param id
   */
  public void deleteItem(int id) {
    String sql = " DELETE FROM items WHERE id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println(" deleteItem failed : " + e.getMessage());
    }
    //TODO: update number of rows affected.
  }


}
