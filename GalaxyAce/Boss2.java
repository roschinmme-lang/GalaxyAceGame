package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * Boss2 (Carrier)
 * ---------------
 * A tougher boss that patrols and periodically spawns EnemyFighter units
 * instead of firing bullet patterns itself.
 */
public class Boss2 {
    private int x, y;
    private int health;
    private final int width = 90, height = 90;
    private final int MAX_HEALTH = 150;
    private final Image image;
    private int spawnCooldown = 0; // Frames until next fighter spawn
    
    // Movement variables for faster, consistent movement
    private final int speedH = 3; 
    private final int speedV = 1; 
    private int directionH = 1; 
    private int directionV = 1; 
    private final int MIN_Y = 30;
    private final int MAX_Y = 100;

    /**
     * Constructs the boss carrier with HP scaled by healthMultiplier.
     */
    public Boss2(int x, int y, double healthMultiplier) {
        this.x = x;
        this.y = y;
        // Apply the health multiplier for wave difficulty
        this.health = (int)(MAX_HEALTH * healthMultiplier);
        // NOTE: Create a Boss2.png image
        image = new ImageIcon("src/GalaxyAce/resources/Boss2.png").getImage();
    }
    
    /**
     * Patrol movement within the top band and cooldown for spawning fighters.
     */
    public void update() {
        // Consistent left/right movement (QUICKER)
        x += speedH * directionH;
        // Consistent up/down movement (NEW)
        y += speedV * directionV;
        
        // Boundary check H
        if (x < 0) {
            x = 0;
            directionH = 1;
        } else if (x > 500 - width) {
            x = 500 - width;
            directionH = -1;
        }
        
        // Boundary check V
        if (y < MIN_Y) {
            y = MIN_Y;
            directionV = 1;
        } else if (y > MAX_Y) {
            y = MAX_Y;
            directionV = -1;
        }

        if (spawnCooldown > 0) spawnCooldown--;
    }
    
    /**
     * Returns true when ready to spawn an EnemyFighter and resets cooldown.
     */
    public boolean canSpawnFighters() {
        if (spawnCooldown <= 0) {
            spawnCooldown = 250; 
            return true;
        }
        return false;
    }
    
    /**
     * Creates a new EnemyFighter near the boss's horizontal center.
     */
    public EnemyFighter spawnFighter() {
        // Spawns fighters slightly to the side of the boss
        return new EnemyFighter(x + (width / 2) - 15);
    }
    
    /** Applies damage from player bullets. */
    public void takeDamage(int damage) {
        health -= damage;
    }
    
    /** True when HP is depleted. */
    public boolean isDestroyed() {
        return health <= 0;
    }
    
    /** Collision rectangle for player bullets and crashes. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    /** Draws the carrier and its HP bar. */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int)((double)health / MAX_HEALTH * width), 5);
        g.setColor(Color.BLACK);
        g.drawRect(x, y - 10, width, 5);
    }
}