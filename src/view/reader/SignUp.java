package view.reader;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

public class SignUp extends JFrame {
    private JTextField txtUsername, txtEmail, txtAddress, txtAvatar;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JRadioButton rbtnMale, rbtnFemale;
    private ButtonGroup genderGroup;
    private JButton btnRegister, btnBack;
    private Connection connection;
    
    public SignUp() {
        setTitle("Sign Up");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Panel chính với gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(46, 204, 113);
                Color color2 = new Color(51, 51, 51);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // Panel đăng ký
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new BoxLayout(registerPanel, BoxLayout.Y_AXIS));
        registerPanel.setBackground(new Color(255, 255, 255, 240));
        registerPanel.setBorder(new CompoundBorder(
            new EmptyBorder(20, 40, 20, 40),
            new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(20, 20, 20, 20)
            )
        ));
        
        // Title
        JLabel lblTitle = new JLabel("Create Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(51, 51, 51));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerPanel.add(lblTitle);
        registerPanel.add(Box.createVerticalStrut(20));
        
        // Form fields
        addFormField(registerPanel, "Username", txtUsername = new JTextField());
        addFormField(registerPanel, "Email", txtEmail = new JTextField());
        addFormField(registerPanel, "Password", txtPassword = new JPasswordField());
        addFormField(registerPanel, "Confirm Password", txtConfirmPassword = new JPasswordField());
        addFormField(registerPanel, "Address", txtAddress = new JTextField());
        addFormField(registerPanel, "Avatar URL", txtAvatar = new JTextField());
        
        // Gender
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        genderPanel.setOpaque(false);
        rbtnMale = new JRadioButton("Male");
        rbtnFemale = new JRadioButton("Female");
        genderGroup = new ButtonGroup();
        genderGroup.add(rbtnMale);
        genderGroup.add(rbtnFemale);
        genderPanel.add(rbtnMale);
        genderPanel.add(rbtnFemale);
        
        JPanel genderContainer = new JPanel(new BorderLayout());
        genderContainer.setOpaque(false);
        genderContainer.add(new JLabel("Gender:"), BorderLayout.NORTH);
        genderContainer.add(genderPanel, BorderLayout.CENTER);
        registerPanel.add(genderContainer);
        registerPanel.add(Box.createVerticalStrut(15));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setOpaque(false);
        
        btnRegister = new JButton("Register");
        styleButton(btnRegister, new Color(46, 204, 113));
        btnRegister.addActionListener(e -> register());
        
        btnBack = new JButton("Back to Sign In");
        styleButton(btnBack, new Color(66, 139, 202));
        btnBack.addActionListener(e -> {
            dispose();
            new SignIn().setVisible(true);
        });
        
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBack);
        registerPanel.add(buttonPanel);
        
        mainPanel.add(registerPanel);
        setContentPane(mainPanel);
        pack();
        setSize(400, 700);
        setLocationRelativeTo(null);
    }
    
    private void addFormField(JPanel panel, String label, JTextField field) {
        JPanel container = new JPanel(new BorderLayout(10, 5));
        container.setOpaque(false);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        field.setPreferredSize(new Dimension(200, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(0, 5, 0, 5)
        ));
        
        container.add(lbl, BorderLayout.NORTH);
        container.add(field, BorderLayout.CENTER);
        
        panel.add(container);
        panel.add(Box.createVerticalStrut(15));
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }
    
    private void register() {
        if (!validateInput()) return;
        
        try {
            connection = DBConnection.getConnection();
            String query = "INSERT INTO tbl_user (username, email, password, gender, address, avatar) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            
            ps.setString(1, txtUsername.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            String hashedPassword = BCrypt.hashpw(new String(txtPassword.getPassword()), BCrypt.gensalt());
            ps.setString(3, hashedPassword);
            ps.setString(4, rbtnMale.isSelected() ? "Male" : "Female");
            ps.setString(5, txtAddress.getText().trim());
            String avatar = txtAvatar.getText().trim();
            ps.setString(6, avatar.isEmpty() ? null : avatar);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Registration successful! Please sign in.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new SignIn().setVisible(true);
            }
        } catch (SQLException ex) {
            showError("Error registering user: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private boolean validateInput() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        if (username.isEmpty()) {
            showError("Username is required");
            return false;
        }
        
        if (email.isEmpty()) {
            showError("Email is required");
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return false;
        }
        
        if (password.isEmpty()) {
            showError("Password is required");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }
        
        if (!rbtnMale.isSelected() && !rbtnFemale.isSelected()) {
            showError("Please select a gender");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
} 