package view.reader;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

public class SignUp extends JFrame {
    // Khai báo các biến thành viên
    private JTextField txtUsername, txtEmail, txtAddress, txtAvatar; // Các trường nhập liệu
    private JPasswordField txtPassword, txtConfirmPassword; // Các trường nhập mật khẩu
    private JRadioButton rbtnMale, rbtnFemale; // Radio button chọn giới tính
    private ButtonGroup genderGroup; // Nhóm radio button giới tính
    private JButton btnRegister, btnBack; // Các nút đăng ký và quay lại
    private Connection connection; // Kết nối database
    
    // Khởi tạo giao diện đăng ký
    public SignUp() {
        // Thiết lập cấu hình cửa sổ
        setTitle("Tạo tài khoản");
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
        JLabel lblTitle = new JLabel("Tạo tài khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(51, 51, 51));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerPanel.add(lblTitle);
        registerPanel.add(Box.createVerticalStrut(20));
        
        // Thêm các trường nhập liệu
        addFormField(registerPanel, "Username", txtUsername = new JTextField());
        addFormField(registerPanel, "Email", txtEmail = new JTextField());
        addFormField(registerPanel, "Mật khẩu", txtPassword = new JPasswordField());
        addFormField(registerPanel, "Nhập lại mật khẩu", txtConfirmPassword = new JPasswordField());
        addFormField(registerPanel, "Địa chỉ", txtAddress = new JTextField());
        addFormField(registerPanel, "Avatar URL", txtAvatar = new JTextField());
        
        // Gender
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        genderPanel.setOpaque(false);
        rbtnMale = new JRadioButton("Nam");
        rbtnFemale = new JRadioButton("Nữ");
        genderGroup = new ButtonGroup();
        genderGroup.add(rbtnMale);
        genderGroup.add(rbtnFemale);
        genderPanel.add(rbtnMale);
        genderPanel.add(rbtnFemale);
        
        JPanel genderContainer = new JPanel(new BorderLayout());
        genderContainer.setOpaque(false);
        genderContainer.add(new JLabel("Giới tính:"), BorderLayout.NORTH);
        genderContainer.add(genderPanel, BorderLayout.CENTER);
        registerPanel.add(genderContainer);
        registerPanel.add(Box.createVerticalStrut(15));
        
        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setOpaque(false);
        
        btnRegister = new JButton("Đăng ký");
        styleButton(btnRegister, new Color(46, 204, 113));
        btnRegister.addActionListener(e -> register());
        
        btnBack = new JButton("Chuyển sang đăng nhập");
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
    
    // Thêm trường dữ liệu vào form
    private void addFormField(JPanel panel, String label, JTextField field) {
        // Tạo container cho trường dữ liệu
        JPanel container = new JPanel(new BorderLayout(10, 5));
        container.setOpaque(false);
        
        // Tạo và định dạng label
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Định dạng trường nhập liệu
        field.setPreferredSize(new Dimension(200, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(0, 5, 0, 5)
        ));
        
        // Thêm vào container
        container.add(lbl, BorderLayout.NORTH);
        container.add(field, BorderLayout.CENTER);
        
        // Thêm vào panel chính
        panel.add(container);
        panel.add(Box.createVerticalStrut(15));
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
    
    // Xử lý đăng ký tài khoản
    private void register() {
        // Kiểm tra dữ liệu đầu vào
        if (!validateInput()) return;
        
        try {
            // Kết nối database và thêm người dùng mới
            connection = DBConnection.getConnection();
            String query = "INSERT INTO tbl_user (username, email, password, gender, address, avatar) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            
            // Thiết lập các tham số
            ps.setString(1, txtUsername.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            String hashedPassword = BCrypt.hashpw(new String(txtPassword.getPassword()), BCrypt.gensalt());
            ps.setString(3, hashedPassword);
            ps.setString(4, rbtnMale.isSelected() ? "Male" : "Female");
            ps.setString(5, txtAddress.getText().trim());
            String avatar = txtAvatar.getText().trim();
            ps.setString(6, avatar.isEmpty() ? null : avatar);
            
            // Thực hiện thêm dữ liệu
            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Đăng ký thành công! Vui lòng đăng nhập.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new SignIn().setVisible(true);
            }
        } catch (SQLException ex) {
            showError("Lỗi đăng ký: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // Kiểm tra dữ liệu đầu vào
    private boolean validateInput() {
        // Lấy dữ liệu từ các trường nhập
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        // Kiểm tra các trường bắt buộc
        if (username.isEmpty()) {
            showError("Vui lòng nhập username");
            return false;
        }
        
        if (email.isEmpty()) {
            showError("Vui lòng nhập email");
            return false;
        }
        
        // Kiểm tra định dạng email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Email không hợp lệ");
            return false;
        }
        
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu");
            return false;
        }
        
        // Kiểm tra mật khẩu xác nhận
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp");
            return false;
        }
        
        // Kiểm tra giới tính
        if (!rbtnMale.isSelected() && !rbtnFemale.isSelected()) {
            showError("Vui lòng chọn giới tính");
            return false;
        }
        
        return true;
    }
    
    // Hiển thị thông báo lỗi
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Lỗi",
            JOptionPane.ERROR_MESSAGE
        );
    }
} 