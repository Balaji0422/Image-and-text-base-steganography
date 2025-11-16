package UI;

import sample.SimpleSecureChat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import database.HideAndSeekForm;

public class MainMenuUI extends JFrame {

    private JButton stegoButton, textCryptoButton;
    private JLabel changePasswordLabel, greetingLabel, tempPasswordLabel;
    private JLabel logoutLabel;
    private String userEmail;
    private String firstName;
    private String lastName;
    private boolean isTempPasswordUser;

    public MainMenuUI(String email, String firstName, String lastName, boolean isTempPassword) {
        this.userEmail = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isTempPasswordUser = isTempPassword;

        setTitle("Hide & Seek");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 500));

        // Background panel
        JPanel mainPanel = new JPanel() {
            private Image backgroundImage = new ImageIcon("resources/bg_img.jpg").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // === Top Panel (Greeting) ===
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        greetingLabel = new JLabel("", SwingConstants.CENTER);
        greetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        greetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(greetingLabel);

        tempPasswordLabel = new JLabel("You are using a temporary password. Please change it!");
        tempPasswordLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tempPasswordLabel.setForeground(Color.RED);
        tempPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (isTempPasswordUser) {
            topPanel.add(Box.createVerticalStrut(10));
            topPanel.add(tempPasswordLabel);
        }

        startGreetingFadeIn(greetingLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // === Center Panel (Buttons + Images + Change Password) ===
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20); // spacing
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Buttons + Images panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0)); // side by side

        // --- Text Encryption Panel (Image + Button) ---
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        ImageIcon textIcon = new ImageIcon(new ImageIcon("resources/crypt_img.jpg")
                .getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));
        JLabel textImageLabel = new JLabel(textIcon);
        textImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textCryptoButton = createStyledButton("Encryption / Decryption");
        textCryptoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        textCryptoButton.setMaximumSize(new Dimension(300, 60)); // force size
        textCryptoButton.setPreferredSize(new Dimension(300, 60));
        textPanel.add(textImageLabel);
        textPanel.add(Box.createVerticalStrut(10)); // space between image and button
        textPanel.add(textCryptoButton);

        // --- Stego Panel (Image + Button) ---
        JPanel stegoPanel = new JPanel();
        stegoPanel.setOpaque(false);
        stegoPanel.setLayout(new BoxLayout(stegoPanel, BoxLayout.Y_AXIS));
        ImageIcon stegoIcon = new ImageIcon(new ImageIcon("resources/steg_img.jpg")
                .getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));
        JLabel stegoImageLabel = new JLabel(stegoIcon);
        stegoImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stegoButton = createStyledButton("Image Steganography");
        stegoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stegoButton.setMaximumSize(new Dimension(300, 60)); // force size
        stegoButton.setPreferredSize(new Dimension(300, 60));
        stegoPanel.add(stegoImageLabel);
        stegoPanel.add(Box.createVerticalStrut(10));
        stegoPanel.add(stegoButton);

        buttonPanel.add(textPanel);
        buttonPanel.add(stegoPanel);
        centerPanel.add(buttonPanel, gbc);

        // Button actions
        textCryptoButton.addActionListener(e -> {
            try {
                SimpleSecureChat textUI = new SimpleSecureChat(this);
                textUI.setVisible(true);
                this.setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error opening Text Encryption window.\n" + ex.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        stegoButton.addActionListener(e -> {
            try {
                SteganographySwingUI stegoUI = new SteganographySwingUI(this);
                stegoUI.setVisible(true);
                this.setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error opening Steganography window.\n" + ex.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Change Password label
        gbc.gridy = 1;
        changePasswordLabel = new JLabel("<html><u>Change Password</u></html>");
        changePasswordLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        changePasswordLabel.setForeground(new Color(0, 192, 255));
        changePasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new ChangePasswordForm(MainMenuUI.this, userEmail, null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainMenuUI.this,
                            "Unable to open Change Password window.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) { changePasswordLabel.setForeground(new Color(150, 220, 255)); }
            @Override
            public void mouseExited(MouseEvent e) { changePasswordLabel.setForeground(new Color(0, 192, 255)); }
        });
        centerPanel.add(changePasswordLabel, gbc);

        // Logout label below Change Password
        gbc.gridy = 2;
        logoutLabel = new JLabel("<html><u>Logout</u></html>");
        logoutLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        logoutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateLogoutLabelState(); // sets initial state based on temp password

        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isTempPasswordUser) { // Only allow logout if not temp password
                    try {
                        new HideAndSeekForm().setVisible(true);
                        MainMenuUI.this.dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainMenuUI.this,
                                "Error opening Login page.\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isTempPasswordUser) logoutLabel.setForeground(new Color(150, 220, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isTempPasswordUser) logoutLabel.setForeground(new Color(0, 192, 255));
            }
        });
        centerPanel.add(logoutLabel, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);
    }

    // Helper to update logout label enabled/disabled look and tooltip
    private void updateLogoutLabelState() {
        if (isTempPasswordUser) {
            logoutLabel.setEnabled(false);
            logoutLabel.setForeground(Color.GRAY);
            logoutLabel.setToolTipText("You must change your password before logging out.");
        } else {
            logoutLabel.setEnabled(true);
            logoutLabel.setForeground(new Color(0, 192, 255));
            logoutLabel.setToolTipText(null);
        }
    }

    public void setTempPasswordUser(boolean isTemp) {
        this.isTempPasswordUser = isTemp;
        if (tempPasswordLabel != null) {
            tempPasswordLabel.setVisible(isTemp);
        }
        updateLogoutLabelState();
    }

    private void startGreetingFadeIn(JLabel label) {
        final float[] opacity = {0f};
        Timer timer = new Timer(50, null);
        timer.addActionListener(e -> {
            if (opacity[0] < 1f) {
                opacity[0] += 0.05f;
                int hour = LocalTime.now().getHour();
                String greeting;
                if (hour < 12) greeting = "Good Morning, ";
                else if (hour < 18) greeting = "Good Afternoon, ";
                else greeting = "Good Evening, ";
                String htmlText = "<html>" +
                        "<span style='color:rgba(255,255,255," + opacity[0] + ");'>" + greeting + "</span>" +
                        "<span style='color:rgba(100, 255, 255," + opacity[0] + ");'>" + firstName + " " + lastName + "</span>" +
                        "<span style='color:rgba(255,255,255," + opacity[0] + ");'>!</span></html>";
                label.setText(htmlText);
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setComposite(AlphaComposite.SrcOver.derive(0f));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setColor(new Color(70, 130, 180));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2d.setColor(new Color(0, 192, 255));
                g2d.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 4);
                g2d.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(300, 60));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(100,180,220)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setForeground(new Color(0,192,255)); }
        });
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenuUI("test@example.com", "Balaji", "R", true));
    }
}
