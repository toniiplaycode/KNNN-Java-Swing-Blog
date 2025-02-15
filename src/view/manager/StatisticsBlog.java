package view.manager;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import utils.DBConnection;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;

public class StatisticsBlog extends JPanel {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private JLabel lblTotalUsers, lblTotalBlogs;
    private JTable tableUserStats;
    private DefaultTableModel tableModel;

    public StatisticsBlog() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Khởi tạo connection trước
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error connecting to database: " + e.getMessage(),
                "Lỗi cơ sở dữ liệu",
                JOptionPane.ERROR_MESSAGE);
        }
        
        // Panel tổng quan
        JPanel overviewPanel = createOverviewPanel();
        add(overviewPanel, BorderLayout.NORTH);
        
        // Panel biểu đồ
        JPanel chartsPanel = createChartsPanel();
        add(chartsPanel, BorderLayout.CENTER);
        
        // Load dữ liệu
        loadStatistics();
    }
    
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Card tổng số người dùng
        JPanel usersCard = createStatCard("Tổng người dùng", "users.png");
        lblTotalUsers = (JLabel) usersCard.getComponent(1);
        
        // Card tổng số bài viết
        JPanel blogsCard = createStatCard("Tổng bài viết", "blogs.png");
        lblTotalBlogs = (JLabel) blogsCard.getComponent(1);
        
        panel.add(usersCard);
        panel.add(blogsCard);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String iconName) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(100, 100, 100));
        
        // Number
        JLabel lblNumber = new JLabel("0");
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblNumber.setForeground(new Color(52, 152, 219));
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblNumber, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Biểu đồ cột cho số bài viết theo user
        JFreeChart barChart = createBarChart();
        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setBorder(BorderFactory.createTitledBorder("Bài viết theo người dùng"));
        
        // Biểu đồ tròn cho tỷ lệ likes
        JFreeChart pieChart1 = createPieChart("Thống kê lượt thích");
        ChartPanel pieChartPanel1 = new ChartPanel(pieChart1);
        pieChartPanel1.setBorder(BorderFactory.createTitledBorder("Thống kê lượt thích"));
        
        // Biểu đồ tròn cho tỷ lệ comments
        JFreeChart pieChart2 = createPieChart("Thống kê bình luận");
        ChartPanel pieChartPanel2 = new ChartPanel(pieChart2);
        pieChartPanel2.setBorder(BorderFactory.createTitledBorder("Thống kê bình luận"));
        
        panel.add(barChartPanel);
        panel.add(pieChartPanel1);
        panel.add(pieChartPanel2);
        
        return panel;
    }
    
    private JFreeChart createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
            }
            
            // Truy vấn lấy top 10 người dùng có nhiều bài viết nhất
            String query = "SELECT u.username, COUNT(p.id) as post_count " +
                          "FROM tbl_user u LEFT JOIN tbl_post p ON u.id = p.user_id " +
                          "GROUP BY u.id, u.username ORDER BY post_count DESC LIMIT 10";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            // Thêm dữ liệu vào dataset
            while (rs.next()) {
                dataset.addValue(rs.getInt("post_count"), 
                               "Posts", 
                               rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tạo biểu đồ cột: " + e.getMessage(),
                "Lỗi cơ sở dữ liệu",
                JOptionPane.ERROR_MESSAGE);
        }
        
        // Tạo biểu đồ cột
        JFreeChart chart = ChartFactory.createBarChart(
            "Top 10 người dùng theo số bài viết",
            "Người dùng",
            "Số bài viết",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false
        );
        
        // Tùy chỉnh giao diện biểu đồ
        customizeChart(chart);
        return chart;
    }
    
    private JFreeChart createPieChart(String title) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try {
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
            }
            
            String query;
            // Chọn query dựa trên loại biểu đồ
            if (title.contains("Likes")) {
                // Truy vấn đếm số lượt thích cho bài viết của mỗi người dùng
                query = """
                    SELECT u.username, COUNT(*) as count 
                    FROM tbl_user u 
                    LEFT JOIN tbl_post p ON u.id = p.user_id 
                    LEFT JOIN tbl_like l ON p.id = l.post_id 
                    WHERE l.post_id IS NOT NULL
                    GROUP BY u.id, u.username 
                    ORDER BY count DESC 
                    LIMIT 5
                """;
            } else {
                // Truy vấn đếm số bình luận cho bài viết của mỗi người dùng
                query = """
                    SELECT u.username, COUNT(*) as count 
                    FROM tbl_user u 
                    LEFT JOIN tbl_post p ON u.id = p.user_id 
                    LEFT JOIN tbl_comment c ON p.id = c.post_id 
                    WHERE c.post_id IS NOT NULL
                    GROUP BY u.id, u.username 
                    ORDER BY count DESC 
                    LIMIT 5
                """;
            }
            
            // Thực thi truy vấn và thêm dữ liệu vào dataset
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                dataset.setValue(
                    rs.getString("username") + " (" + rs.getInt("count") + ")",
                    rs.getInt("count")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tạo biểu đồ tròn: " + e.getMessage(),
                "Lỗi cơ sở dữ liệu",
                JOptionPane.ERROR_MESSAGE);
        }
        
        // Tạo biểu đồ tròn
        JFreeChart chart = ChartFactory.createPieChart(
            title,
            dataset,
            true,
            true,
            false
        );
        
        // Tùy chỉnh giao diện biểu đồ
        customizeChart(chart);
        return chart;
    }
    
    private void customizeChart(JFreeChart chart) {
        // Thiết lập màu nền cho biểu đồ
        chart.setBackgroundPaint(Color.white);
        
        // Tùy chỉnh plot dựa trên loại biểu đồ
        if (chart.getPlot() instanceof CategoryPlot) {
            // Tùy chỉnh cho biểu đồ cột
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setRangeGridlinePaint(Color.lightGray);
            plot.getRenderer().setSeriesPaint(0, new Color(52, 152, 219));
        } else if (chart.getPlot() instanceof PiePlot) {
            // Tùy chỉnh cho biểu đồ tròn
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
            plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        }
        
        // Tùy chỉnh font chữ cho tiêu đề
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void loadStatistics() {
        try {
            // Đếm tổng số người dùng
            String userQuery = "SELECT COUNT(*) as total FROM tbl_user";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(userQuery);
            if (rs.next()) {
                lblTotalUsers.setText(String.valueOf(rs.getInt("total")));
            }
            
            // Đếm tổng số bài viết
            String blogQuery = "SELECT COUNT(*) as total FROM tbl_post";
            rs = stmt.executeQuery(blogQuery);
            if (rs.next()) {
                lblTotalBlogs.setText(String.valueOf(rs.getInt("total")));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải thống kê: " + e.getMessage(),
                "Lỗi cơ sở dữ liệu",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Làm mới dữ liệu thống kê
    public void refreshStatistics() {
        try {
            // Kiểm tra và tạo kết nối mới nếu cần
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
            }
            
            // Tải lại dữ liệu thống kê
            loadStatistics();
            
            // Cập nhật lại giao diện
            removeAll();
            add(createOverviewPanel(), BorderLayout.NORTH);
            add(createChartsPanel(), BorderLayout.CENTER);
            revalidate();
            repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi làm mới thống kê: " + e.getMessage(),
                "Lỗi cơ sở dữ liệu",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
