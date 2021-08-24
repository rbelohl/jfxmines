package uur.minesweeper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Stores highscores in a CSV file
 * @author rada
 *
 */
public class HighscoreFileStorage implements IHighscoreStorage {

    private static final char separator = ';';
    private static final Map<String, Difficulty> difficutlyTable = Map.of(  "easy", Difficulty.getEasy(),
                                                                            "medium", Difficulty.getMedium(), 
                                                                            "hard", Difficulty.getHard()
                                                                            );
    
    
    private final String filename;
    
    public HighscoreFileStorage(String filename) {
        this.filename = filename;
    }
    
    @Override
    public Map<Difficulty, List<HighscoreEntry>> load() {
        var map = new HashMap<Difficulty, List<HighscoreEntry>>();
        for (var difficulty : difficutlyTable.values()) {
            // Create a linked list for each difficulty
            map.put(difficulty, new LinkedList<HighscoreEntry>());
        }
        try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var pair = lineToEntry(line);
                if (pair == null) {
                    // Skip invalid lines
                    System.err.println("Invalid line: " + line);
                    continue;
                }
                map.get(pair.x).add(pair.y);
            }
        } catch (IOException e) {
            return map;
        } 
        return map;
    }

    @Override
    public boolean save(Map<Difficulty, List<HighscoreEntry>> highscores) {
        try (var writer = new BufferedWriter(new FileWriter(filename))) {
            for (var mapEntry : highscores.entrySet()) {
                var difficulty = mapEntry.getKey();
                for (var highscoreEntry : mapEntry.getValue()) {
                    String line = entryToLine(difficulty, highscoreEntry);
                    if (line != null) {
                        writer.write(line);
                        writer.newLine();
                    } else {
                        System.err.println("Cannot save custom difficulty.");
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
    
    private Pair<Difficulty, HighscoreEntry> lineToEntry(String line) {
        String[] split = line.split(separator + "");
        
        try {
            Difficulty difficulty = difficutlyTable.get(split[0]);
            if (difficulty == null) {
                return null;
            }
            String name = split[1];
            int time = Integer.parseInt(split[2]);
            
            if (time < 0) {
                return null;
            }
            
            return new Pair<Difficulty, HighscoreEntry>(difficulty, new HighscoreEntry(name, time));
        } catch (Exception e) {
            return null;
        }
        
    }
    
    private String entryToLine( Difficulty difficulty, HighscoreEntry entry) {
        // Find a key whose value is equal to difficulty
        // Ideally, this would be done using a bi-directional map collection
        // But this is good enough for our purposes 
        Optional<String> diffString = difficutlyTable.entrySet()
                                            .stream()
                                            .filter(e -> e.getValue() == difficulty)
                                            .map(e -> e.getKey())
                                            .findFirst();
        
        if (!diffString.isPresent()) {
            return null;
        }
        
        // Remove separator from name
        var name = entry.getName().replaceAll(separator + "", "");
        
        return String.format("%s%c%s%c%d", diffString.get(), separator, name, separator, entry.getTime());
    }
}
