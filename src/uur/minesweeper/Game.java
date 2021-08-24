package uur.minesweeper;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class Game {
    private CellNode[][] cellNodes;
    private Cell[][] cells;
    private int numMines;
    private SimpleIntegerProperty numFlagsProperty;
    private SimpleIntegerProperty timeProperty;
    private Timeline timeline;
    private int unrevealedCells;
    private Consumer<GameOverArgs> onGameOver;
    private Difficulty difficulty;
    private boolean gameInProgress;
    
    public Game(Difficulty difficulty) {
        this(difficulty.getWidth(), difficulty.getHeight(), difficulty.getNumMines());
        this.difficulty = difficulty;
    }
    
    private Game(int width, int height, int numMines) {
        cellNodes = new CellNode[height][];
        cells = new Cell[height][width];
        this.numMines = numMines;
        unrevealedCells = width * height;
        numFlagsProperty = new SimpleIntegerProperty(0);
        timeProperty = new SimpleIntegerProperty(0);
        gameInProgress = false;
        
        // Timer that counts seconds
        timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), e -> timeProperty.set(timeProperty.get() + 1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        
        for (int y = 0; y < cellNodes.length; y++) {
            cellNodes[y] = new CellNode[width];
            cells[y] = new Cell[width];
            for (int x = 0; x < cellNodes[y].length; x++) {
                cellNodes[y][x] = new CellNode(this, x, y);
                cells[y][x] = new Cell();
                cells[y][x].x = x;
                cells[y][x].y = y;
            }
        }
    }
    
    /**
     * Starts the game.
     * @param x x-coordinate of clicked cell
     * @param y y-coordinate of clicked cell
     */
    public void start(int clickedX, int clickedY) {
        var random = new Random();
        int width = cells[0].length;
        int height = cells.length;
        
        int remaining = numMines;
        
        while (remaining > 0) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            
            // Check if generated point is near clicked cell
            // That is to ensure that the first click is an opening
            if (Math.abs(x - clickedX) > 1 || Math.abs(y - clickedY) > 1) {
                // If there is a mine already, another number is generated
                if (!cells[y][x].isMine) {
                    cells[y][x].isMine = true;
                    remaining--;
                }
            }
        }
        calculateAdjacent();
        removeFilters();
        gameInProgress = true;
        timeline.playFromStart();
    }
    
    private void calculateAdjacent() {
        int width = cells[0].length;
        int height = cells.length;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x].adjacent = (int)neighbors(x, y)
                                                .stream()
                                                .filter(cell -> cell.isMine)
                                                .count();
                
                cellNodes[y][x].setAdjacent(cells[y][x].adjacent);
                cellNodes[y][x].setMine(cells[y][x].isMine);
            }
        }
    }
    
    private void removeFilters() {
        int width = cells[0].length;
        int height = cells.length;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cellNodes[y][x].removeFirstClickFilter();
            }
        }
    }
    
    public SimpleIntegerProperty numFlagsProperty() {
        return numFlagsProperty;
    }
    
    public SimpleIntegerProperty timeProperty() {
        return timeProperty;
    }
    
    public CellNode[][] getCellNodes() {
        return cellNodes;
    }
    
    public void setOnGameOver(Consumer<GameOverArgs> f) {
        this.onGameOver = f;
    }
    
    public void reveal(int x, int y) {
        var queue = new LinkedList<Cell>();
        queue.add(cells[y][x]);
        
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (current.flag || current.revealed) {
                continue;
            }
            cellNodes[current.y][current.x].reveal();
            if (current.isMine) {
                gameOver(false);
                return;
            }
            current.revealed = true;
            unrevealedCells--;
            if (unrevealedCells <= numMines) {
                gameOver(true);
            }
            if (current.adjacent == 0) {
                queue.addAll(neighbors(current.x, current.y));
            }
        }
    }
    
    public void revealNeighbors(int x, int y) {
        neighbors(x, y).stream()
            .filter(c -> !c.revealed)
            .forEach(c -> reveal(c.x, c.y));
    }
    
    public void middleClick(int x, int y) {
        long flaggedNeighbors = neighbors(x, y).stream()
                                    .filter(c -> c.flag)
                                    .count();
        // Only do this if there is a correct number of flags
        if (flaggedNeighbors == cells[y][x].adjacent) {
            revealNeighbors(x, y);
        }
    }
    
    private List<Cell> neighbors(int x, int y) {
        int width = cells[0].length;
        int height = cells.length;
        var neighbors = new LinkedList<Cell>();
        
        // Make sure the index is within range
        int startY = y > 0 ? y - 1 : 0;
        int startX = x > 0 ? x - 1 : 0;
        int endX = x < width - 1 ? x + 2 : width;
        int endY = y < height - 1 ? y + 2 : height;
        
        
        for (int tmpY = startY; tmpY < endY; tmpY++) {
            for (int tmpX = startX; tmpX < endX; tmpX++) {
                if (!((tmpX == x) && (tmpY == y))) {
                    neighbors.add(cells[tmpY][tmpX]);
                }
            }
        }
        
        return neighbors;
    }
    
    public List<CellNode> getNeighborCellNodes(int x, int y) {
        return neighbors(x, y).stream()
                    .map(c -> cellNodes[c.y][c.x])
                    .collect(Collectors.toList());
    }
    
    public void toggleFlag(int x, int y) {
        var cell = cells[y][x];
        cell.flag = !cell.flag;
        cellNodes[y][x].setFlag(cell.flag);
        
        // Update number of flags
        numFlagsProperty.set(numFlagsProperty.get() + (cell.flag ? +1 : -1));
    }
    
    private void gameOver(boolean win) {
        // Stop counting seconds
        timeline.stop();
        gameInProgress = false;
        if (onGameOver != null) {
            onGameOver.accept(new GameOverArgs(win, timeProperty.intValue(), difficulty));
        }
    }
    
    public boolean isInProgress() {
        return gameInProgress;
    }
    
    private static class Cell {
        public int adjacent;
        public boolean flag;
        public boolean isMine;
        public boolean revealed;
        public int x;
        public int y;
    }

}
