package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon; // Required for loading images directly from the file system

/**
 * Bullet
 * ------
 * Represents a projectile in the game. Encodes both player bullets (dy < 0)
 * and enemy/boss bullets (dy > 0) using direction and size conventions.
 *
 * Responsibilities:
 * - Maintain position with sub-pixel accuracy (double x/y)
 * - Carry velocity (dx, dy) allowing straight or angled shots
 * - Update movement each tick and cull when off-screen
 * - Provide collision bounds sized by bullet type (player vs enemy)
 * - Render using custom sprite images for visual clarity
 */
public class Bullet {
    // Sub-pixel precise position
    private double x, y;

    // Velocity components: public for read access in collision logic
    public double dx, dy; 
    
    // Visual characteristics for player vs enemy bullets
    private final int PLAYER_WIDTH = 20, PLAYER_HEIGHT = 30;
    private final int ENEMY_WIDTH = 20, ENEMY_HEIGHT = 30; // Larger enemy bullet size
    private final int PLAYER_SPEED = 10; // Default upward speed for player bullets

    // 1. Declare static images so they are loaded into memory only once
    private static Image playerBulletImg;
    private static Image enemyBulletImg;

    // 2. Use a static block to load the images when the class is first initialized
    static {
        // Using ImageIcon reads directly from your computer's file system.
        // This bypasses strict classpath rules, making it easier to test right now.
        playerBulletImg = new ImageIcon("src/GalaxyAce/resources/player_bullet.png").getImage();
        enemyBulletImg = new ImageIcon("src/GalaxyAce/resources/enemy_bullet.png").getImage();
    }

    /**
     * Player bullet constructor: spawns a straight upward shot.
     */
    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
        this.dx = 0;
        this.dy = -PLAYER_SPEED; // Player bullets move up
    }
    
    /**
     * Generic bullet constructor supporting custom velocity (angled shots,
     * enemy/boss fire, etc.). Enemy bullets use dy > 0 to move downward.
     */
    public Bullet(int x, int y, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy; // Enemy bullets move down (dy > 0)
    }

    /**
     * Advances the bullet by its velocity.
     */
    public void update() {
        x += dx;
        y += dy;
    }

    /**
     * Returns true when the bullet has left the visible playfield.
     */
    public boolean isOffScreen() {
        return y < 0 || y > 700 || x < 0 || x > 500;
    }

    /**
     * Collision bounds: player bullets are smaller than enemy bullets.
     */
    public Rectangle getBounds() {
        // Use different sizes based on direction (dy < 0 is player bullet)
        int w = dy < 0 ? PLAYER_WIDTH : ENEMY_WIDTH;
        int h = dy < 0 ? PLAYER_HEIGHT : ENEMY_HEIGHT;
        return new Rectangle((int)x, (int)y, w, h);
    }

    /**
     * Renders the bullet using sprite images instead of colored rectangles.
     */
    public void draw(Graphics g) {
        if (dy < 0) { // Player Bullet (moving up)
            // Draw the player image, scaling it to your defined WIDTH and HEIGHT
            g.drawImage(playerBulletImg, (int)x, (int)y, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        } 
        else { // Enemy Bullet (moving down)
            // Draw the enemy image, scaling it to your defined WIDTH and HEIGHT
            g.drawImage(enemyBulletImg, (int)x, (int)y, ENEMY_WIDTH, ENEMY_HEIGHT, null);
        }
    }
}