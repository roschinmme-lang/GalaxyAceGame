package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.Random;

/**
 * Boss3 (Fan Shooter)
 * -------------------
 * Heavy boss that moves within the top band and periodically emits a multi-
 * shot fan pattern using angle math. HP scales with wave multiplier.
 */
public class Boss3 {
    private int x, y;
    private int health;
    private final int width = 100, height = 100;
    private final int MAX_HEALTH = 200;
    private final Image image;
    private final Random random = new Random();
    private int shootCooldown = 0;
    private final int SHOOT_COOLDOWN = 60;
    private int phase = 1;
    
    // Movement variables (retained)
    private final int speedH = 3; 
    private final int speedV = 1; 
    private int directionH = 1; 
    private int directionV = 1; 
    private final int MIN_Y = 30;
    private final int MAX_Y = 100;
    
    // Fan pattern constants
    private final double ENEMY_BULLET_SPEED = 5.5;
    private int NUM_SHOTS = 5; // increases with phase

    /**
     * Constructs Boss3 with HP scaled by healthMultiplier.
     */
    public Boss3(int x, int y, double healthMultiplier) {
        this.x = x;
        this.y = y;
        // Apply the health multiplier for wave difficulty
        this.health = (int)(MAX_HEALTH * healthMultiplier);
        // NOTE: Create a Boss3.png image
        image = new ImageIcon("src/GalaxyAce/resources/Boss3.png").getImage();
    }

    /**
     * Patrols within the top band and decrements cooldown to gate firing.
     */
    public void update() {
        // Consistent movement (retained)
        x += speedH * directionH;
        y += speedV * directionV;
        
        // Boundary checks (retained)
        if (x < 0) {
            x = 0;
            directionH = 1;
        } else if (x > 500 - width) {
            x = 500 - width;
            directionH = -1;
        }
        
        if (y < MIN_Y) {
            y = MIN_Y;
            directionV = 1;
        } else if (y > MAX_Y) {
            y = MAX_Y;
            directionV = -1;
        }
        
        if (shootCooldown > 0) shootCooldown--;
        double hpRatio = (double) health / MAX_HEALTH;
        if (hpRatio <= 0.25 && phase < 3) phase = 3;
        else if (hpRatio <= 0.5 && phase < 2) phase = 2;
    }
    
    /**
     * Returns true when ready to emit the fan pattern and resets cooldown.
     */
    public boolean canShoot() {
        if (shootCooldown <= 0) {
            shootCooldown = phase == 3 ? 30 : phase == 2 ? 45 : SHOOT_COOLDOWN;
            return true;
        }
        return false;
    }

    public int getPhase() { return phase; }
    
    /**
     * Emits a symmetric fan of NUM_SHOTS bullets using angles from -30..+30.
     * Ensures dy is downward (positive) so bullets move toward the player.
     */
    public ArrayList<Bullet> shootMultiple() {
        NUM_SHOTS = phase == 3 ? 9 : phase == 2 ? 7 : 5;
        ArrayList<Bullet> shots = new ArrayList<>();
        int center_x = x + width / 2;
        int bottom_y = y + height;
        
        for (int i = 0; i < NUM_SHOTS; i++) {
            double angle = Math.toRadians(-30 + (60.0 / (NUM_SHOTS - 1)) * i);
            double dx = ENEMY_BULLET_SPEED * Math.sin(angle);
            double dy = ENEMY_BULLET_SPEED * Math.cos(angle);
            
            if (dy < 0) dy = -dy; 
            
            shots.add(new Bullet(center_x, bottom_y, dx, dy));
        }
        return shots;
    }

    /** Applies damage from player attacks. */
    public void takeDamage(int damage) {
        health -= damage;
    }

    /** True when HP is depleted. */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /** Collision rectangle for bullets and crash detection. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /** Draws the boss and its HP bar. */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
        Color barColor = phase == 3 ? new Color(220, 50, 50)
                       : phase == 2 ? new Color(255, 165, 0)
                       : new Color(50, 200, 80);
        g.setColor(barColor);
        g.fillRect(x, y - 10, (int)((double)health / MAX_HEALTH * width), 5);
        g.setColor(Color.BLACK);
        g.drawRect(x, y - 10, width, 5);
        if (phase > 1) {
            g.setColor(phase == 3 ? new Color(220, 50, 50) : new Color(255, 165, 0));
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString("PHASE " + phase, x, y - 12);
        }
    }
}