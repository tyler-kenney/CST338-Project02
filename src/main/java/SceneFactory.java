import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Running UI for program, Should handle scene switches. Allow user to log in / out. Allow user to
 * have options to do stuff.
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
  static Scene Create(SceneType sceneType, Stage stage,
      DatabaseManager db) {   //TODO: refactor to accept database
    return switch (sceneType) {
      case Login -> BuildUserLogin(stage, db);
      //Most likely won't be reach unless we're testing.
      case NewUser -> BuildNewAccount(stage, db);
      case Administrator -> BuildAdminUser(stage, db);
      case General -> BuildGeneralUser(stage, db);

    };
  }

  /**
   * This method displays the stage for creating new account.
   * @param stage contains stage
   * @param db contains database
   * @return Scene
   */
  private static Scene BuildNewAccount(Stage stage, DatabaseManager db) {
    Label PromptNewUserName = new Label("Enter New Username: ");
    Label PromptNewPassword = new Label("Enter New Password: ");

    TextField s2Input1 = new TextField();
    TextField s2Input2 = new TextField();

    Label s2Output1 = new Label("");

    s2Input1.setPromptText(PromptNewUserName.getText());
    s2Input1.setPrefWidth(INPUT_WIDTH);
    s2Input2.setPromptText(PromptNewPassword.getText());
    s2Input2.setPrefWidth(INPUT_WIDTH);
    Button PromptNewAccount = new Button("Create Account");
    CheckBox adminCheck = new CheckBox("Admin");

    PromptNewAccount.setOnAction(a -> {
      String username = s2Input1.getText().trim();
      String password = s2Input2.getText().trim();
      int role_num;
      if (adminCheck.isSelected()) {
        role_num = 1;
      } else {
        role_num = 0;
      }
      if (!db.isUsername(username)) {
        db.insertUserItem(username, password, role_num);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Account Creation");
        alert.setHeaderText("Account Created");
        alert.setContentText("Account Created Successfully!");
        alert.showAndWait();
        Scene BackScene = BuildUserLogin(stage, db);
        stage.setScene(BackScene);
      } else {
        Alert AlertCreation = new Alert(AlertType.ERROR);
        AlertCreation.setTitle("Username Error");
        AlertCreation.setHeaderText("Username Already Exists");
        AlertCreation.setContentText("Please choose another username");
        AlertCreation.showAndWait();
      }
    });
    stage.setTitle("Welcome New User");
    VBox root2 = new VBox(
        12,
        PromptNewUserName,
        s2Input1,
        PromptNewPassword,
        s2Input2,
        adminCheck,
        PromptNewAccount,
        s2Output1);
    root2.setPadding(new Insets(SCENE_PADDING));
    root2.setAlignment(Pos.CENTER);
    return new Scene(root2, SCENE_WIDTH, SCENE_HEIGHT);
  }

  /**
   * This method displays the scene for All user log ins.
   * @param stage BuildUserLogin
   * @return User Login screen, prompting user to enter Username and Password.
   *
   */
  private static Scene BuildUserLogin(Stage stage, DatabaseManager db) {
    Label PromptUserName = new Label("Username: ");
    Label PromptPassword = new Label("Password: ");

    List<User> users = new ArrayList<>();
    TextField s1Input1 = new TextField();
    TextField s1Input2 = new TextField();
    s1Input1.setPromptText(PromptUserName.getText());
    s1Input1.setPrefWidth(INPUT_WIDTH);
    s1Input2.setPromptText(PromptPassword.getText());
    s1Input2.setPrefWidth(INPUT_WIDTH);
    Button PromptLogin = new Button("Login");
    Button PromptNewUser = new Button("Create New Account");

    PromptNewUser.setOnAction(e -> {
      Scene NewAccount = BuildNewAccount(stage, db);
      stage.setScene(NewAccount);
    });

    PromptLogin.setOnAction(a -> {
      String username = s1Input1.getText().trim();
      String password = s1Input2.getText().trim();

      if (db.isUsername(username) && db.isPassword(username, password)) {
        if (db.isAdmin(username, password)) {
          User currentUser = new User(username, password, "Administrator");
          Alert Alert = new Alert(AlertType.INFORMATION);
          Alert.setTitle("Welcome");
          Alert.setHeaderText("Welcome Administrator");
          Alert.setContentText("You have successfully logged in");
          Scene Adminscene = BuildAdminUser(stage, db);
          stage.setScene(Adminscene);
        } else {
          User currentUser = new User(username, password, "User");
          Alert Alert = new Alert(AlertType.INFORMATION);
          Alert.setTitle("Welcome");
          Alert.setHeaderText("Welcome User");
          Alert.setContentText("You have successfully logged in");
          Scene Generalscene = BuildGeneralUser(stage, db);
          stage.setScene(Generalscene);
        }
      } else {
        Alert Alert = new Alert(AlertType.ERROR);
        Alert.setTitle("Log in Error");
        Alert.setHeaderText("Username or Password is incorrect.");
        Alert.setContentText("Please try again.");
        Alert.showAndWait();
      }

//      try (BufferedReader UsernamePassword = new BufferedReader(
//          new FileReader("UsernamePasswordTest.txt"))) {
//        String line;
//        while ((line = UsernamePassword.readLine()) != null) {
//          if (line.trim().isEmpty()) {
//            continue;
//          }
//          String[] user = line.split(",");
//          if (user.length == 3) {
//            String username = user[0].trim();
//            String password = user[1].trim();
//            String role = user[2].trim();
//
//            users.add(new User(username, password, role));
//          } else {
//            System.out.println(ERROR);
//          }
//        }
//
//        for (User user : users) {
//          if (user.username.equals(s1Input1.getText()) && user.password.equals(s1Input2.getText())) {
//            Alert AlertLogin = new Alert(Alert.AlertType.INFORMATION);
//            AlertLogin.setTitle("Successfully Logged In!");
//            if (user.role.equals("Admin")) {
//              Scene Adminscene = BuildAdminUser(stage, db);
//              stage.setScene(Adminscene);
//            }
//
//            if (user.role.equals("General")) {
//              Scene Generalscene = BuildGeneralUser(stage, db);
//              stage.setScene(Generalscene);
//            }
//          }
//        }
//      } catch (IOException e) {
//        System.err.println(e.getMessage());
//        e.printStackTrace();
//      }
    });
    stage.setTitle("Welcome");
    VBox root = new VBox(
        12,
        PromptUserName,
        s1Input1,
        PromptPassword,
        s1Input2,
        PromptLogin,
        PromptNewUser);
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
  private static Scene BuildAdminUser(Stage stage, DatabaseManager db) {
    Button Logout = new Button("Logout");
    Button DisplayLeaderboard = new Button("Display Leaderboard");
    Button MakeQuestion = new Button("Make Question");
    stage.setTitle("Administrator Menu");

    DisplayLeaderboard.setOnAction(e -> {
      Scene Leaderboard = BuildLeaderboard(stage, db);
      stage.setScene(Leaderboard);
    });

    MakeQuestion.setOnAction(e -> {
      Scene CreateQuestion = BuildQuestionGenerator(stage, db);
      stage.setScene(CreateQuestion);
    });

    Logout.setOnAction(a -> {
      LogoutMessage();
      Scene BackScene = BuildUserLogin(stage, db);
      stage.setScene(BackScene);
    });
    //stage.setTitle("Administrator");
    VBox root = new VBox(12, DisplayLeaderboard, MakeQuestion, Logout);
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
  private static Scene BuildGeneralUser(Stage stage, DatabaseManager db) {
    Button Logout = new Button("Logout");
    stage.setTitle("User Menu");

    Logout.setOnAction(a -> {
      LogoutMessage();
      Scene BackScene = BuildUserLogin(stage, db);
      stage.setScene(BackScene);
    });
    stage.setTitle("General");
    VBox root = new VBox(12, Logout);
    root.setPadding(new Insets(SCENE_PADDING));
    root.setAlignment(Pos.CENTER);
    // Scene holds the layout and defines the window size
    return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
  }

  private static Scene BuildLeaderboard(Stage stage, DatabaseManager db){
    Button Logout = new Button("Logout");
    Button ReturnToMenu = new Button("Return to Menu");
    List<Leaderboard> leaders = new ArrayList<>(db.getAllQuizes());
    StringBuilder stringBuilder = new StringBuilder();
    for (Leaderboard leader : leaders) {
      stringBuilder.append(leader.toString());
    }
    Label leaderboardDisplay = new Label(stringBuilder.toString());







    Logout.setOnAction(a -> {
      LogoutMessage();
      Scene BackScene = BuildUserLogin(stage, db);
      stage.setScene(BackScene);
    });

    ReturnToMenu.setOnAction(a -> {
      Scene Adminscene = BuildAdminUser(stage, db);
      stage.setScene(Adminscene);
    });

    VBox root = new VBox(12,leaderboardDisplay,ReturnToMenu,Logout);
    root.setPadding(new Insets(SCENE_PADDING));
    root.setAlignment(Pos.CENTER);
    return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
  }

  private static Scene BuildQuestionGenerator(Stage stage, DatabaseManager db){
    Button Logout = new Button("Logout");
    Button ReturnToMenu = new Button("Return to Menu");

    Logout.setOnAction(a -> {
      LogoutMessage();
      Scene BackScene = BuildUserLogin(stage, db);
      stage.setScene(BackScene);
    });

    ReturnToMenu.setOnAction(a -> {
      Scene Adminscene = BuildAdminUser(stage, db);
      stage.setScene(Adminscene);
    });

    VBox root = new VBox(12,ReturnToMenu,Logout);
    root.setPadding(new Insets(SCENE_PADDING));
    root.setAlignment(Pos.CENTER);
    return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
  }

  private static void LogoutMessage(){
    Alert AlertLogout = new Alert(Alert.AlertType.INFORMATION);
    AlertLogout.setTitle("Successfully Logged Out!");
    AlertLogout.setHeaderText("Logging Out...");
    AlertLogout.setContentText("You have successfully logged out");
    AlertLogout.showAndWait();
  }
}
