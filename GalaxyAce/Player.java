package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * Player
 * ------
 * Represents the user-controlled plane. Handles:
 * - Position and movement with boundary clamping
 * - Shooting (single-shot or triple-shot in super mode)
 * - Defensive states (shield and super mode)
 * - Rendering including shield/super visual effects
 */
public class Player {
    // Current top-left position in the playfield
    private int x, y;
    
    // Visual footprint and movement speed
    private final int width = 40, height = 40;
    private final int speed = 5;
    
    // Sprites for normal and super mode appearances
    private final Image defaultImage;
    private final Image superImage;
    
    // Play area bounds (based on game window 500x700)
    private final int MAX_X = 500 - width; 
    private final int MAX_Y = 700 - height; 

    // Shield state: absorbs a limited number of hits
    private boolean hasShield = false;
    private int shieldHits = 0;
    private final int MAX_SHIELD_HITS = 3; 

    // Super mode: enhanced offense and persistent shield
    private boolean isSuperMode = false;

    // PHASE 3: WEAPON SYSTEM
    public enum Weapon { SPREAD, LASER, HOMING }
    private Weapon currentWeapon = Weapon.SPREAD;

    // PHASE 3: UPGRADE STATS
    private int damageLevel   = 0; // each level = +20% damage (tracked for display)
    private int speedBonus    = 0; // added to base speed per upgrade
    private int fireRateBonus = 0; // subtracted from cooldown per upgrade

    // Super-mode bullet spread parameters (angled shots)
    private final double SUPER_BULLET_DX_WEAK = 2; 
    private final double SUPER_BULLET_DY = -8; // Slightly slower than standard bullet (-10)
    
    /**
     * Creates a player at the given starting coordinates and loads sprites.
     */
    public Player(int x, int y) {
        this.x = x;
        this.y = y;

        defaultImage = new ImageIcon("src/GalaxyAce/resources/Player.png").getImage();
        superImage = new ImageIcon("src/GalaxyAce/resources/SuperPlayer.png").getImage();
    }

    // --- SUPER MODE METHODS ---

    /**
     * Activates super mode, enabling a persistent shield and triple-shot.
     */
    public void activateSuperMode() {
        this.isSuperMode = true;
        this.hasShield = true; 
        this.shieldHits = 999; // Effectively infinite while super is active
    }

    /**
     * Deactivates super mode and clears the persistent shield.
     */
    public void deactivateSuperMode() {
        this.isSuperMode = false;
        this.hasShield = false;
        this.shieldHits = 0;
    }

    /**
     * Returns true when the player is currently in super mode.
     */
    public boolean isSuperMode() {
        return isSuperMode;
    }
    
    // --- SHIELD/HEALTH METHODS ---

    /**
     * Grants a temporary shield that can absorb a limited number of hits.
     * No-op while in super mode (super already provides protection).
     */
    public void activateShield() {
        if (!isSuperMode) { 
            this.hasShield = true;
            this.shieldHits = MAX_SHIELD_HITS;
        }
    }
    
    /**
     * Attempts to consume defensive protection for an incoming hit.
     * @return true if the hit was absorbed (super or shield); false if it should damage health
     */
    public boolean takeHit() {
        if (isSuperMode) {
            return true; // Always protected during super
        }
        if (hasShield) {
            shieldHits--;
            if (shieldHits <= 0) {
                hasShield = false; 
            }
            return true; // Shield absorbed the hit
        }
        return false; // No protection; caller should reduce health
    }

    /**
     * Whether the player currently has a shield (temporary or super-mode).
     */
    public boolean hasShield() {
        return hasShield;
    }
    
    // --- MOVEMENT LOGIC ---
    // Each movement method clamps the player into the playfield bounds.

    // --- UPGRADE METHODS ---
    public void applyDamageUpgrade()   { damageLevel++;   }
    public void applySpeedUpgrade()    { speedBonus += 1; }
    public void applyFireRateUpgrade() { fireRateBonus++; }
    public void applyHealthUpgrade(int amount) { /* handled in mechanics */ }

    public int getSpeedBonus()    { return speedBonus; }
    public int getFireRateBonus() { return fireRateBonus; }
    public int getDamageLevel()   { return damageLevel; }

    // --- WEAPON METHODS ---
    public void setWeapon(Weapon w) { currentWeapon = w; }
    public Weapon getWeapon()       { return currentWeapon; }
    public void cycleWeapon() {
        switch (currentWeapon) {
            case SPREAD: currentWeapon = Weapon.LASER;  break;
            case LASER:  currentWeapon = Weapon.HOMING; break;
            case HOMING: currentWeapon = Weapon.SPREAD; break;
        }
    }

    public void moveLeft() {
        x -= (speed + speedBonus);
        if (x < 0) x = 0;
    }
    
    public void moveRight() {
        x += (speed + speedBonus);
        if (x > MAX_X) x = MAX_X;
    }
    
    public void moveUp() {
        y -= (speed + speedBonus);
        if (y < 0) y = 0;
    }
    
    public void moveDown() {
        y += (speed + speedBonus);
        if (y > MAX_Y) y = MAX_Y;
    }
    
    // --- SHOOTING LOGIC ---
    /**
     * Fires bullets from the player's current position.
     * - Normal: 1 straight bullet
     * - Super:  3 bullets (straight + two angled)
     */
    public ArrayList<Bullet> shoot() {
        ArrayList<Bullet> shots = new ArrayList<>();
        int cx = x + width / 2;

        if (isSuperMode) {
            shots.add(new Bullet(cx - 2, y));
            shots.add(new Bullet(cx - 2, y, -SUPER_BULLET_DX_WEAK, SUPER_BULLET_DY));
            shots.add(new Bullet(cx - 2, y,  SUPER_BULLET_DX_WEAK, SUPER_BULLET_DY));
            return shots;
        }

        switch (currentWeapon) {
            case SPREAD:
                // 3-way spread
                shots.add(new Bullet(cx - 2, y));
                shots.add(new Bullet(cx - 2, y, -2.5, -9));
                shots.add(new Bullet(cx - 2, y,  2.5, -9));
                break;
            case LASER:
                // Single fast narrow shot
                shots.add(new Bullet(cx - 2, y, 0, -18));
                break;
            case HOMING:
                // Standard shot — homing logic handled in AirforceBBMechanics
                shots.add(new Bullet(cx - 2, y));
                break;
        }
        return shots;
    }
    
    // --- DRAW LOGIC ---
    /**
     * Renders the player sprite. When shielded, draws a translucent aura and
     * remaining shield hits; when in super mode, draws a golden aura.
     */
    public void draw(Graphics g) {
        Image currentImage = isSuperMode ? superImage : defaultImage;
        g.drawImage(currentImage, x, y, width, height, null);
        
        if (hasShield && !isSuperMode) {
            // Cyan shield ring with a small info label
            g.setColor(new Color(0, 255, 255, 128)); 
            g.fillOval(x - 5, y - 5, width + 10, height + 10);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("Shld: " + shieldHits, x, y - 5);
        } else if (isSuperMode) {
            // Golden aura to indicate super state
            g.setColor(new Color(255, 215, 0, 150)); 
            g.fillOval(x - 10, y - 10, width + 20, height + 20);
        }
    }
    
    /**
     * Returns a rectangle for collision detection with bullets/enemies.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public String getWeaponName() {
        switch (currentWeapon) {
            case SPREAD: return "SPREAD";
            case LASER:  return "LASER";
            case HOMING: return "HOMING";
            default:     return "SPREAD";
        }
    }
}