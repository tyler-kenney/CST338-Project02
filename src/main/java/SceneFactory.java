import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

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

  static class Session {
    static String username = null;
    static int userId = -1;
  }

  //Local variables.
  String CSS_PATH = "/css/stylesheet.css";

  //Static method for applying CSS to scenes
  static void applyCSS(Scene scene) {
    try{
      String css = SceneFactory.class.getResource(CSS_PATH).toString();
      scene.getStylesheets().add(css);
    } catch (Exception e) {
      System.out.println("Failed to load stylesheet " + e.getMessage());
    }
  }

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
      applyCSS(scene);
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
     * This method displays the scene for All user log ins.
     *
     * @param stage BuildUserLogin
     * @return User Login screen, prompting user to enter Username and Password.
     *
     */
    private static VBox BuildUserLogin (Stage stage, DatabaseManager db){

      Label PromptUserName = new Label("Username: ");
      Label PromptPassword = new Label("Password: ");

      PromptUserName.getStyleClass().add("Label");
      PromptPassword.getStyleClass().add("Label");

      TextField s1Input1 = new TextField();
      TextField s1Input2 = new TextField();
      s1Input1.setPromptText(PromptUserName.getText());
      s1Input1.setPrefWidth(INPUT_WIDTH);
      s1Input2.setPromptText(PromptPassword.getText());
      s1Input2.setPrefWidth(INPUT_WIDTH);

      s1Input1.getStyleClass().add("TextField");
      s1Input2.getStyleClass().add("TextField");

      Button PromptLogin = new Button("Login");
      Button PromptNewUser = new Button("Create New Account");

      PromptLogin.getStyleClass().add("button");
      PromptNewUser.getStyleClass().add("button");

      PromptNewUser.setOnAction(e -> {
        Scene NewAccount = Create(SceneType.NewUser, stage, db);
        stage.setScene(NewAccount);
      });

      PromptLogin.setOnAction(a -> {
        String username = s1Input1.getText().trim();
        String password = s1Input2.getText().trim();

        if (db.isUsername(username) && db.isPassword(username, password)) {

          Session.username = username;
          Session.userId = db.getUserId(username);

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
      root.getStyleClass().add("vbox");
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
      Button GenerateQuestion = new Button("Generate Questions");

      DisplayLeaderboard.getStyleClass().add("button");
      MakeQuestion.getStyleClass().add("button");
      GenerateQuestion.getStyleClass().add("button");
      Logout.getStyleClass().add("button-logout");

      stage.setTitle("Administrator Menu");

      DisplayLeaderboard.setOnAction(e -> {
        Scene Leaderboard = Create(SceneType.Leaderboard, stage, db);
        stage.setScene(Leaderboard);
      });

      GenerateQuestion.setOnAction(e -> {
        String QuestionType = "Generate";
        Scene CreateQuestion = BuildQuestionGenerator(stage, db, QuestionType);
        stage.setScene(CreateQuestion);
      });

      MakeQuestion.setOnAction(e -> {
        String QuestionType = "Create";
        Scene CreateQuestion = BuildQuestionGenerator(stage, db, QuestionType);
        stage.setScene(CreateQuestion);
      });

      Logout.setOnAction(a -> {
        LogoutMessage("Reg_Logout");
        Scene BackScene = Create(SceneType.Login, stage, db);
        stage.setScene(BackScene);
      });
      //stage.setTitle("Administrator");
      VBox root = new
              VBox(
              12,
              DisplayLeaderboard,
              MakeQuestion, GenerateQuestion,
              Logout);
      root.setPadding(new Insets(SCENE_PADDING));
      root.setAlignment(Pos.CENTER);
      root.getStyleClass().add("vbox");
      // Scene holds the layout and defines the window size
      return root;
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
    Label s2Output1 = new Label("");

    PromptNewUserName.getStyleClass().add("Label");
    PromptNewPassword.getStyleClass().add("Label");
    s2Output1.getStyleClass().add("Label");

    TextField s2Input1 = new TextField();
    TextField s2Input2 = new TextField();

    s2Input1.getStyleClass().add("TextField");
    s2Input2.getStyleClass().add("TextField");

    Button PromptNewAccount = new Button("Create Account");
    PromptNewAccount.getStyleClass().add("button");

    Button returnToMenuButton = new Button("Return to Login");
    returnToMenuButton.getStyleClass().add("button-logout");

    returnToMenuButton.setOnAction(a -> {
      LogoutMessage("");
      Scene BackScene = Create(SceneType.Login, stage, db);
      stage.setScene(BackScene);
    });



    s2Input1.setPromptText(PromptNewUserName.getText());
    s2Input1.setPrefWidth(INPUT_WIDTH);
    s2Input2.setPromptText(PromptNewPassword.getText());
    s2Input2.setPrefWidth(INPUT_WIDTH);

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
            s2Output1, returnToMenuButton);
    root2.setPadding(new Insets(SCENE_PADDING));
    root2.setAlignment(Pos.CENTER);
    root2.getStyleClass().add("vbox");
    return root2;
  }

    /**
     *
     * @param stage BuildGeneralUser, builds admin page.
     * @return interactable General Page. Currently Only with Built in back button.
     */
    private static VBox BuildGeneralUser (Stage stage, DatabaseManager db){
      Button Logout = new Button("Logout");
      Button DisplayLeaderboard = new Button("Display Leaderboard");


      DisplayLeaderboard.getStyleClass().add("button");
      Logout.getStyleClass().add("button-logout");

      stage.setTitle("User Menu");

      DisplayLeaderboard.setOnAction(e -> {
        Scene Leaderboard = Create(SceneType.Leaderboard, stage, db);
        stage.setScene(Leaderboard);
      });

      Logout.setOnAction(a -> {
        LogoutMessage("Reg_Logout");
        Scene BackScene = Create(SceneType.Login, stage, db);
        stage.setScene(BackScene);
      });

      Button TakeQuiz = new Button("Take Quiz");
      TakeQuiz.getStyleClass().add("button");

      TakeQuiz.setOnAction(e -> {
        Scene quizSetup = BuildCategorySelection(stage, db);
        stage.setScene(quizSetup);
      });

      VBox root = new
              VBox(
              12,
              TakeQuiz,
              DisplayLeaderboard,
              Logout);
      root.setPadding(new Insets(SCENE_PADDING));
      root.setAlignment(Pos.CENTER);
      root.getStyleClass().add("vbox");
      // Scene holds the layout and defines the window size
      return root;
    }

    private static Scene BuildLeaderboard (Stage stage, DatabaseManager db){
      Button Logout = new Button("Logout");
      Button ReturnToMenu = new Button("Return to Menu");


      ReturnToMenu.getStyleClass().add("button");
      Logout.getStyleClass().add("button-logout");

      stage.setTitle("Leaderboard");

      Logout.setOnAction(a -> {
        LogoutMessage("Reg_Logout");
        Scene BackScene = Create(SceneType.Login, stage, db);
        stage.setScene(BackScene);
      });

      ReturnToMenu.setOnAction(a -> {
        Scene Adminscene = Create(SceneType.Administrator, stage, db);
        stage.setScene(Adminscene);
      });

      //TODO: Get Leaderboard.

      VBox root = new VBox(12, ReturnToMenu, Logout);
      root.setPadding(new Insets(SCENE_PADDING));
      root.setAlignment(Pos.CENTER);
      root.getStyleClass().add("vbox");
      return new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    }

  private static Scene BuildQuestionGenerator(Stage stage, DatabaseManager db, String QuestionType) {
    Button Logout = new Button("Logout");
    Button returnToMenuButton = new Button("Return to Menu");

    returnToMenuButton.getStyleClass().add("button");
    Logout.getStyleClass().add("button-logout");

    Logout.setOnAction(e -> {
      LogoutMessage("Reg_Logout");
      Scene BackScene = Create(SceneType.Login, stage, db);
      stage.setScene(BackScene);
    });

    returnToMenuButton.setOnAction(e -> {
      Scene Adminscene = Create(SceneType.Administrator, stage, db);
      stage.setScene(Adminscene);
    });

    if(QuestionType.equals("Create")){
      // Create labels and text fields for question input
      Label questionLabel = new Label("Enter Question:");
      TextField questionField = new TextField();
      questionField.setPrefWidth(INPUT_WIDTH);
      questionField.setPromptText("Type your question here...");

      Label optionALabel = new Label("Option A:");
      TextField optionAField = new TextField();
      optionAField.setPrefWidth(INPUT_WIDTH);
      optionAField.setPromptText("Enter option A");

      Label optionBLabel = new Label("Option B:");
      TextField optionBField = new TextField();
      optionBField.setPrefWidth(INPUT_WIDTH);
      optionBField.setPromptText("Enter option B");

      Label optionCLabel = new Label("Option C:");
      TextField optionCField = new TextField();
      optionCField.setPrefWidth(INPUT_WIDTH);
      optionCField.setPromptText("Enter option C");

      Label optionDLabel = new Label("Option D:");
      TextField optionDField = new TextField();
      optionDField.setPrefWidth(INPUT_WIDTH);
      optionDField.setPromptText("Enter option D");

      optionALabel.getStyleClass().add("Label");
      optionBLabel.getStyleClass().add("Label");
      optionCLabel.getStyleClass().add("Label");
      optionDLabel.getStyleClass().add("Label");

      optionAField.getStyleClass().add("TextField");
      optionBField.getStyleClass().add("TextField");
      optionCField.getStyleClass().add("TextField");
      optionDField.getStyleClass().add("TextField");

      // Answer selection using RadioButtons
      Label answerLabel = new Label("Select Correct Answer:");

      answerLabel.getStyleClass().add("Label");

      ToggleGroup answerGroup = new ToggleGroup();

      RadioButton answerA = new RadioButton("A");
      RadioButton answerB = new RadioButton("B");
      RadioButton answerC = new RadioButton("C");
      RadioButton answerD = new RadioButton("D");

      answerA.getStyleClass().add("radio-button");
      answerB.getStyleClass().add("radio-button");
      answerC.getStyleClass().add("radio-button");
      answerD.getStyleClass().add("radio-button");

      answerA.setToggleGroup(answerGroup);
      answerB.setToggleGroup(answerGroup);
      answerC.setToggleGroup(answerGroup);
      answerD.setToggleGroup(answerGroup);

      // Create horizontal box for radio buttons
      HBox answerBox = new HBox(20, answerA, answerB, answerC, answerD);
      answerBox.setAlignment(Pos.CENTER);
      answerBox.getStyleClass().add("hbox");

      // Category selection
      Label categoryLabel = new Label("Category:");
      ComboBox<String> categoryCombo = new ComboBox<>();
      categoryCombo.setPrefWidth(INPUT_WIDTH);
      categoryCombo.setPromptText("Select a category");

      // Status label for feedback
      Label statusLabel = new Label("");

      // Load categories from database
      List<String> categories = db.getAllCategories();
      if (categories.isEmpty()) {
        statusLabel.setText("No categories available. Please add categories first.");
      } else {
        categoryCombo.getItems().addAll(categories);
      }

      // Buttons
      Button submitButton = new Button("Submit Question");

      // Submit button action
      submitButton.setOnAction(e -> {
        String question = questionField.getText().trim();
        String optionAtext = optionAField.getText().trim();
        String optionBtext = optionBField.getText().trim();
        String optionCtext = optionCField.getText().trim();
        String optionDtext = optionDField.getText().trim();
        String selectedCategory = categoryCombo.getValue();

        // Validation for text fields
        if (question.isEmpty() || optionAtext.isEmpty() || optionBtext.isEmpty() ||
                optionCtext.isEmpty() || optionDtext.isEmpty() || selectedCategory == null) {
          statusLabel.setText("Please fill in all fields and select a category!");
          return;
        }

        // Get selected answer from radio buttons
        int selectedAnswer = 0;
        if (answerA.isSelected()) selectedAnswer = 1;
        else if (answerB.isSelected()) selectedAnswer = 2;
        else if (answerC.isSelected()) selectedAnswer = 3;
        else if (answerD.isSelected()) selectedAnswer = 4;

        if (selectedAnswer == 0) {
          statusLabel.setText("Please select the correct answer (A, B, C, or D)!");
          return;
        }

        // Get category ID from category name
        int categoryId = db.getCategoryId(selectedCategory);
        if (categoryId == -1) {
          statusLabel.setText("Invalid category selected!");
          return;
        }

        // Get current user ID (you'll need to get the actual logged-in user)
        // For now, using admin user ID 1 as placeholder
        int userId = 1; // TODO: Get actual logged-in user ID

        // Insert question into database
        int questionId = db.insertQuestion(categoryId, question, optionAtext, optionBtext,
                optionCtext, optionDtext, selectedAnswer, userId);

        if (questionId != -1) {
          statusLabel.setText("Question submitted successfully!");

          // Clear all fields
          questionField.clear();
          optionAField.clear();
          optionBField.clear();
          optionCField.clear();
          optionDField.clear();
          answerGroup.selectToggle(null);  // Deselect all radio buttons
          categoryCombo.setValue(null);
        } else {
          statusLabel.setText("Failed to submit question. Question may already exist.");
        }
      });
      // Create container for all input fields
      VBox Container = new VBox(10,
              questionLabel, questionField,
              optionALabel, optionAField,
              optionBLabel, optionBField,
              optionCLabel, optionCField,
              optionDLabel, optionDField,
              answerLabel, answerBox,
              categoryLabel, categoryCombo,
              submitButton,
              statusLabel,
              returnToMenuButton, Logout
      );

      Container.setPadding(new Insets(SCENE_PADDING));
      Container.setAlignment(Pos.CENTER);

      // Wrap in ScrollPane in case content is too tall
      ScrollPane scrollPane = new ScrollPane(Container);
      scrollPane.setFitToWidth(true);
      scrollPane.setPrefHeight(SCENE_HEIGHT);

      Scene scene = new Scene(scrollPane, SCENE_WIDTH, SCENE_HEIGHT);
      applyCSS(scene);
      return scene;
    }
    if(QuestionType.equals("Generate")) {
      // Category selection
      Label categoryLabel = new Label("Category:");
      categoryLabel.getStyleClass().add("Label");
      ComboBox<String> categoryCombo = new ComboBox<>();
      categoryCombo.setPrefWidth(INPUT_WIDTH);
      categoryCombo.setPromptText("Select a category");
      categoryCombo.getStyleClass().add("combo-box");

      // Status label for feedback
      Label statusLabel = new Label("");

      // Load categories from database
      List<String> categories = db.getAllCategories();
      if (categories.isEmpty()) {
        statusLabel.setText("No categories available. Please add categories first.");
        statusLabel.getStyleClass().add("label-status-fail");
      } else {
        categoryCombo.getItems().addAll(categories);
      }
      VBox GenerateButton = new VBox(
              20,
              categoryLabel, categoryCombo, statusLabel,
              returnToMenuButton, Logout
      );

      GenerateButton.setPadding(new Insets(SCENE_PADDING));
      GenerateButton.setAlignment(Pos.CENTER);

      // Wrap in ScrollPane in case content is too tall
      ScrollPane scrollPane = new ScrollPane(GenerateButton);
      scrollPane.setFitToWidth(true);
      scrollPane.setPrefHeight(SCENE_HEIGHT);
      Scene Scene = new Scene(scrollPane, SCENE_WIDTH, SCENE_HEIGHT);
      applyCSS(Scene);
      return Scene;
    }
    return null;
  }

    private static String LogoutMessage (String string) {
      if(string == "Reg_Logout") {
        Alert AlertLogout = new Alert(Alert.AlertType.INFORMATION);
        AlertLogout.setTitle("Successfully Logged Out!");
        AlertLogout.setHeaderText("Logging Out...");
        AlertLogout.setContentText("You have successfully logged out");
        AlertLogout.showAndWait();
      }
      return null;
    }

  /**
   * Quiz Category Selection
   */
  private static Scene BuildCategorySelection(Stage stage, DatabaseManager db) {
    Label title = new Label("Select a Category");
    title.getStyleClass().add("Label");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

    ListView<String> categoryList = new ListView<>();
    categoryList.setPrefHeight(150);
    categoryList.setPrefWidth(300);

    List<String> categories = db.getAllCategories();
    if (!categories.isEmpty()) {
      categoryList.getItems().addAll(categories);
    }

    Label messageLabel = new Label("");
    messageLabel.getStyleClass().add("Label");

    Button startButton = new Button("Start Quiz");
    startButton.getStyleClass().add("button");

    Button returnButton = new Button("Return to Menu");
    returnButton.getStyleClass().add("button-logout");

    startButton.setOnAction(e -> {
      String selected = categoryList.getSelectionModel().getSelectedItem();
      if (selected == null) {
        messageLabel.setText("Please select a category.");
        return;
      }

      int categoryId = db.getCategoryId(selected);
      int questionCount = db.getQuestionCount(categoryId);

      if (questionCount == 0) {
        messageLabel.setText("No questions available for " + selected + ". Check back later.");
        return;
      }

      int quizSize = Math.min(questionCount, 10);

      Scene quizScene = BuildQuiz(stage, db, categoryId, selected, quizSize);
      stage.setScene(quizScene);
    });

    returnButton.setOnAction(e -> {
      Scene menu = Create(SceneType.General, stage, db);
      stage.setScene(menu);
    });

    VBox root = new VBox(15, title, categoryList, startButton, messageLabel, returnButton);
    root.setPadding(new Insets(SCENE_PADDING));
    root.setAlignment(Pos.CENTER);
    root.getStyleClass().add("vbox");

    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    applyCSS(scene);
    return scene;
  }

  /**
   * Load Quix.fxml and starts the quiz.
   */
  static Scene BuildQuiz(Stage stage, DatabaseManager db,
                         int categoryId, String categoryName, int quizSize) {
    try {
      FXMLLoader loader = new FXMLLoader(SceneFactory.class.getResource("/fxml/Quiz.fxml"));
      ScrollPane scrollPane = new ScrollPane(loader.load());
      scrollPane.setFitToWidth(true);
      scrollPane.setPrefHeight(SCENE_HEIGHT);

      QuizController controller = loader.getController();
      controller.setDependencies(db, stage);
      controller.startQuiz(categoryId, categoryName, quizSize);

      Scene scene = new Scene(scrollPane, SCENE_WIDTH, SCENE_HEIGHT);
      applyCSS(scene);
      return scene;
    } catch (IOException e) {
      System.out.println("Failed to load Quiz.fxml: " + e.getMessage());
      return BuildCategorySelection(stage, db);
    }
  }

  /**
   * Display Quiz Results
   */
   static Scene BuildQuizResults(Stage stage, DatabaseManager db,
                                        int score, int total, String categoryName) {
    Label title = new Label("Quiz Complete!");
    title.getStyleClass().add("Label");
    title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

    Label categoryLabel = new Label("Category: " + categoryName);
    categoryLabel.getStyleClass().add("Label");

    Label scoreLabel = new Label("Score: " + score + " / " + total);
    scoreLabel.getStyleClass().add("Label");
    scoreLabel.setStyle("-fx-font-size: 18px;");

    Label correctLabel = new Label("Correct: " + score);
    correctLabel.getStyleClass().add("Label");

    Label incorrectLabel = new Label("Incorrect: " + (total - score));
    incorrectLabel.getStyleClass().add("Label");

    // Percentage
    int percentage = (int) Math.round((double) score / total * 100);
    Label percentLabel = new Label(percentage + "%");
    percentLabel.getStyleClass().add("Label");
    percentLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

    Button retryButton = new Button("Try Again");
    retryButton.getStyleClass().add("button");

    Button menuButton = new Button("Return to Menu");
    menuButton.getStyleClass().add("button");

    Button leaderboardButton = new Button("View Leaderboard");
    leaderboardButton.getStyleClass().add("button");

    retryButton.setOnAction(e -> {
      Scene categoryScene = BuildCategorySelection(stage, db);
      stage.setScene(categoryScene);
    });

    menuButton.setOnAction(e -> {
      Scene menu = Create(SceneType.General, stage, db);
      stage.setScene(menu);
    });

    leaderboardButton.setOnAction(e -> {
      Scene leaderboard = Create(SceneType.Leaderboard, stage, db);
      stage.setScene(leaderboard);
    });

    VBox root = new VBox(15, title, categoryLabel, percentLabel,
            scoreLabel, correctLabel, incorrectLabel,
            retryButton, leaderboardButton, menuButton);
    root.setPadding(new Insets(SCENE_PADDING));
    root.setAlignment(Pos.CENTER);
    root.getStyleClass().add("vbox");

    Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
    applyCSS(scene);
    return scene;
  }

}
