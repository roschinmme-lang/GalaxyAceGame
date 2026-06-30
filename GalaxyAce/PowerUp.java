package GalaxyAce;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * PowerUp
 * -------
 * Falling collectible that grants an effect when picked up by the player:
 * - HEALTH: restores a fixed amount of HP
 * - SHIELD: grants temporary hit absorption
 * - SUPER: enables super mode (enhanced offense + shield timer managed elsewhere)
 */
public class PowerUp {
    
    // Enum of supported power-up types
    public enum Type {
        HEALTH, SHIELD, SUPER
    }

    // Position and visuals
    private int x, y;
    private final int width = 50, height = 50;
    private final int speed = 4; // Floating down quickly
    private final Type type;
    private final Image image;

    /**
     * Spawns a power-up at the given X at the top of the screen.
     */
    public PowerUp(int x, Type type) {
        this.x = x;
        this.y = 0;
        this.type = type;
        
        String imagePath;
        if (type == Type.HEALTH) {
            imagePath = "src/GalaxyAce/resources/HealthPack.png";
        } else if (type == Type.SHIELD) {
            imagePath = "src/GalaxyAce/resources/ShieldPack.png";
        } else { // SUPER
            // NOTE: SuperPowerUp.png should exist in resources
            imagePath = "src/GalaxyAce/resources/SuperPowerUp.png"; 
        }
        image = new ImageIcon(imagePath).getImage();
    }

    /**
     * Falls downward each tick.
     */
    public void update() {
        y += speed;
    }

    /**
     * Returns true when it exits the bottom of the screen.
     */
    public boolean isOffScreen() {
        return y > 700;
    }

    /** Returns the type so the caller can apply the correct effect. */
    public Type getType() {
        return type;
    }

    /** Collision rectangle for pickup detection. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /** Draws the appropriate sprite for the power-up. */
    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }
}