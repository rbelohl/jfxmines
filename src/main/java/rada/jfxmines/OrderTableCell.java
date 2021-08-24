package rada.jfxmines;

import javafx.scene.control.TableCell;

public class OrderTableCell<T> extends TableCell<T, String> {
    
    @Override 
    protected void updateItem(String item, boolean empty) {
        // TODO Auto-generated method stub
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        } else {
            setText((getIndex() + 1) + ".");
        }
    }
}
