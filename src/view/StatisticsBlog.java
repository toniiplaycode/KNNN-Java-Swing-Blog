package view;

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
                "Database Error",
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
        JPanel usersCard = createStatCard("Total Users", "users.png");
        lblTotalUsers = (JLabel) usersCard.getComponent(1);
        
        // Card tổng số bài viết
        JPanel blogsCard = createStatCard("Total Blogs", "blogs.png");
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
        barChartPanel.setBorder(BorderFactory.createTitledBorder("Posts by User"));
        
        // Biểu đồ tròn cho tỷ lệ likes
        JFreeChart pieChart1 = createPieChart("Likes Distribution");
        ChartPanel pieChartPanel1 = new ChartPanel(pieChart1);
        pieChartPanel1.setBorder(BorderFactory.createTitledBorder("Likes Distribution"));
        
        // Biểu đồ tròn cho tỷ lệ comments
        JFreeChart pieChart2 = createPieChart("Comments Distribution");
        ChartPanel pieChartPanel2 = new ChartPanel(pieChart2);
        pieChartPanel2.setBorder(BorderFactory.createTitledBorder("Comments Distribution"));
        
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
            
            String query = "SELECT u.username, COUNT(p.id) as post_count " +
                          "FROM tbl_user u LEFT JOIN tbl_post p ON u.id = p.user_id " +
                          "GROUP BY u.id, u.username ORDER BY post_count DESC LIMIT 10";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                dataset.addValue(rs.getInt("post_count"), 
                               "Posts", 
                               rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error creating bar chart: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Top 10 Users by Posts",
            "Users",
            "Number of Posts",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false
        );
        
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
            if (title.contains("Likes")) {
                // Query đếm số lượt like cho bài viết của mỗi user
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
                // Query đếm số lượt comment cho bài viết của mỗi user
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
                "Error creating pie chart: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            title,
            dataset,
            true,
            true,
            false
        );
        
        customizeChart(chart);
        return chart;
    }
    
    private void customizeChart(JFreeChart chart) {
        // Tùy chỉnh màu sắc và font chữ
        chart.setBackgroundPaint(Color.white);
        
        // Tùy chỉnh plot
        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setRangeGridlinePaint(Color.lightGray);
            plot.getRenderer().setSeriesPaint(0, new Color(52, 152, 219));
        } else if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
            plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        }
        
        // Tùy chỉnh title
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void loadStatistics() {
        try {
            // Load tổng số người dùng
            String userQuery = "SELECT COUNT(*) as total FROM tbl_user";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(userQuery);
            if (rs.next()) {
                lblTotalUsers.setText(String.valueOf(rs.getInt("total")));
            }
            
            // Load tổng số bài viết
            String blogQuery = "SELECT COUNT(*) as total FROM tbl_post";
            rs = stmt.executeQuery(blogQuery);
            if (rs.next()) {
                lblTotalBlogs.setText(String.valueOf(rs.getInt("total")));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading statistics: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Phương thức để refresh thống kê
    public void refreshStatistics() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
            }
            loadStatistics();
            removeAll();
            add(createOverviewPanel(), BorderLayout.NORTH);
            add(createChartsPanel(), BorderLayout.CENTER);
            revalidate();
            repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error refreshing statistics: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
