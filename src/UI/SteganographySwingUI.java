package UI;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import steganography.SteganographyDecoder;
import steganography.SteganographyEncoder;
import javax.swing.border.TitledBorder;

public class SteganographySwingUI extends JFrame {

    private JLabel imagePreviewLabel;
    private JButton browseButton, encodeButton, decodeButton, clearButton, backButton;
    private JTextArea messageArea, decodedMessageArea;
    private final List<String> ALLOWED_FORMATS = Arrays.asList("png", "gif", "bmp", "tif", "tiff");
    private String currentImagePath = "";
    private JFrame parent;

    public SteganographySwingUI() {
        this(null);
    }

    public SteganographySwingUI(JFrame parent) {
        this.parent = parent;
        initUI();
        setLocationRelativeTo(parent);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // Neon circuit panel with shaded background, used for all main boxes (fills whole area)
    static class NeonShadePanel extends JPanel {
        public NeonShadePanel(String title, JTextArea area, Color borderAndText, Color shade) {
            setLayout(new BorderLayout());
            setOpaque(false);

            area.setOpaque(false);
            area.setBackground(null);
            area.setForeground(Color.WHITE); // White text always
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
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Fill shade from very top (no gap)
            g2d.setColor(shade);
            g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 18, 18);
            g2d.dispose();
        }
    }

    private void initUI() {
        setTitle("Hide & Seek");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        ImageIcon bgIcon = new ImageIcon("resources/stegBg_img.jpeg");
        Image bgImage = bgIcon.getImage();

        JPanel bgPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Steganography Encoder / Decoder", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        mainPanel.add(header, BorderLayout.NORTH);

        // Both left panels: blue neon border and frosted blue shade (no top gap)
        messageArea = new JTextArea();
        decodedMessageArea = new JTextArea();
        decodedMessageArea.setEditable(false);

        Color blueNeon = new Color(100, 255, 255);
        Color blueShade = new Color(100, 255, 255, 45);

        NeonShadePanel secretPanel = new NeonShadePanel("Secret Message", messageArea, blueNeon, blueShade);
        NeonShadePanel decodedPanel = new NeonShadePanel("Decoded Message", decodedMessageArea, blueNeon, blueShade);

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        leftPanel.setOpaque(false);
        leftPanel.add(secretPanel);
        leftPanel.add(decodedPanel);

        // Image selection card: green neon and frosted green shade (fills whole card, no gap)
        imagePreviewLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(500, 200));
        imagePreviewLabel.setOpaque(false);
        imagePreviewLabel.setBackground(null);
        imagePreviewLabel.setBorder(null);
        imagePreviewLabel.setForeground(new Color(0, 255, 180)); // neon green

        browseButton = createGreenStyledButton("Browse Image");
        browseButton.addActionListener(this::browseImage);

        Color greenNeon = new Color(0, 255, 180);
        Color greenShade = new Color(0, 255, 180, 45);

        JPanel imageCardContent = new JPanel(new BorderLayout());
        imageCardContent.setOpaque(false);
        imageCardContent.setBorder(BorderFactory.createEmptyBorder(40, 50, 0, 50));
        imageCardContent.add(imagePreviewLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 24));
        buttonPanel.setOpaque(false);
        buttonPanel.add(browseButton);

        NeonShadePanel previewCard = new NeonShadePanel("", new JTextArea(), greenNeon, greenShade) {
            @Override
            protected void paintComponent(Graphics g) {
                // Fill whole card with shade, neon border on top
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(greenShade);
                g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 18, 18);
                g2d.setColor(greenNeon);
                g2d.setStroke(new BasicStroke(2.7f));
                g2d.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 18, 18);
                g2d.dispose();
            }
        };
        previewCard.setOpaque(false);
        previewCard.setPreferredSize(new Dimension(520, 320));
        previewCard.setLayout(new BorderLayout());
        previewCard.setBorder(null);
        previewCard.add(imageCardContent, BorderLayout.CENTER);
        previewCard.add(buttonPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(previewCard, BorderLayout.CENTER);

        JPanel innerPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        innerPanel.setOpaque(false);
        innerPanel.add(leftPanel);
        innerPanel.add(rightPanel);

        mainPanel.add(innerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setOpaque(false);

        encodeButton = createStyledButton("Encode Message");
        decodeButton = createStyledButton("Decode Message");
        clearButton = createStyledButton("Clear");
        backButton = createStyledButton("Back");

        clearButton.setEnabled(false);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        clearButton.addChangeListener(e -> {
            if (!clearButton.isEnabled()) {
                clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else {
                clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });

        encodeButton.addActionListener(e -> encodeMessage());
        decodeButton.addActionListener(e -> decodeMessage());
        clearButton.addActionListener(e -> clearAll());
        backButton.addActionListener(e -> {
            this.dispose();
            if (parent != null) {
                parent.setVisible(true);
                parent.toFront();
            }
        });

        bottomPanel.add(encodeButton);
        bottomPanel.add(decodeButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(backButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        bgPanel.add(mainPanel, BorderLayout.CENTER);

        setContentPane(bgPanel);
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
                g2d.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 4);

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

    // Green neon styled Browse Image button
    private JButton createGreenStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 255, 180));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2d.setColor(new Color(0, 255, 180));
                g2d.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 4);

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

    private void browseImage(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String ext = getFileExtension(file.getName());
            if (ext == null || !ALLOWED_FORMATS.contains(ext.toLowerCase())) {
                JOptionPane.showMessageDialog(this, "Unsupported format. Use PNG, GIF, BMP, or TIFF.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            currentImagePath = file.getAbsolutePath();
            showImagePreview(currentImagePath);
        }
    }

    private void showImagePreview(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            Image scaled = img.getScaledInstance(imagePreviewLabel.getWidth(),
                    imagePreviewLabel.getHeight(), Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(scaled));
            imagePreviewLabel.setText("");
        } catch (Exception ex) {
            imagePreviewLabel.setText("Error loading image");
        }
    }

    private String getFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : null;
    }

    private void encodeMessage() {
        if (currentImagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an image first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = messageArea.getText();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a secret message.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String password = JOptionPane.showInputDialog(this, "Enter password for encoding:");
        if (password == null || password.isEmpty()) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("encoded_image.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String savePath = chooser.getSelectedFile().getAbsolutePath();

        try {
            SteganographyEncoder.encode(message, password, currentImagePath, savePath);
            JOptionPane.showMessageDialog(this, "Message encoded successfully!");
            clearAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Encoding failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decodeMessage() {
        if (currentImagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an image first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String password = JOptionPane.showInputDialog(this, "Enter password for decoding:");
        if (password == null || password.isEmpty()) return;

        try {
            String decoded = SteganographyDecoder.decode(currentImagePath, password);
            decodedMessageArea.setText(decoded);
            clearButton.setEnabled(true);
            clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Decoding failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAll() {
        messageArea.setText("");
        decodedMessageArea.setText("");
        imagePreviewLabel.setIcon(null);
        imagePreviewLabel.setText("No Image Selected");
        currentImagePath = "";
        clearButton.setEnabled(false);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new SteganographySwingUI().setVisible(true);
        });
    }
}
