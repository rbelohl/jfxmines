package rada.jfxmines;

import java.util.List;
import java.util.Map;

import javafx.beans.binding.StringBinding;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HighscoreStage extends Stage {
    private Map<Difficulty, List<HighscoreEntry>> highscores;
    
    public HighscoreStage(Map<Difficulty, List<HighscoreEntry>> highscores) {
        super();
        this.highscores = highscores;
        var scene = new Scene(getRoot(), 300, 500);
        this.setMinWidth(300);
        this.setMinHeight(500);
        this.setScene(scene);
        
        this.setTitle("Highscores");
    }
    
    private BorderPane getRoot() {
        var root = new BorderPane();
        var tabPane = new TabPane();
        
        tabPane.getTabs().add(getTab("Easy", Difficulty.getEasy()));
        tabPane.getTabs().add(getTab("Medium", Difficulty.getMedium()));
        tabPane.getTabs().add(getTab("Hard", Difficulty.getHard()));
        
        root.setCenter(tabPane);
        
        return root;
    }
    
    private Tab getTab(String title, Difficulty difficulty) {
        var tab = new Tab();
        tab.setClosable(false);
        tab.setText(title);
        tab.setContent(getTable(difficulty));
        return tab;
    }
    
    private TableView<HighscoreEntry> getTable(Difficulty difficulty) {
        var table = new TableView<HighscoreEntry>();
        
        var orderCol = new TableColumn<HighscoreEntry, String>("");
        orderCol.setCellFactory(value -> new OrderTableCell<HighscoreEntry>());
        orderCol.setMinWidth(32);
        orderCol.setMaxWidth(48);
        orderCol.setPrefWidth(32);
        orderCol.setEditable(false);
        orderCol.setSortable(false);
        
        var nameCol = new TableColumn<HighscoreEntry, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setEditable(false);
        nameCol.setSortable(false);
        nameCol.setMinWidth(100);
        
        var timeCol = new TableColumn<HighscoreEntry, String>("Time");
        timeCol.setCellValueFactory(col -> new StringBinding() {
            @Override
            protected String computeValue() {
                return col.getValue().getFormatedTime();
            }
        });
        timeCol.setMinWidth(50);
        timeCol.setEditable(false);
        timeCol.setSortable(false);
        
        table.getColumns().add(orderCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(timeCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        if (highscores != null) {
            table.getItems().addAll(highscores.get(difficulty));
            table.getItems().sort(HighscoreEntry.TIME_COMPARATOR);
        }
        
        return table;
    }
}
