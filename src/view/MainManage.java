package view;

import com.formdev.flatlaf.FlatLightLaf; // Import thư viện FlatLaf
import javax.swing.*;
import java.awt.*;

public class MainManage extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTabbedPane tabbedPane; // JTabbedPane để chứa các tab

    public static void main(String[] args) {
        // Thiết lập FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Áp dụng FlatLaf Light
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                MainManage frame = new MainManage();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MainManage() {
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

        // Tạo các tab "List Users", "List Blogs", "Statistics"
        JPanel listUsersPanel = new ListUsers(); // Giả sử ListUsers đã được định nghĩa đúng
        JPanel listBlogPanel = new ListBlog(); // Giả sử ListBlog đã được định nghĩa đúng
        JPanel statisticsPanel = new StatisticsBlog(); // Giả sử StatisticsBlog đã được định nghĩa đúng

        // Thêm các panel vào JTabbedPane với icon
        tabbedPane.addTab("List Users", new ImageIcon(new ImageIcon(getClass().getResource("/icons/users.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)), listUsersPanel, "Manage Users");
        tabbedPane.addTab("List Blogs", new ImageIcon(new ImageIcon(getClass().getResource("/icons/blogs.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)), listBlogPanel, "Manage Blogs");
        tabbedPane.addTab("Statistics", new ImageIcon(new ImageIcon(getClass().getResource("/icons/statistics.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)), statisticsPanel, "View Statistics");
    }
}
