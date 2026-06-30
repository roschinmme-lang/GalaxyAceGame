package GalaxyAce;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Random;

public class MainMenu extends JFrame {

    private final int FRAME_WIDTH = 600;
    private final int FRAME_HEIGHT = 600;
    private LeaderboardManager leaderboardManager;

    public MainMenu() {
        leaderboardManager = new LeaderboardManager();
        setTitle("Galaxy Ace");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        MenuPanel menuPanel = new MenuPanel();
        menuPanel.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("GALAXY ACE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 46));
        titleLabel.setForeground(new Color(180, 130, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("INTERGALACTIC DEFENSE FORCE");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(140, 180, 255));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons
        GameButton startButton       = new GameButton("START GAME",   new Color(80, 40, 160));
        GameButton leaderboardButton = new GameButton("LEADERBOARDS", new Color(40, 60, 160));
        GameButton controlsButton    = new GameButton("CONTROLS",     new Color(60, 30, 130));
        GameButton quitButton        = new GameButton("QUIT",         new Color(120, 30, 80));

        startButton.addActionListener(e -> handleStartGame());
        leaderboardButton.addActionListener(e -> showLeaderboard());
        controlsButton.addActionListener(e -> showControls());
        quitButton.addActionListener(e -> System.exit(0));

        centerPanel.add(Box.createVerticalStrut(130));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(60));
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(leaderboardButton);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(controlsButton);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(quitButton);

        JLabel versionLabel = new JLabel("Galaxy Ace  v1.0  ");
        versionLabel.setForeground(new Color(80, 80, 120));
        versionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        menuPanel.add(centerPanel, BorderLayout.CENTER);
        menuPanel.add(versionLabel, BorderLayout.SOUTH);

        add(menuPanel);
        setVisible(true);
    }

    // ── Galaxy pill button ───────────────────────────────────────────────────
    static class GameButton extends JButton {
        private final Color baseColor;
        private Color currentColor;
        private static final int ARC = 16;

        GameButton(String text, Color base) {
            super(text);
            this.baseColor = base;
            this.currentColor = base;

            setFont(new Font("Arial", Font.BOLD, 16));
            setForeground(new Color(220, 200, 255));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);

            Dimension size = new Dimension(250, 48);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { currentColor = baseColor.brighter(); repaint(); }
                @Override public void mouseExited(MouseEvent e)   { currentColor = baseColor;            repaint(); }
                @Override public void mousePressed(MouseEvent e)  { currentColor = baseColor.darker();   repaint(); }
                @Override public void mouseReleased(MouseEvent e) { currentColor = baseColor.brighter(); repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fill(new RoundRectangle2D.Float(3, 6, getWidth() - 3, getHeight() - 3, ARC, ARC));
            g2.setColor(currentColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 5, ARC, ARC));
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, (getHeight() - 5) / 2, ARC, ARC));
            g2.setColor(new Color(180, 130, 255, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 6, ARC, ARC));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Starfield background panel ───────────────────────────────────────────
    private class MenuPanel extends JPanel {
        private Image background;
        private final int[] starX, starY, starSize;
        private final float[] starBrightness;
        private static final int STAR_COUNT = 120;

        MenuPanel() {
            background = new ImageIcon("src/GalaxyAce/resources/MainMenuBackground.png").getImage();
            setDoubleBuffered(true);
            Random rng = new Random(42);
            starX = new int[STAR_COUNT];
            starY = new int[STAR_COUNT];
            starSize = new int[STAR_COUNT];
            starBrightness = new float[STAR_COUNT];
            for (int i = 0; i < STAR_COUNT; i++) {
                starX[i] = rng.nextInt(FRAME_WIDTH);
                starY[i] = rng.nextInt(FRAME_HEIGHT);
                starSize[i] = rng.nextInt(3) + 1;
                starBrightness[i] = 0.3f + rng.nextFloat() * 0.7f;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background image
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), this);

            // Dark space overlay
            g2.setColor(new Color(5, 0, 20, 180));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Stars
            for (int i = 0; i < STAR_COUNT; i++) {
                int alpha = (int)(starBrightness[i] * 255);
                g2.setColor(new Color(200, 190, 255, alpha));
                g2.fillOval(starX[i], starY[i], starSize[i], starSize[i]);
            }
        }
    }

    private void handleStartGame() {
        ProfileAccess profileAccess = new ProfileAccess(this, leaderboardManager);
        profileAccess.setVisible(true);
        PlayerProfile selectedProfile = profileAccess.getAuthenticatedProfile();
        if (selectedProfile != null) {
            setVisible(false);
            dispose();
            GalaxyAceMechanics.startGame(selectedProfile, leaderboardManager);
        }
    }

    private void showLeaderboard() {
        new LeaderboardTableDialog(this, leaderboardManager).setVisible(true);
    }

    private void showControls() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
        panel.setBackground(new Color(10, 5, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        String[][] keys = {
            {"W", "Move Up"}, {"S", "Move Down"},
            {"A", "Move Left"}, {"D", "Move Right"},
            {"SPACE", "Shoot"}
        };
        for (String[] pair : keys) {
            JLabel key = new JLabel(pair[0], SwingConstants.CENTER);
            key.setFont(new Font("Arial", Font.BOLD, 14));
            key.setForeground(new Color(180, 130, 255));
            key.setBorder(BorderFactory.createLineBorder(new Color(100, 60, 200), 1));
            key.setOpaque(true);
            key.setBackground(new Color(30, 15, 60));
            JLabel desc = new JLabel(pair[1]);
            desc.setFont(new Font("Arial", Font.PLAIN, 14));
            desc.setForeground(new Color(200, 190, 255));
            panel.add(key);
            panel.add(desc);
        }
        JOptionPane.showMessageDialog(this, panel, "Controls", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}

// ── Leaderboard Dialog ────────────────────────────────────────────────────────
class LeaderboardTableDialog extends JDialog {
    private static final Color BG        = new Color(8, 5, 25);
    private static final Color HEADER_BG = new Color(20, 10, 50);
    private static final Color ROW_ODD   = new Color(14, 8, 38);
    private static final Color ROW_EVEN  = new Color(20, 12, 48);
    private static final Color PURPLE    = new Color(180, 130, 255);
    private static final Color TEXT      = new Color(210, 200, 240);

    public LeaderboardTableDialog(JFrame parent, LeaderboardManager manager) {
        super(parent, "Galaxy Ace — Leaderboards", true);
        setSize(660, 480);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(BG);

        JLabel title = new JLabel("GALAXY ACE — TOP PILOTS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(PURPLE);
        title.setBorder(BorderFactory.createEmptyBorder(18, 0, 14, 0));
        title.setOpaque(true);
        title.setBackground(HEADER_BG);
        add(title, BorderLayout.NORTH);

        JTable table = createLeaderboardTable(manager.getSortedLeaderboard());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        add(scrollPane, BorderLayout.CENTER);

        MainMenu.GameButton closeButton = new MainMenu.GameButton("CLOSE", new Color(100, 30, 80));
        closeButton.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(BG);
        south.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        south.add(closeButton);
        add(south, BorderLayout.SOUTH);
    }

    private JTable createLeaderboardTable(List<PlayerProfile> leaders) {
        String[] cols = {"#", "PILOT NAME", "SCORE", "BOSSES", "VICTORY"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        int count = Math.min(leaders.size(), 15);
        for (int i = 0; i < count; i++) {
            PlayerProfile p = leaders.get(i);
            String rank = i == 0 ? "1st" : i == 1 ? "2nd" : i == 2 ? "3rd" : String.valueOf(i + 1);
            model.addRow(new Object[]{rank, p.getProfileName(), p.getFinalScore(), p.getBossesKilled(), p.hasReachedVictory() ? "YES" : "NO"});
        }
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                c.setForeground(col == 0 ? PURPLE : TEXT);
                return c;
            }
        };
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setForeground(TEXT);
        table.setBackground(ROW_ODD);
        table.setGridColor(new Color(40, 20, 70));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(PURPLE);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PURPLE));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        return table;
    }
}