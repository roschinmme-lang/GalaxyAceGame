package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * Enemy
 * -----
 * Basic enemy unit that descends straight down at a constant speed,
 * can be destroyed by player bullets, and penalizes the player if it
 * slips past the bottom of the screen.
 */
public class Enemy {
    // Position and appearance
    private int x, y;
    private final int width = 25, height = 25, speed = 3;
    private final Image image;

    /**
     * Spawns an enemy at the given coordinates and loads its sprite.
     */
    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;

        image = new ImageIcon("src/GalaxyAce/resources/Enemy.png").getImage();
    }

    /**
     * Moves the enemy downward each tick.
     */
    public void update() {
        y += speed; // Moves down
    }

    /**
     * True when the enemy has exited the bottom of the screen.
     */
    public boolean isOffScreen() {
        return y > 700;
    }

    /**
     * Collision rectangle used for bullet hits and crash checks.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Renders the enemy sprite at its current position.
     */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }
}