package GalaxyAce;

import java.awt.*;

/**
 * BossBullet
 * ----------
 * Projectile type commonly emitted by bosses (or advanced enemies).
 * Uses double precision for smoother diagonal movement and larger size
 * compared to standard player bullets.
 */
public class BossBullet {
    // Sub-pixel position for smooth trajectories
    private double x, y; 
    // Velocity components (can form radial or aimed patterns)
    private double dx, dy; 
    private final int width = 8, height = 15;

    /**
     * Creates a boss bullet at (x,y) traveling with velocity (dx,dy).
     */
    public BossBullet(int x, int y, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Advances the projectile by its velocity.
     */
    public void update() {
        x += dx;
        y += dy;
    }

    /**
     * Returns true if the bullet has left the visible area.
     * @param panelHeight runtime panel height (e.g., 700)
     */
    public boolean isOffScreen(int panelHeight) {
        // Check boundaries: below screen, off left, or off right (assuming panel width is 500)
        return y > panelHeight || x < -width || x > 500; 
    }

    /**
     * Collision rectangle for hits on player or partner planes.
     */
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    /**
     * Renders the boss bullet as a red rectangle.
     */
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)x, (int)y, width, height);
    }
}