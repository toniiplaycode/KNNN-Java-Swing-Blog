package view;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class StatisticsBlog extends JPanel {

    private static final long serialVersionUID = 1L;
    private Connection connection;

    public StatisticsBlog() {
        // Set layout to BorderLayout for centering the chart
        setLayout(new BorderLayout());

        // Connect to the database
        connectToDB();

        // Create and add charts
        createCharts();
    }

    private void connectToDB() {
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try {
            // Count number of users
            String userQuery = "SELECT COUNT(*) AS user_count FROM tbl_user";
            PreparedStatement userStatement = connection.prepareStatement(userQuery);
            ResultSet userResultSet = userStatement.executeQuery();
            if (userResultSet.next()) {
                int userCount = userResultSet.getInt("user_count");
                // Update the dataset with the actual number of users
            }

            // Count number of blogs
            String blogQuery = "SELECT COUNT(*) AS blog_count FROM tbl_post";
            PreparedStatement blogStatement = connection.prepareStatement(blogQuery);
            ResultSet blogResultSet = blogStatement.executeQuery();
            if (blogResultSet.next()) {
                int blogCount = blogResultSet.getInt("blog_count");
                // Update the dataset with the actual number of blogs
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCharts() {
        // Create a dataset for the statistics
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Fetch the statistics and update the dataset
        try {
            // Example data; replace with actual database data
            dataset.addValue(10, "Users", "Total Users");
            dataset.addValue(20, "Blogs", "Total Blogs");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Statistics",             // Chart Title
                "Category",               // X-Axis Label
                "Count",                  // Y-Axis Label
                dataset,                  // Dataset
                org.jfree.chart.plot.PlotOrientation.VERTICAL, 
                true,                     // Include legend
                true,                     // Tooltips
                false                     // URLs
        );

        // Create a chart panel to display the chart
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600)); // Adjust the size as needed
        chartPanel.setMouseWheelEnabled(true);  // Enable zoom with mouse wheel

        // Center the chart panel within the parent panel
        add(chartPanel, BorderLayout.CENTER);
    }
}
