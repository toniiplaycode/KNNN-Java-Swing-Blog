package view.reader;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;
import java.util.prefs.Preferences;
import org.mindrot.jbcrypt.BCrypt;
import view.MainManage;

public class SignIn extends JFrame {
    // Khai báo các biến thành viên
    private JTextField txtEmail; // Trường nhập email
    private JPasswordField txtPassword; // Trường nhập mật khẩu
    private JButton btnLogin, btnRegister; // Các nút đăng nhập và đăng ký
    private JCheckBox chkRemember; // Checkbox ghi nhớ đăng nhập
    private Connection connection; // Kết nối database
    
    // Khởi tạo giao diện đăng nhập
    public SignIn() {
        // Thiết lập cấu hình cửa sổ
        setTitle("Đăng nhập");
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
                Color color1 = new Color(66, 139, 202);
                Color color2 = new Color(51, 51, 51);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // Panel đăng nhập
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBackground(new Color(255, 255, 255, 240));
        loginPanel.setBorder(new CompoundBorder(
            new EmptyBorder(20, 40, 20, 40),
            new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(20, 20, 20, 20)
            )
        ));
        
        // Logo/title
        JLabel lblLogo = new JLabel("Đăng nhập");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(new Color(51, 51, 51));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(lblLogo);
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Email field
        JPanel emailPanel = new JPanel(new BorderLayout(10, 0));
        emailPanel.setOpaque(false);
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(200, 30));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(0, 5, 0, 5)
        ));
        emailPanel.add(lblEmail, BorderLayout.NORTH);
        emailPanel.add(txtEmail, BorderLayout.CENTER);
        loginPanel.add(emailPanel);
        loginPanel.add(Box.createVerticalStrut(15));
        
        // Password field
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0));
        passwordPanel.setOpaque(false);
        JLabel lblPassword = new JLabel("Mật khẩu");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(200, 30));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(0, 5, 0, 5)
        ));
        passwordPanel.add(lblPassword, BorderLayout.NORTH);
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        loginPanel.add(passwordPanel);
        loginPanel.add(Box.createVerticalStrut(5));
        
        // Remember me checkbox
        JPanel rememberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rememberPanel.setOpaque(false);
        chkRemember = new JCheckBox("Ghi nhớ tài khoản");
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkRemember.setOpaque(false);
        rememberPanel.add(chkRemember);
        loginPanel.add(rememberPanel);
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Login button
        btnLogin = new JButton("Đăng nhập");
        styleButton(btnLogin, new Color(66, 139, 202));
        btnLogin.addActionListener(e -> signIn());
        
        // Register button
        btnRegister = new JButton("Tạo Tài khoản");
        styleButton(btnRegister, new Color(46, 204, 113));
        btnRegister.addActionListener(e -> {
            dispose();
            new SignUp().setVisible(true);
        });
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        loginPanel.add(buttonPanel);
        
        mainPanel.add(loginPanel);
        setContentPane(mainPanel);
        pack();
        setSize(400, 500);
        setLocationRelativeTo(null);
        
        // Thêm key listener cho phím Enter
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    signIn();
                }
            }
        };
        txtEmail.addKeyListener(enterKeyListener);
        txtPassword.addKeyListener(enterKeyListener);
    }
    
    // Tạo style cho nút
    private void styleButton(JButton button, Color color) {
        // Thiết lập font và màu sắc
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm hiệu ứng hover
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
    
    // Xử lý đăng nhập
    private void signIn() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Kiểm tra dữ liệu nhập vào
        if (email.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập email và mật khẩu");
            return;
        }
        
        // Kiểm tra định dạng email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Định dạng email không hợp lệ");
            return;
        }
        
        try {
            // Kết nối database và kiểm tra thông tin đăng nhập
            connection = DBConnection.getConnection();
            String query = "SELECT * FROM tbl_user WHERE email = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    int userId = rs.getInt("id");
                    
                    // Lưu thông tin đăng nhập nếu chọn Remember me
                    if (chkRemember.isSelected()) {
                        Preferences prefs = Preferences.userRoot();
                        prefs.put("reader_email", email);
                        prefs.put("reader_password", storedHashedPassword);
                    } else {
                        // Xóa thông tin đăng nhập nếu không chọn Remember me
                        Preferences prefs = Preferences.userRoot();
                        prefs.remove("reader_email");
                        prefs.remove("reader_password");
                    }
                    
                    // Chuyển đến màn hình chính
                    dispose();
                    new MainManage(userId).showNewsFeed(userId);
                } else {
                    showError("Email hoặc mật khẩu không đúng");
                }
            } else {
                showError("Email hoặc mật khẩu không đúng");
            }
        } catch (SQLException ex) {
            showError("Lỗi cơ sở dữ liệu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // Hiển thị thông báo lỗi
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Hiển thị màn hình tin tức
    private void showNewsFeed(int userId) {
        JFrame newsFeedFrame = new JFrame("News Feed");
        newsFeedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newsFeedFrame.setBounds(100, 100, 1200, 700);
        newsFeedFrame.setLocationRelativeTo(null);
        
        NewsFeed newsFeed = new NewsFeed(userId);
        newsFeedFrame.setContentPane(newsFeed.getContentPane());
        newsFeedFrame.setVisible(true);
    }
} 