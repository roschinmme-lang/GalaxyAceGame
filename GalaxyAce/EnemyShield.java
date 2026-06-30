package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * EnemyShield
 * -----------
 * Tanky enemy with a front-facing shield that blocks bullets from above.
 * Must be killed by flanking shots (angled bullets) or by depleting the
 * shield with repeated hits. Slowly descends toward the player.
 */
public class EnemyShield {
    private int x, y;
    private int health = 60;
    private int shieldHealth = 3; // Takes 3 hits to break the shield
    private boolean shieldActive = true;
    private final int width = 45, height = 55;
    private final Image image;

    private final int speed = 1;
    private int driftX = 1;

    // Shoots straight down occasionally
    private int shootCooldown = 100;
    private final int SHOOT_COOLDOWN = 150;
    private final double BULLET_SPEED = 4.0;

    // Shield flicker timer after hit
    private int shieldFlicker = 0;

    public EnemyShield(int x) {
        this.x = x;
        this.y = 0;
        image = new ImageIcon("src/GalaxyAce/resources/Enemy.png").getImage();
        shootCooldown = (int)(Math.random() * 80);
    }

    public void update() {
        y += speed;
        x += driftX;
        if (x <= 0) { x = 0; driftX = 1; }
        else if (x >= 455) { x = 455; driftX = -1; }
        if (shootCooldown > 0) shootCooldown--;
        if (shieldFlicker > 0) shieldFlicker--;
    }

    /**
     * Attempts to apply damage. Straight-down bullets (dx == 0) are blocked
     * by the active shield. Angled bullets bypass it.
     */
    public boolean takeDamage(int damage, double bulletDx) {
        if (shieldActive && Math.abs(bulletDx) < 1.0) {
            // Straight shot — shield absorbs it
            shieldHealth--;
            shieldFlicker = 8;
            if (shieldHealth <= 0) {
                shieldActive = false;
                showShieldBreak = true;
            }
            return false; // blocked
        }
        health -= damage;
        return true; // hit landed
    }

    private boolean showShieldBreak = false;
    public boolean didShieldJustBreak() {
        boolean v = showShieldBreak;
        showShieldBreak = false;
        return v;
    }

    public boolean canShoot() { return shootCooldown <= 0; }

    public ArrayList<Bullet> shoot() {
        ArrayList<Bullet> shots = new ArrayList<>();
        shots.add(new Bullet(x + width / 2, y + height, 0, BULLET_SPEED));
        shootCooldown = SHOOT_COOLDOWN;
        return shots;
    }

    public boolean isOffScreen() { return y > 700; }
    public boolean isDestroyed() { return health <= 0; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);

        // Draw shield arc on top
        if (shieldActive) {
            int alpha = shieldFlicker > 0 ? 80 : 160;
            g.setColor(new Color(80, 160, 255, alpha));
            g.fillArc(x - 5, y - 10, width + 10, 30, 0, 180);
            g.setColor(new Color(140, 200, 255, 200));
            g.drawArc(x - 5, y - 10, width + 10, 30, 0, 180);

            // Shield health pips
            for (int i = 0; i < shieldHealth; i++) {
                g.setColor(new Color(80, 200, 255));
                g.fillRect(x + 5 + i * 12, y - 16, 8, 4);
            }
        }

        // Health bar
        g.setColor(new Color(60, 180, 255));
        g.fillRect(x, y - 22, (int)((double)health / 60 * width), 4);
        g.setColor(Color.BLACK);
        g.drawRect(x, y - 22, width, 4);
    }
}
