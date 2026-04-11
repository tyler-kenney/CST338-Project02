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

  //TODO: restructure sqlite file.

  // " jdbc : sqlite :" tells JDBC which driver to use .
  // The path after it is the database file location .
  private static final String DB_URL = " jdbc:sqlite:app.db ";
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
      System.err.println(" Connection failed : " + e.getMessage());
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
    String sql = """
            CREATE TABLE IF NOT EXISTS items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            done INTEGER NOT NULL DEFAULT 0,
                created TEXT DEFAULT (datetime( ’ now ’))
        )
        """;
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
    } catch (SQLException e) {
      System.err.println(" createTables failed : " + e.getMessage());
    }
  }

  /**
   * CRUD - Create (insert)
   *
   * @param name
   */
  //TODO: refactor to accept more values such as: question, answers, correct_answer, scores.
  public void insertItem(String name) {
    String sql = " INSERT INTO items ( name ) VALUES (?) ";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, name); // bind parameter 1 to ’name ’
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println(" insertItem failed : " + e.getMessage());
    }
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
      System.err.println(" getAllItems failed : " + e.getMessage());
    }
    return items;
  }

  /**
   * CRUD - Update.
   * @param id
   */
  public void markDone(int id) {
    String sql = " UPDATE items SET done = 1 WHERE id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println(" markDone failed : " + e.getMessage());
    }
    //TODO: update number of rows affected.
  }

  /**
   * CRUD - Delete.
   * @param id
   */
  public void deleteItem(int id) {
    String sql = " DELETE FROM items WHERE id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println(" deleteItem failed : " + e.getMessage());
    }
    //TODO: update number of rows affected.
  }


}
