package view;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;
import view.reader.NewsFeed;
import view.reader.SignIn;
import view.manager.ListBlog;
import view.manager.ListUsers;
import view.manager.StatisticsBlog;
import view.manager.ProfileAdmin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.prefs.Preferences;
import utils.DBConnection;

public class MainManage extends JFrame {
    private static final long serialVersionUID = 1L;
    private int userId;
    
    public MainManage(int userId) {
        this.userId = userId;
        initRoleSelectionUI();
    }
    
    private void initRoleSelectionUI() {
        setTitle("Chọn giao diện");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Main container với padding
        JPanel container = new JPanel(new BorderLayout(0, 30));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        // Header
        JLabel headerLabel = new JLabel("Chọn giao diện bạn muốn sử dụng", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        // Panel chứa các options với spacing
        JPanel optionsPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        optionsPanel.setBackground(Color.WHITE);
        
        // Panel cho option Người đọc
        JPanel readerPanel = createOptionPanel(
            "Người đọc",
            "Đọc và tương tác với bài viết, bình luận",
            "/icons/news.png",
            new Color(52, 152, 219),
            e -> initReaderUI()
        );
        
        // Panel cho option Quản lý
        JPanel managerPanel = createOptionPanel(
            "Quản lý",
            "Quản lý bài viết, người dùng và xem thống kê",
            "/icons/manage.png",
            new Color(46, 204, 113),
            e -> initManagerUI()
        );
        
        optionsPanel.add(readerPanel);
        optionsPanel.add(managerPanel);
        
        container.add(headerLabel, BorderLayout.NORTH);
        container.add(optionsPanel, BorderLayout.CENTER);
        
        setContentPane(container);
        setVisible(true);
    }
    
    private JPanel createOptionPanel(String title, String description, String iconPath, 
            Color color, java.awt.event.ActionListener action) {
        
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Icon
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
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel("<html><div style='text-align: center; width: 250px; margin: 0 auto; display: block;'>" + 
            description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Wrap description in a panel for better centering
        JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        descPanel.setBackground(Color.WHITE);
        descPanel.add(descLabel);
        
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(descPanel);
        
        // Button
        JButton button = new JButton("Chọn");
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 45));
        
        // Hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        button.addActionListener(e -> {
            dispose();
            action.actionPerformed(e);
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(button);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Panel hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 2),
                    BorderFactory.createEmptyBorder(29, 29, 29, 29)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)
                ));
            }
        });
        
        return panel;
    }
    
    public void initReaderUI() {
        String savedEmail = getSavedEmail();
        String savedHashedPassword = getSavedPassword();
        
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
        
        dispose();
        new SignIn().setVisible(true);
    }
    
    private void showNewsFeed(int userId) {
        dispose();
        JFrame newsFeedFrame = new JFrame("News Feed");
        newsFeedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newsFeedFrame.setBounds(100, 100, 1200, 700);
        newsFeedFrame.setLocationRelativeTo(null);
        
        NewsFeed newsFeed = new NewsFeed(userId);
        newsFeedFrame.setContentPane(newsFeed.getContentPane());
        newsFeedFrame.setVisible(true);
    }
    
    // Phương thức để lấy thông tin đăng nhập đã lưu
    private String getSavedEmail() {
        return Preferences.userRoot().get("reader_email", null);
    }
    
    private String getSavedPassword() {
        return Preferences.userRoot().get("reader_password", null);
    }
    
    public void initManagerUI() {
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
        
        JButton btnSwitchToReader = new JButton("Chuyển sang người đọc");
        btnSwitchToReader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSwitchToReader.setForeground(Color.WHITE);
        btnSwitchToReader.setBackground(new Color(52, 152, 219));
        btnSwitchToReader.setBorderPainted(false);
        btnSwitchToReader.setFocusPainted(false);
        btnSwitchToReader.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSwitchToReader.addActionListener(e -> {
            managerFrame.dispose();
            initReaderUI();
        });
        
        headerPanel.add(btnSwitchToReader);
        
        // Tạo tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Quản lý bài viết", new ListBlog());
        tabbedPane.addTab("Quản lý người dùng", new ListUsers());
        tabbedPane.addTab("Thống kê", new StatisticsBlog());
        tabbedPane.addTab("Thông tin cá nhân", new ProfileAdmin(userId));
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        managerFrame.add(mainPanel);
        managerFrame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // Giả sử userId = 1 cho test
            new MainManage(1);
        });
    }
}
