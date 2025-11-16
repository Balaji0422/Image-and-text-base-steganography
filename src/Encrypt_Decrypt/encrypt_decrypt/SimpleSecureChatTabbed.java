package encrypt_decrypt;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;

public class SimpleSecureChatTabbed extends JFrame {

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton clearInputButton;
    private JButton copyOutputButton;
    private JButton clearOutputButton;
    private JButton backButton;
    private JFrame parent;
    private static final byte[] FIXED_KEY = "SimpleFixedKey12".getBytes(StandardCharsets.UTF_8);

    static class NeonShadePanel extends JPanel {
        public NeonShadePanel(String title, JTextArea area, Color borderAndText, Color shade) {
            setLayout(new BorderLayout());
            setOpaque(false);
            area.setOpaque(false);
            area.setBackground(null);
            area.setForeground(Color.WHITE);
            area.setFont(new Font("Segoe UI", Font.PLAIN, 21));
            area.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            if (!title.isEmpty()) {
                setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(borderAndText, 2, true),
                        title,
                        TitledBorder.CENTER, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 16),
                        borderAndText
                ));
            } else {
                setBorder(BorderFactory.createLineBorder(borderAndText, 2, true));
            }
            add(area, BorderLayout.CENTER);
            this.shade = shade;
        }

        private final Color shade;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(shade);
            g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 18, 18);
            g2d.dispose();
        }
    }

    public SimpleSecureChatTabbed() {
        this(null);
    }

    public SimpleSecureChatTabbed(JFrame parent) {
        setTitle("Hide & Seek");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        this.parent = parent;

        ImageIcon bgIcon = new ImageIcon("resources/stegBg_img.jpeg");
        Image bgImage = bgIcon.getImage();

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20)) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);

        JLabel header = new JLabel("Encryption / Decryption", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        mainPanel.add(header, BorderLayout.NORTH);

        Color blueNeon = new Color(100, 255, 255);
        Color blueShade = new Color(100, 255, 255, 45);

        Color greenNeon = new Color(0, 255, 180);
        Color greenShade = new Color(0, 255, 180, 45);

        inputArea = new JTextArea();
        NeonShadePanel inputPanel = new NeonShadePanel("Input", inputArea, blueNeon, blueShade);
        inputArea.setCaretColor(Color.WHITE);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        NeonShadePanel outputPanel = new NeonShadePanel("Result", outputArea, greenNeon, greenShade);
        outputArea.setCaretColor(Color.WHITE);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(12));

        JPanel midButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        midButtonPanel.setOpaque(false);

        encryptButton = createStyledButton("Encrypt");
        decryptButton = createStyledButton("Decrypt");
        clearInputButton = createStyledButton("Clear Input");
        midButtonPanel.add(encryptButton);
        midButtonPanel.add(decryptButton);
        midButtonPanel.add(clearInputButton);
        centerPanel.add(midButtonPanel);

        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(outputPanel);
        centerPanel.add(Box.createVerticalStrut(12));

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomButtonPanel.setOpaque(false);

        copyOutputButton = createStyledButton("Copy Output");
        clearOutputButton = createStyledButton("Clear Output");
        backButton = createStyledButton("Back");
        bottomButtonPanel.add(copyOutputButton);
        bottomButtonPanel.add(clearOutputButton);
        bottomButtonPanel.add(backButton);
        centerPanel.add(bottomButtonPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        encryptButton.addActionListener(this::handleEncrypt);
        decryptButton.addActionListener(this::handleDecrypt);
        clearInputButton.addActionListener(e -> inputArea.setText(""));
        clearOutputButton.addActionListener(e -> outputArea.setText(""));
        copyOutputButton.addActionListener(this::copyOutputToClipboard);
        backButton.addActionListener(e -> {
            this.dispose();
            if (parent != null) {
                parent.setVisible(true);
                parent.toFront();
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(100, 255, 255));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2d.setColor(new Color(100, 255, 255));
                g2d.drawString(getText(), (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 4);
                g2d.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void handleEncrypt(ActionEvent e) {
        String message = inputArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message to encrypt.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(FIXED_KEY, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
            outputArea.setText(encrypted);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Encryption failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDecrypt(ActionEvent e) {
        String encryptedMessage = inputArea.getText().trim();
        if (encryptedMessage.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please paste the encrypted message.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(FIXED_KEY, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            outputArea.setText(new String(decryptedBytes, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Decryption failed. Possibly corrupted message.", "Error", JOptionPane.ERROR_MESSAGE);
            outputArea.setText("");
        }
    }

    private void copyOutputToClipboard(ActionEvent e) {
        String textToCopy = outputArea.getText();
        if (textToCopy.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Output area is empty.", "Copy Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        StringSelection stringSelection = new StringSelection(textToCopy);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(this, "Output copied to clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new SimpleSecureChatTabbed().setVisible(true));
    }
}
