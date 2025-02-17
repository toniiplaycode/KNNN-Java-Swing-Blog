package view;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;

import connection.DBConnection;
import view.reader.NewsFeed;
import view.reader.SignIn;
import view.manager.ListBlog;
import view.manager.ListUsers;
import view.manager.StatisticsBlog;
import view.manager.ProfileAdmin;
import view.manager.ListCategory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.prefs.Preferences;

import org.mindrot.jbcrypt.BCrypt;
import view.manager.Login;

public class MainManage extends JFrame {
    private static final long serialVersionUID = 1L;
    private int userId;
    
    // Constructor nhận userId để quản lý người dùng hiện tại
    public MainManage(int userId) {
        this.userId = userId;
        initRoleSelectionUI();
    }
    
    // Khởi tạo giao diện chọn vai trò (Reader/Manager)
    private void initRoleSelectionUI() {
        setTitle("Chọn giao diện");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Container chính với padding
        JPanel container = new JPanel(new BorderLayout(0, 30));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        // Tiêu đề
        JLabel headerLabel = new JLabel("Chọn giao diện bạn muốn sử dụng", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        // Panel chứa các lựa chọn với khoảng cách
        JPanel optionsPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        optionsPanel.setBackground(Color.WHITE);
        
        // Panel cho lựa chọn Người đọc
        JPanel readerPanel = createOptionPanel(
            "Người đọc",
            "Đọc và tương tác với bài viết, bình luận và xem blog cá nhân",
            "/icons/news.png",
            new Color(52, 152, 219),
            e -> initReaderUI()
        );
        
        // Panel cho lựa chọn Quản lý
        JPanel managerPanel = createOptionPanel(
            "Quản lý",
            "Quản lý bài viết, thể loại, người dùng và xem thống kê",
            "/icons/manage.png",
            new Color(46, 204, 113),
            e -> initManagerUI()
        );
        
        // Thêm các panel vào giao diện
        optionsPanel.add(readerPanel);
        optionsPanel.add(managerPanel);
        
        container.add(headerLabel, BorderLayout.NORTH);
        container.add(optionsPanel, BorderLayout.CENTER);
        
        setContentPane(container);
        setVisible(true);
    }
    
    // Tạo panel tùy chọn với các thông số được truyền vào
    private JPanel createOptionPanel(String title, String description, String iconPath, 
            Color color, java.awt.event.ActionListener action) {
        
        // Tạo panel với border và padding
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Panel chứa nội dung
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Thêm icon
        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            iconLabel.setText(title.substring(0, 1));
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
            iconLabel.setForeground(color);
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Thêm tiêu đề
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Thêm mô tả
        JLabel descLabel = new JLabel("<html><div style='text-align: center; width: 250px; margin: 0 auto; display: block;'>" + 
            description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Panel chứa mô tả để căn giữa tốt hơn
        JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        descPanel.setBackground(Color.WHITE);
        descPanel.add(descLabel);
        
        // Thêm các thành phần vào panel
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(descPanel);
        
        // Tạo và tùy chỉnh nút
        JButton button = new JButton("Chọn");
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 45));
        
        // Thêm hiệu ứng hover cho nút
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        // Thêm action listener cho nút
        button.addActionListener(action);
        
        // Panel chứa nút để căn giữa
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(button);
        
        // Thêm các panel vào panel chính
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Khởi tạo giao diện người đọc
    public void initReaderUI() {
        // Kiểm tra thông tin đăng nhập đã lưu
        String savedEmail = getSavedEmail();
        String savedHashedPassword = getSavedPassword();
        
        // Nếu có thông tin đăng nhập đã lưu, thử đăng nhập tự động
        if (savedEmail != null && savedHashedPassword != null) {
            try {
                Connection connection = DBConnection.getConnection();
                String query = "SELECT * FROM tbl_user WHERE email = ? AND password = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, savedEmail);
                ps.setString(2, savedHashedPassword);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    showNewsFeed(userId);
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        // Nếu không có thông tin đăng nhập hoặc đăng nhập thất bại, hiển thị dưới dạng khách
        showNewsFeed(0);
    }
    
    // Hiển thị giao diện NewsFeed
    public void showNewsFeed(int userId) {
        dispose();
        JFrame newsFeedFrame = new JFrame("News Feed");
        newsFeedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newsFeedFrame.setBounds(100, 100, 1200, 700);
        newsFeedFrame.setLocationRelativeTo(null);
        
        NewsFeed newsFeed = new NewsFeed(userId);
        newsFeedFrame.setContentPane(newsFeed.getContentPane());
        newsFeedFrame.setVisible(true);
    }
    
    // Lấy thông tin đăng nhập đã lưu
    private String getSavedEmail() {
        return Preferences.userRoot().get("reader_email", null);
    }
    
    private String getSavedPassword() {
        return Preferences.userRoot().get("reader_password", null);
    }
    
    // Khởi tạo giao diện quản lý
    public void initManagerUI() {
        // Kiểm tra thông tin đăng nhập admin đã lưu
        String savedEmail = getSavedManagerEmail();
        String savedHashedPassword = getSavedManagerPassword();
        
        if (savedEmail != null && savedHashedPassword != null) {
            try {
                Connection connection = DBConnection.getConnection();
                String query = "SELECT * FROM tbl_admin WHERE email = ? AND password = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, savedEmail);
                ps.setString(2, savedHashedPassword);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    int adminId = rs.getInt("id");
                    showManagerUI(adminId);
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        // Nếu không có thông tin đăng nhập hoặc đăng nhập thất bại, chuyển đến màn hình đăng nhập
        dispose();
        new Login().setVisible(true);
    }

    // Hiển thị giao diện quản lý
    public void showManagerUI(int adminId) {
        // Tạo frame mới cho giao diện quản lý
        JFrame managerFrame = new JFrame("Quản lý");
        managerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        managerFrame.setBounds(100, 100, 1200, 700);
        managerFrame.setLocationRelativeTo(null);
        
        // Tạo panel chứa header và tabbed pane
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header panel với nút chuyển đổi
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tạo nút chuyển đổi sang giao diện người đọc
        JButton btnSwitchToReader = new JButton("Chuyển sang người đọc");
        btnSwitchToReader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSwitchToReader.setForeground(Color.WHITE);
        btnSwitchToReader.setBackground(new Color(52, 152, 219));
        btnSwitchToReader.setBorderPainted(false);
        btnSwitchToReader.setFocusPainted(false);
        btnSwitchToReader.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Thêm icon cho nút
        ImageIcon switchIcon = new ImageIcon(getClass().getResource("/icons/switch.png"));
        Image switchImg = switchIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        btnSwitchToReader.setIcon(new ImageIcon(switchImg));
        btnSwitchToReader.setIconTextGap(8);

        // Thêm action listener cho nút chuyển đổi
        btnSwitchToReader.addActionListener(e -> {
            managerFrame.dispose();
            initReaderUI();
        });
        
        headerPanel.add(btnSwitchToReader);
        
        // Tạo tabbed pane và thêm các tab
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab Quản lý bài viết
        ImageIcon blogIcon = new ImageIcon(getClass().getResource("/icons/blogs.png"));
        Image blogImg = blogIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        tabbedPane.addTab("Quản lý bài viết", new ImageIcon(blogImg), new ListBlog());
        
        // Tab Quản lý thể loại
        ImageIcon categoryIcon = new ImageIcon(getClass().getResource("/icons/category.png"));
        Image categoryImg = categoryIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        tabbedPane.addTab("Quản lý thể loại", new ImageIcon(categoryImg), new ListCategory());
        
        // Tab Thống kê
        ImageIcon statsIcon = new ImageIcon(getClass().getResource("/icons/statistics.png"));
        Image statsImg = statsIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        tabbedPane.addTab("Thống kê", new ImageIcon(statsImg), new StatisticsBlog());
        
        // Tab Quản lý người dùng
        ImageIcon userIcon = new ImageIcon(getClass().getResource("/icons/users.png"));
        Image userImg = userIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        tabbedPane.addTab("Quản lý người dùng", new ImageIcon(userImg), new ListUsers());
        
        // Tab Thông tin cá nhân
        ImageIcon profileIcon = new ImageIcon(getClass().getResource("/icons/profile.png"));
        Image profileImg = profileIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        tabbedPane.addTab("Thông tin cá nhân", new ImageIcon(profileImg), new ProfileAdmin(adminId));
        
        // Thêm các panel vào frame
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        managerFrame.add(mainPanel);
        managerFrame.setVisible(true);
    }

    // Lấy thông tin đăng nhập đã lưu cho quản lý
    private String getSavedManagerEmail() {
        return Preferences.userRoot().get("manager_email", null);
    }

    private String getSavedManagerPassword() {
        return Preferences.userRoot().get("manager_password", null);
    }

    // Phương thức main để chạy ứng dụng
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MainManage(1);
        });
    }
}
