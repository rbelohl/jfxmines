package rada.jfxmines;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public class CustomDifficultyDialog extends Dialog<Difficulty> {
    private Spinner<Integer> widthSp;
    private Spinner<Integer> heightSp;
    private Spinner<Integer> minesSp;
    
    private static final int MIN_MINES = 1;
    private static final int MAX_MINES = 99;
    
    private static final int MAX_SIZE = 40;
    private static final int MIN_SIZE = 5;
    
    private Difficulty difficulty;
    private int maxMines;
    
    
    private static final StringConverter<Integer> STRING_CONVERTER = new StringConverter<Integer>() {
        @Override public String toString(Integer object) {
            return object + "";
        }
        
        @Override public Integer fromString(String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return Integer.MIN_VALUE;
            }
        }
    };
    
    public CustomDifficultyDialog(Difficulty difficulty) {
        setHeaderText("Custom difficulty");
        setTitle("Custom difficulty");
        var dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        
        this.difficulty = difficulty;
        
        dialogPane.setContent(createContent());
        
        setResultConverter(param -> ButtonType.OK.equals(param) ? this.difficulty : null);
    }
    
    private GridPane createContent() {
        var gridPane = new GridPane();
        
        widthSp = new Spinner<>(MIN_SIZE, MAX_SIZE, difficulty.getWidth());
        widthSp.valueProperty().addListener((obs, oldVal, newVal) -> sizeChanged(newVal, widthSp));
        widthSp.getValueFactory().setConverter(STRING_CONVERTER);
        widthSp.setEditable(true);
        var widthLbl = new Label("Width:");
        gridPane.add(widthLbl, 0, 0);
        gridPane.add(widthSp, 1, 0);
        
        heightSp = new Spinner<>(MIN_SIZE, MAX_SIZE, difficulty.getHeight());
        heightSp.valueProperty().addListener((obs, oldVal, newVal) -> sizeChanged(newVal, heightSp));
        heightSp.setEditable(true);
        heightSp.getValueFactory().setConverter(STRING_CONVERTER);
        var heightLbl = new Label("Height:");
        gridPane.add(heightLbl, 0, 1);
        gridPane.add(heightSp, 1, 1);
        
        minesSp = new Spinner<>(MIN_MINES, Integer.MAX_VALUE, difficulty.getNumMines());
        changeMaxMines();
        minesSp.setEditable(true);
        minesSp.valueProperty().addListener((obs, oldVal, newVal) -> minesChanged(oldVal, newVal));
        var minesLbl = new Label("Mines:");
        gridPane.add(minesLbl, 0, 2);
        gridPane.add(minesSp, 1, 2);
        
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(20);
        gridPane.setVgap(10);
        
        return gridPane;
    }
    
    private void sizeChanged(int newValue, Spinner<Integer> spinner) {
        if (newValue < MIN_SIZE) {
            spinner.getValueFactory().setValue(MIN_SIZE);
        } else if (newValue > MAX_SIZE) {
            spinner.getValueFactory().setValue(MAX_SIZE);
        }
        changeMaxMines();
        changeResult();
    }
    
    private void minesChanged(int oldValue, int newValue) {
        if (newValue < MIN_MINES) {
            minesSp.getValueFactory().setValue(MIN_MINES);
        } else if (newValue > maxMines) {
            minesSp.getValueFactory().setValue(maxMines);
        }
        changeResult();
    }
    
    private void changeMaxMines() { 
        maxMines = (widthSp.getValue() * heightSp.getValue()) - 20;
        maxMines = Math.min(MAX_MINES, maxMines);
        int currentMines = Math.min(minesSp.getValue(), maxMines);
        minesSp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_MINES, maxMines, currentMines));
        minesSp.getValueFactory().setValue(currentMines);
        minesSp.getValueFactory().setConverter(STRING_CONVERTER);
    }
    
    
    private void changeResult() {
        difficulty = Difficulty.getCustom(widthSp.getValue(), heightSp.getValue(), minesSp.getValue());
    }
}
