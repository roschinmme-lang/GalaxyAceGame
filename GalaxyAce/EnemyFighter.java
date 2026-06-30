package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * EnemyFighter
 * ------------
 * Agile enemy that patrols horizontally and vertically near the top of the
 * screen and fires in short bursts. Has HP and displays a small health bar.
 */
public class EnemyFighter {
    // Position and HP
    private int x, y;
    private int health = 30;

    // Visuals
    private final int width = 60, height = 70;
    private final Image image;
    
    // --- BURST FIRE VARIABLES ---
    private boolean isBursting = false;         // In the middle of a burst
    private int shotsFiredInBurst = 0;          // Shots fired during the current burst
    private final int MAX_BURST_SHOTS = 3;      // 3 shots per burst
    
    private int timer = 0;                      // Cooldown timer between bursts
    private final int COOLDOWN_TIME = 130;      // ~2s at 15ms per frame
    private final int BURST_DELAY = 12;         // ~0.2s between individual shots
    private int burstTimer = 0;                 // Timer for spacing shots within burst

    // Movement variables (bounce within a small top-band window)
    private final int speedH = 3; 
    private final int speedV = 1; 
    private int directionH = 1; 
    private int directionV = 1; 
    private final int MIN_Y = 30;
    private final int MAX_Y = 100;
    
    // Standard bullet speed downward
    private final double ENEMY_BULLET_SPEED = 5.5;

    /**
     * Spawns a fighter at x within the top band and staggers its initial
     * firing timer to avoid synchronized volleys.
     */
    public EnemyFighter(int x) {
        this.x = x;
        this.y = 50;
        image = new ImageIcon("src/GalaxyAce/resources/EnemyFighter.png").getImage();
        // Initialize timer so they don't all shoot instantly upon spawning
        timer = (int)(Math.random() * 50); 
    }

    /**
     * Updates patrol movement and burst-fire timers.
     */
    public void update() {
        // Movement Logic
        x += speedH * directionH;
        y += speedV * directionV;
        
        // Bounce off horizontal boundaries
        if (x < 0) { x = 0; directionH = 1; } 
        else if (x > 500 - width) { x = 500 - width; directionH = -1; }
        
        // Bounce within a vertical band
        if (y < MIN_Y) { y = MIN_Y; directionV = 1; } 
        else if (y > MAX_Y) { y = MAX_Y; directionV = -1; }
        
        // --- SHOOTING LOGIC ---
        if (!isBursting) {
            // Waiting for cooldown
            timer++;
            if (timer >= COOLDOWN_TIME) {
                isBursting = true;
                shotsFiredInBurst = 0;
                burstTimer = 0; // Ready to fire first shot immediately
                timer = 0;
            }
        } else {
            // Currently in a burst; tick down to next shot
            if (burstTimer > 0) {
                burstTimer--;
            }
        }
    }

    /**
     * Returns true when the fighter should fire a shot on this frame.
     */
    public boolean canShoot() {
        if (isBursting && burstTimer <= 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Emits one bullet downward and advances burst state; ends burst after
     * MAX_BURST_SHOTS and resets long cooldown.
     */
    public ArrayList<Bullet> shootBullet() {
        ArrayList<Bullet> shots = new ArrayList<>();
        int center_x = x + width / 2 - 2;
        int bottom_y = y + height;

        // Fire 1 straight bullet
        shots.add(new Bullet(center_x, bottom_y, 0, ENEMY_BULLET_SPEED));
        
        // Advance Burst State
        shotsFiredInBurst++;
        burstTimer = BURST_DELAY; // Reset delay for next shot
        
        // Check if burst is finished
        if (shotsFiredInBurst >= MAX_BURST_SHOTS) {
            isBursting = false; // End burst
            timer = 0; // Start long cooldown
        }

        return shots;
    }

    /** Damage intake for player bullets. */
    public void takeDamage(int damage) {
        health -= damage;
    }

    /** Returns true when HP is depleted. */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /** Collision bounds for player bullets and crash detection. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Renders the fighter and a small health bar when damaged.
     */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
        if (health < 30) {
            g.setColor(Color.YELLOW);
            g.fillRect(x, y - 5, (int)((double)health / 30 * width), 3);
            g.setColor(Color.BLACK);
            g.drawRect(x, y - 5, width, 3);
        }
    }
}