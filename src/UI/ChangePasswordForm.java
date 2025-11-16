package UI;

import database.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ChangePasswordForm extends JDialog {

    private JPasswordField currentPassField, newPassField, confirmPassField;
    private JLabel currentErrorLabel, newErrorLabel, confirmErrorLabel;
    private JButton updateBtn, cancelBtn;
    private String userEmail;
    private JFrame parentFrame;
    private MainMenuUI mainMenuRef;
    private static HashMap<String, String> tempPassDB = new HashMap<>();

    public ChangePasswordForm(JFrame parent, String email, String tempPassword) {
        super(parent, "Change Password", true);
        this.userEmail = email;
        if (parent instanceof MainMenuUI) {
            this.mainMenuRef = (MainMenuUI) parent;
        }
        if (tempPassword != null && !tempPassword.isEmpty()) {
            tempPassDB.put(email, tempPassword);
        }
        setSize(600, 480);
        setLocationRelativeTo(parent);
        setUndecorated(true);

        ImageIcon bgIcon = new ImageIcon("resources/stegBg_img.jpeg");
        Image bgImage = bgIcon.getImage();

        JPanel panel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel lblTitle = new JLabel("Change Your Password", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        JLabel currentLbl = new JLabel("Current Password:");
        currentLbl.setForeground(Color.WHITE);
        gbc.gridx = 0;
        panel.add(currentLbl, gbc);

        currentPassField = createTransparentPasswordField();
        JPanel currentPanel = createPasswordPanel(currentPassField);
        gbc.gridx = 1;
        panel.add(currentPanel, gbc);

        currentErrorLabel = createErrorLabel();
        gbc.gridy++;
        gbc.gridx = 1;
        panel.add(currentErrorLabel, gbc);

        gbc.gridy++;
        JLabel newLbl = new JLabel("New Password:");
        newLbl.setForeground(Color.WHITE);
        gbc.gridx = 0;
        panel.add(newLbl, gbc);

        newPassField = createTransparentPasswordField();
        JPanel newPanel = createPasswordPanel(newPassField);
        gbc.gridx = 1;
        panel.add(newPanel, gbc);

        newErrorLabel = createErrorLabel();
        gbc.gridy++;
        gbc.gridx = 1;
        panel.add(newErrorLabel, gbc);

        gbc.gridy++;
        JLabel confirmLbl = new JLabel("Confirm Password:");
        confirmLbl.setForeground(Color.WHITE);
        gbc.gridx = 0;
        panel.add(confirmLbl, gbc);

        confirmPassField = createTransparentPasswordField();
        JPanel confirmPanel = createPasswordPanel(confirmPassField);
        gbc.gridx = 1;
        panel.add(confirmPanel, gbc);

        confirmErrorLabel = createErrorLabel();
        gbc.gridy++;
        gbc.gridx = 1;
        panel.add(confirmErrorLabel, gbc);

        gbc.gridy++;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        btnPanel.setOpaque(false);
        updateBtn = createStyledButton("Update", 140, 45);
        cancelBtn = createStyledButton("Cancel", 140, 45);
        updateBtn.setEnabled(false);
        // updateBtn.setForeground(Color.WHITE);
        // cancelBtn.setForeground(Color.WHITE);
        btnPanel.add(updateBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        add(panel);

        addDocumentListeners();
        updateBtn.addActionListener(e -> handleChangePassword());
        cancelBtn.addActionListener(e -> dispose());

        SwingUtilities.invokeLater(this::validateFields);

        setVisible(true);
    }

    private JPasswordField createTransparentPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pf.setEchoChar('\u2022');
        pf.setPreferredSize(new Dimension(250, 35));
        pf.setOpaque(false);
        pf.setBackground(new Color(0, 0, 0, 30));
        pf.setForeground(Color.WHITE);
        pf.setCaretColor(Color.WHITE);
        return pf;
    }

    // Eye toggle with professional icons (open/close eye)
    private JPanel createPasswordPanel(JPasswordField pf) {
        JButton eyeBtn = new JButton();
        eyeBtn.setFocusable(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setForeground(Color.WHITE);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Unicode characters for open eye 👁 and closed eye 🙈
        final String openEye = "\uD83D\uDC41"; // 👁
        final String closedEye = "\uD83D\uDE48"; // 🙈

        eyeBtn.setText(closedEye);
        pf.setEchoChar('\u2022');

        eyeBtn.addActionListener(e -> {
            if (pf.getEchoChar() != (char) 0) {
                pf.setEchoChar((char) 0);
                eyeBtn.setText(openEye);
            } else {
                pf.setEchoChar('\u2022');
                eyeBtn.setText(closedEye);
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(pf, BorderLayout.CENTER);
        panel.add(eyeBtn, BorderLayout.EAST);
        return panel;
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(Color.RED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return lbl;
    }

    private JButton createStyledButton(String text, int width, int height) {
        JButton btn = new JButton(text) {
           @Override
protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Color c1 = isEnabled() ? new Color(70, 130, 180, 120) : new Color(200, 200, 200, 150);
    Color c2 = isEnabled() ? new Color(100, 180, 220, 120) : new Color(200, 200, 200, 150);
    g2d.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

    // Border
    Color borderColor = new Color(0, 120, 200, 160);
    g2d.setColor(borderColor);
    g2d.setStroke(new BasicStroke(2));
    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);

    // Text with dynamic foreground color (for hover effect)
    g2d.setColor(getForeground());
    g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));

    FontMetrics fm = g2d.getFontMetrics();
    int textWidth = fm.stringWidth(getText());
    int textHeight = fm.getAscent();

    g2d.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 3);

    g2d.dispose();
}



        };
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(width, height));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(new Color(0, 192, 255));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setForeground(new Color(100, 180, 220));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setForeground(new Color(0, 192, 255));
            }
        });

        return btn;
    }

    private void addDocumentListeners() {
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateFields(); }
            public void removeUpdate(DocumentEvent e) { validateFields(); }
            public void changedUpdate(DocumentEvent e) { validateFields(); }
        };
        currentPassField.getDocument().addDocumentListener(listener);
        newPassField.getDocument().addDocumentListener(listener);
        confirmPassField.getDocument().addDocumentListener(listener);
    }

    private void validateFields() {
        String current = new String(currentPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        currentErrorLabel.setText(" ");
        newErrorLabel.setText(" ");
        confirmErrorLabel.setText(" ");

        boolean valid = true;

        if (current.isEmpty()) {
            currentErrorLabel.setText("Current password required!");
            currentErrorLabel.setForeground(Color.RED);
            valid = false;
        } else {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT password_hash FROM users WHERE email=?");
                ps.setString(1, userEmail);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String hashCurrent = hashPassword(current);
                    if (!storedHash.equals(hashCurrent) &&
                            !(tempPassDB.containsKey(userEmail) && tempPassDB.get(userEmail).equals(current))) {
                        currentErrorLabel.setText("Current password is incorrect!");
                        currentErrorLabel.setForeground(Color.RED);
                        valid = false;
                    } else {
                        currentErrorLabel.setText("Password is valid ");
                        currentErrorLabel.setForeground(new Color(0, 128, 0));
                    }
                }
                rs.close();
                ps.close();
            } catch (Exception ignored) {
            }
        }

        if (newPass.isEmpty()) {
            newErrorLabel.setText("New password required!");
            newErrorLabel.setForeground(Color.RED);
            valid = false;
        } else if (newPass.equals(current)) {
            newErrorLabel.setText("New password must differ!");
            newErrorLabel.setForeground(Color.RED);
            valid = false;
        }

        if (confirm.isEmpty()) {
            confirmErrorLabel.setText("Confirm password required!");
            confirmErrorLabel.setForeground(Color.RED);
            valid = false;
        } else if (!newPass.equals(confirm)) {
            confirmErrorLabel.setText("Passwords do not match!");
            confirmErrorLabel.setForeground(Color.RED);
            valid = false;
        } else if (newPass.equals(confirm) && !newPass.isEmpty()) {
            confirmErrorLabel.setText("Password matched ✅");
            confirmErrorLabel.setForeground(new Color(0, 128, 0));
        }

        updateBtn.setEnabled(valid);
    }

    private void handleChangePassword() {
        String current = new String(currentPassField.getPassword());
        String newPass = new String(newPassField.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT password_hash FROM users WHERE email=?");
            ps.setString(1, userEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String currentHash = hashPassword(current);

                boolean validCurrent = storedHash.equals(currentHash) ||
                        (tempPassDB.containsKey(userEmail) && tempPassDB.get(userEmail).equals(current));

                if (!validCurrent) {
                    currentErrorLabel.setText("Current password is incorrect!");
                    return;
                }

                String newHash = hashPassword(newPass);
                PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE users SET password_hash=? WHERE email=?");
                updatePs.setString(1, newHash);
                updatePs.setString(2, userEmail);
                int rows = updatePs.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    if (mainMenuRef != null) {
                        mainMenuRef.setTempPasswordUser(false);
                    }
                    tempPassDB.remove(userEmail);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update password!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                updatePs.close();
            } else {
                JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        return hex.toString();
    }
}
