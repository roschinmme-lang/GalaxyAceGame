package GalaxyAce;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * LeaderboardManager
 * ------------------
 * Handles persistence and management of PlayerProfile objects.
 * Responsibilities:
 * - Load/save serialized profiles from/to disk
 * - Create/authenticate profiles
 * - Update profiles after game runs (skips guest accounts)
 * - Provide a leaderboard sorted by finalScore
 */
public class LeaderboardManager {
    // File to store the serialized profiles
    private static final String FILE_PATH = "src/GalaxyAce/resources/leaderboard_profiles.dat"; 
    private List<PlayerProfile> profiles;

    /** Loads existing profiles from disk or creates an empty list. */
    public LeaderboardManager() {
        profiles = loadProfiles();
    }

    // --- File I/O ---

    /**
     * Reads serialized profile list from FILE_PATH; returns empty list if
     * the file is missing or cannot be parsed.
     */
    @SuppressWarnings("unchecked")
    private List<PlayerProfile> loadProfiles() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (List<PlayerProfile>) ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>(); // File doesn't exist yet
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading profiles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Writes the profile list to FILE_PATH using Java serialization. */
    public void saveProfiles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(profiles);
        } catch (IOException e) {
            System.err.println("Error saving profiles: " + e.getMessage());
        }
    }

    // --- Profile Management & Authentication ---
    
    /** Returns a profile matching name (case-insensitive) or null. */
    public PlayerProfile getProfileByName(String name) {
        for (PlayerProfile profile : profiles) {
            if (profile.getProfileName().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        return null; // Profile not found
    }

    /**
     * Creates a new profile when the name is unused, saves to disk, and returns it.
     * Returns null if the name already exists.
     */
    public PlayerProfile createNewProfile(String name, String password) {
        if (getProfileByName(name) != null) {
            return null; // Profile already exists
        }
        PlayerProfile newProfile = new PlayerProfile(name, password);
        profiles.add(newProfile);
        saveProfiles();
        return newProfile;
    }
    
    /**
     * Authenticates by comparing hash of provided password against stored hash.
     * Returns the profile on success; null otherwise.
     */
    public PlayerProfile authenticateProfile(String name, String password) {
        PlayerProfile profile = getProfileByName(name);
        if (profile != null) {
            // Compare the simple hash of the provided password with the stored hash
            if (profile.getPasswordHash() == password.hashCode()) {
                return profile; // Authentication successful
            }
        }
        return null; // Authentication failed
    }

    /**
     * Updates a non-guest profile with the latest run results, then saves.
     * Guest profiles are intentionally not persisted.
     */
    public void updateAndSaveProfile(PlayerProfile profile, int score, int bossesKilled, boolean victory) {
        // DO NOT save or update profile data if the user is a guest
        if (profile.isGuest()) {
            return;
        }
        
        profile.updateProfile(score, bossesKilled, victory);
        saveProfiles();
    }

    /**
     * Returns a new list sorted by finalScore descending for leaderboard views.
     */
    public List<PlayerProfile> getSortedLeaderboard() {
        List<PlayerProfile> sortedList = new ArrayList<>(profiles);
        // Sorts by finalScore descending
        Collections.sort(sortedList, Comparator.comparingInt(PlayerProfile::getFinalScore).reversed());
        return sortedList;
    }
}