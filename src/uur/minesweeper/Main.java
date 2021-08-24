package uur.minesweeper;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

    private BorderPane rootPane;
    private MenuBar topMenu;
    private GridPane minefield;
    private Game game;
    private Label flagsLbl;
    private Label timeLbl;
    private Difficulty difficulty;
    private Difficulty customDifficulty;
    private ChangeListener<? super Number> flagsListener;
    private ChangeListener<? super Number> timeListener;
    private Map<Difficulty, List<HighscoreEntry>> highscores;
    private IHighscoreStorage highscoreStrorage;
    
    private String lastPlayerName = "player";
    private RadioMenuItem easyRBtn;
    private RadioMenuItem mediumRBtn;
    private RadioMenuItem hardRBtn;
    private RadioMenuItem customRBtn;
    private Stage primaryStage;
    
    private Image icon;
    
    private static final int MAX_HIGHSCORES = 10;
    private static final Font TOP_LABELS_FONT = Font.font("sans", FontWeight.BOLD, 14);
    private static final String HIGHSCORES_FILENAME = "mines_hs";
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            icon = new Image(getClass().getResourceAsStream("icon.png"));
        } catch (Exception e) {
            e.printStackTrace();
            icon = null;
        }
        this.primaryStage = primaryStage;
        highscoreStrorage = new HighscoreFileStorage(HIGHSCORES_FILENAME);
        highscores = highscoreStrorage.load();
        
        // Sort all highscore entries and limit them to MAX_HIGHSCORES
        for (var mapEntry : highscores.entrySet()) {
            var newVal = mapEntry.getValue()
                                    .stream()
                                    .limit(MAX_HIGHSCORES)
                                    .collect(Collectors.toList());
            mapEntry.setValue(newVal);
        }
        
        flagsListener = (obs, oldVal, newVal) -> updateFlagsLabel(newVal);
        timeListener = (obs, oldVal, newVal) -> updateTimeLabel(newVal);
        
        difficulty = Difficulty.getHard();
        customDifficulty = difficulty;
        Parent root = getRoot();
        rootPane = (BorderPane) root;        
        
        primaryStage.setTitle("Minesweeper");
        if (icon != null) {
            primaryStage.getIcons().add(icon);
        }
        var scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        
        newGame();
        primaryStage.show();
        
    }
    

    private Parent getRoot() {
        var root = new BorderPane();
        
        
        root.setTop(getTopPanel());
        return root;
    }
    
    private Node getTopPanel() {
        var top = new VBox();
        topMenu = getTopMenu();
        top.getChildren().add(topMenu);
        var labelsPane = new GridPane();
       
        labelsPane.setPadding(new Insets(10));
        flagsLbl = new Label("");
        flagsLbl.setFont(TOP_LABELS_FONT);
        labelsPane.add(flagsLbl, 0, 0);
        GridPane.setFillWidth(flagsLbl, true);
        
        timeLbl = new Label("");
        timeLbl.setFont(TOP_LABELS_FONT);
        labelsPane.add(timeLbl, 1, 0);
        GridPane.setHalignment(timeLbl, HPos.RIGHT);
        GridPane.setFillWidth(timeLbl, true);
        GridPane.setHgrow(timeLbl, Priority.ALWAYS);
        
        top.getChildren().add(labelsPane);
        
        return top;
    }
    
    private MenuBar getTopMenu() {
        var menu = new MenuBar();
        
        var gameMenu = new Menu("_Game");
        var newGame = new MenuItem("_New game");
        newGame.setOnAction(e -> onNewGameAction());
        newGame.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        var showHS = new MenuItem("Show _Highscores");
        showHS.setOnAction(e -> showHighscores());
        showHS.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
        gameMenu.getItems().add(newGame);
        gameMenu.getItems().add(showHS);
        
        var settingMenu = new Menu("_Settings");
        var difficultyMenu = new Menu("_Difficutly");
        
        var toggleGroup = new ToggleGroup();
        easyRBtn = new RadioMenuItem("_Easy");
        easyRBtn.setOnAction(e -> changeDifficulty(Difficulty.getEasy()));
        easyRBtn.setToggleGroup(toggleGroup);
        mediumRBtn = new RadioMenuItem("_Medium");
        mediumRBtn.setOnAction(e -> changeDifficulty(Difficulty.getMedium()));
        mediumRBtn.setToggleGroup(toggleGroup);
        hardRBtn = new RadioMenuItem("_Hard");
        hardRBtn.setSelected(true);
        hardRBtn.setOnAction(e -> changeDifficulty(Difficulty.getHard()));
        hardRBtn.setToggleGroup(toggleGroup);
        customRBtn = new RadioMenuItem("_Custom");
        customRBtn.setOnAction(e -> changeDifficulty(customDifficulty));
        customRBtn.setToggleGroup(toggleGroup);
        
        var confCustom = new MenuItem("Con_figure custom");
        confCustom.setOnAction(e -> changeCustomDifficulty());
        
        difficultyMenu.getItems().addAll(easyRBtn, mediumRBtn, hardRBtn, customRBtn, new SeparatorMenuItem(), confCustom);
        
        settingMenu.getItems().add(difficultyMenu);
        
        menu.getMenus().add(gameMenu);
        menu.getMenus().add(settingMenu);
        
        return menu;        
    }
    
    private void newGame() {
        if (game != null) {
            game.numFlagsProperty().removeListener(flagsListener);
            game.timeProperty().removeListener(timeListener);
            game.setOnGameOver(null);
        }
        game = new Game(difficulty);
        
        updateFlagsLabel(0);
        updateTimeLabel(0);
        game.numFlagsProperty().addListener(flagsListener);
        game.timeProperty().addListener(timeListener);
        game.setOnGameOver(this::gameOver);
        minefield = getMinefield();
        rootPane.setCenter(minefield);
        
        return;
    }
    
    private void onNewGameAction() {
        if (game != null && game.isInProgress()) {
            var alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("New game");
            alert.setContentText("Do you wish to start a new game?\nCurrent progress will be lost.");
            alert.showAndWait();
            if (alert.getResult() != ButtonType.OK) {
                return;
            } 
        }
        newGame();
    }

    private void changeDifficulty(Difficulty difficulty) {
        if (difficulty == this.difficulty) {
            // No need to change
            return;
        }
        if (game != null && game.isInProgress()) {
            var alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("Changing difficulty");
            alert.setContentText("Do you wish to change difficutly?\nChanging difficulty will reset the game.");
            alert.showAndWait();
            if (alert.getResult() != ButtonType.OK) {
                // Revert selection
                if (this.difficulty == Difficulty.getEasy()) {
                    easyRBtn.setSelected(true);
                } else if (this.difficulty == Difficulty.getMedium()) {
                    mediumRBtn.setSelected(true);
                } else if (this.difficulty == Difficulty.getHard()) {
                    hardRBtn.setSelected(true);
                } else {
                    customRBtn.setSelected(true);
                }
                return;
            } 
        }
        
        this.difficulty = difficulty;
        newGame();
    }
    
    private GridPane getMinefield() {
        var field = new GridPane();
        
        var cells = game.getCellNodes();
        int h = cells.length;
        int w = cells[0].length;
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                var cell = cells[y][x];
                GridPane.setFillHeight(cell, true);
                GridPane.setFillWidth(cell, true);
                field.add(cell, x, y);
                
            }
        }
        field.setPadding(new Insets(10));
        
        field.setPrefWidth(Double.MAX_VALUE);
        
        return field;
    }
    
    private void gameOver(GameOverArgs args) {
        var text = args.win ? "You win!" : "You lost.";
        
        var alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game over");
        alert.setHeaderText(text);
        
        // Add event filter to disable all mouse interaction with the field
        minefield.addEventFilter(MouseEvent.ANY, e -> e.consume());
        
        alert.showAndWait();
        
        // Player won and the difficulty is not custom
        if (args.win && highscores.containsKey(args.difficulty)) {
            var highscoresForDifficulty = highscores.get(args.difficulty);
            int highscoreSize = highscoresForDifficulty.size();
            
            // Either there aren't enough highscores yet 
            // Or the player beat at least the worst player
            // This assumes the highscores are sorted
            if (highscoreSize < MAX_HIGHSCORES || highscoresForDifficulty.get(highscoreSize - 1).getTime() > args.timeSeconds) {
                String playerName = showNameInputDialog();
                var newEntry = new HighscoreEntry(playerName, args.timeSeconds);
                if (highscoreSize < MAX_HIGHSCORES) {
                    highscoresForDifficulty.add(newEntry);
                } else {
                    highscoresForDifficulty.set(highscoreSize - 1, newEntry);
                }
                highscoresForDifficulty.sort(HighscoreEntry.TIME_COMPARATOR);
                highscoreStrorage.save(highscores);
                showHighscores();
            }
        }
    }
    
    private String showNameInputDialog() {
        var defaultName = lastPlayerName;
        var dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Name input");
        dialog.setHeaderText("Congratulations, you made it to the highscores!");
        dialog.setContentText("Please enter your name");
        
        var name = dialog.showAndWait().orElse(defaultName);
        lastPlayerName = name;
        return name;
    }
    
    
    private void showHighscores() {
        var window = new HighscoreStage(highscores);
        
        if (icon != null) {
            window.getIcons().add(icon);
        }
        
        window.initOwner(primaryStage);
        window.initModality(Modality.WINDOW_MODAL);
        window.showAndWait();
    }
    
    private void changeCustomDifficulty() {
        boolean customSelected = difficulty == customDifficulty;
        // Depending on the dialog result 
        // change custom or keep current value
        customDifficulty = new CustomDifficultyDialog(customDifficulty)
                                    .showAndWait()
                                    .orElse(customDifficulty);
        if (!game.isInProgress()) {
            changeDifficulty(customDifficulty);
        } else if(customSelected) {
            // If custom difficulty is currently selected
            // We must update it, change will be visible when new game is started
            difficulty = customDifficulty;
        }
    }
    
    private void updateFlagsLabel(Number newVal) {
        int numFlags = newVal.intValue();
        flagsLbl.setText("Mines: " + numFlags + " / " + difficulty.getNumMines());
        if (numFlags > difficulty.getNumMines()) {
            flagsLbl.setTextFill(Color.RED);
        } else {
            flagsLbl.setTextFill(Color.BLACK);
        }
    }
    
    private void updateTimeLabel(Number time) {
        int minutes = time.intValue() / 60;
        int seconds = time.intValue() % 60;
        timeLbl.setText(String.format("Time: %02d:%02d", minutes, seconds));
        
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    
}
