package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 * PartnerPlane
 * ------------
 * AI-controlled ally that follows the main player at a fixed offset,
 * contributes additional shots, can be damaged and temporarily destroyed,
 * and automatically respawns after a delay.
 */
public class PartnerPlane {
    // Current position (computed from player position + offset)
    private int x, y;
    private final int width = 40, height = 40;
    
    // Simple HP model and death/respawn handling
    private int health = 50;
    private final int MAX_HEALTH = 50;
    private boolean isDead = false;
    
    private int respawnTimer = 0;
    private final int RESPAWN_DELAY = 333; // ~5 seconds at 15ms/frame
    
    // Offset from the main player's position
    private int offsetX, offsetY;
    
    // Visuals: reuse player sprites to keep a cohesive look
    private final Image defaultImage;
    private final Image superImage;
    
    // Reference to the main player (updated on revival)
    private Player mainPlayer;

    /**
     * Creates a partner bound to the specified player, anchored by an offset
     * relative to the player's position.
     */
    public PartnerPlane(Player player, int offsetX, int offsetY) {
        this.mainPlayer = player;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        
        defaultImage = new ImageIcon("src/GalaxyAce/resources/Player.png").getImage();
        superImage = new ImageIcon("src/GalaxyAce/resources/SuperPlayer.png").getImage();
    }
    
    /**
     * Updates the player reference (used when the player is revived and a
     * new Player instance is created).
     */
    public void setPlayer(Player newPlayer) {
        this.mainPlayer = newPlayer;
    }

    /**
     * Handles respawn countdown when dead, otherwise tracks the main
     * player's position applying the configured offset.
     */
    public void update() {
        if (isDead) {
            respawnTimer--;
            if (respawnTimer <= 0) {
                isDead = false;
                health = MAX_HEALTH; 
            }
            return;
        }
        
        // Follow the player with offset
        Rectangle playerBounds = mainPlayer.getBounds();
        this.x = (int)playerBounds.getX() + offsetX;
        this.y = (int)playerBounds.getY() + offsetY;
    }
    
    /**
     * Fires a single straight shot when alive. Returns empty when dead.
     */
    public ArrayList<Bullet> shoot() {
        ArrayList<Bullet> shots = new ArrayList<>();
        if (isDead) return shots;

        shots.add(new Bullet(x + width / 2 - 2, y));
        return shots;
    }
    
    /**
     * Applies incoming damage. On death, starts a respawn timer.
     */
    public void takeDamage(int damage) {
        if (isDead) return;
        
        health -= damage;
        if (health <= 0) {
            isDead = true;
            respawnTimer = RESPAWN_DELAY;
        }
    }
    
    /** True when currently destroyed and awaiting respawn. */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Collision bounds; return an off-screen rectangle when dead so the
     * caller treats it as non-collidable.
     */
    public Rectangle getBounds() {
        if (isDead) return new Rectangle(-100, -100, 0, 0); 
        return new Rectangle(x, y, width, height);
    }

    /**
     * Renders the partner (matching the player's super appearance) and a
     * small health bar underneath while alive.
     */
    public void draw(Graphics g) {
        if (isDead) return; 

        Image currentImage = mainPlayer.isSuperMode() ? superImage : defaultImage;
        g.drawImage(currentImage, x, y, width, height, null);
        
        g.setColor(Color.GREEN);
        g.fillRect(x, y + height + 2, (int)((double)health / MAX_HEALTH * width), 3);
    }
}