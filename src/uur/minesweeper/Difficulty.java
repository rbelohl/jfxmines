package uur.minesweeper;

public class Difficulty {
    private int numMines;
    private int width;
    private int height;
    
    private static Difficulty EASY; 
    private static Difficulty MEDIUM;
    private static Difficulty HARD;

    
    private Difficulty(int width, int height, int numMines) {
        this.numMines = numMines;
        this.width = width;
        this.height = height;
    }
    
    public static Difficulty getEasy() {
        if (EASY == null) {
            EASY = new Difficulty(9, 9, 10);
        }
        return EASY;
    }
    
    public static Difficulty getMedium() {
        if (MEDIUM == null) {
            MEDIUM = new Difficulty(16, 16, 40);
        }
        return MEDIUM;
    }
    
    public static Difficulty getHard() {
        if (HARD == null) {
            HARD = new Difficulty(30, 16, 99);
        }
        return HARD;
    }
    
    public static Difficulty getCustom(int width, int height, int numMines) {
        if (!isValid(width, height, numMines)) {
            throw new IllegalArgumentException("Cannot create custom game. Ensure that the field is at least 4x4 and that number of mines doesn't exceed number of square - 10");
        }
        var difficulty = new Difficulty(width, height, numMines);
        if (difficulty.equals(EASY)) {
            return EASY;
        } else if (difficulty.equals(MEDIUM)) {
            return MEDIUM;
        } else if (difficulty.equals(HARD)) {
            return HARD;
        } 
        return difficulty;
    } 
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getNumMines() {
        return numMines;
    }
    
    private static boolean isValid(int width, int height, int numMines) {
        if (width < 4 || height < 4) {
            return false;
        } 

        if (numMines > width * height - 9) {
            return false;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + numMines;
        result = prime * result + width;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Difficulty other = (Difficulty) obj;
        if (height != other.height)
            return false;
        if (numMines != other.numMines)
            return false;
        if (width != other.width)
            return false;
        return true;
    }
}
