import javafx.fxml.FXMLLoader;
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
                      DatabaseManager db) {
    //Create now loads from resource file to help set stage.
    try {
      FXMLLoader FXML = new FXMLLoader(SceneFactory.class.getResource("/fxml/Container.fxml"));

      Scene scene = new Scene(FXML.load(), SCENE_WIDTH, SCENE_HEIGHT);
      ContainerController containerController = FXML.getController();
      containerController.setStage(stage);
      containerController.setDatabase(db);
      VBox Content = switch (sceneType) {
          case Login -> BuildUserLogin(stage, db);
          case NewUser -> BuildNewAccount(stage, db);
          case Administrator -> BuildAdminUser(stage, db);
          case General -> BuildGeneralUser(stage, db);
          case Leaderboard -> {
            Scene S_LeaderBoard = BuildLeaderboard(stage, db);
            yield (VBox) S_LeaderBoard.getRoot();
          }
          default -> throw new IllegalArgumentException("Unknown scene type: " + sceneType);
      };

      containerController.setContent(Content);
      return scene;
    } catch (IOException e) {
      e.printStackTrace();
      Alert SceneAlert = new Alert(AlertType.ERROR);
      SceneAlert.setTitle("Scene Error");
      SceneAlert.setHeaderText("Failed to load scene.");
      SceneAlert.setContentText(e.getMessage());
      return null;
    }
  }

  /**
   * This method displays the stage for creating new account.
   *
   * @param stage contains stage
   * @param db    contains database
   * @return Scene
   */
  private static VBox BuildNewAccount(Stage stage, DatabaseManager db) {
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
    CheckBox AdminCheck = new CheckBox("Admin");

    PromptNewAccount.setOnAction(a -> {
      String username = s2Input1.getText().trim();
      String password = s2Input2.getText().trim();
      int role_num;
      if (AdminCheck.isSelected()) {
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
        Scene BackScene = Create(SceneType.Login, stage, db);
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
            AdminCheck,
            PromptNewAccount,
            s2Output1);
    root2.setPadding(new Insets(SCENE_PADDING));
    root2.setAlignment(Pos.CENTER);
    return root2;
  }
    /**
     * This method displays the scene for All user log ins.
     *
     * @param stage BuildUserLogin
     * @return User Login screen, prompting user to enter Username and Password.
     *
     */
    private static VBox BuildUserLogin (Stage stage, DatabaseManager db){
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
        Scene NewAccount = Create(SceneType.NewUser, stage, db);
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
            Scene Adminscene = Create(SceneType.Administrator, stage, db);
            stage.setScene(Adminscene);
          } else {
            User currentUser = new User(username, password, "User");
            Alert Alert = new Alert(AlertType.INFORMATION);
            Alert.setTitle("Welcome");
            Alert.setHeaderText("Welcome User");
            Alert.setContentText("You have successfully logged in");
            Scene Generalscene = Create(SceneType.General, stage, db);
            stage.setScene(Generalscene);
          }
        } else {
          Alert Alert = new Alert(AlertType.ERROR);
          Alert.setTitle("Log in Error");
          Alert.setHeaderText("Username or Password is incorrect.");
          Alert.setContentText("Please try again.");
          Alert.showAndWait();
        }
      });
      stage.setTitle("Welcome");
      VBox root = new
              VBox(
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
      return root;
    }

    /**
     *
     * @param stage BuildAdminUser, builds admin page.
     * @return interactable Admin Page. Currently Only with Built in back button.
     */
    private static VBox BuildAdminUser (Stage stage, DatabaseManager db){
      Button Logout = new Button("Logout");
      Button DisplayLeaderboard = new Button("Display Leaderboard");
      Button MakeQuestion = new Button("Make Question");
      stage.setTitle("Administrator Menu");

      DisplayLeaderboard.setOnAction(e -> {
        Scene Leaderboard = Create(SceneType.Leaderboard, stage, db);
        stage.setScene(Leaderboard);
      });

      MakeQuestion.setOnAction(e -> {
        Scene CreateQuestion = BuildQuestionGenerator(stage, db);
        stage.setScene(CreateQuestion);
      });

      Logout.setOnAction(a -> {
        LogoutMessage();
        Scene BackScene = Create(SceneType.Login, stage, db);
        stage.setScene(BackScene);
      });
      //stage.setTitle("Administrator");
      VBox root = new
              VBox(
              12,
              DisplayLeaderboard,
              MakeQuestion,
              Logout);
      root.setPadding(new Insets(SCENE_PADDING));
      root.setAlignment(Pos.CENTER);
      // Scene holds the layout and defines the window size
      return root;
    }

    /**
     *
     * @param stage BuildGeneralUser, builds admin page.
     * @return interactable General Page. Currently Only with Built in back button.
     */
    private static VBox BuildGeneralUser (Stage stage, DatabaseManager db){
      Button Logout = new Button("Logout");
      stage.setTitle("User Menu");

      Logout.setOnAction(a -> {
        LogoutMessage();
        Scene BackScene = Create(SceneType.Login, stage, db);
        stage.setScene(BackScene);
      });
      VBox root = new
              VBox(
                12,
                Logout);
      root.setPadding(new Insets(SCENE_PADDING));
      root.setAlignment(Pos.CENTER);
      // Scene holds the layout and defines the window size
      return root;
    }

    private static Scene BuildLeaderboard (Stage stage, DatabaseManager db){
      Button Logout = new Button("Logout");
      Button ReturnToMenu = new Button("Return to Menu");

      Logout.setOnAction(a -> {
        LogoutMessage();
        Scene BackScene = Create(SceneType.Login, stage, db);
        stage.setScene(BackScene);
      });

      ReturnToMenu.setOnAction(a -> {
        Scene Adminscene = Create(SceneType.Administrator, stage, db);
        stage.setScene(Adminscene);
      });

      VBox root = new VBox(12, ReturnToMenu, Logout);
      root.setPadding(new Insets(SCENE_PADDING));
      root.setAlignment(Pos.CENTER);
      return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    }

    private static Scene BuildQuestionGenerator (Stage stage, DatabaseManager db){
      //Creates labels and text fields for question input.
      Label QuestionLabel = new Label("Question:");
      TextField QuestionField = new TextField();
      QuestionField.setPrefWidth(INPUT_WIDTH);
      QuestionField.setPromptText("Enter Question");

      Label OptionALabel = new Label("A) ");
      TextField OptionAField = new TextField();
      OptionALabel.setPrefWidth(INPUT_WIDTH);
      OptionAField.setPromptText("Enter option A:");

      Label OptionBLabel = new Label("B) ");
      TextField OptionBField = new TextField();
      OptionBLabel.setPrefWidth(INPUT_WIDTH);
      OptionBField.setPromptText("Enter option B:");

      Label OptionCLabel = new Label("C) ");
      TextField OptionCField = new TextField();
      OptionCLabel.setPrefWidth(INPUT_WIDTH);
      OptionCField.setPromptText("Enter option C:");

      Label OptionDLabel = new Label("D) ");
      TextField OptionDField = new TextField();
      OptionDLabel.setPrefWidth(INPUT_WIDTH);
      OptionDField.setPromptText("Enter option D");

      Label AnswerLabel = new Label("Select Answer:");

      //Creates Buttons for Answer Choices
      Button A_Button = new Button("A");
      Button B_Button = new Button("B");
      Button C_Button = new Button("C");
      Button D_Button = new Button("D");

      //Stores selected Answer.
      final int[]SelectAnswer = {0};

      A_Button.setPrefWidth(60);
      B_Button.setPrefWidth(60);
      C_Button.setPrefWidth(60);
      D_Button.setPrefWidth(60);

      //Selected Button is highlighted lightgreen.
      A_Button.setOnAction(a -> {
        SelectAnswer[0] =1;
        A_Button.setStyle("-fx-background-color: lightgreen;");
        B_Button.setStyle("");
        C_Button.setStyle("");
        D_Button.setStyle("");
      });

      B_Button.setOnAction(a -> {
        SelectAnswer[0] =1;
        A_Button.setStyle("");
        B_Button.setStyle("-fx-background-color: lightgreen;");
        C_Button.setStyle("");
        D_Button.setStyle("");
      });

      C_Button.setOnAction(a -> {
        SelectAnswer[0] =1;
        A_Button.setStyle("");
        B_Button.setStyle("");
        C_Button.setStyle("-fx-background-color: lightgreen;");
        D_Button.setStyle("");
      });

      D_Button.setOnAction(a -> {
        SelectAnswer[0] =1;
        A_Button.setStyle("");
        B_Button.setStyle("");
        C_Button.setStyle("");
        D_Button.setStyle("-fx-background-color: lightgreen;");
      });

      //Horizontal box for answer buttons.
      javafx.scene.layout.HBox AnswerButtonsBox = new javafx.scene.layout.HBox(10,
              A_Button, B_Button, C_Button, D_Button);
      AnswerButtonsBox.setAlignment(Pos.CENTER);

      Label categoryLabel = new Label("Category:");
      javafx.scene.control.ComboBox<String> categoryCombo = new javafx.scene.control.ComboBox<>();
      categoryCombo.setPrefWidth(INPUT_WIDTH);
      categoryCombo.setPromptText("Select a category");

      try{
        List<String> Categories = db.getAllCategories();
        categoryCombo.getItems().addAll(Categories);
      } catch (Exception e){
        System.out.println("Failed to load Categories" + e.getMessage());
      }

      //Status label for feedback
      Label statusLabel = new Label("");

      Button submitButton = new Button("Submit Question");
      Button Logout = new Button("Logout");
      Button ReturnToMenu = new Button("Return to Menu");

      //Submit button action
      submitButton.setOnAction(a -> {
        String Question =  QuestionField.getText().trim();
        String OptionA =  OptionAField.getText().trim();
        String OptionB =  OptionBField.getText().trim();
        String OptionC =  OptionCField.getText().trim();
        String OptionD =  OptionDField.getText().trim();
        String SelectedCategory = categoryCombo.getValue();

        //Validation
        if(Question.isEmpty() || OptionA.isEmpty() || OptionB.isEmpty() ||  OptionC.isEmpty() ||
                OptionD.isEmpty() || SelectedCategory.isEmpty()){
          statusLabel.setText("Please fill all the fields");
          return;
        }

        if(SelectAnswer[0] == 0){
          statusLabel.setText("Please select the correct answer: ");
          return;
        }

        //TODO: Pass to DatabaseManager through a class.
        statusLabel.setText("Correct Answer: " + (char) ('A' + SelectAnswer[0] -1));

        // Clear fields after successful validation
        QuestionField.clear();
        OptionAField.clear();
        OptionBField.clear();
        OptionCField.clear();
        OptionDField.clear();
        categoryCombo.setValue(null);
        SelectAnswer[0] = 0;
        A_Button.setStyle("");
        B_Button.setStyle("");
        C_Button.setStyle("");
        D_Button.setStyle("");
      });

      Logout.setOnAction(a -> {
        LogoutMessage();
        Scene BackScene = Create(SceneType.Login, stage, db);
        stage.setScene(BackScene);
      });

      ReturnToMenu.setOnAction(a -> {
        Scene Adminscene = Create(SceneType.Administrator, stage, db);
        stage.setScene(Adminscene);
      });
      VBox root = new VBox(
              12, QuestionLabel, QuestionField,
              OptionALabel, OptionAField,
              OptionBLabel, OptionBField,
              OptionCLabel, OptionCField,
              OptionDLabel, OptionDField,
              AnswerLabel, AnswerButtonsBox,
              submitButton, statusLabel,
              ReturnToMenu, Logout);
      root.setPadding(new Insets(SCENE_PADDING));
      root.setAlignment(Pos.CENTER);

      javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(root);
      scrollPane.setFitToWidth(true);
      scrollPane.setPrefHeight(SCENE_HEIGHT);
      return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    }

    private static void LogoutMessage () {
      Alert AlertLogout = new Alert(Alert.AlertType.INFORMATION);
      AlertLogout.setTitle("Successfully Logged Out!");
      AlertLogout.setHeaderText("Logging Out...");
      AlertLogout.setContentText("You have successfully logged out");
      AlertLogout.showAndWait();
    }
}
