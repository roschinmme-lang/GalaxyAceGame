package GalaxyAce;

import java.io.Serializable;

/**
 * PlayerProfile
 * -------------
 * Serializable data class representing a player's identity and progress.
 * Supports registered profiles (with password hash) and guest sessions.
 * Only registered profiles persist high scores and victory flags.
 */
public class PlayerProfile implements Serializable {
    // Unique ID used for serialization
    private static final long serialVersionUID = 3L; // Updated serialVersionUID for guest feature

    // Identity
    private String profileName;
    private int passwordHash; // hashCode of the password for simple auth

    // Progress / stats
    private int finalScore;      // Highest recorded score
    private int bossesKilled;    // Boss kills associated with best score
    private int wavesCleared;    // Mirrors bossesKilled for display
    private boolean reachedVictory; // Persistent flag once victory is achieved

    // Session type
    private boolean isGuest; // Flag to identify guest sessions

    /**
     * Constructor for named/registered profiles; stores password hash.
     */
    public PlayerProfile(String profileName, String password) {
        this.profileName = profileName;
        this.passwordHash = password.hashCode(); // Store simple hash
        this.finalScore = 0;
        this.bossesKilled = 0;
        this.wavesCleared = 0;
        this.reachedVictory = false;
        this.isGuest = false; // Registered profiles are not guests
    }
    
    /**
     * Constructor for guest sessions; no password, not persisted/updated.
     */
    public PlayerProfile(String profileName) {
        this.profileName = profileName;
        this.passwordHash = 0; // No password for guests
        this.finalScore = 0;
        this.bossesKilled = 0;
        this.wavesCleared = 0;
        this.reachedVictory = false;
        this.isGuest = true; // Flag this as a guest session
    }

    // --- Getters ---
    public String getProfileName() { return profileName; }
    public int getFinalScore() { return finalScore; }
    public int getBossesKilled() { return bossesKilled; }
    public int getWavesCleared() { return wavesCleared; }
    public boolean hasReachedVictory() { return reachedVictory; }
    public int getPasswordHash() { return passwordHash; }
    public boolean isGuest() { return isGuest; } // New getter

    // --- Update rules (used when a game run ends) ---
    /**
     * Updates persistent stats for registered profiles only. Applies a high-score
     * rule (only replace when score improves) and permanently records victory.
     */
    public void updateProfile(int score, int bossesKilled, boolean victory) {
        // Only update registered profiles (non-guests)
        if (isGuest) {
            return; 
        }
        
        // Only update if the new score is higher than the existing high score
        if (score > this.finalScore) {
            this.finalScore = score;
            this.bossesKilled = bossesKilled;
            this.wavesCleared = bossesKilled; 
        }
        
        // Victory status is a permanent flag
        if (victory) {
            this.reachedVictory = true;
        }
    }
}