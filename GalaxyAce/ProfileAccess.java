package GalaxyAce;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.UUID;

public class ProfileAccess extends JDialog {

    private static final Color BG         = new Color(8, 5, 25);
    private static final Color PANEL_BG   = new Color(18, 12, 45);
    private static final Color PURPLE     = new Color(180, 130, 255);
    private static final Color TEXT       = new Color(210, 200, 240);
    private static final Color SUBTLE     = new Color(120, 110, 160);

    private final LeaderboardManager manager;
    private PlayerProfile authenticatedProfile = null;

    public ProfileAccess(JFrame parent, LeaderboardManager manager) {
        super(parent, "Galaxy Ace — Profile", true);
        this.manager = manager;
        setSize(340, 310);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(BG);
        initializeUI();
    }

    private void initializeUI() {
        // Header
        JLabel header = new JLabel("SELECT PILOT", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setForeground(PURPLE);
        header.setOpaque(true);
        header.setBackground(new Color(20, 10, 50));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(header, BorderLayout.NORTH);

        // Center buttons
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 0, 30));

        JLabel subLabel = new JLabel("Login or create a new profile", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subLabel.setForeground(SUBTLE);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        MainMenu.GameButton loginButton  = new MainMenu.GameButton("LOGIN",              new Color(60, 40, 140));
        MainMenu.GameButton createButton = new MainMenu.GameButton("CREATE NEW PROFILE", new Color(40, 60, 140));

        loginButton.addActionListener(e  -> openInputDialog(ProfileInputAccess.AccessMode.LOGIN));
        createButton.addActionListener(e -> openInputDialog(ProfileInputAccess.AccessMode.CREATE));

        centerPanel.add(subLabel);
        centerPanel.add(Box.createVerticalStrut(18));
        centerPanel.add(loginButton);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createButton);
        add(centerPanel, BorderLayout.CENTER);

        // Guest section
        JPanel guestPanel = new JPanel();
        guestPanel.setLayout(new BoxLayout(guestPanel, BoxLayout.Y_AXIS));
        guestPanel.setOpaque(false);
        guestPanel.setBorder(BorderFactory.createEmptyBorder(6, 40, 18, 40));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 40, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel guestLabel = new JLabel("or continue without saving", SwingConstants.CENTER);
        guestLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        guestLabel.setForeground(SUBTLE);
        guestLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        MainMenu.GameButton guestButton = new MainMenu.GameButton("PLAY AS GUEST", new Color(80, 30, 80));
        guestButton.addActionListener(this::startAsGuest);

        guestPanel.add(sep);
        guestPanel.add(Box.createVerticalStrut(8));
        guestPanel.add(guestLabel);
        guestPanel.add(Box.createVerticalStrut(8));
        guestPanel.add(guestButton);
        add(guestPanel, BorderLayout.SOUTH);
    }

    private void openInputDialog(ProfileInputAccess.AccessMode mode) {
        ProfileInputAccess inputDialog = new ProfileInputAccess(this, manager, mode);
        inputDialog.setVisible(true);
        PlayerProfile result = inputDialog.getAuthenticatedProfile();
        if (result != null) {
            this.authenticatedProfile = result;
            dispose();
        }
    }

    private void startAsGuest(ActionEvent e) {
        String guestName = "Guest_" + UUID.randomUUID().toString().substring(0, 6);
        authenticatedProfile = new PlayerProfile(guestName);
        showGalaxyDialog(this, "GUEST MODE", "Playing as <b>" + guestName + "</b>.<br>Your progress will not be saved.", new Color(255, 160, 40));
        dispose();
    }

    public PlayerProfile getAuthenticatedProfile() {
        return authenticatedProfile;
    }

    private static void showGalaxyDialog(java.awt.Component parent, String title, String message, Color accent) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBackground(new Color(8, 5, 25));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Title
        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title, javax.swing.SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setForeground(accent);
        titleLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        // Divider
        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(new Color(60, 40, 100));
        sep.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 1));

        // Message
        javax.swing.JLabel msgLabel = new javax.swing.JLabel("<html><div style=\'text-align:center;\'>" + message + "</div></html>",
            javax.swing.SwingConstants.CENTER);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        msgLabel.setForeground(new Color(210, 200, 240));
        msgLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        // OK button
        javax.swing.JButton ok = new javax.swing.JButton("OK");
        ok.setFont(new Font("Arial", Font.BOLD, 13));
        ok.setForeground(Color.WHITE);
        ok.setBackground(new Color(60, 40, 140));
        ok.setFocusPainted(false);
        ok.setBorderPainted(false);
        ok.setOpaque(true);
        ok.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        ok.setPreferredSize(new java.awt.Dimension(100, 34));

        panel.add(titleLabel);
        panel.add(javax.swing.Box.createVerticalStrut(8));
        panel.add(sep);
        panel.add(javax.swing.Box.createVerticalStrut(12));
        panel.add(msgLabel);
        panel.add(javax.swing.Box.createVerticalStrut(16));
        panel.add(ok);

        javax.swing.JDialog dlg = new javax.swing.JDialog(
            (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(parent), title, true);
        dlg.getContentPane().setBackground(new Color(8, 5, 25));
        dlg.setUndecorated(false);
        dlg.setContentPane(panel);
        dlg.pack();
        dlg.setMinimumSize(new java.awt.Dimension(300, 160));
        dlg.setLocationRelativeTo(parent);
        ok.addActionListener(ev -> dlg.dispose());
        dlg.setVisible(true);
    }

}