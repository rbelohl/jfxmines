package rada.jfxmines;


import java.util.Comparator;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HighscoreEntry {
    private IntegerProperty timeProperty;
    private StringProperty nameProperty;
    public static final Comparator<HighscoreEntry> 
        TIME_COMPARATOR = (a, b) -> Integer.compare(a.getTime(), b.getTime());
    
    public HighscoreEntry(String name, int time) {
        this.timeProperty = new SimpleIntegerProperty(time);
        this.nameProperty = new SimpleStringProperty(name);
    }
    
    public int getTime() {
        return timeProperty.get();
    }
    
    public void setTime(int time) {
        this.timeProperty.set(time);
    }
    
    public String getName() {
        return nameProperty.get();
    }
    
    public void setName(String name) {
        this.nameProperty.set(name);
    }
    
    
    public String getFormatedTime() {
        return String.format("%02d:%02d", timeProperty.get() / 60, timeProperty.get() % 60);
    }

    
}
