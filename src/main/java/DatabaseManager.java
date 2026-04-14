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
  private static final String DB_URL = "jdbc:sqlite:mybd.db";
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
    String users_sql = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
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
            possible_answers TEXT NOT NULL,
            answer INTEGER NOT NULL,
            created DATETIME DEFAULT CURRENT_TIMESTAMP,
            user_id INTEGER NO NULL REFERENCES users(id), --foreign key
            attempts INTEGER DEFAULT 0
        )
        """;

    String question_attempts_sql = """
        CREATE TABLE IF NOT EXISTS question_attempts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        question_id INTEGER NOT NULL REFERENCES questions(id),
        attempt_id INTEGER NOT NULL REFERENCES quiz_attempts(id),
        is_correct INTEGER NOT NULL   --0 for no, 1 for yes
        )
        """;

    String quiz_attempts_sql = """
        CREATE TABLE IF NOT EXISTS quiz_attempts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL REFERENCES users(id), --foreign key
            category_id INTEGER NOT NULL REFERENCES categories(id), --foreign key
            time_taken DATETIME DEFAULT CURRENT_TIMESTAMP,
            score DOUBLE NOT NULL
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
      System.out.println(" getAllItems failed : " + e.getMessage());
    }
    return users;
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
