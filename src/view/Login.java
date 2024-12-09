package view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import javax.swing.*;
import utils.DBConnection;  // Import DBConnection class

public class Login extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblMessage;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Login frame = new Login();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public Login() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // Email Label
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(50, 50, 100, 25);
        contentPane.add(lblEmail);

        // Email TextField
        txtEmail = new JTextField();
        txtEmail.setBounds(160, 50, 200, 25);
        contentPane.add(txtEmail);
        txtEmail.setColumns(10);

        // Password Label
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(50, 100, 100, 25);
        contentPane.add(lblPassword);

        // Password Field
        txtPassword = new JPasswordField();
        txtPassword.setBounds(160, 100, 200, 25);
        contentPane.add(txtPassword);

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setBounds(160, 150, 100, 30);
        contentPane.add(btnLogin);

        // Error Message Label
        lblMessage = new JLabel("");
        lblMessage.setBounds(50, 200, 300, 25);
        contentPane.add(lblMessage);

        // Thêm hành động khi bấm nút Login
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Lấy thông tin người dùng nhập vào
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());

                // Kiểm tra thông tin đăng nhập với cơ sở dữ liệu
                if (authenticateUser(email, password)) {
                    lblMessage.setText("Login successful!");
                    lblMessage.setForeground(java.awt.Color.GREEN);
                    // Mở giao diện chính của ứng dụng (Ví dụ: MainApp)
                    // new MainApp().setVisible(true);
                } else {
                    lblMessage.setText("Invalid email or password.");
                    lblMessage.setForeground(java.awt.Color.RED);
                }
            }
        });
    }

    /**
     * Kiểm tra thông tin đăng nhập
     */
    private boolean authenticateUser(String email, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Lấy kết nối từ DBConnection
            conn = DBConnection.getConnection();
            if (conn != null) {
                // Truy vấn SQL để kiểm tra thông tin đăng nhập
                String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, password);
                rs = stmt.executeQuery();

                // Kiểm tra kết quả trả về
                return rs.next();  // Nếu có bản ghi, đăng nhập thành công
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Đóng kết nối và các đối tượng sau khi sử dụng
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
