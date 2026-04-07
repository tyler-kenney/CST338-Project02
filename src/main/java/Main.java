import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * Main File for Application, includes Application launch.
 * @author :erict
 * <br>
 * Created :4/5/2026
 */

public class Main extends Application {
    public void start(Stage stage){
        Scene scene = SceneFactory.Create(SceneType.Login, stage);
        // text shown in the OS title bar
        stage.setScene(scene);
        stage.show();       // make the window visible
    }
    public static void main(String[] args) {
        launch(args);
    }
}
