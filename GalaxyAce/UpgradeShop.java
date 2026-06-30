package GalaxyAce;

import javax.swing.*;
import java.awt.*;

public class UpgradeShop extends JDialog {

    private static final Color BG       = new Color(8, 5, 25);
    private static final Color CARD_BG  = new Color(18, 12, 45);
    private static final Color PURPLE   = new Color(180, 130, 255);
    private static final Color TEXT     = new Color(210, 200, 240);
    private static final Color SUBTLE   = new Color(120, 110, 160);
    private static final Color GOLD     = new Color(255, 210, 60);

    private static final int COST_DAMAGE    = 150;
    private static final int COST_SPEED     = 100;
    private static final int COST_HEALTH    = 120;
    private static final int COST_FIRE_RATE = 130;
    private static final int MAX_STACKS     = 3;

    private int damageUpgrades   = 0;
    private int speedUpgrades    = 0;
    private int healthUpgrades   = 0;
    private int fireRateUpgrades = 0;
    private int remainingScore;

    private JLabel   scoreLabel;
    private JLabel[] countLabels = new JLabel[4];
    private JButton[] buyButtons  = new JButton[4];

    private final int[] COSTS  = {COST_DAMAGE, COST_SPEED, COST_HEALTH, COST_FIRE_RATE};
    private final Color[] ACCENTS = {
        new Color(220, 80,  80),
        new Color(60,  160, 220),
        new Color(60,  200, 100),
        new Color(220, 160, 40)
    };

    public UpgradeShop(JFrame parent, int currentScore, int waveNumber) {
        super(parent, "UPGRADE SHOP", true);
        this.remainingScore = currentScore;

        setSize(580, 480);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(waveNumber), BorderLayout.NORTH);
        add(buildCenter(),           BorderLayout.CENTER);
        add(buildSouth(),            BorderLayout.SOUTH);

        refreshButtons();
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader(int wave) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20, 10, 50));
        p.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));

        JLabel title = new JLabel("UPGRADE SHOP", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(PURPLE);

        JLabel sub = new JLabel("Wave " + wave + " complete — spend your score", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(SUBTLE);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        p.add(title, BorderLayout.CENTER);
        p.add(sub,   BorderLayout.SOUTH);
        return p;
    }

    // ── Center: score + grid ────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(BG);

        // Score label
        scoreLabel = new JLabel("Available:  " + remainingScore + " pts", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 15));
        scoreLabel.setForeground(GOLD);
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(BG);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 8, 0));

        // 2x2 grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setBackground(BG);
        grid.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

        String[] names = {"DAMAGE",       "MOVE SPEED",   "MAX HEALTH",    "FIRE RATE"};
        String[] descs = {"+2 dmg / shot","+1 spd / stack","+25 max HP",   "-1 cooldown / stack"};

        for (int i = 0; i < 4; i++) {
            grid.add(buildCard(i, names[i], descs[i]));
        }

        wrapper.add(scoreLabel, BorderLayout.NORTH);
        wrapper.add(grid,       BorderLayout.CENTER);
        return wrapper;
    }

    // ── Individual card ─────────────────────────────────────────────────────
    private JPanel buildCard(int idx, String name, String desc) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(ACCENTS[idx].getRed(), ACCENTS[idx].getGreen(), ACCENTS[idx].getBlue(), 120), 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel nameL = new JLabel(name);
        nameL.setFont(new Font("Arial", Font.BOLD, 14));
        nameL.setForeground(ACCENTS[idx]);

        JLabel descL = new JLabel(desc);
        descL.setFont(new Font("Arial", Font.PLAIN, 12));
        descL.setForeground(SUBTLE);

        JLabel costL = new JLabel("Cost: " + COSTS[idx] + " pts");
        costL.setFont(new Font("Arial", Font.BOLD, 12));
        costL.setForeground(GOLD);

        countLabels[idx] = new JLabel("Purchased: 0 / " + MAX_STACKS);
        countLabels[idx].setFont(new Font("Arial", Font.PLAIN, 11));
        countLabels[idx].setForeground(TEXT);

        buyButtons[idx] = new JButton("BUY");
        buyButtons[idx].setFont(new Font("Arial", Font.BOLD, 13));
        buyButtons[idx].setForeground(Color.WHITE);
        buyButtons[idx].setBackground(ACCENTS[idx].darker());
        buyButtons[idx].setFocusPainted(false);
        buyButtons[idx].setBorderPainted(false);
        buyButtons[idx].setOpaque(true);
        buyButtons[idx].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buyButtons[idx].setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        buyButtons[idx].setPreferredSize(new Dimension(160, 34));

        final int I = idx;
        buyButtons[idx].addActionListener(e -> handleBuy(I));

        card.add(nameL);
        card.add(Box.createVerticalStrut(4));
        card.add(descL);
        card.add(Box.createVerticalStrut(4));
        card.add(costL);
        card.add(Box.createVerticalStrut(8));
        card.add(countLabels[idx]);
        card.add(Box.createVerticalStrut(8));
        card.add(buyButtons[idx]);

        return card;
    }

    // ── South: continue ─────────────────────────────────────────────────────
    private JPanel buildSouth() {
        JButton btn = new JButton("CONTINUE TO NEXT WAVE");
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 40, 140));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(260, 40));
        btn.addActionListener(e -> dispose());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(8, 0, 14, 0));
        p.add(btn);
        return p;
    }

    // ── Logic ────────────────────────────────────────────────────────────────
    private void handleBuy(int idx) {
        int[] counts = {damageUpgrades, speedUpgrades, healthUpgrades, fireRateUpgrades};
        if (remainingScore < COSTS[idx] || counts[idx] >= MAX_STACKS) return;

        remainingScore -= COSTS[idx];

        switch (idx) {
            case 0: damageUpgrades++;   break;
            case 1: speedUpgrades++;    break;
            case 2: healthUpgrades++;   break;
            case 3: fireRateUpgrades++; break;
        }

        int[] newCounts = {damageUpgrades, speedUpgrades, healthUpgrades, fireRateUpgrades};
        countLabels[idx].setText("Purchased: " + newCounts[idx] + " / " + MAX_STACKS);
        scoreLabel.setText("Available:  " + remainingScore + " pts");
        refreshButtons();
    }

    private void refreshButtons() {
        int[] counts = {damageUpgrades, speedUpgrades, healthUpgrades, fireRateUpgrades};
        for (int i = 0; i < 4; i++) {
            boolean can = remainingScore >= COSTS[i] && counts[i] < MAX_STACKS;
            buyButtons[i].setEnabled(can);
            buyButtons[i].setForeground(can ? Color.WHITE : new Color(100, 90, 130));
            buyButtons[i].setBackground(can ? ACCENTS[i].darker() : new Color(30, 20, 50));
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int getDamageUpgrades()   { return damageUpgrades; }
    public int getSpeedUpgrades()    { return speedUpgrades; }
    public int getHealthUpgrades()   { return healthUpgrades; }
    public int getFireRateUpgrades() { return fireRateUpgrades; }
    public int getRemainingScore()   { return remainingScore; }
}