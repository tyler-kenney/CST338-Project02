/**
 * [brief one-sentence description of what this class is.]
 *
 * @ author: Tyler Kenney
 * @ since:  4/14/2026
 * @ version 0.1.0
 */
public class Leaderboard {
  private int user_id;
  private int category_id;
  private double points;

  public Leaderboard(int user_id, int category_id, double points) {
    this.user_id = user_id;
    this.category_id = category_id;
    this.points = points;
  }

  public int getUser_id() {
    return user_id;
  }
  public int getCategory_id() {
    return category_id;
  }
  public double getPoints() {
    return points;
  }

  @Override
  public String toString() {
    return "user_id = " + user_id
        + ",\t category_id = " + category_id
        + ",\t points = " +  points
        + "\n";
  }

}
