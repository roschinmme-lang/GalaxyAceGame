package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.Random;

/**
 * Boss4 (Shielded Radial Shooter)
 * --------------------------------
 * High-HP boss with a radial bullet pattern (downward-only) and a chance to
 * randomly shield against incoming damage per hit attempt.
 */
public class Boss4 {
    private int x, y;
    private int health;
    private final int width = 100, height = 100;
    private final int MAX_HEALTH = 300;
    private final Image image;
    private final Random random = new Random();
    
    private boolean isShielded = false;         // Renders a temporary shield effect
    private final int SHIELD_CHANCE = 20;       // % chance to ignore a hit
    private final int SHOOT_COOLDOWN = 45;      // Frames between radial volleys
    private int shootTimer = SHOOT_COOLDOWN;
    
    // Movement variables (retained)
    private final int speedH = 3; 
    private final int speedV = 1; 
    private int directionH = 1; 
    private int directionV = 1; 
    private final int MIN_Y = 30;
    private final int MAX_Y = 100;
    
    // Bullet speed for the circular pattern
    private final double ENEMY_BULLET_SPEED = 5.5;

    /**
     * Constructs Boss4 with HP scaled by healthMultiplier.
     */
    public Boss4(int x, int y, double healthMultiplier) {
        this.x = x;
        this.y = y;
        // Apply the health multiplier for wave difficulty
        this.health = (int)(MAX_HEALTH * healthMultiplier);
        // NOTE: Create a Boss4.png image in your resources folder
        image = new ImageIcon("src/GalaxyAce/resources/Boss4.png").getImage();
    }

    /**
     * Patrol movement within the top band and countdown to the next volley.
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
        
        if (shootTimer > 0) {
            shootTimer--;
        }
    }
    
    /** True when ready to emit the radial pattern; resets cooldown. */
    public boolean canShoot() {
        if (shootTimer <= 0) {
            shootTimer = SHOOT_COOLDOWN;
            return true;
        }
        return false;
    }
    
    /**
     * Emits a circular pattern using 12 bullets spaced evenly by angle, but
     * mirrors any upward vectors to ensure bullets travel downward.
     */
    public ArrayList<Bullet> shootCircular() {
        ArrayList<Bullet> shots = new ArrayList<>();
        int center_x = x + width / 2;
        int center_y = y + height / 2;
        int num_bullets = 12;
        
        for (int i = 0; i < num_bullets; i++) {
            double angle = (2 * Math.PI / num_bullets) * i;
            // Use the quicker speed
            double dx = ENEMY_BULLET_SPEED * Math.sin(angle); 
            double dy = ENEMY_BULLET_SPEED * Math.cos(angle);
            
            // Ensure bullets only travel downwards (dy >= 0)
            if (dy < 0) dy = -dy; 
            
            shots.add(new Bullet(center_x, center_y, dx, dy));
        }
        return shots;
    }

    /**
     * Each attempted hit may be ignored based on SHIELD_CHANCE; draws a visual
     * shield indicator on the next frame when this occurs.
     */
    public void takeDamage(int damage) {
        if (random.nextInt(100) < SHIELD_CHANCE) {
             isShielded = true;
             return; 
        }
        isShielded = false; 
        health -= damage;
    }

    /** True when HP is depleted. */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /** Collision rectangle for hits. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * Draws the boss, a temporary shield effect when active, and a HP bar.
     */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
        
        // Draw shield effect if applicable
        if (isShielded) {
            g.setColor(new Color(255, 215, 0, 100)); 
            g.fillOval(x - 5, y - 5, width + 10, height + 10);
        }

        // Draw health bar
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int)((double)health / MAX_HEALTH * width), 5);
        g.setColor(Color.BLACK);
        g.drawRect(x, y - 10, width, 5);
    }
}