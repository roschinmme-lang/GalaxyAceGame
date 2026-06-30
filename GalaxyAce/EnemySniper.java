package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * EnemySniper
 * -----------
 * Hangs back at the top of the screen and fires a slow but high-damage
 * aimed shot directly at the player's current position.
 */
public class EnemySniper {
    private int x, y;
    private int health = 40;
    private final int width = 40, height = 50;
    private final Image image;

    // Slow horizontal drift
    private int driftX = 1;
    private final int MIN_X = 0, MAX_X = 460;

    // Aiming cooldown — fires every ~3 seconds
    private int shootCooldown = 120;
    private final int SHOOT_COOLDOWN = 180;

    // Sniper bullet: slow but aimed and high damage
    private final double BULLET_SPEED = 3.5;

    public EnemySniper(int x) {
        this.x = x;
        this.y = 20;
        image = new ImageIcon("src/GalaxyAce/resources/EnemyFighter.png").getImage();
        shootCooldown = 60 + (int)(Math.random() * 60); // stagger initial shot
    }

    public void update() {
        x += driftX;
        if (x <= MIN_X) { x = MIN_X; driftX = 1; }
        else if (x >= MAX_X) { x = MAX_X; driftX = -1; }
        if (shootCooldown > 0) shootCooldown--;
    }

    /**
     * Returns true when ready to fire an aimed shot.
     */
    public boolean canShoot() {
        return shootCooldown <= 0;
    }

    /**
     * Fires a single bullet aimed at the player's current position.
     */
    public ArrayList<Bullet> shootAt(int targetX, int targetY) {
        ArrayList<Bullet> shots = new ArrayList<>();
        int cx = x + width / 2;
        int cy = y + height;

        double dx = targetX - cx;
        double dy = targetY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) dist = 1;

        double vx = (dx / dist) * BULLET_SPEED;
        double vy = (dy / dist) * BULLET_SPEED;
        if (vy < 0) vy = Math.abs(vy); // always move downward

        shots.add(new Bullet(cx, cy, vx, vy));
        shootCooldown = SHOOT_COOLDOWN;
        return shots;
    }

    public void takeDamage(int damage) { health -= damage; }
    public boolean isDestroyed() { return health <= 0; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public void draw(Graphics g) {
        // Tint the image with a red laser-sight dot effect
        g.drawImage(image, x, y, width, height, null);

        // Red scope dot
        g.setColor(new Color(255, 50, 50, 180));
        g.fillOval(x + width / 2 - 3, y + height - 4, 6, 6);

        // Health bar
        if (health < 40) {
            g.setColor(Color.RED);
            g.fillRect(x, y - 5, (int)((double)health / 40 * width), 3);
            g.setColor(Color.BLACK);
            g.drawRect(x, y - 5, width, 3);
        }
    }
}
