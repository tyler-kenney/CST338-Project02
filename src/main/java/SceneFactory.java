import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Running UI for program, Should handle scene switches.
 * Allow user to log in / out.
 * Allow user to have options to do stuff.
 *
 * @author :erict
 * <br>
 * Created :4/5/2026
 */
public interface SceneFactory {
    //For UI
    int SCENE_WIDTH = 800;
    int SCENE_HEIGHT = 800;
    int SCENE_PADDING = 30;
    int INPUT_WIDTH = 200;

    //Local variables.
    String ERROR = "Not a valid input!";
    TextField s1Input = new TextField();
    TextField s2Input = new TextField();

    //Static Class for setting up users
    static class User {
        private String username;
        private String password;
        private String role;

        public User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }

    //Class static method Create. Using enum case switch.
    static Scene Create(SceneType sceneType, Stage stage) {   //TODO: refactor to accept database
        return switch (sceneType) {
            case Login -> BuildUserLogin(stage);
            //Most likely won't be reach unless we're testing.
            case Administrator -> BuildAdminUser(stage);
            case General-> BuildGeneralUser(stage);
        };
    }

    /**
     *
     * @param stage BuildUserLogin
     * @return User Login screen, prompting user to enter Username and Password.
     *
     */
    private static Scene BuildUserLogin(Stage stage) {
        Label PromptUserName = new Label("Username: ");
        Label PromptPassword = new Label("Password: ");

        List<User> users = new ArrayList<>();
        s1Input.setPromptText(PromptUserName.getText());s1Input.setPrefWidth(INPUT_WIDTH);
        s2Input.setPromptText(PromptPassword.getText());s2Input.setPrefWidth(INPUT_WIDTH);
        Button PromptLogin = new Button("Login");
        PromptLogin.setOnAction(a -> {
            try (BufferedReader UsernamePassword = new BufferedReader(new FileReader("UsernamePasswordTest.txt"))){
                String line;
                while ((line = UsernamePassword.readLine()) != null) {
                    if(line.trim().isEmpty()){continue;}
                    String[] user = line.split(",");
                    if(user.length == 3){
                        String username = user[0].trim();
                        String password = user[1].trim();
                        String role = user[2].trim();

                        users.add(new User(username, password, role));
                    } else {
                        System.out.println(ERROR);
                    }
                }

                for(User user : users){
                    if(user.username.equals(s1Input.getText()) && user.password.equals(s2Input.getText())){
                        Alert AlertLogin = new Alert(Alert.AlertType.INFORMATION);
                        AlertLogin.setTitle("Successfully Logged In!");
                        if(user.role.equals("Admin")){
                            Scene Adminscene = BuildAdminUser(stage);
                            stage.setScene(Adminscene);
                        }

                        if(user.role.equals("General")){
                            Scene Generalscene = BuildGeneralUser(stage);
                            stage.setScene(Generalscene);
                        }
                    }
                }
            } catch (IOException e){
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        });
        stage.setTitle("Welcome");
        VBox root = new VBox(12, PromptUserName, s1Input,PromptPassword , s2Input, PromptLogin);
        root.setPadding(new Insets(SCENE_PADDING));
        root.setAlignment(Pos.CENTER);
        // Scene holds the layout and defines the window size
        return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    }

    /**
     *
     * @param stage BuildAdminUser, builds admin page.
     * @return interactable Admin Page. Currently Only with Built in back button.
     */
    private static Scene BuildAdminUser(Stage stage) {
        Button Logout = new Button("Logout");

        Logout.setOnAction(a -> {
            Alert AlertLogout = new Alert(Alert.AlertType.INFORMATION);
            AlertLogout.setTitle("Successfully Logged Out!");
            Scene BackScene = BuildUserLogin(stage);
            stage.setScene(BackScene);
        });
        stage.setTitle("Administrator");
        VBox root = new VBox(12, Logout);
        root.setPadding(new Insets(SCENE_PADDING));
        root.setAlignment(Pos.CENTER);
        // Scene holds the layout and defines the window size
        return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    }

    /**
     *
     * @param stage BuildGeneralUser, builds admin page.
     * @return interactable General Page. Currently Only with Built in back button.
     */
    private static Scene BuildGeneralUser(Stage stage){
        Button Logout = new Button("Logout");

        Logout.setOnAction(a -> {
            Alert AlertLogout = new Alert(Alert.AlertType.INFORMATION);
            AlertLogout.setTitle("Successfully Logged Out!");
            Scene BackScene = BuildUserLogin(stage);
            stage.setScene(BackScene);
        });
        stage.setTitle("General");
        VBox root = new VBox(12, Logout);
        root.setPadding(new Insets(SCENE_PADDING));
        root.setAlignment(Pos.CENTER);
        // Scene holds the layout and defines the window size
        return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    }

}
