package GalaxyAce;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import GalaxyAce.PowerUp.Type; 

public class GalaxyAceMechanics extends JPanel implements ActionListener, KeyListener {
    
    // FEATURE: GOD MODE - Debug toggle for testing without taking damage
    private final boolean GOD_MODE = false; 
    
    
    private int health = 100;
    private int maxPlayerHealth = 100; 
    
    // FEATURE: GAME STATES - Track game end conditions
    private boolean gameOver = false;
    private boolean victoryState = false;  // Victory triggered after Penta phase completion
    
    private Timer timer;
    private Player player;
    private Image background;
    
    // Flags for Deferred Cleanup (Necessary for stability with complex waves!)
    private boolean pendingWaveCleanup = false;    // Clears player bullets after wave
    private boolean pendingRevivalCleanup = false; // Clears enemy bullets after revival
    private boolean pendingEnemyCleanup = false;   // Clears enemies when boss spawns
    private boolean pendingFighterCleanup = false; // Clears enemy fighters after wave
    
    private ArrayList<Bullet> bullets; 
    private ArrayList<Enemy> enemies; 
    private ArrayList<EnemyFighter> enemyFighters; 
    private ArrayList<PowerUp> powerUps; 
    private ArrayList<Object> activeBosses; // Mixed boss types (Boss1-4)
    private ArrayList<PartnerPlane> partnerPlanes; // FEATURE: Allied support units
    private ArrayList<EnemySniper> enemySnipers;   // PHASE 2: Sniper enemies
    private ArrayList<EnemyShield> enemyShields;   // PHASE 2: Shield enemies

    // PHASE 2: COMBO MULTIPLIER
    private int comboCount = 0;
    private int comboTimer = 0;
    private final int COMBO_TIMEOUT = 90;  // frames before combo resets
    private double comboMultiplier = 1.0;

    // PHASE 2: MINI-BOSS FLAG
    private boolean miniBossSpawnedThisWave = false;

    
    private Random random;
    private int score;
    private int bgY1 = 0;

    // PHASE 1: SCREEN SHAKE
    private int shakeTimer = 0;
    private int shakeMagnitude = 0;
    // PHASE 1: INVINCIBILITY FRAMES
    private int invincibilityTimer = 0;
    private final int INVINCIBILITY_DURATION = 60;
    // PHASE 1: KILL & ACCURACY TRACKING
    private int totalEnemiesKilled = 0;
    private int totalShotsFired = 0;
    private int totalShotsHit = 0;
    // PHASE 1: PARTICLES
    private ArrayList<int[]> particles = new ArrayList<>();

    // PHASE 2: COMBO MULTIPLIER

    // PHASE 2: MINI-BOSS
    private boolean miniBossActive = false;
    private Object miniBoss = null; // reuses Boss type with lower HP

    // PHASE 3: UPGRADE STATS
    private int totalDamageUpgrades   = 0;
    private int totalFireRateUpgrades = 0;
    private int bulletDamage = 10; // base damage per bullet
    private int shootCooldownBase = 10; // base shoot cooldown

    // PHASE 3: WEAPON SWITCHING
    // Weapon is stored in Player — Q key cycles it
    private boolean qPressed = false;

    // FEATURE: KEYBOARD INPUT BUFFERING - Tracks pressed keys each frame
    private boolean upPressed, downPressed, leftPressed, rightPressed, shootPressed;
    private int shootCooldown = 0;
    
    private JButton retryButton;
    private JButton returnToMenuButton; // Return to Menu Button
    private JButton leaderboardButton; 

    private JPanel retryButtonPanel;   
    private JPanel menuButtonPanel;    

    // FEATURE: LEADERBOARD INTEGRATION - Persists player stats to file
    private LeaderboardManager leaderboardManager;
    private PlayerProfile currentPlayerProfile;
    // --------------------------------------

    // FEATURE: NOTIFICATION SYSTEM - Temporary on-screen messages
    private String notificationMessage = "";
    private int notificationTimer = 0;
    private final int NOTIFICATION_DURATION = 100; 
    private Color notificationColor = Color.BLUE; 
    // -------------------------------------

    // FEATURE: PROGRESSIVE DIFFICULTY - Tracks enemy kills for wave progression
    private int enemiesDestroyed = 0;
    private final int ENEMIES_FOR_BOSS = 20; 
    
    // FEATURE: WAVE SYSTEM - Boss kill count determines game progression
    private int bossKillCount = 0; // Tracks waves cleared
    private final int ENEMY_FIGHTER_CHANCE = 30; 
    private final int MAX_ENEMY_FIGHTERS = 3; 
    
    // FEATURE: BOSS TYPE CYCLING - Rotates through 4 boss variants
    private int currentBossLevel = 1; 
    private final int MAX_BOSS_LEVEL = 4;
    
    // FEATURE: POWER-UP SPAWNING - Triggers every 10 enemy kills
    private int lastKillCountForPowerUp = 0;
    private final int KILLS_PER_POWERUP = 10;
    private final int HEAL_AMOUNT = 30; 
    
    // FEATURE: SUPER MODE POWER-UP - Temporary enhanced state
    private final int SUPER_MODE_SCORE_TRIGGER = 1500; 
    private int lastScoreForSuperMode = 0;
    private int superModeTimer = 0; 
    private final int SUPER_MODE_DURATION_TICKS = 15 * 60; 
    
    // Multiplier for the final, tougher Penta Boss Phase
    private final double PENTA_HEALTH_MULTIPLIER = 3.0; 

    // MODIFIED CONSTRUCTOR: Accepts profile and manager
    public GalaxyAceMechanics(PlayerProfile profile, LeaderboardManager manager) {
        this.currentPlayerProfile = profile;
        this.leaderboardManager = manager;
        
        setLayout(new BorderLayout()); 
        
        background = new ImageIcon("src/GalaxyAce/resources/background.jpg").getImage();
        setDoubleBuffered(true);
        
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyFighters = new ArrayList<>(); 
        powerUps = new ArrayList<>(); 
        activeBosses = new ArrayList<>(); 
        partnerPlanes = new ArrayList<>();
        enemySnipers = new ArrayList<>();
        enemyShields = new ArrayList<>();
        
        random = new Random();
        
        retryButton = makeEndButton("RETRY",        new Color(60,  40, 160));
        returnToMenuButton = makeEndButton("MAIN MENU",   new Color(30,  20,  80));
        leaderboardButton  = makeEndButton("LEADERBOARDS",new Color(40,  60, 140));

        retryButton.addActionListener(e -> restartGame());
        returnToMenuButton.addActionListener(e -> returnToMenu());
        leaderboardButton.addActionListener(e -> showLeaderboards());

        retryButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        retryButtonPanel.setOpaque(false);
        retryButtonPanel.setBorder(BorderFactory.createEmptyBorder(430, 0, 0, 0));

        menuButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        menuButtonPanel.setOpaque(false);
        menuButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        resetGameElements();

        timer = new Timer(15, this); 
        timer.start();
        
        this.setFocusable(true);
        this.addKeyListener(this);
    }
    
    // MODIFIED START GAME METHOD: Now requires profile and manager
    public static void startGame(PlayerProfile profile, LeaderboardManager manager) {
        JFrame frame = new JFrame("Galaxy Ace pilot 2.0 - Profile: " + profile.getProfileName());
        GalaxyAceMechanics gamePanel = new GalaxyAceMechanics(profile, manager);
        
        frame.add(gamePanel);
        frame.setSize(500, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); 
        
        frame.setVisible(true);
        gamePanel.requestFocusInWindow(); 
    }     
    private void resetGameElements() {
        health = 100;
        maxPlayerHealth = 100; 
        score = 0;
        gameOver = false;
        victoryState = false;
        
        player = new Player(250, 500); 
        
        bullets.clear(); 
        enemies.clear();
        enemyFighters.clear(); 
        powerUps.clear(); 
        activeBosses.clear(); 
        partnerPlanes.clear();
        enemySnipers.clear();
        enemyShields.clear();
        
        enemiesDestroyed = 0;
        bossKillCount = 0; 
        currentBossLevel = 1; 
        lastKillCountForPowerUp = 0; 
        
        lastScoreForSuperMode = 0;
        superModeTimer = 0;
        
        notificationMessage = "";
        notificationTimer = 0;

        // Phase 1 resets
        enemySnipers = new ArrayList<>();
        enemyShields = new ArrayList<>();
        comboCount = 0; comboTimer = 0; comboMultiplier = 1.0;
        miniBossSpawnedThisWave = false;
        totalDamageUpgrades = 0;
        totalFireRateUpgrades = 0;
        bulletDamage = 10;
        shootCooldownBase = 10;
        shakeTimer = 0; shakeMagnitude = 0;
        invincibilityTimer = 0;
        totalEnemiesKilled = 0; totalShotsFired = 0; totalShotsHit = 0;
        particles.clear();

        // Phase 2 resets

        
        pendingWaveCleanup = false;
        pendingRevivalCleanup = false; 
        pendingEnemyCleanup = false; 
        pendingFighterCleanup = false; 
        
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        shootPressed = false;
        shootCooldown = 0;
        
        if (retryButtonPanel != null) {
            remove(retryButtonPanel);
        }
        if (menuButtonPanel != null) {
            remove(menuButtonPanel);
        }
        revalidate();
        repaint();
    }
    
    private void restartGame() {
        // NOTE: A true restart should probably load the profile again if you want to reset the in-memory profile state,
        // but for simplicity, we just reset the game state.
        resetGameElements();
        timer.start();
        this.requestFocusInWindow(); 
    }
    
    
    private void returnToMenu() { // Return to Main Menu
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            parentFrame.dispose(); 
        }
        new MainMenu(); 
    }

    private void showLeaderboards() { // Show Leaderboard Dialog
    new LeaderboardTableDialog((JFrame) SwingUtilities.getWindowAncestor(this), leaderboardManager).setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (shakeTimer > 0) {
            int sx = (int)((Math.random() - 0.5) * shakeMagnitude * 2);
            int sy = (int)((Math.random() - 0.5) * shakeMagnitude * 2);
            g2.translate(sx, sy);
        }
        g.drawImage(background, 0, bgY1,       500, 700, null);
        g.drawImage(background, 0, bgY1 - 700, 500, 700, null);
        
        player.draw(g);
        
        for (PartnerPlane pp : partnerPlanes) {
            pp.draw(g);
        }
        
        for (Bullet b : bullets) b.draw(g); 
        for (Enemy e : enemies) e.draw(g);
        for (EnemyFighter ef : enemyFighters) ef.draw(g);


        for (Object boss : activeBosses) {
            if (boss instanceof Boss) ((Boss)boss).draw(g);
            else if (boss instanceof Boss2) ((Boss2)boss).draw(g);
            else if (boss instanceof Boss3) ((Boss3)boss).draw(g);
            else if (boss instanceof Boss4) ((Boss4)boss).draw(g);
        }
        
        for (EnemySniper sn : enemySnipers) sn.draw(g);
        for (EnemyShield se : enemyShields) se.draw(g);
        for (PowerUp pu : powerUps) pu.draw(g); 

        // ── HUD PANEL (top-left dark pill) ──────────────────────────────
        int hudW = 185, hudH = 115, hudX = 8, hudY = 8;
        g2.setColor(new Color(5, 0, 20, 190));
        g2.fillRoundRect(hudX, hudY, hudW, hudH, 14, 14);
        g2.setColor(new Color(140, 100, 255, 80));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(hudX, hudY, hudW, hudH, 14, 14);

        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(new Color(160, 130, 255));
        g2.drawString("SCORE", hudX + 10, hudY + 18);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(new Color(220, 200, 255));
        g2.drawString(String.valueOf(score), hudX + 10, hudY + 36);

        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(160, 130, 255));
        g2.drawString("HULL  " + health + " / " + maxPlayerHealth, hudX + 10, hudY + 55);
        int barW = hudW - 20;
        float hpRatio = Math.max(0, (float) health / maxPlayerHealth);
        g2.setColor(new Color(40, 20, 70));
        g2.fillRoundRect(hudX + 10, hudY + 58, barW, 8, 6, 6);
        Color hpColor = hpRatio > 0.5f ? new Color(80, 220, 120)
                      : hpRatio > 0.25f ? new Color(255, 200, 50) : new Color(220, 60, 60);
        g2.setColor(hpColor);
        g2.fillRoundRect(hudX + 10, hudY + 58, (int)(barW * hpRatio), 8, 6, 6);

        String era = "Solo";
        if (bossKillCount >= 4 && bossKillCount < 8) era = "Duo";
        else if (bossKillCount >= 8 && bossKillCount < 12) era = "Trio";
        else if (bossKillCount == 12) era = "PENTA FINALE";
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(160, 130, 255));
        g2.drawString("WAVE  " + (bossKillCount + 1) + "  |  " + era, hudX + 10, hudY + 82);
        g2.setColor(new Color(120, 110, 160));
        g2.drawString("ALLIES: " + partnerPlanes.size(), hudX + 10, hudY + 98);

        // Weapon indicator
        String wname = player.getWeaponName();
        Color wColor = wname.equals("LASER")  ? new Color(255, 80, 80)
                     : wname.equals("HOMING") ? new Color(80, 220, 120)
                     : new Color(160, 130, 255);
        g2.setColor(wColor);
        g2.drawString("[Q] " + wname, hudX + 10, hudY + 112);
        if (miniBossActive) {
            g2.setColor(new Color(255, 160, 0));
            g2.drawString("MINI-BOSS ACTIVE", hudX + 10, hudY + 112);
        }

        if (GOD_MODE) {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(new Color(5, 0, 20, 180));
            g2.fillRoundRect(hudX, hudY + hudH + 4, 110, 20, 8, 8);
            g2.setColor(new Color(80, 255, 120));
            g2.drawString("  GOD MODE", hudX + 2, hudY + hudH + 18);
        }

        // Super mode badge top-right
        if (player.isSuperMode()) {
            int rem = superModeTimer / 60;
            String superText = "SUPER  " + rem + "s";
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics sfm = g2.getFontMetrics();
            int sw = sfm.stringWidth(superText) + 20;
            g2.setColor(new Color(5, 0, 20, 190));
            g2.fillRoundRect(500 - sw - 8, 8, sw, 26, 10, 10);
            g2.setColor(new Color(255, 210, 60));
            g2.drawString(superText, 500 - sw, 26);
        }

        // Notification
        if (notificationTimer > 0) {
            float alpha = Math.min(1.0f, (float) notificationTimer / NOTIFICATION_DURATION);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics nfm = g2.getFontMetrics();
            int nw = nfm.stringWidth(notificationMessage);
            int nx = (getWidth() - nw) / 2;
            g2.setColor(new Color(5, 0, 20, (int)(alpha * 200)));
            g2.fillRoundRect(nx - 12, 140, nw + 24, 30, 12, 12);
            g2.setColor(new Color(notificationColor.getRed(), notificationColor.getGreen(),
                                   notificationColor.getBlue(), (int)(alpha * 255)));
            g2.drawString(notificationMessage, nx, 161);
        }

        // ── GAME OVER ────────────────────────────────────────────────────────
        if (gameOver) {
            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cx = getWidth() / 2;
            int cardW = 400, cardH = 260;
            int cardX = cx - cardW / 2, cardY = 160;

            // Outer glow
            g2.setColor(new Color(180, 30, 30, 40));
            g2.fillRoundRect(cardX - 8, cardY - 8, cardW + 16, cardH + 16, 28, 28);

            // Card body
            g2.setColor(new Color(18, 4, 35, 245));
            g2.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);

            // Red top accent bar
            g2.setColor(new Color(200, 40, 40));
            g2.fillRoundRect(cardX, cardY, cardW, 5, 4, 4);

            // Border
            g2.setColor(new Color(180, 50, 50, 160));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);
            g2.setStroke(new BasicStroke(1f));

            // "MISSION FAILED" eyebrow
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics efm = g2.getFontMetrics();
            String eyebrow = "MISSION FAILED";
            g2.setColor(new Color(180, 60, 60, 200));
            g2.drawString(eyebrow, cx - efm.stringWidth(eyebrow) / 2, cardY + 26);

            // Title
            g2.setFont(new Font("Arial", Font.BOLD, 44));
            FontMetrics gfm = g2.getFontMetrics();
            String goText = "GAME OVER";
            // Shadow
            g2.setColor(new Color(120, 20, 20, 120));
            g2.drawString(goText, cx - gfm.stringWidth(goText) / 2 + 2, cardY + 72);
            g2.setColor(new Color(230, 55, 55));
            g2.drawString(goText, cx - gfm.stringWidth(goText) / 2, cardY + 70);

            // Divider
            g2.setColor(new Color(80, 20, 20, 160));
            g2.fillRect(cardX + 30, cardY + 82, cardW - 60, 1);

            // Score label + value
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            efm = g2.getFontMetrics();
            String scoreLbl = "FINAL SCORE";
            g2.setColor(new Color(160, 130, 255, 180));
            g2.drawString(scoreLbl, cx - efm.stringWidth(scoreLbl) / 2, cardY + 105);

            g2.setFont(new Font("Arial", Font.BOLD, 32));
            gfm = g2.getFontMetrics();
            String scoreVal = String.format("%,d", score);
            g2.setColor(new Color(255, 210, 60));
            g2.drawString(scoreVal, cx - gfm.stringWidth(scoreVal) / 2, cardY + 140);

            // Stat boxes
            int accuracy = totalShotsFired > 0 ? (totalShotsHit * 100 / totalShotsFired) : 0;
            String[] statLabels = {"KILLS", "ACCURACY", "WAVES"};
            String[] statVals   = {String.valueOf(totalEnemiesKilled), accuracy + "%", String.valueOf(bossKillCount)};
            int boxW = 100, boxH = 48, boxY = cardY + 158, gap = 10;
            int totalBoxW = boxW * 3 + gap * 2;
            int startX = cx - totalBoxW / 2;

            for (int i = 0; i < 3; i++) {
                int bx = startX + i * (boxW + gap);
                g2.setColor(new Color(30, 10, 55, 200));
                g2.fillRoundRect(bx, boxY, boxW, boxH, 10, 10);
                g2.setColor(new Color(100, 70, 160, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(bx, boxY, boxW, boxH, 10, 10);

                g2.setFont(new Font("Arial", Font.BOLD, 9));
                efm = g2.getFontMetrics();
                g2.setColor(new Color(140, 110, 200));
                g2.drawString(statLabels[i], bx + boxW/2 - efm.stringWidth(statLabels[i])/2, boxY + 16);

                g2.setFont(new Font("Arial", Font.BOLD, 18));
                gfm = g2.getFontMetrics();
                g2.setColor(new Color(220, 200, 255));
                g2.drawString(statVals[i], bx + boxW/2 - gfm.stringWidth(statVals[i])/2, boxY + 36);
            }
        }

        // ── VICTORY ──────────────────────────────────────────────────────────
        if (victoryState) {
            g2.setColor(new Color(0, 0, 10, 210));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cx = getWidth() / 2;
            int cardW = 400, cardH = 270;
            int cardX = cx - cardW / 2, cardY = 155;

            // Outer glow — purple
            g2.setColor(new Color(100, 40, 200, 50));
            g2.fillRoundRect(cardX - 10, cardY - 10, cardW + 20, cardH + 20, 30, 30);
            // Blue outer ring
            g2.setColor(new Color(50, 100, 200, 40));
            g2.fillRoundRect(cardX - 6, cardY - 6, cardW + 12, cardH + 12, 26, 26);

            // Card body
            g2.setColor(new Color(12, 4, 42, 248));
            g2.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);

            // Purple top accent bar
            GradientPaint topBar = new GradientPaint(cardX, cardY, new Color(120, 60, 255),
                                                      cardX + cardW, cardY, new Color(60, 120, 255));
            g2.setPaint(topBar);
            g2.fillRoundRect(cardX, cardY, cardW, 5, 4, 4);
            g2.setPaint(null);

            // Border — double ring
            g2.setColor(new Color(130, 80, 255, 150));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);
            g2.setColor(new Color(60, 120, 255, 70));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(cardX - 3, cardY - 3, cardW + 6, cardH + 6, 23, 23);
            g2.setStroke(new BasicStroke(1f));

            // Corner star dots
            int[] sx = {cardX + 18, cardX + cardW - 18, cardX + 18, cardX + cardW - 18};
            int[] sy = {cardY + 18, cardY + 18, cardY + cardH - 18, cardY + cardH - 18};
            for (int i = 0; i < sx.length; i++) {
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillOval(sx[i] - 3, sy[i] - 3, 6, 6);
                g2.setColor(new Color(180, 130, 255, 60));
                g2.fillOval(sx[i] - 6, sy[i] - 6, 12, 12);
            }

            // Eyebrow
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics efm = g2.getFontMetrics();
            String eyebrow = "MISSION ACCOMPLISHED";
            g2.setColor(new Color(120, 180, 255, 200));
            g2.drawString(eyebrow, cx - efm.stringWidth(eyebrow) / 2, cardY + 26);

            // Title
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics vfm = g2.getFontMetrics();
            String vicText = "VICTORY!";
            // Shadow
            g2.setColor(new Color(80, 40, 180, 130));
            g2.drawString(vicText, cx - vfm.stringWidth(vicText) / 2 + 2, cardY + 74);
            // Gradient text simulation — draw twice with different colors
            g2.setColor(new Color(200, 160, 255));
            g2.drawString(vicText, cx - vfm.stringWidth(vicText) / 2, cardY + 72);

            // Subtitle
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            efm = g2.getFontMetrics();
            String sub = "Galaxy Ace Defense Successful";
            g2.setColor(new Color(140, 180, 255, 200));
            g2.drawString(sub, cx - efm.stringWidth(sub) / 2, cardY + 96);

            // Divider
            GradientPaint divGrad = new GradientPaint(cardX + 30, 0, new Color(0,0,0,0),
                                                       cx, 0, new Color(120,80,255,120));
            g2.setPaint(divGrad); g2.fillRect(cardX + 30, cardY + 106, cardW/2 - 30, 1);
            GradientPaint divGrad2 = new GradientPaint(cx, 0, new Color(120,80,255,120),
                                                        cardX+cardW-30, 0, new Color(0,0,0,0));
            g2.setPaint(divGrad2); g2.fillRect(cx, cardY + 106, cardW/2 - 30, 1);
            g2.setPaint(null);

            // Score label + value
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            efm = g2.getFontMetrics();
            String scoreLbl = "FINAL SCORE";
            g2.setColor(new Color(160, 130, 255, 180));
            g2.drawString(scoreLbl, cx - efm.stringWidth(scoreLbl) / 2, cardY + 128);

            g2.setFont(new Font("Arial", Font.BOLD, 32));
            vfm = g2.getFontMetrics();
            String scoreVal = String.format("%,d", score);
            g2.setColor(new Color(255, 210, 60));
            g2.drawString(scoreVal, cx - vfm.stringWidth(scoreVal) / 2, cardY + 162);

            // Stat boxes
            int accuracy = totalShotsFired > 0 ? (totalShotsHit * 100 / totalShotsFired) : 0;
            String[] statLabels = {"KILLS", "ACCURACY", "WAVES"};
            String[] statVals   = {String.valueOf(totalEnemiesKilled), accuracy + "%", String.valueOf(bossKillCount)};
            int boxW2 = 100, boxH2 = 50, boxY2 = cardY + 178, gap2 = 10;
            int totalBoxW2 = boxW2 * 3 + gap2 * 2;
            int startX2 = cx - totalBoxW2 / 2;

            for (int i = 0; i < 3; i++) {
                int bx = startX2 + i * (boxW2 + gap2);
                g2.setColor(new Color(20, 8, 55, 200));
                g2.fillRoundRect(bx, boxY2, boxW2, boxH2, 10, 10);
                g2.setColor(new Color(90, 60, 180, 120));
                g2.drawRoundRect(bx, boxY2, boxW2, boxH2, 10, 10);

                g2.setFont(new Font("Arial", Font.BOLD, 9));
                efm = g2.getFontMetrics();
                g2.setColor(new Color(130, 100, 210));
                g2.drawString(statLabels[i], bx + boxW2/2 - efm.stringWidth(statLabels[i])/2, boxY2 + 17);

                g2.setFont(new Font("Arial", Font.BOLD, 20));
                vfm = g2.getFontMetrics();
                g2.setColor(new Color(220, 200, 255));
                g2.drawString(statVals[i], bx + boxW2/2 - vfm.stringWidth(statVals[i])/2, boxY2 + 38);
            }
        }
    }

    private void registerKill(int x, int y, int scoreGain, int pr, int pg, int pb) {
        score += scoreGain;
        totalEnemiesKilled++;
        totalShotsHit++;
        comboCount++;
        comboTimer = COMBO_TIMEOUT;
        int bonus = (comboCount > 1) ? (comboCount - 1) * 5 : 0;
        score += bonus;
        spawnParticles(x, y, 8 + comboCount, pr, pg, pb);
        if (comboCount == 3) showNotification("3x COMBO!", new Color(255, 200, 50));
        else if (comboCount == 5) showNotification("5x COMBO!", new Color(255, 150, 0));
        else if (comboCount >= 10) showNotification(comboCount + "x COMBO!", new Color(255, 80, 0));
    }

    private void spawnMiniBoss() {
        if (miniBossActive) return;
        miniBossActive = true;
        double mult = 0.5 + (bossKillCount * 0.1);
        miniBoss = new Boss(random.nextInt(300) + 50, 50, mult);
        showNotification("MINI-BOSS!", new Color(255, 160, 0));
        triggerShake(5, 12);
    }
    
    private void checkProgressionRewards() { // Rewards based on boss kills
        if (bossKillCount == 4) {
            maxPlayerHealth += 100;
            health = maxPlayerHealth; 
            partnerPlanes.add(new PartnerPlane(player, -50, 20));
            partnerPlanes.add(new PartnerPlane(player, 50, 20));
            showNotification("Reinforcements Arrived! (+Max HP)", Color.BLUE);
        }
        else if (bossKillCount == 8) { // Second reinforcement wave
            partnerPlanes.add(new PartnerPlane(player, -100, 40));
            partnerPlanes.add(new PartnerPlane(player, 100, 40));
            showNotification("Reinforcements Arrived!", Color.BLUE);
        }
        else if (bossKillCount == 12) { 
             // This is the trigger for the final PENTA Phase
             partnerPlanes.add(new PartnerPlane(player, -150, 60));
             partnerPlanes.add(new PartnerPlane(player, 150, 60));
             showNotification("Full Squadron Assembled! (Final Wave)", Color.MAGENTA);
        }
    }
    
    private void spawnBossPhase() { // Spawns bosses based on current wave
        int bossesToSpawn = 1;
        
        // Penta Boss Phase (bossKillCount == 12 is the 13th wave)
        if (bossKillCount == 12) { 
            bossesToSpawn = 5;
        } 
        // Duo Boss Phase
        else if (bossKillCount >= 4 && bossKillCount < 8) {
            bossesToSpawn = 2;
        } 
        // Trio Boss Phase
        else if (bossKillCount >= 8 && bossKillCount < 12) {
            bossesToSpawn = 3;
        }
        
        // Calculate horizontal positions
        int[] xPositions = new int[bossesToSpawn];
        int startX = 50;
        int gap = (400) / (bossesToSpawn > 1 ? bossesToSpawn - 1 : 1); 
        
        if (bossesToSpawn == 1) {
            xPositions[0] = 200;
        } else {
            for (int i = 0; i < bossesToSpawn; i++) {
                xPositions[i] = startX + (i * gap);
            }
        }
        
        // Add the first boss of the current BossLevel type
        addBossByType(currentBossLevel, xPositions[0]);
        
        // Add the rest of the bosses (random types)
        for (int i = 1; i < bossesToSpawn; i++) {
            int randomBossType = 1 + random.nextInt(4);
            addBossByType(randomBossType, xPositions[i]);
        }
    }
    
    private void addBossByType(int type, int x) {
        // Apply health multiplier only for the Penta Phase
        double healthMultiplier = (bossKillCount == 12) ? PENTA_HEALTH_MULTIPLIER : 1.0;

        // Constructors must now accept healthMultiplier
        if (type == 1) activeBosses.add(new Boss(x, 50, healthMultiplier));
        else if (type == 2) activeBosses.add(new Boss2(x, 50, healthMultiplier));
        else if (type == 3) activeBosses.add(new Boss3(x, 50, healthMultiplier));
        else if (type == 4) activeBosses.add(new Boss4(x, 50, healthMultiplier));
    }

    @Override
    public void actionPerformed(ActionEvent e) { // Game Loop
        if (gameOver || victoryState) return; 
        
        if (notificationTimer > 0) notificationTimer--;
        if (invincibilityTimer > 0) invincibilityTimer--;
        if (shakeTimer > 0) shakeTimer--;

        // Combo timer
        if (comboTimer > 0) {
            comboTimer--;
            if (comboTimer == 0) {
                comboCount = 0;
                comboMultiplier = 1.0;
            }
        }
        if (comboTimer > 0) { comboTimer--; if (comboTimer == 0) comboCount = 0; }

        // Update particles
        java.util.Iterator<int[]> particleIt = particles.iterator();
        while (particleIt.hasNext()) {
            int[] p = particleIt.next();
            p[0] += p[2]; p[1] += p[3]; p[4]--;
            if (p[4] <= 0) particleIt.remove();
        }



        // Update Mini-Boss
        if (miniBossActive && miniBoss != null) {
            Boss mb = (Boss) miniBoss;
            mb.update();
            if (mb.canShoot()) bullets.addAll(mb.shoot());
            // mini-boss bullet collisions
            for (java.util.Iterator<Bullet> mbi = bullets.iterator(); mbi.hasNext();) {
                Bullet b = mbi.next();
                if (mb.getBounds().intersects(b.getBounds()) && b.dy < 0) {
                    mb.takeDamage(10); totalShotsHit++;
                    mbi.remove();
                }
            }
            if (mb.isDestroyed()) {
                score += 150;
                triggerShake(8, 15);
                spawnParticles(mb.getBounds().x + 50, mb.getBounds().y + 50, 20, 255, 140, 30);
                showNotification("MINI-BOSS DEFEATED! +150", new Color(255, 200, 50));
                miniBossActive = false; miniBoss = null;
                powerUps.add(new PowerUp(random.nextInt(400) + 50, Type.HEALTH));
            }
        }

        // Scrolling background
        bgY1 += 2;
        if (bgY1 >= 700) bgY1 = 0;
        
        if (player.isSuperMode()) {
            superModeTimer--;
            if (superModeTimer <= 0) {
                player.deactivateSuperMode();
            }
        }

        if (leftPressed) player.moveLeft();
        if (rightPressed) player.moveRight();
        if (upPressed) player.moveUp();
        if (downPressed) player.moveDown();

        if (shootPressed && shootCooldown <= 0) {
            bullets.addAll(player.shoot());
            for (PartnerPlane pp : partnerPlanes) {
                bullets.addAll(pp.shoot());
            }
            totalShotsFired++;
            shootCooldown = shootCooldownBase;
        }
        if (shootCooldown > 0) shootCooldown--;

        Iterator<Bullet> bulletIt = bullets.iterator(); // Update bullets
        while (bulletIt.hasNext()) {
            Bullet b = bulletIt.next();
            // PHASE 3: Homing logic for player bullets
            if (b.dy < 0 && player.getWeapon() == Player.Weapon.HOMING) {
                Enemy nearest = null;
                double nearestDist = Double.MAX_VALUE;
                for (Enemy en : enemies) {
                    Rectangle eb = en.getBounds();
                    double dist = Math.hypot(eb.x - b.getBounds().x, eb.y - b.getBounds().y);
                    if (dist < nearestDist) { nearestDist = dist; nearest = en; }
                }
                if (nearest != null && nearestDist < 200) {
                    Rectangle nb = nearest.getBounds();
                    double dx = nb.x - b.getBounds().x;
                    double dy = nb.y - b.getBounds().y;
                    double dist = Math.hypot(dx, dy);
                    b.dx += (dx / dist) * 0.8;
                    b.dy += (dy / dist) * 0.8;
                    double speed = Math.hypot(b.dx, b.dy);
                    if (speed > 12) { b.dx = b.dx / speed * 12; b.dy = b.dy / speed * 12; }
                }
            }
            b.update();
            if (b.isOffScreen()) bulletIt.remove();
        }
        
        Iterator<PowerUp> powerUpIt = powerUps.iterator(); // Update power-ups
        while (powerUpIt.hasNext()) {
            PowerUp pu = powerUpIt.next();
            pu.update(); 

            if (pu.isOffScreen()) {
                powerUpIt.remove();
            } else if (player.getBounds().intersects(pu.getBounds())) {
                if (pu.getType() == Type.HEALTH) {
                    health = Math.min(maxPlayerHealth, health + HEAL_AMOUNT); 
                } else if (pu.getType() == Type.SHIELD) {
                    player.activateShield(); 
                } else if (pu.getType() == Type.SUPER) {
                    player.activateSuperMode();
                    superModeTimer = SUPER_MODE_DURATION_TICKS;
                }
                powerUpIt.remove();
            }
        }
        
        for (PartnerPlane pp : partnerPlanes) { // Update partner planes
            pp.update();
        }
        
        Iterator<EnemyFighter> enemyFighterIt = enemyFighters.iterator(); // Update enemy fighters
        while (enemyFighterIt.hasNext()) {
            EnemyFighter ef = enemyFighterIt.next();
            ef.update();
            if (ef.canShoot()) {
                bullets.addAll(ef.shootBullet()); 
            }
        }

        boolean isPentaPhase = (bossKillCount == 12); // Check for Penta Phase
        
        // Enemy spawning logic (includes Penta Phase)
        if (activeBosses.isEmpty() || isPentaPhase) {
            
            // Basic enemies still spawn in Penta Phase
            if (random.nextInt(50) == 0) {
                enemies.add(new Enemy(random.nextInt(460), 0));
            }

            if (bossKillCount > 0 &&
                enemyFighters.size() < MAX_ENEMY_FIGHTERS &&
                random.nextInt(100) < ENEMY_FIGHTER_CHANCE)
            {
                enemyFighters.add(new EnemyFighter(random.nextInt(460)));
            }

            // Spawn snipers after wave 2
            if (bossKillCount >= 2 && enemySnipers.size() < 2 && random.nextInt(200) == 0) {
                enemySnipers.add(new EnemySniper(random.nextInt(400) + 30));
            }

            // Spawn shield enemies after wave 3
            if (bossKillCount >= 3 && enemyShields.size() < 2 && random.nextInt(200) == 0) {
                enemyShields.add(new EnemyShield(random.nextInt(400) + 30));
            }

            // Spawn mini-boss every 2 waves (between main bosses)
            if (bossKillCount > 0 && bossKillCount % 2 == 0 && !miniBossActive
                    && activeBosses.isEmpty() && enemiesDestroyed == ENEMIES_FOR_BOSS / 2) {
                spawnMiniBoss();
            }
            
            if (activeBosses.isEmpty() && enemiesDestroyed >= ENEMIES_FOR_BOSS) { 
                showNotification("WARNING -- BOSS INCOMING!", new Color(220, 50, 50));
                triggerShake(6, 15);
                spawnBossPhase();
                // Only clear normal enemies if we are NOT in the Penta Phase.
                if (!isPentaPhase) { 
                    pendingEnemyCleanup = true; 
                }
            } 
            
            if (enemiesDestroyed > lastKillCountForPowerUp + KILLS_PER_POWERUP) { // Spawn power-ups
                lastKillCountForPowerUp = enemiesDestroyed;
                Type powerUpType = random.nextBoolean() ? Type.HEALTH : Type.SHIELD;
                powerUps.add(new PowerUp(random.nextInt(460), powerUpType));
                
                if (random.nextInt(100) < 40) {
                    Type secondType = random.nextBoolean() ? Type.HEALTH : Type.SHIELD;
                    int x2 = random.nextInt(460);
                    powerUps.add(new PowerUp(x2, secondType));
                }
            }
            
            if (score >= lastScoreForSuperMode + SUPER_MODE_SCORE_TRIGGER) { // Spawn Super Mode power-up
                lastScoreForSuperMode = (score / SUPER_MODE_SCORE_TRIGGER) * SUPER_MODE_SCORE_TRIGGER;
                powerUps.add(new PowerUp(random.nextInt(460), Type.SUPER));
            }
        }

        Iterator<Enemy> enemyIt = enemies.iterator(); // Update enemies
        while (enemyIt.hasNext()) {
            Enemy en = enemyIt.next();
            en.update();

            if (en.isOffScreen()) {
                enemyIt.remove();
                if (!player.isSuperMode() && !GOD_MODE) {
                    health -= 10;
                    checkGameOver(); 
                }
            }
        }   

        if (!activeBosses.isEmpty()) { // Update bosses
            Iterator<Object> bossIt = activeBosses.iterator();
            while (bossIt.hasNext()) {
                Object currentBoss = bossIt.next();
                Rectangle bossBounds = null;
                boolean destroyed = false;

                if (currentBoss instanceof Boss) {
                    Boss b = (Boss)currentBoss;
                    int prevPhase = b.getPhase();
                    b.update();
                    bossBounds = b.getBounds();
                    if (b.canShoot()) bullets.addAll(b.shoot());
                    if (b.isDestroyed()) destroyed = true;
                    if (b.getPhase() > prevPhase) {
                        showNotification("BOSS PHASE " + b.getPhase() + "!", new Color(220, 50, 50));
                        triggerShake(8, 12);
                    }
                } else if (currentBoss instanceof Boss2) {
                    Boss2 b2 = (Boss2)currentBoss;
                    b2.update();
                    bossBounds = b2.getBounds();
                    if (b2.canSpawnFighters()) enemyFighters.add(b2.spawnFighter());
                    if (b2.isDestroyed()) destroyed = true;
                } else if (currentBoss instanceof Boss3) {
                    Boss3 b3 = (Boss3)currentBoss;
                    b3.update();
                    bossBounds = b3.getBounds();
                    if (b3.canShoot()) bullets.addAll(b3.shootMultiple());
                    if (b3.isDestroyed()) destroyed = true;
                } else if (currentBoss instanceof Boss4) {
                    Boss4 b4 = (Boss4)currentBoss;
                    b4.update();
                    bossBounds = b4.getBounds();
                    if (b4.canShoot()) bullets.addAll(b4.shootCircular());
                    if (b4.isDestroyed()) destroyed = true;
                }

                Iterator<Bullet> bi = bullets.iterator(); // Check bullet collisions with boss
                while (bi.hasNext()) {
                    Bullet b = bi.next();
                    // Boss takes damage only from player bullets (dy < 0)
                    if (bossBounds != null && b.getBounds().intersects(bossBounds) && b.dy < 0) {
                        if (currentBoss instanceof Boss) ((Boss)currentBoss).takeDamage(bulletDamage);
                        else if (currentBoss instanceof Boss2) ((Boss2)currentBoss).takeDamage(bulletDamage);
                        else if (currentBoss instanceof Boss3) ((Boss3)currentBoss).takeDamage(bulletDamage);
                        else if (currentBoss instanceof Boss4) ((Boss4)currentBoss).takeDamage(bulletDamage);
                        bi.remove(); 
                    }
                }
                
                if (destroyed) { // Boss destroyed
                    score += 100;
                    totalEnemiesKilled++;
                    triggerShake(10, 20);
                    if (bossBounds != null) {
                        spawnParticles(bossBounds.x + bossBounds.width/2,
                                       bossBounds.y + bossBounds.height/2, 25, 255, 80, 20);
                    }
                    bossIt.remove();
                }
            }
            
            if (activeBosses.isEmpty()) { // All bosses defeated
                bossKillCount++; 
                
                // VICTORY CONDITION: Triggered after clearing the Penta Boss Phase (bossKillCount == 13)
                if (bossKillCount == 13) { 
                    triggerVictory();
                    return; 
                }
                
                showNotification("WAVE " + (bossKillCount) + " CLEARED!", new Color(0, 128, 0));
                // PHASE 3: Show upgrade shop
                timer.stop();
                SwingUtilities.invokeLater(() -> openUpgradeShop()); 
                
                checkProgressionRewards(); 
                
                enemiesDestroyed = 0;
                lastKillCountForPowerUp = 0;
                miniBossSpawnedThisWave = false;
                currentBossLevel++;
                if (currentBossLevel > MAX_BOSS_LEVEL) {
                    currentBossLevel = 1; 
                }
                // Set flags for deferred cleanup
                pendingWaveCleanup = true;
                pendingFighterCleanup = true;
                enemySnipers.clear();
                enemyShields.clear();
            }
        }
        
        // Sniper bullet hits player (already in bullets list — handled above)
        // Shield enemy bullet collision with player (already in bullets list)

        // EnemySniper bullet collision with player bullets
        for (java.util.Iterator<EnemySniper> sni = enemySnipers.iterator(); sni.hasNext();) {
            EnemySniper sn = sni.next();
            for (java.util.Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
                Bullet b = bi.next();
                if (sn.getBounds().intersects(b.getBounds()) && b.dy < 0) {
                    sn.takeDamage(10); totalShotsHit++;
                    bi.remove();
                    if (sn.isDestroyed()) {
                        Rectangle eb = sn.getBounds();
                        registerKill(eb.x + eb.width/2, eb.y + eb.height/2, 40, 180, 80, 255);
                        sni.remove();
                        break;
                    }
                }
            }
        }

        // EnemyShield collision with player bullets (flanking required)
        for (java.util.Iterator<EnemyShield> shi = enemyShields.iterator(); shi.hasNext();) {
            EnemyShield sh = shi.next();
            for (java.util.Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
                Bullet b = bi.next();
                if (sh.getBounds().intersects(b.getBounds()) && b.dy < 0) {
                    boolean hit = sh.takeDamage(10, b.dx);
                    if (hit) { totalShotsHit++; }
                    bi.remove();
                    if (sh.isDestroyed()) {
                        Rectangle eb = sh.getBounds();
                        registerKill(eb.x + eb.width/2, eb.y + eb.height/2, 50, 80, 160, 255);
                        shi.remove();
                        break;
                    }
                }
            }
        }

        // Player/Bullet/Enemy/Fighter collision logic remains the same...
        for (Iterator<Bullet> bbi = bullets.iterator(); bbi.hasNext();) {
            Bullet bb = bbi.next();
            
            if (bb.dy > 0) {
                if (player.getBounds().intersects(bb.getBounds())) {
                    if (invincibilityTimer <= 0) {
                        if (!player.takeHit() && !GOD_MODE) {
                            health -= 15;
                            comboCount = 0; comboMultiplier = 1.0; comboTimer = 0;
                            triggerShake(5, 10);
                            invincibilityTimer = INVINCIBILITY_DURATION;
                            comboCount = 0; comboTimer = 0;
                            Rectangle pb = player.getBounds();
                            spawnParticles(pb.x + pb.width/2, pb.y + pb.height/2, 10, 220, 60, 60);
                            checkGameOver();
                        } else {
                            triggerShake(3, 6);
                        }
                        bbi.remove();
                    } else {
                        bbi.remove();
                    }
                    continue;
                }
                for (PartnerPlane pp : partnerPlanes) {
                    if (!pp.isDead() && pp.getBounds().intersects(bb.getBounds())) {
                        pp.takeDamage(15); 
                        bbi.remove(); 
                        break; 
                    }
                }
            }
        }

        for (Iterator<Enemy> ei = enemies.iterator(); ei.hasNext();) {
            Enemy en = ei.next();
            for (Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
                Bullet b = bi.next();
                if (en.getBounds().intersects(b.getBounds()) && b.dy < 0) {
                    Rectangle eb = en.getBounds();
                    registerKill(eb.x + eb.width/2, eb.y + eb.height/2, 10, 255, 160, 40);
                    ei.remove(); bi.remove();
                    enemiesDestroyed++;
                    break;
                }
            }
        }
        
        Iterator<EnemyFighter> efCollisionIt = enemyFighters.iterator();
        while (efCollisionIt.hasNext()) {
            EnemyFighter ef = efCollisionIt.next();
            for (Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
                Bullet b = bi.next();
                if (ef.getBounds().intersects(b.getBounds()) && b.dy < 0) {
                    ef.takeDamage(10);
                    bi.remove();
                    if (ef.isDestroyed()) {
                        Rectangle eb = ef.getBounds();
                        registerKill(eb.x + eb.width/2, eb.y + eb.height/2, 30, 255, 120, 30);
                        efCollisionIt.remove();
                        break;
                    }
                }
            }
        }
        
        for (Iterator<Enemy> ei = enemies.iterator(); ei.hasNext();) {
            Enemy en = ei.next();
            if (player.getBounds().intersects(en.getBounds())) {
                ei.remove(); 
                break;
            }
        }
        
        for (Iterator<EnemyFighter> efCrashIt = enemyFighters.iterator(); efCrashIt.hasNext();) {
             EnemyFighter ef = efCrashIt.next();
            if (player.getBounds().intersects(ef.getBounds())) {
                efCrashIt.remove(); 
                break;
            }
        }
        
        
        
        // PHASE 2: Update snipers
        Rectangle playerRect = player.getBounds();
        Iterator<EnemySniper> sniperIt = enemySnipers.iterator();
        while (sniperIt.hasNext()) {
            EnemySniper sn = sniperIt.next();
            sn.update();
            if (sn.canShoot()) {
                bullets.addAll(sn.shootAt(
                    playerRect.x + playerRect.width / 2,
                    playerRect.y + playerRect.height / 2));
            }
            // Bullet hits sniper
            for (Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
                Bullet b = bi.next();
                if (b.dy < 0 && sn.getBounds().intersects(b.getBounds())) {
                    sn.takeDamage(10);
                    totalShotsHit++;
                    bi.remove();
                    if (sn.isDestroyed()) {
                        Rectangle sb = sn.getBounds();
                        spawnParticles(sb.x + sb.width/2, sb.y + sb.height/2, 10, 255, 80, 80);
                        sniperIt.remove();
                        score += (int)(20 * comboMultiplier);
                        totalEnemiesKilled++;
                        incrementCombo();
                    }
                    break;
                }
            }
        }

        // PHASE 2: Update shield enemies
        Iterator<EnemyShield> shieldIt = enemyShields.iterator();
        while (shieldIt.hasNext()) {
            EnemyShield se = shieldIt.next();
            se.update();
            if (se.isOffScreen()) { shieldIt.remove(); continue; }
            if (se.canShoot()) bullets.addAll(se.shoot());
            // Bullet hits shield enemy
            for (Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
                Bullet b = bi.next();
                if (b.dy < 0 && se.getBounds().intersects(b.getBounds())) {
                    boolean hit = se.takeDamage(10, b.dx);
                    if (hit) totalShotsHit++;
                    if (se.didShieldJustBreak()) {
                        showNotification("SHIELD BROKEN!", new Color(80, 200, 255));
                    }
                    bi.remove();
                    if (se.isDestroyed()) {
                        Rectangle sb = se.getBounds();
                        spawnParticles(sb.x + sb.width/2, sb.y + sb.height/2, 12, 80, 180, 255);
                        shieldIt.remove();
                        score += (int)(35 * comboMultiplier);
                        totalEnemiesKilled++;
                        incrementCombo();
                    }
                    break;
                }
            }
        }

        // 1. Safe clear of player bullets after wave clear
        if (pendingWaveCleanup) {
            bullets.removeIf(bullet -> bullet.dy < 0);
            pendingWaveCleanup = false;
        }

        // 2. Safe clear of enemy bullets after player revival
        if (pendingRevivalCleanup) {
            bullets.removeIf(bullet -> bullet.dy > 0);
            pendingRevivalCleanup = false;
        }
        
        // 3. Safe clear of enemies list after new boss wave begins
        if (pendingEnemyCleanup) {
            enemies.clear();
            pendingEnemyCleanup = false;
        }

        // 4. Safe clear of enemy fighters after wave clear
        if (pendingFighterCleanup) {
            enemyFighters.clear();
            enemySnipers.clear();
            enemyShields.clear();
            pendingFighterCleanup = false;
        }
        
        repaint();
    }
    

    // ── END-GAME BUTTON FACTORY ─────────────────────────────────────────────
    private JButton makeEndButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2b = (java.awt.Graphics2D) g.create();
                g2b.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                     java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg;
                g2b.setColor(base);
                g2b.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Top highlight
                g2b.setColor(new Color(255,255,255,30));
                g2b.fillRoundRect(0, 0, getWidth(), getHeight()/2, 12, 12);
                // Border
                g2b.setColor(new Color(180,130,255,100));
                g2b.setStroke(new java.awt.BasicStroke(1.2f));
                g2b.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2b.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(new Color(220, 200, 255));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new java.awt.Dimension(160, 40));
        return btn;
    }

    // ── PHASE 1 HELPERS ─────────────────────────────────────────────────────

    private void triggerShake(int magnitude, int duration) {
        shakeTimer     = duration;
        shakeMagnitude = magnitude;
    }

    private void spawnParticles(int x, int y, int count, int r, int g, int b) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.5 + Math.random() * 3.5;
            int dx   = (int)(Math.cos(angle) * speed);
            int dy   = (int)(Math.sin(angle) * speed);
            int life = 15 + (int)(Math.random() * 20);
            particles.add(new int[]{x, y, dx, dy, life, life, r, g, b});
        }
    }

    private void showNotification(String message, Color color) {
        this.notificationMessage = message;
        this.notificationColor   = color;
        this.notificationTimer   = NOTIFICATION_DURATION;
    }

    private void incrementCombo() {
        comboCount++;
        comboTimer = COMBO_TIMEOUT;
        if      (comboCount >= 10) comboMultiplier = 3.0;
        else if (comboCount >= 6)  comboMultiplier = 2.0;
        else if (comboCount >= 3)  comboMultiplier = 1.5;
    }

    private void openUpgradeShop() {
        // Find parent JFrame safely
        Window win = SwingUtilities.getWindowAncestor(this);
        JFrame parent = (win instanceof JFrame) ? (JFrame) win : null;
        UpgradeShop shop = new UpgradeShop(parent, score, bossKillCount);
        shop.setVisible(true);

        // Apply upgrades
        int dmg  = shop.getDamageUpgrades();
        int spd  = shop.getSpeedUpgrades();
        int hp   = shop.getHealthUpgrades();
        int fr   = shop.getFireRateUpgrades();
        score    = shop.getRemainingScore();

        for (int i = 0; i < dmg; i++)  { bulletDamage += 2; totalDamageUpgrades++; player.applyDamageUpgrade(); }
        for (int i = 0; i < spd; i++)  { player.applySpeedUpgrade(); }
        for (int i = 0; i < fr; i++)   { shootCooldownBase = Math.max(4, shootCooldownBase - 1); totalFireRateUpgrades++; player.applyFireRateUpgrade(); }
        for (int i = 0; i < hp; i++)   { maxPlayerHealth += 25; health = Math.min(health + 25, maxPlayerHealth); }

        timer.start();
        requestFocusInWindow();
    }

    private void triggerVictory() {
        victoryState = true;
        timer.stop();
        bullets.clear(); 
        
        // --- LEADERBOARD: Save Victory Data ---
        if (currentPlayerProfile != null && leaderboardManager != null) {
            leaderboardManager.updateAndSaveProfile(
                currentPlayerProfile, 
                score, 
                bossKillCount, 
                true // Victory is true
            );
        }
        // --------------------------------------
        
        // Setup Button Panels
        retryButtonPanel.removeAll();
        retryButtonPanel.add(retryButton); 
        add(retryButtonPanel, BorderLayout.CENTER);
        
        menuButtonPanel.removeAll();
        menuButtonPanel.add(returnToMenuButton);
        menuButtonPanel.add(leaderboardButton); 
        add(menuButtonPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }
    
    private void checkGameOver() {
        if (health <= 0) {
            
            if (!partnerPlanes.isEmpty()) {
                
                partnerPlanes.remove(partnerPlanes.size() - 1);
                
                health = maxPlayerHealth; 
                player = new Player(250, 500); 
                
                for (PartnerPlane pp : partnerPlanes) {
                    pp.setPlayer(player);
                }
                
                pendingRevivalCleanup = true; 
                
                showNotification("Airforce Bro Sacrificed! (Player Revived)", Color.BLUE);
                
                return; 
            }
            
            gameOver = true;
            timer.stop();
            
            // --- LEADERBOARD: Save Game Over Data ---
            if (currentPlayerProfile != null && leaderboardManager != null) {
                leaderboardManager.updateAndSaveProfile(
                    currentPlayerProfile, 
                    score, 
                    bossKillCount, 
                    false // Victory is false
                );
            }
            // ----------------------------------------
            
            // Setup Button Panels
            retryButtonPanel.removeAll();
            retryButtonPanel.add(retryButton);
            add(retryButtonPanel, BorderLayout.CENTER); 

            menuButtonPanel.removeAll();
            menuButtonPanel.add(returnToMenuButton);
            menuButtonPanel.add(leaderboardButton); 
            add(menuButtonPanel, BorderLayout.SOUTH);
            
            revalidate(); 
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || victoryState) return;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) upPressed = true;
        if (key == KeyEvent.VK_S) downPressed = true;
        if (key == KeyEvent.VK_A) leftPressed = true;
        if (key == KeyEvent.VK_D) rightPressed = true;
        if (key == KeyEvent.VK_SPACE) shootPressed = true;
        if (key == KeyEvent.VK_Q && !qPressed) {
            qPressed = true;
            player.cycleWeapon();
            showNotification("WEAPON: " + player.getWeaponName(), new Color(140, 200, 255));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver || victoryState) return;
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) upPressed = false;
        if (key == KeyEvent.VK_S) downPressed = false;
        if (key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_D) rightPressed = false;
        if (key == KeyEvent.VK_SPACE) shootPressed = false;
        if (key == KeyEvent.VK_Q) qPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new MainMenu(); 
    }
}