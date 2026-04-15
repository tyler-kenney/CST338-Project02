import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests Leaderboard
 *
 * @ author: Tyler Kenney
 * @ since: 4/14/2026
 * @ version 0.1.0
 */
class LeaderboardTest {

  int user_idOne = 1;
  int category_idOne = 1;
  double pointsOne = 1;

  int user_idTwo = 2;
  int category_idTwo = 2;
  double pointsTwo = 2;

  Leaderboard testLeaderboardOne = null;
  Leaderboard testLeaderboardTwo = null;

  @BeforeEach
  void setUp() {
    testLeaderboardOne = new Leaderboard(user_idOne,category_idOne, pointsOne);
    testLeaderboardTwo = new Leaderboard(user_idTwo,category_idTwo, pointsTwo);

  }

  @AfterEach
  void tearDown() {
    testLeaderboardOne = null;
    testLeaderboardTwo = null;
  }

  @Test
  void getUser_id() {
    assertEquals(user_idOne, testLeaderboardOne.getUser_id());

  }

  @Test
  void getCategory_id() {
    assertEquals(category_idOne, testLeaderboardOne.getCategory_id());
  }

  @Test
  void getPoints() {
    assertEquals(pointsOne, testLeaderboardOne.getPoints());
  }

}