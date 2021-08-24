package rada.jfxmines;

import java.util.List;
import java.util.Map;

public interface IHighscoreStorage {
    /**
     * Loads highscores from storage
     * @return highscores loaded highscores, if unsuccessful, returns empty highscores
     */
    public Map<Difficulty, List<HighscoreEntry>> load();
    
    /**
     * Save highscores to storage
     * @param highscores highscores to save
     * @return {@code true} if successful, {@code false} otherwise
     */ 
    public boolean save(Map<Difficulty, List<HighscoreEntry>> highscores);
}
