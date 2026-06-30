package GalaxyAce;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ProfileInputAccess extends JDialog {

    public enum AccessMode { LOGIN, CREATE }

    private static final Color BG       = new Color(8, 5, 25);
    private static final Color FIELD_BG = new Color(20, 14, 48);
    private static final Color PURPLE   = new Color(180, 130, 255);
    private static final Color TEXT     = new Color(210, 200, 240);
    private static final Color SUBTLE   = new Color(120, 110, 160);

    private final LeaderboardManager manager;
    private final AccessMode mode;
    private PlayerProfile authenticatedProfile = null;

    private JTextField nameField;
    private JPasswordField passwordField;

    public ProfileInputAccess(JDialog parent, LeaderboardManager manager, AccessMode mode) {
        super(parent, mode == AccessMode.LOGIN ? "Login" : "Create Profile", true);
        this.manager = manager;
        this.mode = mode;
        setSize(370, 240);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(BG);
        initializeUI();
    }

    private void initializeUI() {
        // Header
        String headerText = mode == AccessMode.LOGIN ? "PILOT LOGIN" : "NEW PILOT PROFILE";
        JLabel header = new JLabel(headerText, SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 17));
        header.setForeground(PURPLE);
        header.setOpaque(true);
        header.setBackground(new Color(20, 10, 50));
        header.setBorder(BorderFactory.createEmptyBorder(14, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(16, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name label + field
        JLabel nameLabel = styledLabel("Pilot Name");
        nameField = styledTextField();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; inputPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; inputPanel.add(nameField, gbc);

        // Password label + field
        JLabel passLabel = styledLabel("Password");
        passwordField = styledPasswordField();
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; inputPanel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; inputPanel.add(passwordField, gbc);

        add(inputPanel, BorderLayout.CENTER);

        // Action button
        String btnText = mode == AccessMode.LOGIN ? "LOGIN" : "CREATE PROFILE";
        MainMenu.GameButton actionButton = new MainMenu.GameButton(btnText, new Color(70, 40, 150));
        actionButton.addActionListener(this::handleAction);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(BG);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        south.add(actionButton);
        add(south, BorderLayout.SOUTH);
    }

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(SUBTLE);
        return label;
    }

    private JTextField styledTextField() {
        JTextField field = new JTextField(15);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT);
        field.setCaretColor(PURPLE);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 50, 150), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField(15);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT);
        field.setCaretColor(PURPLE);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 50, 150), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private void handleAction(ActionEvent e) {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (name.isEmpty() || password.isEmpty()) {
            showError("Name and Password cannot be empty.");
            return;
        }
        if (mode == AccessMode.LOGIN) attemptLogin(name, password);
        else attemptCreate(name, password);
    }

    private void attemptLogin(String name, String password) {
        authenticatedProfile = manager.authenticateProfile(name, password);
        if (authenticatedProfile != null) {
            showGalaxyDialog(this, "LOGIN SUCCESS", "Welcome back,<br><b>" + name + "</b>!", new Color(80, 220, 120));
            dispose();
        } else {
            showError("Login failed. Check your name and password.");
        }
    }

    private void attemptCreate(String name, String password) {
        if (name.length() > 15) { showError("Name too long (max 15 characters)."); return; }
        authenticatedProfile = manager.createNewProfile(name, password);
        if (authenticatedProfile != null) {
            showGalaxyDialog(this, "PROFILE CREATED", "Welcome to Galaxy Ace,<br><b>" + name + "</b>!", new Color(180, 130, 255));
            dispose();
        } else {
            showError("Profile name already taken.");
        }
    }

    private void showError(String msg) {
        showGalaxyDialog(this, "ERROR", msg, new Color(220, 60, 60));
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