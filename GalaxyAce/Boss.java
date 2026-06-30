package GalaxyAce;
import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.Random;
/**
 * Boss (Type 1)
 * -------------
 * Large enemy with bouncing patrol movement across the top band, a fixed HP
 * pool (scaled by wave multiplier), and a 5-bullet fan pattern attack.
 */
class Boss {
    // Position and stats
    private int x, y;
    private int health;
    private final int width = 100, height = 100;
    private final int MAX_HEALTH = 200; // Base HP before multipliers
    private final Image image;
    private int shootCooldown = 0;      // Frames until next shot
    private int phase = 1;              // PHASE 3: health phases (1, 2, 3)
    
    // Movement variables (bounce within a band)
    private final int speedH = 3; 
    private final int speedV = 1; 
    private int directionH = 1; 
    private int directionV = 1; 
    private final int MIN_Y = 30;
    private final int MAX_Y = 100;
    
    // Bullet fan pattern constants (faster variant)
    private final double ENEMY_BULLET_SPEED = 5.5;
    private final double DIAG_DX_INNER = 3.2; 
    private final double DIAG_DY_INNER = 3.8; 
    private final double DIAG_DX_OUTER = 4.5; 
    private final double DIAG_DY_OUTER = 2.5;

    /**
     * Constructs the boss at (x,y) with health scaled by healthMultiplier.
     */
    public Boss(int x, int y, double healthMultiplier) {
        this.x = x;
        this.y = y;
        // Apply the health multiplier for wave difficulty
        this.health = (int)(MAX_HEALTH * healthMultiplier); 
        image = new ImageIcon("src/GalaxyAce/resources/Boss.png").getImage();
    }
    
    /**
     * Bounce movement within the top band and decrement the shoot cooldown.
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

        // PHASE 3: Update health phase
        double hpRatio = (double) health / MAX_HEALTH;
        if (hpRatio <= 0.25 && phase < 3) { phase = 3; }
        else if (hpRatio <= 0.5 && phase < 2) { phase = 2; }
    }
    
    /**
     * Returns true when the boss is ready to emit its bullet pattern.
     */
    public boolean canShoot() {
        if (shootCooldown <= 0) {
            // Phase 2: fire faster, Phase 3: fire even faster
            shootCooldown = phase == 3 ? 45 : phase == 2 ? 65 : 90;
            return true;
        }
        return false;
    }

    public int getPhase() { return phase; }
    
    /**
     * Shoots a 5-bullet fan: straight + two inner diagonals + two outer
     * diagonals using tuned velocities.
     */
    public ArrayList<Bullet> shoot() {
        ArrayList<Bullet> shots = new ArrayList<>();
        int center_x = x + width / 2;
        int bottom_y = y + height;

        // 1. Straight Down (dy = 5.5)
        shots.add(new Bullet(center_x, bottom_y, 0, ENEMY_BULLET_SPEED));
        
        // 2. Inner Diagonal Left 
        shots.add(new Bullet(center_x, bottom_y, -DIAG_DX_INNER, DIAG_DY_INNER));
        // 3. Inner Diagonal Right 
        shots.add(new Bullet(center_x, bottom_y, DIAG_DX_INNER, DIAG_DY_INNER));
        
        // 4. Outer Diagonal Left 
        shots.add(new Bullet(center_x, bottom_y, -DIAG_DX_OUTER, DIAG_DY_OUTER));
        // 5. Outer Diagonal Right 
        shots.add(new Bullet(center_x, bottom_y, DIAG_DX_OUTER, DIAG_DY_OUTER));

        return shots;
    }
    
    /** Reduce HP by damage amount. */
    public void takeDamage(int damage) {
        health -= damage;
    }
    
    /** True when HP has reached zero. */
    public boolean isDestroyed() {
        return health <= 0;
    }
    
    /** Collision bounds for player bullets. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * Draws the sprite and a green HP bar above it.
     */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
        // Phase-colored HP bar
        Color barColor = phase == 3 ? new Color(220, 50, 50)
                       : phase == 2 ? new Color(255, 165, 0)
                       : new Color(50, 200, 80);
        g.setColor(barColor);
        g.fillRect(x, y - 10, (int)((double)health / MAX_HEALTH * width), 5);
        g.setColor(Color.BLACK);
        g.drawRect(x, y - 10, width, 5);
        // Phase label
        if (phase > 1) {
            g.setColor(phase == 3 ? new Color(220, 50, 50) : new Color(255, 165, 0));
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString("PHASE " + phase, x, y - 12);
        }
    }
}