package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainManage extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JPanel cardPanel;  // Panel to hold other panels
    private CardLayout cardLayout;
    private JButton currentActiveButton = null;  // Track the current active button

    public static void main(String[] args) {
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
        setBounds(0, 0, 700, 500); // Initial size, but will be maximized
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize the frame to full screen

        // Set layout for contentPane
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout()); // Use BorderLayout to allow proper expansion of the cardPanel
        setContentPane(contentPane);

        // Set a background color for the content panel
        contentPane.setBackground(new Color(245, 245, 245)); // Light grey background for content pane

        // Navigation panel (button panel)
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));  // Horizontal layout
        navigationPanel.setBackground(new Color(56, 142, 60)); // Green background for navigation panel
        contentPane.add(navigationPanel, BorderLayout.NORTH);  // Add navigation panel to top

        // Set border and style for navigation buttons
        JButton btnListUsers = new JButton("List Users");
        styleButton(btnListUsers);
        btnListUsers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "ListUsers");  // Show "ListUsers" panel
                setActiveButton(btnListUsers);
            }
        });
        navigationPanel.add(btnListUsers);

        JButton btnListBlogs = new JButton("List Blogs");
        styleButton(btnListBlogs);
        btnListBlogs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "ListBlogs");  // Show "ListBlogs" panel
                setActiveButton(btnListBlogs);
            }
        });
        navigationPanel.add(btnListBlogs);

        // Add a button for Statistics
        JButton btnStatistics = new JButton("Statistics");
        styleButton(btnStatistics);
        btnStatistics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "Statistics");  // Show "Statistics" panel
                setActiveButton(btnStatistics);
            }
        });
        navigationPanel.add(btnStatistics);

        // Panel to hold the content (ListUsers, ListBlogs, Statistics)
        cardPanel = new JPanel();
        contentPane.add(cardPanel, BorderLayout.CENTER); // Add cardPanel to center of contentPane

        // Initialize CardLayout
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // Create the ListUsers, ListBlog, and StatisticsBlog panels
        JPanel listUsersPanel = new ListUsers();  // Assuming ListUsers class is properly defined
        JPanel listBlogPanel = new ListBlog();  // Assuming ListBlog class is properly defined
        JPanel statisticsPanel = new StatisticsBlog();  // Assuming StatisticsBlog class is properly defined

        // Add panels to the card layout
        cardPanel.add(listUsersPanel, "ListUsers");
        cardPanel.add(listBlogPanel, "ListBlogs");
        cardPanel.add(statisticsPanel, "Statistics");

        // Optionally, set the initial active button to the first one
        setActiveButton(btnListUsers);
    }

    // Method to style buttons with rounded borders and other properties
    private void styleButton(JButton button) {
        button.setBackground(new Color(56, 142, 60)); // Set background color
        button.setForeground(Color.WHITE); // Set text color
        button.setBorder(new LineBorder(new Color(56, 142, 60), 2, true)); // Set border with rounded edges
        button.setFocusPainted(false); // Remove focus painting
        button.setFont(button.getFont().deriveFont(14f)); // Set font size
    }

    // Set the given button as the active button by applying an active style.
    private void setActiveButton(JButton button) {
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(new Color(56, 142, 60)); // Reset background for inactive buttons
        }
        currentActiveButton = button;
        button.setBackground(new Color(80, 170, 70)); // Highlight the active button (darker green)
    }
}
