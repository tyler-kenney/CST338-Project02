import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main File for Application, includes Application launch.
 *
 * @author :erict
 * <br>
 * Created :4/5/2026
 */

public class Main extends Application {

  private DatabaseManager db;

  /**
   *
   * @param stage the primary stage for this application, onto which
   * the application scene can be set.
   * Applications may create other stages, if needed, but they will not be
   * primary stages.
   */
  @Override
  public void start(Stage stage) {

    db = new DatabaseManager();

    Scene scene = SceneFactory.Create(SceneType.Login, stage, db);
    // text shown in the OS title bar
    stage.setScene(scene);
    stage.show();       // make the window visible
  }

  /**
   * closes database. JavaFX calls stop() when the primary window is closed — the correct place to release database.
   */
  @Override
  public void stop () {
    if ( db != null ) db . close () ; // called automatically on window close
  }

  public static void main(String[] args) {
    launch(args);
  }
}
