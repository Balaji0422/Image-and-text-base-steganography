package database;

// import UI.MainMenuUI;

import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.Constructor;


import UI.MainMenuUI;

public class HideAndSeekForm extends JFrame {

    private HashMap<String, String> tempPassDB = new HashMap<>();
    private PlaceholderTextField firstNameField, lastNameField, signupEmailField, emailField;
    private PlaceholderPasswordField signupPasswordField, signupPasswordConfirmField, passwordField;
    private JButton signupButton, loginButton;

    private JPanel mainPanel, loginPanel, signupPanel;
    private boolean isSignupShown = false;
    private boolean isTempPassword = false;
     private String loggedInEmail; 


    public HideAndSeekForm() {
    setTitle("Hide & Seek");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setMinimumSize(new Dimension(1000, 800));
    setLocationRelativeTo(null);

    // Load background image
    ImageIcon bgIcon = new ImageIcon("resources/bg_img.jpg"); // <- put your image in resources folder
    Image bgImage = bgIcon.getImage();

    JPanel bgPanel = new JPanel() {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw the image to fill the entire panel
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    };
    bgPanel.setLayout(new GridBagLayout());

    // Main panel (your card)
    mainPanel = new JPanel() {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(255, 255, 255, 0));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int screenWidth = getWidth();
            int screenHeight = getHeight();
            int cardWidth = (int) (screenWidth * 0.88);
            int cardHeight = (int) (screenHeight * 0.85);
            cardWidth = Math.min(cardWidth, 2500);
            cardHeight = Math.min(cardHeight, 2500);
            int x = (screenWidth - cardWidth) / 2;
            int y = (screenHeight - cardHeight) / 2;

            g2.fillRoundRect(x, y, cardWidth, cardHeight, 40, 40);

            g2.setColor(new Color(0, 0, 0, 0)); // subtle shadow
            g2.fillRoundRect(x + 3, y + 3, cardWidth, cardHeight, 40, 40);

            g2.dispose();
        }
    };
    mainPanel.setLayout(new GridBagLayout());
    mainPanel.setOpaque(false);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

    buildLoginPanel();
    buildSignupPanel();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.weightx = 1; gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    mainPanel.add(loginPanel, gbc);

    bgPanel.add(mainPanel);
    setContentPane(bgPanel);
    pack();
    setVisible(true);
}



    private void checkSignupFields() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = signupEmailField.getText().trim();
        String password = new String(signupPasswordField.getPassword());
        String passwordConfirm = new String(signupPasswordConfirmField.getPassword());

        boolean allFilled = !firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty()
                && !password.isEmpty() && !passwordConfirm.isEmpty();
        boolean passwordsMatch = password.equals(passwordConfirm);
        boolean emailValid = isValidEmail(email);
        boolean firstNameValid = isValidFirstName(firstName);
        boolean lastNameValid = isValidLastName(lastName);
        boolean passwordValid = isValidSignupPassword(password);

        signupButton.setEnabled(allFilled && passwordsMatch && emailValid && firstNameValid && lastNameValid && passwordValid);
    }

    private void checkLoginFields() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        boolean allFilled = !email.isEmpty() && !password.isEmpty();
        boolean emailValid = isValidEmail(email);
        boolean passwordCorrect = false;

        // Only check password if email is valid and exists
        if (allFilled && emailValid && emailExists(email)) {
            passwordCorrect = authenticateUser(email, hashPassword(password));
        }

        loginButton.setEnabled(allFilled && emailValid && passwordCorrect);
    }

    private boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private boolean registerUser(String firstName, String lastName, String email, String passwordHash) {
        String sql = "INSERT INTO users (first_name, last_name, email, password_hash) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, passwordHash);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    public boolean authenticateUser(String email, String passwordHash) {
        String sql = "SELECT password_hash FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash").equals(passwordHash);
            }
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String email, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.setString(2, email.toLowerCase());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private void buildLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setOpaque(false);
        loginPanel.setLayout(new GridBagLayout());

        JLabel heading = new JLabel("HIDE & SEEK");
        heading.setFont(new Font("SansSerif", Font.BOLD, 40));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setForeground(Color.WHITE); // Steel blue color

        JLabel subheading = new JLabel("ACCESS SECURED - ENTER YOUR CREDENTIALS");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subheading.setAlignmentX(Component.CENTER_ALIGNMENT);
        subheading.setForeground(new Color(100, 180, 255)); // #64B4FF soft sky blue


        emailField = new PlaceholderTextField("Email");
        stylizeField(emailField);

        JLabel emailIndicator = new JLabel(" ");
        emailIndicator.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailIndicator.setForeground(Color.ORANGE);

        emailField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { verifyLoginEmail(emailField, emailIndicator); checkLoginFields(); }
            public void removeUpdate(DocumentEvent e) { verifyLoginEmail(emailField, emailIndicator); checkLoginFields(); }
            public void changedUpdate(DocumentEvent e) { verifyLoginEmail(emailField, emailIndicator); checkLoginFields(); }
        });

        passwordField = new PlaceholderPasswordField("Password");
        stylizeField(passwordField);
        passwordField.setEnabled(false); // Initially disable password field

        JLabel passwordIndicator = new JLabel(" ");
        passwordIndicator.setFont(new Font("SansSerif", Font.PLAIN, 12));
        passwordIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { verifyLoginPassword(passwordField, passwordIndicator); checkLoginFields(); }
            public void removeUpdate(DocumentEvent e) { verifyLoginPassword(passwordField, passwordIndicator); checkLoginFields(); }
            public void changedUpdate(DocumentEvent e) { verifyLoginPassword(passwordField, passwordIndicator); checkLoginFields(); }
        });

        loginButton = new JButton("Login");
        loginButton.setEnabled(false);
        stylizePurpleButton(loginButton);
        loginButton.setForeground(Color.BLACK);

        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setFont(new Font("SansSerif", Font.PLAIN, 13));
        showPass.setOpaque(false);
        showPass.setForeground(Color.WHITE); // ← Add this line
        showPass.addActionListener(e -> passwordField.setEchoChar(showPass.isSelected() ? (char) 0 : '•'));

        JPanel showPassPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        showPassPanel.setOpaque(false);
        showPassPanel.add(showPass);

        JPanel forgotWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        forgotWrapper.setOpaque(false);

        


        JLabel forgotLabelInside = new JLabel("<html><span style='color:#A020F0;'><u>Forgot password?</u></span></html>");
forgotLabelInside.setFont(new Font("SansSerif", Font.PLAIN, 14));
forgotLabelInside.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
forgotLabelInside.addMouseListener(new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
        String email = JOptionPane.showInputDialog(mainPanel, "Enter your email to recover password:");
        if (email == null) return; // cancelled

        email = email.trim();

        // 1) Reject uppercase and enforce lowercase-only emails
        //    - must equal its own lowercase
        //    - also basic sanity: not empty and contains '@'
        if (email.isEmpty() || !email.equals(email.toLowerCase()) || !email.contains("@")) {
            JOptionPane.showMessageDialog(
                mainPanel,
                "Please enter a valid email (lowercase only).",
                "Invalid Email",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 2) Continue with your existing logic unchanged
        if (emailExists(email)) {
            String tempPass = generateTempPassword(10);
            if (updatePassword(email, hashPassword(tempPass))) {
                tempPassDB.put(email, tempPass);
                emailField.setText(email);
                passwordField.setText(tempPass);

                JPanel panel = new JPanel(new BorderLayout(5, 5));
                panel.setBackground(Color.WHITE);
                panel.add(new JLabel("Temporary password:(Please copy this temporary password for updating it)"), BorderLayout.NORTH);
                JTextField tempPassField = new JTextField(tempPass);
                tempPassField.setEditable(false);
                panel.add(tempPassField, BorderLayout.CENTER);

                JButton copyBtn = new JButton("Copy");
                copyBtn.addActionListener(a -> {
                    tempPassField.selectAll();
                    tempPassField.copy();
                });
                panel.add(copyBtn, BorderLayout.EAST);

                JOptionPane.showMessageDialog(mainPanel, panel, "Temporary Password", JOptionPane.INFORMATION_MESSAGE);
                JOptionPane.showMessageDialog(mainPanel,
                        "Temporary password auto-filled! Click Login to continue.",
                        "Temporary Login", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Email not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
});
forgotWrapper.add(forgotLabelInside);






        JLabel signupLabel = new JLabel(
                "<HTML><span style='color:white;'>Don’t have an account?</span> <span style='color:#B400E6;text-decoration:underline;cursor:pointer;'>Sign Up\n</span></HTML>");
        signupLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        signupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!isSignupShown) {
                    mainPanel.removeAll();
                    mainPanel.add(signupPanel);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                    isSignupShown = true;
                }
            }
        });

        // Create main content panel with better layout
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Header section
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(heading);
        gbc.gridy = 0;
        contentPanel.add(headerPanel, gbc);

        gbc.gridy = 1;
        contentPanel.add(subheading, gbc);

        // Form fields section with proper spacing
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(3, 6, 3, 6);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.weightx = 1.0;

        // Email field
        formGbc.gridy = 0;
        formPanel.add(emailField, formGbc);

        formGbc.gridy = 1;
        formPanel.add(emailIndicator, formGbc);

        // Password field
        formGbc.gridy = 2;
        formPanel.add(passwordField, formGbc);

        formGbc.gridy = 3;
        formPanel.add(passwordIndicator, formGbc);

        // Show password checkbox
        formGbc.gridy = 4;
        formPanel.add(showPassPanel, formGbc);

        gbc.gridy = 2;
        contentPanel.add(formPanel, gbc);

        // Action buttons section
        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
// actionPanel.setBackground(new Color(102, 178, 255)); // Optional: use same color for panel

        actionPanel.setLayout(new GridBagLayout());

        GridBagConstraints actionGbc = new GridBagConstraints();
        actionGbc.insets = new Insets(4, 8, 4, 8);
        actionGbc.fill = GridBagConstraints.HORIZONTAL;
        actionGbc.weightx = 1.0;

        actionGbc.gridy = 0;
        actionPanel.add(loginButton, actionGbc);

        actionGbc.gridy = 1;
        actionPanel.add(forgotWrapper, actionGbc);

        actionGbc.gridy = 2;
        actionPanel.add(signupLabel, actionGbc);

        gbc.gridy = 3;
        contentPanel.add(actionPanel, gbc);

        // Add content panel to login panel
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0; mainGbc.gridy = 0;
        mainGbc.weightx = 1; mainGbc.weighty = 1;
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.insets = new Insets(40, 40, 40, 40);
        loginPanel.add(contentPanel, mainGbc);


Color semiTransparentBtn = new Color(40, 70, 70); // 10% opacity sky blue

loginButton.setOpaque(true);
loginButton.setContentAreaFilled(true);
loginButton.setBorderPainted(true); // optional, keeps border
loginButton.setBackground(semiTransparentBtn);
loginButton.setForeground(Color.WHITE);
loginButton.setFocusPainted(false);
loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
loginButton.setPreferredSize(new Dimension(150, 35)); // narrower width





        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Check if email is registered
            if (!emailExists(email)) {
                JOptionPane.showMessageDialog(mainPanel, "Email is not registered, please do the sign-up process.", "Email Not Registered",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if password is valid
            if (!authenticateUser(email, hashPassword(password))) {
                JOptionPane.showMessageDialog(mainPanel, "Password is invalid.", "Invalid Password",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tempPassDB.containsKey(email) && tempPassDB.get(email).equals(password)) {
                isTempPassword = true;
                loggedInEmail = email;
                tempPassDB.remove(email);
                showLoginSuccessDialog("Logged in using temporary password! Welcome to Hide & Seek!");
                return;
            }

            tempPassDB.remove(email);
            isTempPassword = false;
    loggedInEmail = email;
            showLoginSuccessDialog("Login successful! Welcome to Hide & Seek!");
        });
    }

    private void buildSignupPanel() {
    signupPanel = new JPanel();
    signupPanel.setOpaque(false);
    signupPanel.setLayout(new GridBagLayout());

    JLabel heading = new JLabel("HIDE & SEEK");
    heading.setFont(new Font("SansSerif", Font.BOLD, 40));
    heading.setForeground(Color.WHITE); // Steel blue color

    JLabel subheading = new JLabel("CREATE ACCOUNT - YOUR SECURE JOURNEY BEGINS");
    subheading.setFont(new Font("SansSerif", Font.PLAIN, 15));
    subheading.setForeground(new Color(100, 180, 255)); // #64B4FF soft sky blue

    // subheading.setForeground(Color.WHITE);

    int fieldWidth = 600, labelHeight = 15;

    firstNameField = new PlaceholderTextField("First Name");
    stylizeField(firstNameField);
    JLabel firstNameIndicator = new JLabel();
    firstNameIndicator.setFont(new Font("SansSerif", Font.PLAIN, 12));
    firstNameIndicator.setPreferredSize(new Dimension(fieldWidth, labelHeight));
    firstNameField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { verifyName(firstNameField, firstNameIndicator); checkSignupFields(); }
        public void removeUpdate(DocumentEvent e) { verifyName(firstNameField, firstNameIndicator); checkSignupFields(); }
        public void changedUpdate(DocumentEvent e) { verifyName(firstNameField, firstNameIndicator); checkSignupFields(); }
    });

    lastNameField = new PlaceholderTextField("Last Name");
    stylizeField(lastNameField);
    JLabel lastNameIndicator = new JLabel();
    lastNameIndicator.setFont(new Font("SansSerif", Font.PLAIN, 12));
    lastNameIndicator.setPreferredSize(new Dimension(fieldWidth, labelHeight));
    lastNameField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { verifyName(lastNameField, lastNameIndicator); checkSignupFields(); }
        public void removeUpdate(DocumentEvent e) { verifyName(lastNameField, lastNameIndicator); checkSignupFields(); }
        public void changedUpdate(DocumentEvent e) { verifyName(lastNameField, lastNameIndicator); checkSignupFields(); }
    });

    signupEmailField = new PlaceholderTextField("Email");
    stylizeField(signupEmailField);
    JLabel emailIndicator = new JLabel();
    emailIndicator.setFont(new Font("SansSerif", Font.PLAIN, 12));
    emailIndicator.setPreferredSize(new Dimension(fieldWidth, labelHeight));
    emailIndicator.setForeground(Color.ORANGE);
    signupEmailField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { verifyEmail(signupEmailField, emailIndicator); checkSignupFields(); }
        public void removeUpdate(DocumentEvent e) { verifyEmail(signupEmailField, emailIndicator); checkSignupFields(); }
        public void changedUpdate(DocumentEvent e) { verifyEmail(signupEmailField, emailIndicator); checkSignupFields(); }
    });

    signupPasswordField = new PlaceholderPasswordField("Password");
    stylizeField(signupPasswordField);
    JLabel passwordStrengthLabel = new JLabel();
    passwordStrengthLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    passwordStrengthLabel.setPreferredSize(new Dimension(fieldWidth, labelHeight));

    signupPasswordConfirmField = new PlaceholderPasswordField("Confirm Password");
    stylizeField(signupPasswordConfirmField);
    JLabel passwordMatchLabel = new JLabel();
    passwordMatchLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    passwordMatchLabel.setPreferredSize(new Dimension(fieldWidth, labelHeight));

    DocumentListener passwordMatchListener = new DocumentListener() {
        private void update() {
            String pass = new String(signupPasswordField.getPassword());
            String confirm = new String(signupPasswordConfirmField.getPassword());
            if (pass.isEmpty() && confirm.isEmpty()) {
                passwordMatchLabel.setText("");
            } else if (pass.equals(confirm)) {
                passwordMatchLabel.setForeground(new Color(0, 128, 0));
                passwordMatchLabel.setText("Passwords match");
            } else {
                passwordMatchLabel.setForeground(Color.RED);
                passwordMatchLabel.setText("Passwords do not match");
            }
            checkSignupFields();
        }
        public void insertUpdate(DocumentEvent e) { update(); }
        public void removeUpdate(DocumentEvent e) { update(); }
        public void changedUpdate(DocumentEvent e) { update(); }
    };
    signupPasswordField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { verifyPasswordStrength(signupPasswordField, passwordStrengthLabel); checkSignupFields(); }
        public void removeUpdate(DocumentEvent e) { verifyPasswordStrength(signupPasswordField, passwordStrengthLabel); checkSignupFields(); }
        public void changedUpdate(DocumentEvent e) { verifyPasswordStrength(signupPasswordField, passwordStrengthLabel); checkSignupFields(); }
    });
    signupPasswordField.getDocument().addDocumentListener(passwordMatchListener);
    signupPasswordConfirmField.getDocument().addDocumentListener(passwordMatchListener);

    signupButton = new JButton("Sign Up");
    stylizePurpleButton(signupButton);
    signupButton.setEnabled(false);
    // signupButton.setForeground(Color.WHITE);

Color semiTransparentBtn1 = new Color(40, 70, 70); // 10% opacity sky blue


signupButton.setOpaque(true);
signupButton.setContentAreaFilled(true);
signupButton.setBorderPainted(true);
signupButton.setBackground(semiTransparentBtn1);
signupButton.setForeground(Color.WHITE);
signupButton.setFocusPainted(false);
signupButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
signupButton.setPreferredSize(new Dimension(150, 35)); // narrower width

    
    signupButton.addActionListener(e -> {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = signupEmailField.getText().trim();
        String password = new String(signupPasswordField.getPassword());
        if (emailExists(email)) {
            JOptionPane.showMessageDialog(mainPanel, "Email already exists, please try to login using the same credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (registerUser(firstName, lastName, email, hashPassword(password))) {
            JOptionPane.showMessageDialog(mainPanel, "Account created successfully!");
            emailField.setText(email);
            passwordField.setText(password);
            mainPanel.removeAll();
            mainPanel.add(loginPanel);
            mainPanel.revalidate();
            mainPanel.repaint();
            isSignupShown = false;
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Failed to create account, email is not registered to this portal!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    JCheckBox showPass = new JCheckBox("Show Password");
    showPass.setFont(new Font("SansSerif", Font.PLAIN, 13));
    showPass.setOpaque(false);
    showPass.setForeground(Color.WHITE);
    showPass.addActionListener(e -> {
        boolean show = showPass.isSelected();
        signupPasswordField.setEchoChar(show ? (char) 0 : '•');
        signupPasswordConfirmField.setEchoChar(show ? (char) 0 : '•');
    });

    JLabel backToLogin = new JLabel("<html><span style='color:#B400E6;text-decoration:underline;cursor:pointer;'>Back to Login</span></html>");
    backToLogin.setFont(new Font("SansSerif", Font.PLAIN, 15));
    backToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    backToLogin.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (isSignupShown) {
                mainPanel.removeAll();
                mainPanel.add(loginPanel);
                mainPanel.revalidate();
                mainPanel.repaint();
                isSignupShown = false;
            }
        }
    });

    JPanel contentPanel = new JPanel();
    contentPanel.setOpaque(false);
    contentPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 15, 6, 15);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    headerPanel.setOpaque(false);
    headerPanel.add(heading);
    gbc.gridy = 0; contentPanel.add(headerPanel, gbc);
    gbc.gridy = 1; contentPanel.add(subheading, gbc);

    JPanel formPanel = new JPanel();
    formPanel.setOpaque(false);
    formPanel.setLayout(new GridBagLayout());
    GridBagConstraints formGbc = new GridBagConstraints();
    formGbc.insets = new Insets(2, 10, 2, 10);
    formGbc.fill = GridBagConstraints.HORIZONTAL;
    formGbc.weightx = 1.0;

    formGbc.gridy = 0; formPanel.add(firstNameField, formGbc);
    formGbc.gridy = 1; formPanel.add(firstNameIndicator, formGbc);
    formGbc.gridy = 2; formPanel.add(lastNameField, formGbc);
    formGbc.gridy = 3; formPanel.add(lastNameIndicator, formGbc);
    formGbc.gridy = 4; formPanel.add(signupEmailField, formGbc);
    formGbc.gridy = 5; formPanel.add(emailIndicator, formGbc);
    formGbc.gridy = 6; formPanel.add(signupPasswordField, formGbc);
    formGbc.gridy = 7; formPanel.add(passwordStrengthLabel, formGbc);
    formGbc.gridy = 8; formPanel.add(signupPasswordConfirmField, formGbc);
    formGbc.gridy = 9; formPanel.add(passwordMatchLabel, formGbc);

    JPanel showPasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    showPasswordPanel.setOpaque(false);
    showPasswordPanel.add(showPass);
    formGbc.gridy = 10; formPanel.add(showPasswordPanel, formGbc);

    gbc.gridy = 2; contentPanel.add(formPanel, gbc);

    JPanel actionPanel = new JPanel();
    actionPanel.setOpaque(false);
    actionPanel.setLayout(new GridBagLayout());
    GridBagConstraints actionGbc = new GridBagConstraints();
    actionGbc.insets = new Insets(6, 8, 6, 8);
    actionGbc.fill = GridBagConstraints.HORIZONTAL;
    actionGbc.weightx = 1.0;

    actionGbc.gridy = 0; actionPanel.add(signupButton, actionGbc);
    actionGbc.gridy = 1; actionPanel.add(backToLogin, actionGbc);

    gbc.gridy = 3; contentPanel.add(actionPanel, gbc);

    GridBagConstraints mainGbc = new GridBagConstraints();
    mainGbc.gridx = 0; mainGbc.gridy = 0;
    mainGbc.weightx = 1; mainGbc.weighty = 1;
    mainGbc.fill = GridBagConstraints.BOTH;
    mainGbc.insets = new Insets(40, 40, 40, 40);

    signupPanel.add(contentPanel, mainGbc);
}


    void stylizeField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 200, 240), 2, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        field.setPreferredSize(new Dimension(550, 55)); // 🔹 wider fields
        field.setMaximumSize(new Dimension(650, 55));   // 🔹 allows layout to stretch if needed
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setOpaque(false);  // <-- makes the background transparent
        field.setForeground(Color.WHITE); // text color visible over dark bg
        field.setFont(new Font("SansSerif", Font.PLAIN, 15)); // ← increase input text size
    }

    void stylizeField(JPasswordField field) {
        field.setOpaque(false);  // <-- make transparent
    field.setForeground(Color.WHITE); // text color
    field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 200, 240), 2, true),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
    
    field.setPreferredSize(new Dimension(550, 55)); // same width
    field.setMaximumSize(new Dimension(650, 55));
    
    field.setAlignmentX(Component.CENTER_ALIGNMENT);
    field.setFont(new Font("SansSerif", Font.PLAIN, 15)); // ← increase input text size
}


    void stylizePurpleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setBackground(new Color(102, 178, 255));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(150, 35));
        btn.setPreferredSize(new Dimension(150, 35));

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);




    }

    void verifyEmail(JTextField field, JLabel indicator) {
        String email = field.getText().trim();
        if (email.isEmpty()) {
            indicator.setText(" ");
            indicator.setForeground(Color.GRAY);
        } else if (isValidEmail(email)) {
            indicator.setText("Valid email");
            indicator.setForeground(new Color(0, 150, 0));
        } else {
            indicator.setText("Invalid email");
            indicator.setForeground(Color.RED);
        }
    }

    void verifyLoginEmail(JTextField field, JLabel indicator) {
        String email = field.getText().trim();
        if (email.isEmpty()) {
            indicator.setText(" ");
            indicator.setForeground(Color.GRAY);
            passwordField.setEnabled(false); // Disable password field when email is empty
        } else if (isValidEmail(email)) {
            if (emailExists(email)) {
                indicator.setText("Email is registered");
                indicator.setForeground(new Color(0, 150, 0));
                passwordField.setEnabled(true); // Enable password field for registered email
            } else {
                indicator.setText("Email is not registered, please do the sign-up process");
                indicator.setForeground(Color.RED);
                passwordField.setEnabled(false); // Disable password field for unregistered email
            }
        } else {
            if (email.length() >= 2 && email.length() <= 3) {
                indicator.setText("Invalid email");
                indicator.setForeground(Color.RED);
            } else {
                indicator.setText("Invalid email format");
                indicator.setForeground(Color.RED);
            }
            passwordField.setEnabled(false); // Disable password field for invalid email format
        }
    }

    void verifyLoginPassword(JPasswordField field, JLabel indicator) {
        String password = new String(field.getPassword());
        String email = emailField.getText().trim();

        if (password.isEmpty()) {
            indicator.setText(" ");
            indicator.setForeground(Color.GRAY);
        } else if (email.isEmpty() || !isValidEmail(email) || !emailExists(email)) {
            indicator.setText("Enter email first");
            indicator.setForeground(Color.ORANGE);
        } else {
            String passwordHash = hashPassword(password);
            if (authenticateUser(email, passwordHash)) {
                indicator.setText("Password is valid");
                indicator.setForeground(new Color(0, 150, 0));
            } else {
                indicator.setText("Password is invalid");
                indicator.setForeground(Color.RED);
            }
        }
    }

    void verifyName(JTextField field, JLabel indicator) {
        String name = field.getText().trim();
        if (name.isEmpty()) {
            indicator.setText(" ");
            indicator.setForeground(Color.GRAY);
        } else if (field == firstNameField && isValidFirstName(name)) {
            indicator.setText("Valid first name");
            indicator.setForeground(new Color(0, 150, 0));
        } else if (field == lastNameField && isValidLastName(name)) {
            indicator.setText("Valid last name");
            indicator.setForeground(new Color(0, 150, 0));
        } else if (field == firstNameField) {
            indicator.setText("First name must be at least 2 characters");
            indicator.setForeground(Color.RED);
        } else if (field == lastNameField) {
            indicator.setText("Last name must be at least 1 character");
            indicator.setForeground(Color.RED);
        } else {
            indicator.setText("Enter proper name");
            indicator.setForeground(Color.RED);
        }
    }

    void verifyPasswordStrength(JPasswordField field, JLabel indicator) {
        String password = new String(field.getPassword());
        if (password.isEmpty()) {
            indicator.setText(" ");
            indicator.setForeground(Color.GRAY);
        } else {
            String strength = getPasswordStrength(password);
            indicator.setText(strength);
            indicator.setForeground(getPasswordStrengthColor(strength));
        }
    }

    boolean isValidEmail(String email) {
        // Only allow Gmail addresses with letters (a-z), numbers (0-9), and periods (.)
        String regex = "^[a-z0-9.]+@gmail\\.com$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    boolean isValidName(String name) {
        // Check if name contains only alphabetic characters and spaces
        // Allow names with spaces (like "Mary Jane") but no numbers or special characters
        String regex = "^[a-zA-Z]+(\\s[a-zA-Z]+)*$";
        return Pattern.compile(regex).matcher(name).matches() && name.length() >= 2;
    }

    boolean isValidFirstName(String firstName) {
        // First name must contain only alphabetic characters and spaces, at least 3 characters
        String regex = "^[a-zA-Z]+(\\s[a-zA-Z]+)*$";
        return Pattern.compile(regex).matcher(firstName).matches() && firstName.length() >= 2;
    }

    boolean isValidLastName(String lastName) {
        // Last name must contain only alphabetic characters and spaces, at least 1 character
        String regex = "^[a-zA-Z]+(\\s[a-zA-Z]+)*$";
        return Pattern.compile(regex).matcher(lastName).matches() && lastName.length() >= 1;
    }

    boolean isValidSignupPassword(String password) {
        // Check length requirement (8-20 characters)
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }

        return true;
    }

    String getPasswordStrength(String password) {
        // Check if password meets basic length requirement (8-20 characters)
        if (password.length() < 8 || password.length() > 20) {
            return "⚫ Must be 8-20 characters";
        }

        // Check specific requirements and provide detailed feedback
        StringBuilder missing = new StringBuilder();

        if (!password.matches(".*[a-z].*")) {
            missing.append("lowercase letter, ");
        }
        if (!password.matches(".*[A-Z].*")) {
            missing.append("uppercase letter, ");
        }
        if (!password.matches(".*[0-9].*")) {
            missing.append("number, ");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            missing.append("special character, ");
        }

        if (missing.length() > 0) {
            // Remove trailing comma and space
            String missingReqs = missing.toString().replaceAll(", $", "");
            return "⚫ Missing: " + missingReqs;
        }

        // All requirements met, now check strength
        int score = 0;

        // Length check (within 8-20 range)
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // Character variety checks
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*[0-9].*")) score++; // numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++; // special characters

        // Return strength with emoji
        if (score <= 2) {
            return "🟡 Weak";
        } else if (score <= 4) {
            return "🟠 Medium";
        } else {
            return "🟢 Strong";
        }
    }

    Color getPasswordStrengthColor(String strength) {
        if (strength.contains("Must be")) {
            return Color.RED;
        } else if (strength.contains("Weak")) {
            return Color.ORANGE;
        } else if (strength.contains("Medium")) {
            return Color.BLUE;
        } else if (strength.contains("Strong")) {
            return new Color(0, 150, 0); // Green
        } else {
            return Color.GRAY;
        }
    }

    String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return null; }
    }

    String generateTempPassword(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    private void showLoginSuccessDialog(String message) {
        // Create a custom dialog
        JDialog dialog = new JDialog(this, "Login Successful", true);
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
mainPanel.setBackground(new Color(245, 245, 245)); // dialog background
mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

JLabel messageLabel = new JLabel("Login Successful!");
messageLabel.setForeground(new Color(50, 50, 50));
messageLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

JButton okayButton = new JButton("Okay");
okayButton.setBackground(new Color(30, 215, 96)); // green
okayButton.setForeground(Color.WHITE);
okayButton.setFocusPainted(false);
okayButton.setOpaque(true);
okayButton.setContentAreaFilled(true);

Color hoverColor = new Color(50, 235, 116);
okayButton.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseEntered(java.awt.event.MouseEvent evt) {
        okayButton.setBackground(hoverColor);
    }
    @Override
    public void mouseExited(java.awt.event.MouseEvent evt) {
        okayButton.setBackground(new Color(30, 215, 96));
    }
});

// Add components with GridBagConstraints
GridBagConstraints gbc = new GridBagConstraints();
gbc.gridx = 0;
gbc.gridy = 0;
gbc.insets = new Insets(10, 0, 10, 0);
gbc.anchor = GridBagConstraints.CENTER;
mainPanel.add(messageLabel, gbc);

gbc.gridy = 1;
mainPanel.add(okayButton, gbc);

        // Add action listener to navigate to MainMenuUI
       okayButton.addActionListener(e -> {
    dialog.dispose();

    // Navigate to MainMenuUI using reflection
    SwingUtilities.invokeLater(() -> {
        try {
            // Fetch first name and last name from database
            String firstName = "";
            String lastName = "";
            try (Connection conn = DBConnection.getConnection()) {
                String query = "SELECT first_name, last_name FROM users WHERE email=?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, loggedInEmail);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    firstName = rs.getString("first_name");
                    lastName = rs.getString("last_name");
                }
                rs.close();
                pst.close();
            }

            // Call MainMenuUI constructor with 4 parameters
            Class<?> mainMenuClass = Class.forName("UI.MainMenuUI");
            Constructor<?> constructor = mainMenuClass.getDeclaredConstructor(
                String.class, String.class, String.class, boolean.class
            );
            constructor.newInstance(loggedInEmail, firstName, lastName, isTempPassword);
            this.dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error opening Main Menu: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
});


        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(okayButton, gbc);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static class PlaceholderTextField extends JTextField {
        private String placeholder;
        public PlaceholderTextField(String placeholder) { this.placeholder = placeholder; }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.PLAIN));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = insets.top + fm.getAscent() + ((getHeight() - insets.top - insets.bottom - fm.getHeight()) / 2);
                g2.drawString(placeholder, insets.left + 6, y);
                g2.dispose();
            }
        }
    }

    private static class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;
        public PlaceholderPasswordField(String placeholder) { this.placeholder = placeholder; }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.PLAIN));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = insets.top + fm.getAscent() + ((getHeight() - insets.top - insets.bottom - fm.getHeight()) / 2);
                g2.drawString(placeholder, insets.left + 6, y);
                g2.dispose();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HideAndSeekForm::new);
    }
}
