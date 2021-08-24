package uur.minesweeper;

public class GameOverArgs {
    
    public final boolean win;
    public final int timeSeconds;
    public final Difficulty difficulty;
    
    public GameOverArgs(boolean win, int timeSeconds, Difficulty difficulty) {
        this.win = win;
        this.timeSeconds = timeSeconds;
        this.difficulty = difficulty;
    }
}
