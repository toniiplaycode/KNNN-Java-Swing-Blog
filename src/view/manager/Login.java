package view.manager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;
import java.util.prefs.Preferences;
import org.mindrot.jbcrypt.BCrypt;
import view.MainManage;

public class Login extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField txtEmail; // Trường nhập email
    private JPasswordField txtPassword; // Trường nhập mật khẩu
    private JButton btnLogin; // Nút đăng nhập
    private JCheckBox chkRemember; // Checkbox ghi nhớ đăng nhập
    private Connection connection; // Kết nối database
    
    public Login() {
        // Thiết lập cấu hình cửa sổ
        setTitle("Blog Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Tạo panel chính với nền gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(66, 139, 202); // Màu gradient bắt đầu
                Color color2 = new Color(51, 51, 51);   // Màu gradient kết thúc
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // Tạo panel đăng nhập
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
        
        // Thêm logo và tiêu đề
        JLabel lblLogo = new JLabel("Đăng nhập admin");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(new Color(51, 51, 51));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(lblLogo);
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Tạo trường nhập email
        JPanel emailPanel = new JPanel(new BorderLayout(10, 0));
        emailPanel.setOpaque(false);
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtEmail = new JTextField("tyadmin@example.com");
        txtEmail.setPreferredSize(new Dimension(200, 30));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(0, 5, 0, 5)
        ));
        emailPanel.add(lblEmail, BorderLayout.NORTH);
        emailPanel.add(txtEmail, BorderLayout.CENTER);
        loginPanel.add(emailPanel);
        loginPanel.add(Box.createVerticalStrut(15));
        
        // Thêm sự kiện focus để tự động chọn toàn bộ text khi focus vào field
        txtEmail.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtEmail.selectAll();
            }
        });
        
        // Tạo trường nhập mật khẩu
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
        
        // Tạo checkbox ghi nhớ đăng nhập
        JPanel rememberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rememberPanel.setOpaque(false);
        chkRemember = new JCheckBox("Lưu tài khoản");
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkRemember.setOpaque(false);
        rememberPanel.add(chkRemember);
        loginPanel.add(rememberPanel);
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Tạo nút đăng nhập
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(66, 139, 202));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setPreferredSize(new Dimension(200, 35));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm hiệu ứng hover cho nút đăng nhập
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(51, 122, 183));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(66, 139, 202));
            }
        });
        
        // Căn giữa nút đăng nhập
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnLogin);
        loginPanel.add(buttonPanel);
        
        // Thêm panel đăng nhập vào panel chính
        mainPanel.add(loginPanel);
        
        // Thêm sự kiện cho nút đăng nhập
        btnLogin.addActionListener(e -> signIn());
        
        // Thêm sự kiện phím Enter
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
        
        // Thiết lập cửa sổ
        setContentPane(mainPanel);
        pack();
        setSize(400, 500);
        setLocationRelativeTo(null);
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
            String query = "SELECT * FROM tbl_admin WHERE email = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    int adminId = rs.getInt("id");
                    
                    // Lưu thông tin đăng nhập nếu chọn Remember me
                    if (chkRemember.isSelected()) {
                        Preferences prefs = Preferences.userRoot();
                        prefs.put("manager_email", email);
                        prefs.put("manager_password", storedHashedPassword);
                    } else {
                        Preferences prefs = Preferences.userRoot();
                        prefs.remove("manager_email");
                        prefs.remove("manager_password");
                    }
                    
                    // Chuyển đến màn hình chính
                    dispose();
                    new MainManage(adminId).showManagerUI(adminId);
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
    
    // Hàm main khởi động ứng dụng
    public static void main(String[] args) {
        try {
            // Thiết lập giao diện theo hệ điều hành
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Khởi tạo giao diện đăng nhập
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}
