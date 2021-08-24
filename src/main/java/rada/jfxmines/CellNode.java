package rada.jfxmines;


import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CellNode extends StackPane {
    private Button button;
    private Label label;
    private ImageView buttonImgV;
    private boolean flag;
    private int x;
    private int y;
    private Game game;
    private EventHandler<ActionEvent> firstClickEventFilter;
    
    private static final Color[] LABEL_COLORS = {
            Color.color(0, 0, 0, 0),    
            Color.rgb( 66,   0, 255),
            Color.rgb(  0, 136,   0),
            Color.rgb(255,   0,   0),
            Color.rgb( 29,   0, 130),
            Color.rgb(140,   0,   0),
            Color.rgb(  0, 132, 131),
            Color.rgb(  0,   0,   0),
            Color.rgb(128, 128, 128)
    };
    
    private static final Font LABEL_FONT = Font.font("sans", FontWeight.BOLD, 10);
    
    private static final int IMG_HEIGHT = 18;
    private static final int IMG_WIDTH = 15;
    
    private static final PseudoClass FOCUSED_PSEUDOCLASS = PseudoClass.getPseudoClass("focused");
    
    private static final String REVEALED_LBL_CSS = 
            "-fx-border-color: gray;\n"
            + "-fx-border-insets: 0;\n"
            + "-fx-border-width: 0.25;\n"
            + "-fx-border-style: solid;\n";
    
    private static Image FLAG_IMG;
    private static Image BOMB_IMG;
    
    static {
        try {
            FLAG_IMG = new Image(CellNode.class.getResourceAsStream("/flag.png"));
        } catch (Exception e) {
            e.printStackTrace();
            FLAG_IMG = null;
        }
        
        try {
            BOMB_IMG = new Image(CellNode.class.getResourceAsStream("/bomb.png"));
        } catch (Exception e) {
            e.printStackTrace();
            BOMB_IMG = null;
        }
    }
    
    public CellNode(Game game, int x, int y) {
        super();
        this.x = x;
        this.y = y;
        this.game = game;
        
        makeLabel();
        makeButton();
        
        // Filter for first click, it is removed in Game::start
        firstClickEventFilter = e -> game.start(x, y);
        this.addEventFilter(ActionEvent.ACTION, firstClickEventFilter);
        
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        
        this.getChildren().add(label);
        this.getChildren().add(button);
        setFlag(false);
    }
    
    private void makeLabel() {
        label = new Label("");
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setFont(LABEL_FONT);
        label.setAlignment(Pos.CENTER);
        
        

        
        // Focus / unfocus neighboring cells on middle click / both buttons click
        label.setOnMousePressed(e -> onLabelPress(e));
        label.setOnMouseReleased(e -> setNeighborFocus(x, y, false));
        // Unfocus neighbors when mouse exits
        label.setOnMouseExited(e -> setNeighborFocus(x, y, false));
        
        // Actual middle click reaction that reveals neighboring cells
        label.setOnMouseClicked(e -> onLabelClick(e));
    }
    
    private void makeButton() {
        button = new Button();
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setFont(LABEL_FONT);
        button.setOnMouseClicked(e -> onButtonClick(e));
        
        if (FLAG_IMG != null) {
            buttonImgV = new ImageView(FLAG_IMG);
            buttonImgV.setFitHeight(IMG_HEIGHT);
            buttonImgV.setFitWidth(IMG_WIDTH);
            button.setGraphic(buttonImgV);
        } else {
            button.setText("🚩");
        }
        // Button cannot be selected by keyboard
        // We don't want the blue border around any button
        button.setFocusTraversable(false);
    }
    
    private void onLabelPress(MouseEvent e) {
        // Either middle button or both left and right button
        if (e.isMiddleButtonDown() || (e.isPrimaryButtonDown() && e.isSecondaryButtonDown())) {
            setNeighborFocus(x, y, true);
        }
    }

    /**
     * Sets the focused pseudo class for all unrevealed neighboring cells.
     * 
     * @param x x-coordinate of cell
     * @param y y-coordinate of cell
     * @param focused value to set
     */
    private void setNeighborFocus(int x, int y, boolean focused) {
        game.getNeighborCellNodes(x, y).stream()
                .map(n -> n.button)
                .filter(btn -> btn != null)
                .forEach(btn -> btn.pseudoClassStateChanged(FOCUSED_PSEUDOCLASS, focused));
    }
    
    private void onLabelClick(MouseEvent e) {
        // One of either left or right button is still down, button was clicked with both buttons
        // Or middle button was used
        if (e.isPrimaryButtonDown() || e.isSecondaryButtonDown() || e.getButton() == MouseButton.MIDDLE) {
            game.middleClick(x, y);
        }
        return;
    }

    public void removeFirstClickFilter() {
        this.removeEventFilter(ActionEvent.ACTION, firstClickEventFilter);
    }
    
    private void onButtonClick(MouseEvent e) {
        // Button was clicked with both buttons
        // Do nothing
        if (e.isPrimaryButtonDown() || e.isSecondaryButtonDown()) {
            return;
        }
        
        var mouseBtn = e.getButton();
        
        if (mouseBtn == MouseButton.PRIMARY) {
            game.reveal(x, y);
        } else if (mouseBtn == MouseButton.SECONDARY) {
            game.toggleFlag(x, y);
        }
        
        return;
    }

    /**
     * If {@code true} is passed, button text turns into a flag.
     * Else the button is cleared.
     * @param flag 
     */
    public void setFlag(boolean flag) {
        this.flag = flag;
        if (this.flag) {
            // Opaque color
            button.setTextFill(new Color(0,0,0,1));
            if (buttonImgV != null) {
                buttonImgV.setOpacity(1);
            }
        } else {
            // Transparent color
            button.setTextFill(new Color(0,0,0,0));
            if (buttonImgV != null) {
                buttonImgV.setOpacity(0);
            }
        }
    }
    
    /**
     * Makes the cell label into a mine.
     * Passing {@code false} into this method does nothing!
     * @param mine
     */
    public void setMine(boolean mine) {
        if (mine) {
            if (BOMB_IMG != null) {
                var imgV = new ImageView(BOMB_IMG);
                imgV.setFitHeight(IMG_HEIGHT);
                imgV.setFitWidth(IMG_WIDTH);
                label.setGraphic(imgV);
                label.setText("");
            } else {
                label.setText("💣");
                label.setTextFill(Color.BLACK);
            }
        } 
    }
    
    public void reveal() {
        // If we were to remove the button, the size of the cell could change
        // Instead the button is made invisible
        button.setOpacity(0);
        button.setDisable(true);
        
        label.setStyle(REVEALED_LBL_CSS);
    }
    
    public void setAdjacent(int adjacent) {
        label.setText("" + adjacent);
        label.setTextFill(LABEL_COLORS[adjacent]);
    }
    
}
