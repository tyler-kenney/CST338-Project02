import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Used by Container.fxml, to help setup the correct stage.
 *
 * @author : @erict
 * <br>
 * @created : 4/14/26
 **/
public class ContainerController {
    @FXML
    private StackPane rootContainer;
    private Stage stage;
    private DatabaseManager db;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDatabase(DatabaseManager db) {
        this.db = db;
    }

    public void setContent(Pane Content) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(Content);
    }

    public Stage getStage() {
        return stage;
    }

    public DatabaseManager getDb() {
        return db;
    }
}

