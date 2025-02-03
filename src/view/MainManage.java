package view;

import com.formdev.flatlaf.FlatLightLaf; // Import thư viện FlatLaf
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Graphics2D;
import java.awt.font.FontMetrics;
import java.net.URL;

public class MainManage extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTabbedPane tabbedPane; // JTabbedPane để chứa các tab
    private int adminId = 1; // Gán giá trị mặc định là 1

    public MainManage() { // Constructor không cần tham số
        setTitle("Blog Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, 700, 500); // Kích thước ban đầu, nhưng sẽ được tối đa hóa
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tối đa hóa cửa sổ

        // Set layout cho contentPane
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        // Tạo JTabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Font của tab
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Tạo các tab với icon
        try {
            // Tạo các panel
            JPanel listUsersPanel = new ListUsers();
            JPanel listBlogPanel = new ListBlog();
            JPanel statisticsPanel = new StatisticsBlog();
            JPanel profilePanel = new ProfileAdmin(adminId);
            
            // Tạo panel NewsFeed và đặt trong JPanel để có thể thêm vào tab
            JPanel newsFeedWrapper = new JPanel(new BorderLayout());
            NewsFeed newsFeed = new NewsFeed(adminId);
            // Lấy contentPane của NewsFeed và thêm vào wrapper
            newsFeedWrapper.add(newsFeed.getContentPane(), BorderLayout.CENTER);

            // Thêm các tab với icon
            tabbedPane.addTab("News Feed",
                getIconOrDefault("/icons/news.png", "news"),
                newsFeedWrapper,
                "View News Feed"
            );
            
            tabbedPane.addTab("List Users", 
                getIconOrDefault("/icons/users.png", "users"), 
                listUsersPanel, 
                "Manage Users"
            );
            
            tabbedPane.addTab("List Blogs", 
                getIconOrDefault("/icons/blogs.png", "blogs"), 
                listBlogPanel, 
                "Manage Blogs"
            );
            
            tabbedPane.addTab("Statistics", 
                getIconOrDefault("/icons/statistics.png", "stats"), 
                statisticsPanel, 
                "View Statistics"
            );
            
            tabbedPane.addTab("Profile", 
                getIconOrDefault("/icons/profile.png", "profile"), 
                profilePanel, 
                "Admin Profile"
            );

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading icons: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon getIconOrDefault(String path, String fallbackText) {
        try {
            URL iconUrl = getClass().getResource(path);
            if (iconUrl != null) {
                return new ImageIcon(new ImageIcon(iconUrl)
                    .getImage()
                    .getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Trả về một icon mặc định nếu không tìm thấy file
        return createDefaultIcon(fallbackText);
    }

    private ImageIcon createDefaultIcon(String text) {
        // Tạo một icon mặc định với chữ
        BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillOval(0, 0, 19, 19);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        String iconText = text.substring(0, 1).toUpperCase();
        g2d.drawString(iconText, 
            10 - fm.stringWidth(iconText)/2, 
            10 + fm.getAscent()/2 - 1);
        g2d.dispose();
        return new ImageIcon(image);
    }

    public static void main(String[] args) {
        // Thiết lập FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Áp dụng FlatLaf Light
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                // Tạo và hiển thị MainManage trực tiếp
                MainManage frame = new MainManage();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
