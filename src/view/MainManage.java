package view;

import com.formdev.flatlaf.FlatLightLaf; // Import thư viện FlatLaf
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainManage extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JPanel cardPanel;  // Panel để chứa các panel khác
    private CardLayout cardLayout;
    private JButton currentActiveButton = null;  // Theo dõi nút đang hoạt động

    public static void main(String[] args) {
        // Thiết lập FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Áp dụng FlatLaf Light
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainManage frame = new MainManage();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MainManage() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, 700, 500); // Kích thước ban đầu, nhưng sẽ được tối đa hóa
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tối đa hóa cửa sổ

        // Set layout cho contentPane
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout()); // Dùng BorderLayout để cho phép mở rộng cardPanel
        setContentPane(contentPane);

        // Set background màu cho contentPane
        contentPane.setBackground(new Color(245, 245, 245)); // Màu nền sáng cho contentPane

        // Panel điều hướng (button panel)
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));  // Bố cục ngang
        navigationPanel.setBackground(new Color(56, 142, 60)); // Màu nền xanh cho panel điều hướng
        contentPane.add(navigationPanel, BorderLayout.NORTH);  // Thêm navigationPanel vào phần trên của contentPane

        // Thiết lập border và style cho các nút điều hướng
        JButton btnListUsers = new JButton("List Users");
        styleButton(btnListUsers);
        btnListUsers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "ListUsers");  // Hiển thị panel "ListUsers"
                setActiveButton(btnListUsers);
            }
        });
        navigationPanel.add(btnListUsers);

        JButton btnListBlogs = new JButton("List Blogs");
        styleButton(btnListBlogs);
        btnListBlogs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "ListBlogs");  // Hiển thị panel "ListBlogs"
                setActiveButton(btnListBlogs);
            }
        });
        navigationPanel.add(btnListBlogs);

        // Thêm nút cho thống kê
        JButton btnStatistics = new JButton("Statistics");
        styleButton(btnStatistics);
        btnStatistics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "Statistics");  // Hiển thị panel "Statistics"
                setActiveButton(btnStatistics);
            }
        });
        navigationPanel.add(btnStatistics);

        // Panel để chứa các nội dung (ListUsers, ListBlogs, Statistics)
        cardPanel = new JPanel();
        contentPane.add(cardPanel, BorderLayout.CENTER); // Thêm cardPanel vào phần giữa của contentPane

        // Khởi tạo CardLayout
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // Tạo các panel ListUsers, ListBlog, và StatisticsBlog
        JPanel listUsersPanel = new ListUsers();  // Giả sử ListUsers đã được định nghĩa đúng
        JPanel listBlogPanel = new ListBlog();  // Giả sử ListBlog đã được định nghĩa đúng
        JPanel statisticsPanel = new StatisticsBlog();  // Giả sử StatisticsBlog đã được định nghĩa đúng

        // Thêm các panel vào cardLayout
        cardPanel.add(listUsersPanel, "ListUsers");
        cardPanel.add(listBlogPanel, "ListBlogs");
        cardPanel.add(statisticsPanel, "Statistics");

        // Đặt nút đầu tiên làm nút đang hoạt động
        setActiveButton(btnListUsers);
    }

    // Phương thức để tạo kiểu cho các nút với đường viền bo tròn và các thuộc tính khác
    private void styleButton(JButton button) {
        button.setBackground(new Color(56, 142, 60)); // Màu nền cho nút
        button.setForeground(Color.WHITE); // Màu chữ trắng
        button.setBorder(new LineBorder(new Color(56, 142, 60), 2, true)); // Đường viền với cạnh bo tròn
        button.setFocusPainted(false); // Loại bỏ hiệu ứng khi nút được chọn
        button.setFont(button.getFont().deriveFont(14f)); // Cỡ chữ cho nút
    }

    // Đặt nút được truyền vào là nút hoạt động bằng cách áp dụng kiểu active
    private void setActiveButton(JButton button) {
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(new Color(56, 142, 60)); // Đặt lại màu nền cho các nút không hoạt động
        }
        currentActiveButton = button;
        button.setBackground(new Color(80, 170, 70)); // Nút đang hoạt động có màu nền xanh đậm hơn
    }
}
