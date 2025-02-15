package view.reader;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

public class AccountSettings extends JDialog {
    // Khai báo các biến thành viên
    private JTextField txtUsername, txtEmail, txtAddress, txtAvatar; // Các trường nhập liệu
    private JPasswordField txtCurrentPassword, txtNewPassword, txtConfirmPassword; // Các trường nhập mật khẩu
    private JRadioButton rbtnMale, rbtnFemale; // Radio button chọn giới tính
    private ButtonGroup genderGroup; // Nhóm radio button giới tính
    private JButton btnUpdate, btnChangePassword; // Các nút chức năng
    private Connection connection; // Kết nối database
    private int userId; // ID người dùng hiện tại
    
    // Constructor của lớp AccountSettings
    public AccountSettings(JFrame parent, int userId) {
        super(parent, "Account Settings", true);
        this.userId = userId;
        
        // Thiết lập layout và kích thước cửa sổ
        setLayout(new BorderLayout(10, 10));
        setSize(800, 600);
        setLocationRelativeTo(parent);
        
        // Tạo panel chính
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Tạo panel thông tin cá nhân và đổi mật khẩu
        JPanel profilePanel = createProfilePanel();
        JPanel passwordPanel = createPasswordPanel();
        
        // Thêm các panel vào panel chính
        mainPanel.add(profilePanel);
        mainPanel.add(passwordPanel);
        add(mainPanel);
        
        // Tải dữ liệu người dùng
        loadUserData();
    }
    
    // Tạo panel thông tin cá nhân
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(createTitledBorder("Thông tin cá nhân"));
        
        // Tạo panel form sử dụng GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Thêm các trường thông tin vào form
        addFormField(formPanel, "Tên người dùng:", txtUsername = new JTextField(), gbc, 0);
        addFormField(formPanel, "Email:", txtEmail = new JTextField(), gbc, 1);
        addFormField(formPanel, "Địa chỉ:", txtAddress = new JTextField(), gbc, 2);
        addFormField(formPanel, "URL ảnh đại diện:", txtAvatar = new JTextField(), gbc, 3);
        
        // Tạo panel chọn giới tính
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rbtnMale = new JRadioButton("Nam");
        rbtnFemale = new JRadioButton("Nữ");
        genderGroup = new ButtonGroup();
        genderGroup.add(rbtnMale);
        genderGroup.add(rbtnFemale);
        genderPanel.add(rbtnMale);
        genderPanel.add(rbtnFemale);
        addFormField(formPanel, "Giới tính:", genderPanel, gbc, 4);
        
        // Tạo nút cập nhật thông tin
        btnUpdate = new JButton("Cập nhật");
        styleButton(btnUpdate, new Color(52, 152, 219));
        btnUpdate.addActionListener(e -> updateProfile());
        
        // Thêm các thành phần vào panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(btnUpdate, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Tạo panel đổi mật khẩu
    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(createTitledBorder("Đổi mật khẩu"));
        
        // Tạo panel form sử dụng GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Thêm các trường mật khẩu vào form
        addFormField(formPanel, "Mật khẩu hiện tại:", 
            txtCurrentPassword = new JPasswordField(), gbc, 0);
        addFormField(formPanel, "Mật khẩu mới:", 
            txtNewPassword = new JPasswordField(), gbc, 1);
        addFormField(formPanel, "Xác nhận mật khẩu:", 
            txtConfirmPassword = new JPasswordField(), gbc, 2);
        
        // Tạo nút đổi mật khẩu
        btnChangePassword = new JButton("Đổi mật khẩu");
        styleButton(btnChangePassword, new Color(46, 204, 113));
        btnChangePassword.addActionListener(e -> changePassword());
        
        // Thêm các thành phần vào panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(btnChangePassword, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Thêm trường dữ liệu vào form
    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        // Thiết lập vị trí và trọng số cho label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.1;
        panel.add(new JLabel(label), gbc);
        
        // Thiết lập vị trí và trọng số cho trường nhập liệu
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        field.setPreferredSize(new Dimension(0, 30));
        panel.add(field, gbc);
    }
    
    // Tạo border có tiêu đề
    private Border createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        );
    }
    
    // Tạo style cho nút
    private void styleButton(JButton button, Color color) {
        // Thiết lập font và màu sắc
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(0, 35));
        
        // Thêm hiệu ứng hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }
    
    // Tải dữ liệu người dùng từ database
    private void loadUserData() {
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT * FROM tbl_user WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            // Đổ dữ liệu vào các trường nhập liệu
            if (rs.next()) {
                txtUsername.setText(rs.getString("username"));
                txtEmail.setText(rs.getString("email"));
                txtAddress.setText(rs.getString("address"));
                txtAvatar.setText(rs.getString("avatar"));
                
                // Thiết lập giới tính
                String gender = rs.getString("gender");
                if ("Male".equals(gender)) {
                    rbtnMale.setSelected(true);
                } else if ("Female".equals(gender)) {
                    rbtnFemale.setSelected(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading user data: " + e.getMessage());
        }
    }
    
    // Cập nhật thông tin cá nhân
    private void updateProfile() {
        // Kiểm tra dữ liệu đầu vào
        if (!validateProfileInput()) return;
        
        try {
            connection = DBConnection.getConnection();
            String query = """
                UPDATE tbl_user 
                SET username = ?, email = ?, gender = ?, address = ?, avatar = ? 
                WHERE id = ?
            """;
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, txtUsername.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, rbtnMale.isSelected() ? "Male" : "Female");
            ps.setString(4, txtAddress.getText().trim());
            String avatar = txtAvatar.getText().trim();
            ps.setString(5, avatar.isEmpty() ? null : avatar);
            ps.setInt(6, userId);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Thông tin cá nhân đã được cập nhật thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error updating profile: " + e.getMessage());
        }
    }
    
    // Đổi mật khẩu
    private void changePassword() {
        // Kiểm tra dữ liệu đầu vào
        if (!validatePasswordInput()) return;
        
        try {
            connection = DBConnection.getConnection();
            
            // Xác thực mật khẩu hiện tại
            String verifyQuery = "SELECT password FROM tbl_user WHERE id = ?";
            PreparedStatement verifyPs = connection.prepareStatement(verifyQuery);
            verifyPs.setInt(1, userId);
            ResultSet rs = verifyPs.executeQuery();
            
            if (rs.next()) {
                String currentPassword = new String(txtCurrentPassword.getPassword());
                if (!BCrypt.checkpw(currentPassword, rs.getString("password"))) {
                    showError("Mật khẩu hiện tại không đúng");
                    return;
                }
            }
            
            // Cập nhật mật khẩu mới
            String newPassword = new String(txtNewPassword.getPassword());
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            String updateQuery = "UPDATE tbl_user SET password = ? WHERE id = ?";
            PreparedStatement updatePs = connection.prepareStatement(updateQuery);
            updatePs.setString(1, hashedPassword);
            updatePs.setInt(2, userId);
            
            int result = updatePs.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Đổi mật khẩu thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                clearPasswordFields();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error changing password: " + e.getMessage());
        }
    }
    
    // Kiểm tra dữ liệu thông tin cá nhân
    private boolean validateProfileInput() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        
        // Kiểm tra tên người dùng
        if (username.isEmpty()) {
            showError("Vui lòng nhập tên người dùng");
            return false;
        }
        
        // Kiểm tra email
        if (email.isEmpty()) {
            showError("Vui lòng nhập email");
            return false;
        }
        
        // Kiểm tra định dạng email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Email không hợp lệ");
            return false;
        }
        
        // Kiểm tra giới tính
        if (!rbtnMale.isSelected() && !rbtnFemale.isSelected()) {
            showError("Vui lòng chọn giới tính");
            return false;
        }
        
        return true;
    }
    
    // Kiểm tra dữ liệu mật khẩu
    private boolean validatePasswordInput() {
        String currentPassword = new String(txtCurrentPassword.getPassword());
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        // Kiểm tra mật khẩu hiện tại
        if (currentPassword.isEmpty()) {
            showError("Vui lòng nhập mật khẩu hiện tại");
            return false;
        }
        
        // Kiểm tra mật khẩu mới
        if (newPassword.isEmpty()) {
            showError("Vui lòng nhập mật khẩu mới");
            return false;
        }
        
        // Kiểm tra xác nhận mật khẩu
        if (confirmPassword.isEmpty()) {
            showError("Vui lòng xác nhận mật khẩu mới");
            return false;
        }
        
        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp nhau
        if (!newPassword.equals(confirmPassword)) {
            showError("Mật khẩu mới không khớp");
            return false;
        }
        
        return true;
    }
    
    // Xóa trắng các trường mật khẩu
    private void clearPasswordFields() {
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }
    
    // Hiển thị thông báo lỗi
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Lỗi",
            JOptionPane.ERROR_MESSAGE);
    }
} 