package sample;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;

import UI.MainMenuUI;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
// import org.apache.poi.xwpf.usermodel.XWPFDocument;
// import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class SimpleSecureChat extends JFrame {

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton copyButton;
    private JButton clearButton;
    private JButton backButton;
    private JFrame parent;
    private JButton selectFileButton;

    private JToggleButton toggleTextMode;
    private JToggleButton toggleFileMode;
    private JPanel fileButtonsPanel;

    private File selectedFile;

    static class NeonShadePanel extends JPanel {
        private final Color shade;

        NeonShadePanel(String title, JComponent comp, Color borderAndText, Color shade) {
            super(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(950, 120));
            if (!title.isEmpty()) {
                setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(borderAndText, 2, true),
                        title,
                        javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 16),
                        borderAndText));
            } else {
                setBorder(BorderFactory.createLineBorder(borderAndText, 2, true));
            }
            add(comp, BorderLayout.CENTER);
            this.shade = shade;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Only this panel paints the translucent background
            g2d.setColor(new Color(30, 30, 30, 180));
            g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 18, 18);
            g2d.setColor(shade);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 18, 18);
            g2d.dispose();
        }
    }

    public SimpleSecureChat() {
        this(null);
    }

    public SimpleSecureChat(JFrame parent) {
        setTitle("Encryption / Decryption");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setForeground(Color.WHITE);
        mainPanel.add(header, BorderLayout.NORTH);

        Color blueNeon = new Color(100, 255, 255);
        Color greenNeon = new Color(0, 255, 180);

        // Input Area - TEXTAREA fully transparent; panel paints background
        inputArea = new JTextArea(6, 64);
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setOpaque(false);
        inputArea.setBackground(new Color(0, 0, 0, 0)); // Fully transparent
        inputArea.setForeground(Color.WHITE);
        inputArea.setCaretColor(Color.WHITE);

        JScrollPane inputScroll = new JScrollPane(inputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inputScroll.setOpaque(false);
        inputScroll.getViewport().setOpaque(false);
        inputScroll.setBorder(BorderFactory.createEmptyBorder());

        NeonShadePanel inputPanel = new NeonShadePanel("Input", inputScroll, blueNeon, blueNeon);

        // Output Area - same treatment
        outputArea = new JTextArea(6, 64);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setOpaque(false);
        outputArea.setBackground(new Color(0, 0, 0, 0));
        outputArea.setForeground(Color.WHITE);
        outputArea.setCaretColor(Color.WHITE);

        JScrollPane outputScroll = new JScrollPane(outputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputScroll.setOpaque(false);
        outputScroll.getViewport().setOpaque(false);
        outputScroll.setBorder(BorderFactory.createEmptyBorder());

        NeonShadePanel outputPanel = new NeonShadePanel("Output", outputScroll, greenNeon, greenNeon);

        // Center Panel GridBagLayout
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.insets = new Insets(12, 0, 12, 0);

        // Toggle Buttons
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        togglePanel.setOpaque(false);
        toggleTextMode = createToggleButton("Text Mode");
        toggleFileMode = createToggleButton("File Mode");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(toggleTextMode);
        modeGroup.add(toggleFileMode);
        toggleTextMode.setSelected(true);
        togglePanel.add(toggleTextMode);
        togglePanel.add(toggleFileMode);
        gbc.gridy = 0;
        gbc.weighty = 0;
        centerPanel.add(togglePanel, gbc);
        toggleTextMode.addActionListener(e -> switchMode(true));
        toggleFileMode.addActionListener(e -> switchMode(false));

        // Input Panel
        gbc.gridy = 1;
        gbc.weighty = 0.1;
        centerPanel.add(inputPanel, gbc);

        // File Buttons Panel
        fileButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileButtonsPanel.setOpaque(false);
        selectFileButton = createStyledButton("Select File");
        selectFileButton.addActionListener(this::handleSelectFile);
        fileButtonsPanel.add(selectFileButton);
        gbc.gridy = 2;
        gbc.weighty = 0;
        centerPanel.add(fileButtonsPanel, gbc);
        fileButtonsPanel.setVisible(false);

        // Encrypt/Decrypt Buttons
        JPanel midButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        midButtonPanel.setOpaque(false);
        encryptButton = createStyledButton("Encrypt");
        decryptButton = createStyledButton("Decrypt");
        encryptButton.addActionListener(this::handleEncryptAction);
        decryptButton.addActionListener(this::handleDecryptAction);
        midButtonPanel.add(encryptButton);
        midButtonPanel.add(decryptButton);
        gbc.gridy = 3;
        gbc.weighty = 0;
        centerPanel.add(midButtonPanel, gbc);

        // Output Panel
        gbc.gridy = 4;
        gbc.weighty = 0.1;
        centerPanel.add(outputPanel, gbc);

        // Password Info
        JPanel pwdInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pwdInfoPanel.setOpaque(false);
        JLabel pwdInfoLabel = new JLabel();
        pwdInfoLabel.setForeground(Color.WHITE);
        pwdInfoPanel.add(pwdInfoLabel);
        gbc.gridy = 5;
        gbc.weighty = 0;
        centerPanel.add(pwdInfoPanel, gbc);

        // Bottom Buttons
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomButtonPanel.setOpaque(false);
        copyButton = createStyledButton("Copy Output");
        clearButton = createStyledButton("Clear");
        backButton = createStyledButton("Back");
        bottomButtonPanel.add(copyButton);
        bottomButtonPanel.add(clearButton);
        bottomButtonPanel.add(backButton);

        copyButton.addActionListener(this::handleCopy);
        clearButton.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
            selectedFile = null;
        });
        backButton.addActionListener(e -> {
            this.dispose();
            if (parent != null) {
                parent.setVisible(true);
                parent.toFront();
            }
        });
        gbc.gridy = 6;
        gbc.weighty = 0;
        centerPanel.add(bottomButtonPanel, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JToggleButton createToggleButton(String text) {
        JToggleButton toggle = new JToggleButton(text);
        toggle.setFocusPainted(false);
        toggle.setPreferredSize(new Dimension(110, 28));
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return toggle;
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
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2d.setColor(new Color(100, 255, 255));
                g2d.drawString(getText(), (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 4);
                g2d.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void switchMode(boolean textModeSelected) {
        fileButtonsPanel.setVisible(!textModeSelected);
        inputArea.setText("");
        outputArea.setText("");
        selectedFile = null;
    }

    private void handleEncryptAction(ActionEvent e) {
        String password = JOptionPane.showInputDialog(this, "Enter password for encryption:", "Password", JOptionPane.PLAIN_MESSAGE);
        if (password == null || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (toggleTextMode.isSelected()) {
            if (inputArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the input text for text-based encryption.", "Input Needed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            handleTextEncrypt(password);
        } else {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "Please select a file for file-based encryption.", "Input Needed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            handleFileEncrypt(password);
        }
    }

    private void handleDecryptAction(ActionEvent e) {
        String password = JOptionPane.showInputDialog(this, "Enter password for decryption:", "Password", JOptionPane.PLAIN_MESSAGE);
        if (password == null || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (toggleTextMode.isSelected()) {
            if (inputArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the encrypted text for text-based decryption.", "Input Needed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            handleTextDecrypt(password);
        } else {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "Please select a file for file-based decryption.", "Input Needed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            handleFileDecrypt(password);
        }
    }

    private void handleSelectFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file");
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath().toLowerCase();
        String fileName = selectedFile.getName().toLowerCase();

        try {
            if (fileName.endsWith(".txt") || fileName.endsWith(".csv") || fileName.endsWith(".java") ||
                    fileName.endsWith(".html") || fileName.endsWith(".xml") || fileName.endsWith(".json") ||
                    fileName.endsWith(".py") || fileName.endsWith(".js") || fileName.endsWith(".md") || fileName.endsWith(".css") ||
                    fileName.endsWith(".jsx") || fileName.endsWith(".tsx") || fileName.endsWith(".json") || fileName.endsWith(".yml") ||
                    fileName.endsWith(".yaml") || fileName.endsWith(".ini") || fileName.endsWith(".log") || fileName.endsWith(".rtf") ||
                    fileName.endsWith(".tex") || fileName.endsWith(".bat") || fileName.endsWith(".sh") || fileName.endsWith(".ipynb") ||
                    fileName.endsWith(".c") || fileName.endsWith(".cpp") || fileName.endsWith(".h") || fileName.endsWith(".hpp") ||
                    fileName.endsWith(".scss") || fileName.endsWith(".go") || fileName.endsWith(".rs") || fileName.endsWith(".swift") ||
                    fileName.endsWith(".kt") || fileName.endsWith(".docx") || fileName.endsWith(".db") || fileName.endsWith(".php") ||
                    fileName.endsWith(".rb") || fileName.endsWith(".pl") || fileName.endsWith(".r") || fileName.endsWith(".sql") ||
                    fileName.endsWith(".sqlite") || fileName.endsWith(".tsv") || fileName.endsWith(".env") || fileName.endsWith(".ts") ||
                    fileName.endsWith(".dart") || fileName.endsWith(".ex")) {
                String content = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath()), StandardCharsets.UTF_8);
                inputArea.setText(content);
            } else if (filePath.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(selectedFile)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String content = stripper.getText(document);
                    inputArea.setText(content);
                }
            } else {
                inputArea.setText("Selected file: " + selectedFile.getAbsolutePath() + " (binary file, will be encrypted directly)");
            }
            JOptionPane.showMessageDialog(this, "File loaded: " + selectedFile.getName(), "File Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            inputArea.setText("");
            selectedFile = null;
        }
    }

    private void handleTextEncrypt(String password) {
        try {
            String inputText = inputArea.getText().trim();
            SecretKeySpec key = new SecretKeySpec(Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 16), "AES");
            String encrypted = encrypt(inputText, key);
            outputArea.setText(encrypted);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Encryption failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTextDecrypt(String password) {
        try {
            String inputText = inputArea.getText().trim();
            SecretKeySpec key = new SecretKeySpec(Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 16), "AES");
            String decrypted = decrypt(inputText, key);
            outputArea.setText(decrypted);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Decryption failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleFileEncrypt(String password) {
        try {
            byte[] keyBytes = Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 16);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            byte[] fileBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(fileBytes);
            File encryptedFile = new File(selectedFile.getParent(), selectedFile.getName() + ".enc");
            java.nio.file.Files.write(encryptedFile.toPath(), encryptedBytes);
            outputArea.setText("✅ File encrypted successfully:\n" + encryptedFile.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Encryption failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleFileDecrypt(String password) {
        try {
            byte[] keyBytes = Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 16);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            byte[] encryptedBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String newFileName = selectedFile.getName().replaceFirst("\\.enc$", "");
            File decryptedFile = new File(selectedFile.getParent(), "decrypted_" + newFileName);
            java.nio.file.Files.write(decryptedFile.toPath(), decryptedBytes);
            outputArea.setText("✅ File decrypted successfully:\n" + decryptedFile.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Decryption failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCopy(ActionEvent e) {
        String text = outputArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to copy.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringSelection ss = new StringSelection(text);
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(ss, ss);
        JOptionPane.showMessageDialog(this, "Copied to clipboard.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private String encrypt(String plainText, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
    }

    private String decrypt(String cipherText, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(cipherText);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new SimpleSecureChat().setVisible(true));
    }
}
