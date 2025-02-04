package view.reader;

import view.MainManage;
import view.Login; 

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import utils.DBConnection;
import javax.swing.text.html.*;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.geom.Ellipse2D;
import java.util.prefs.Preferences;
import view.reader.HandlePostDialog;

public class NewsFeed extends JFrame {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private int userId; // ID của user đang đăng nhập
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private ImageIcon likeIcon, commentIcon;
    private static final int COMMENT_PREVIEW_LINES = 2; // Số dòng hiển thị khi thu nhỏ
    private JButton btnScrollTop;
    private JTextField txtSearch;
    private JComboBox<CategoryItem> cboCategory;
    
    public NewsFeed(int userId) {
        this.userId = userId;
        // Không cần setTitle và setDefaultCloseOperation vì sẽ được nhúng vào tab
        // setTitle("News Feed");
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setBounds(100, 100, 800, 600);
        // setLocationRelativeTo(null);
        
        // Main container
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        // Main panel for posts
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Scroll pane
        scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        
        // Load icons
        try {
            // Load từ resources folder
            likeIcon = new ImageIcon(getClass().getResource("/icons/like.png"));
            commentIcon = new ImageIcon(getClass().getResource("/icons/comment.png"));
            
            // Resize icons
            likeIcon = new ImageIcon(likeIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            commentIcon = new ImageIcon(commentIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Thêm nút scroll to top
        addScrollToTopButton();
        
        // Load posts
        loadPosts();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // User info panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userPanel.setOpaque(false);
        
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT username, email, avatar FROM tbl_user WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                String avatarUrl = rs.getString("avatar");
                
                // Avatar
                JLabel avatarLabel = new JLabel();
                avatarLabel.setPreferredSize(new Dimension(40, 40));
                
                if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    try {
                        // Tải và tạo avatar tròn từ URL
                        URL url = new URL(avatarUrl);
                        BufferedImage originalImage = ImageIO.read(url);
                        
                        // Tạo ảnh tròn
                        BufferedImage circularImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2 = circularImage.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, 40, 40);
                        g2.setClip(circle);
                        g2.drawImage(originalImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH), 0, 0, null);
                        g2.dispose();
                        
                        avatarLabel.setIcon(new ImageIcon(circularImage));
                    } catch (Exception e) {
                        // Nếu không tải được ảnh, hiển thị chữ cái đầu của username
                        createDefaultAvatar(avatarLabel, username);
                    }
                } else {
                    // Nếu không có avatar URL, hiển thị chữ cái đầu của username
                    createDefaultAvatar(avatarLabel, username);
                }
                
                // Panel chứa thông tin user
                JPanel userInfoPanel = new JPanel();
                userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
                userInfoPanel.setOpaque(false);
                
                // Username
                JLabel lblUsername = new JLabel(username);
                lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 14));
                
                // Email
                JLabel lblEmail = new JLabel(email);
                lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblEmail.setForeground(new Color(102, 102, 102));
                
                userInfoPanel.add(lblUsername);
                userInfoPanel.add(lblEmail);
                
                userPanel.add(avatarLabel);
                userPanel.add(userInfoPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        txtSearch.setPreferredSize(new Dimension(300, 36));
        
        // Placeholder text
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm bài viết...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm bài viết...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        
        // Xử lý sự kiện tìm kiếm khi nhấn Enter
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchPosts(txtSearch.getText());
                }
            }
        });
        
        // Nút tìm kiếm
        JButton btnSearch = new JButton();
        try {
            ImageIcon searchIcon = new ImageIcon(getClass().getResource("/icons/search.png"));
            Image img = searchIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnSearch.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btnSearch.setText("🔍"); // Fallback text
        }
        btnSearch.setBorderPainted(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> searchPosts(txtSearch.getText()));
        
        JPanel searchInputPanel = new JPanel(new BorderLayout());
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(txtSearch, BorderLayout.CENTER);
        searchInputPanel.add(btnSearch, BorderLayout.EAST);
        
        searchPanel.add(searchInputPanel, BorderLayout.CENTER);
        
        // Filter panel
        JPanel filterPanel = createFilterPanel();
        searchPanel.add(filterPanel, BorderLayout.EAST);
        
        // Action buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        // Account Settings button
        JButton btnAccount = createHeaderButton("Tài khoản", "/icons/user.png");
        btnAccount.addActionListener(e -> {
            AccountSettings dialog = new AccountSettings(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                userId
            );
            dialog.setVisible(true);
        });
        
        // Add Post button
        JButton btnAddPost = createHeaderButton("Bài viết mới", "/icons/edit.png");
        btnAddPost.addActionListener(e -> {
            HandlePostDialog dialog = new HandlePostDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                userId,
                () -> loadPosts()
            );
            dialog.setVisible(true);
        });
        
        // Switch to Manager button
        JButton btnSwitchToManager = createHeaderButton("Chuyển sang quản lý", "/icons/switch.png");
        btnSwitchToManager.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                window.dispose();
            }
            MainManage manager = new MainManage(userId);
            manager.initManagerUI();
        });
        
        // Logout button
        JButton btnLogout = createHeaderButton("Đăng xuất", "/icons/logout.png");
        btnLogout.addActionListener(e -> logout());
        
        buttonPanel.add(btnAccount);
        buttonPanel.add(btnAddPost);
        buttonPanel.add(btnSwitchToManager);
        buttonPanel.add(btnLogout);
        
        headerPanel.add(userPanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private void createDefaultAvatar(JLabel avatarLabel, String username) {
        // Tạo avatar mặc định với chữ cái đầu của username
        BufferedImage defaultAvatar = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = defaultAvatar.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ hình tròn
        g2.setColor(new Color(52, 152, 219));
        g2.fill(new Ellipse2D.Double(0, 0, 40, 40));
        
        // Vẽ chữ
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        String initial = username.substring(0, 1).toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        int x = (40 - fm.stringWidth(initial)) / 2;
        int y = ((40 - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(initial, x, y);
        g2.dispose();
        
        avatarLabel.setIcon(new ImageIcon(defaultAvatar));
    }
    
    private void loadPosts() {
        mainPanel.removeAll();
        
        // Thêm panel căn giữa để chứa tất cả posts
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        try {
            connection = DBConnection.getConnection();
            String query = """
                SELECT p.*, u.username, u.avatar,
                    (SELECT COUNT(*) FROM tbl_like WHERE post_id = p.id) as like_count,
                    (SELECT COUNT(*) FROM tbl_comment WHERE post_id = p.id) as comment_count,
                    EXISTS(SELECT 1 FROM tbl_like WHERE post_id = p.id AND user_id = ?) as user_liked,
                    COALESCE(p.create_at, CURRENT_TIMESTAMP) as post_date,
                    p.hash_img
                FROM tbl_post p 
                JOIN tbl_user u ON p.user_id = u.id 
                ORDER BY post_date DESC
            """;
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                JPanel postPanel = createPostPanel(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("post_date"),
                    rs.getInt("like_count"),
                    rs.getInt("comment_count"),
                    rs.getBoolean("user_liked"),
                    rs.getString("hash_img"),
                    rs.getString("avatar")
                );
                
                // Wrap postPanel trong một panel căn giữa
                JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                wrapperPanel.setOpaque(false);
                postPanel.setPreferredSize(new Dimension(500, postPanel.getPreferredSize().height));
                wrapperPanel.add(postPanel);
                
                centerPanel.add(wrapperPanel);
                centerPanel.add(Box.createVerticalStrut(10));
            }
            
            mainPanel.add(centerPanel);
            mainPanel.revalidate();
            mainPanel.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading posts: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createPostPanel(int postId, String username, String title, String content,
            Timestamp postDate, int likeCount, int commentCount, boolean userLiked, 
            String imageUrls, String avatar) {
        
        JPanel postPanel = new JPanel(new BorderLayout(10, 10));
        postPanel.setBackground(Color.WHITE);
        postPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(240, 240, 240)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Header panel chứa thông tin user và nút delete
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        
        // User info panel (bên trái)
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userInfoPanel.setOpaque(false);
        
        // Avatar
        JLabel avatarLabel = createAvatarLabel(username, avatar);
        
        // Panel chứa username và date
        JPanel nameTimePanel = new JPanel();
        nameTimePanel.setLayout(new BoxLayout(nameTimePanel, BoxLayout.Y_AXIS));
        nameTimePanel.setOpaque(false);
        
        // Username
        JLabel lblUsername = new JLabel(username);
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Date
        JLabel lblDate = new JLabel(sdf.format(postDate));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(Color.GRAY);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        nameTimePanel.add(lblUsername);
        nameTimePanel.add(lblDate);
        
        userInfoPanel.add(avatarLabel);
        userInfoPanel.add(nameTimePanel);
        
        // Delete button panel (bên phải)
        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deletePanel.setOpaque(false);
        
        // Thêm kiểm tra xem post có phải của user hiện tại không
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT user_id FROM tbl_post WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next() && rs.getInt("user_id") == userId) {
                // Panel chứa các nút action
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                actionPanel.setOpaque(false);
                
                // Nút Edit
                JButton btnEdit = new JButton();
                try {
                    ImageIcon editIcon = new ImageIcon(getClass().getResource("/icons/edit.png"));
                    Image img = editIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    btnEdit.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    btnEdit.setText("✎"); // Fallback text nếu không load được icon
                }
                
                btnEdit.setBorderPainted(false);
                btnEdit.setContentAreaFilled(false);
                btnEdit.setFocusPainted(false);
                btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnEdit.setToolTipText("Chỉnh sửa bài viết");
                
                btnEdit.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        btnEdit.setContentAreaFilled(true);
                        btnEdit.setBackground(new Color(240, 240, 240));
                    }
                    public void mouseExited(MouseEvent e) {
                        btnEdit.setContentAreaFilled(false);
                    }
                });
                
                btnEdit.addActionListener(e -> {
                    HandlePostDialog dialog = new HandlePostDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this),
                        userId,
                        postId,
                        title,
                        content,
                        imageUrls,
                        () -> loadPosts()
                    );
                    dialog.setVisible(true);
                });
                
                // Nút Delete
                JButton btnDelete = new JButton();
                try {
                    ImageIcon deleteIcon = new ImageIcon(getClass().getResource("/icons/trash.png"));
                    Image img = deleteIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                    btnDelete.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    btnDelete.setText("×"); // Fallback text nếu không load được icon
                }
                
                btnDelete.setBorderPainted(false);
                btnDelete.setContentAreaFilled(false);
                btnDelete.setFocusPainted(false);
                btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnDelete.setToolTipText("Xóa bài viết");
                
                btnDelete.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        btnDelete.setContentAreaFilled(true);
                        btnDelete.setBackground(new Color(255, 235, 235));
                    }
                    public void mouseExited(MouseEvent e) {
                        btnDelete.setContentAreaFilled(false);
                    }
                });
                
                btnDelete.addActionListener(e -> deletePost(postId));
                
                // Thêm các nút vào panel
                actionPanel.add(btnEdit);
                actionPanel.add(btnDelete);
                deletePanel.add(actionPanel);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        headerPanel.add(userInfoPanel, BorderLayout.WEST);
        headerPanel.add(deletePanel, BorderLayout.EAST);
        
        // Thêm các phần còn lại của post panel
        postPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Thêm title vào panel riêng và đặt dưới header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(10, 0, 5, 0)); // Padding trên dưới
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Tăng font size
        titlePanel.add(lblTitle, BorderLayout.CENTER);
        
        // Panel chứa cả header và title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Hiển thị ảnh từ hash_img
        if (imageUrls != null && !imageUrls.trim().isEmpty()) {
            String[] imageUrlArray = imageUrls.split("\n");
            for (String imageUrl : imageUrlArray) {
                if (!imageUrl.trim().isEmpty()) {
                    try {
                        // Tạo panel cho ảnh
                        JPanel imagePanel = new JPanel(new BorderLayout());
                        imagePanel.setOpaque(false);
                        imagePanel.setBorder(new EmptyBorder(5, 0, 5, 0));
                        
                        // Tải và resize ảnh
                        URL url = new URL(imageUrl.trim());
                        BufferedImage originalImage = ImageIO.read(url);
                        if (originalImage != null) {
                            // Tính toán kích thước mới giữ nguyên tỷ lệ
                            int maxWidth = 470; // Giảm xuống để phù hợp với panel 500px
                            int newWidth = Math.min(originalImage.getWidth(), maxWidth);
                            int newHeight = (newWidth * originalImage.getHeight()) / originalImage.getWidth();
                            
                            // Resize ảnh
                            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                            ImageIcon imageIcon = new ImageIcon(scaledImage);
                            
                            // Tạo label chứa ảnh và căn giữa
                            JLabel imageLabel = new JLabel(imageIcon);
                            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            imagePanel.add(imageLabel, BorderLayout.CENTER);
                            
                            contentPanel.add(imagePanel);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Nếu không tải được ảnh, hiển thị thông báo lỗi
                        JLabel errorLabel = new JLabel("Cannot load image: " + imageUrl);
                        errorLabel.setForeground(Color.RED);
                        contentPanel.add(errorLabel);
                    }
                }
            }
        }
        
        // Content text
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(formatContent(content));
        textPane.setEditable(false);
        textPane.setBackground(null);
        textPane.setBorder(null);

        // Cho phép textPane tự động điều chỉnh kích thước theo nội dung
        textPane.setPreferredSize(new Dimension(0, textPane.getPreferredSize().height));

        // Thêm sự kiện để tự động điều chỉnh kích thước khi nội dung thay đổi
        HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSize();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSize();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSize();
            }
            
            private void updateSize() {
                SwingUtilities.invokeLater(() -> {
                    textPane.setPreferredSize(new Dimension(0, textPane.getPreferredSize().height));
                    textPane.revalidate();
                });
            }
        });

        contentPanel.add(textPane);
        
        // Thêm categories vào phía dưới content
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createCategoryPanel(postId));
        
        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Like button with icon and count
        JPanel likePanel = new JPanel();
        likePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        likePanel.setOpaque(false);
        
        JButton btnLike = createActionButton(
            userLiked ? "Bỏ thích" : "Thích",
            userLiked ? new Color(231, 76, 60) : new Color(52, 152, 219)
        );
        btnLike.setIcon(likeIcon);
        btnLike.setMargin(new Insets(0, 0, 0, 2));
        
        // Label for like count
        JLabel lblLikeCount = new JLabel(String.valueOf(likeCount));
        lblLikeCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLikeCount.setForeground(new Color(100, 100, 100));
        lblLikeCount.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        btnLike.addActionListener(e -> {
            try {
                connection = DBConnection.getConnection();
                boolean isLiked = btnLike.getText().equals("Bỏ thích");
                
                if (isLiked) {
                    // Unlike
                    String query = "DELETE FROM tbl_like WHERE post_id = ? AND user_id = ?";
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setInt(1, postId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                    
                    // Update UI
                    btnLike.setText("Thích");
                    btnLike.setForeground(new Color(52, 152, 219));
                    lblLikeCount.setText(String.valueOf(Integer.parseInt(lblLikeCount.getText()) - 1));
                    
                } else {
                    // Like
                    String query = "INSERT INTO tbl_like (post_id, user_id) VALUES (?, ?)";
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setInt(1, postId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                    
                    // Update UI
                    btnLike.setText("Bỏ thích");
                    btnLike.setForeground(new Color(231, 76, 60));
                    lblLikeCount.setText(String.valueOf(Integer.parseInt(lblLikeCount.getText()) + 1));
                }
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error toggling like: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        likePanel.add(btnLike);
        likePanel.add(lblLikeCount);
        
        // Comments section
        JPanel commentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        commentsPanel.setOpaque(false);
        
        // Button để hiển thị comments dialog
        JButton btnShowComments = new JButton(commentCount + " bình luận");
        btnShowComments.setIcon(commentIcon);
        btnShowComments.setBorderPainted(false);
        btnShowComments.setContentAreaFilled(false);
        btnShowComments.setForeground(new Color(52, 152, 219));
        btnShowComments.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnShowComments.addActionListener(e -> showCommentsDialog(postId, title));
        
        // Thêm nút Xem
        JButton btnView = new JButton("Xem blog cá nhân");
        btnView.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnView.setForeground(new Color(24, 119, 242));
        btnView.setBorderPainted(false);
        btnView.setContentAreaFilled(false);
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Thêm icon cho nút
        ImageIcon viewIcon = new ImageIcon(getClass().getResource("/icons/user-blog.png"));
        Image viewImg = viewIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        btnView.setIcon(new ImageIcon(viewImg));
        btnView.setIconTextGap(5); // Khoảng cách giữa icon và text

        btnView.addActionListener(e -> viewUserPosts(getUserIdByUsername(username), username));
        
        // Add components to actions panel
        actionsPanel.add(likePanel);
        actionsPanel.add(Box.createHorizontalStrut(20));
        actionsPanel.add(btnShowComments);
        actionsPanel.add(btnView);
        
        // Add all components
        postPanel.add(topPanel, BorderLayout.NORTH);
        postPanel.add(contentPanel, BorderLayout.CENTER);
        postPanel.add(actionsPanel, BorderLayout.SOUTH);
        
        return postPanel;
    }
    
    private String formatContent(String content) {
        // Định dạng HTML cơ bản
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><body>");
        
        try {
            // Xử lý ảnh trong nội dung
            Pattern imgPattern = Pattern.compile("\\[img\\](.*?)\\[/img\\]");
            Matcher imgMatcher = imgPattern.matcher(content);
            StringBuffer sb = new StringBuffer();
            
            while (imgMatcher.find()) {
                String imageUrl = imgMatcher.group(1);
                String replacement;
                try {
                    // Kiểm tra URL hình ảnh hợp lệ
                    new URL(imageUrl).openStream().close();
                    replacement = String.format("<div class='image-container'><img src='%s' alt='Post Image'/></div>", imageUrl);
                } catch (Exception e) {
                    replacement = "<div class='error'>Invalid image URL</div>";
                }
                imgMatcher.appendReplacement(sb, replacement.replace("$", "\\$"));
            }
            imgMatcher.appendTail(sb);
            content = sb.toString();
            
            // Xử lý các định dạng khác
            content = content.replaceAll("\n", "<br/>");
            content = content.replaceAll("\\[b\\](.*?)\\[/b\\]", "<strong>$1</strong>");
            content = content.replaceAll("\\[i\\](.*?)\\[/i\\]", "<em>$1</em>");
            content = content.replaceAll("\\[url\\](.*?)\\[/url\\]", "<a href='$1' target='_blank'>$1</a>");
            content = content.replaceAll("\\[url=(.*?)\\](.*?)\\[/url\\]", "<a href='$1' target='_blank'>$2</a>");
            
            htmlContent.append(content);
            
        } catch (Exception e) {
            e.printStackTrace();
            htmlContent.append("Error formatting content: ").append(e.getMessage());
        }
        
        htmlContent.append("</body></html>");
        return htmlContent.toString();
    }
    
    private void createNewPost() {
        HandlePostDialog dialog = new HandlePostDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            userId,
            () -> {
                loadPosts();
                scrollToTop();
            }
        );
        dialog.setVisible(true);
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Xóa thông tin đăng nhập đã lưu (nếu có)
            Preferences prefs = Preferences.userRoot();
            prefs.remove("reader_email");
            prefs.remove("reader_password");
            
            // Đóng frame NewsFeed hiện tại
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                window.dispose();
            }
            
            // Mở SignIn frame
            SwingUtilities.invokeLater(() -> {
                new SignIn().setVisible(true);
            });
        }
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm padding cho nút thay vì set kích thước cố định
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }
    
    // Thêm phương thức helper để kiểm tra URL hình ảnh
    private boolean isValidImageUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            try (InputStream is = url.openStream()) {
                BufferedImage img = ImageIO.read(is);
                return img != null;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    // Thêm phương thức helper để format hash_img
    private String formatHashImg(String hashImg) {
        if (hashImg == null || hashImg.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder formatted = new StringBuilder();
        String[] hashes = hashImg.split("\n");
        for (String hash : hashes) {
            if (!hash.trim().isEmpty()) {
                formatted.append("<span style='color: #3498db; margin-right: 10px;'>")
                        .append(hash.trim())
                        .append("</span>");
            }
        }
        return formatted.toString();
    }
    
    // Helper methods
    private JLabel createHashTagLabel(String tag) {
        JLabel label = new JLabel("#" + tag);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(52, 152, 219));
        label.setBorder(new EmptyBorder(2, 5, 2, 5));
        return label;
    }
    
    private ImageIcon createCircularAvatar(String letter) {
        int size = 40;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ circle background
        g2.setColor(new Color(52, 152, 219));
        g2.fillOval(0, 0, size - 1, size - 1);
        
        // Vẽ chữ
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(letter)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(letter, x, y);
        
        g2.dispose();
        return new ImageIcon(image);
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(color);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JLabel createStatsLabel(String text, ImageIcon icon) {
        JLabel label = new JLabel(text);
        label.setIcon(icon);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(100, 100, 100));
        return label;
    }
    
    private void showCommentsDialog(int postId, String postTitle) {
        CommentsDialog dialog = new CommentsDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            postTitle,
            postId,
            userId,
            this::loadPosts // Callback để refresh posts khi có comment mới
        );
        dialog.setVisible(true);
    }
    
    private void addScrollToTopButton() {
        btnScrollTop = new JButton();
        
        // Tạo icon đơn giản
        ImageIcon icon = new ImageIcon(getClass().getResource("/icons/up.png"));
        // Resize icon nếu cần
        Image img = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        btnScrollTop.setIcon(new ImageIcon(img));
        
        btnScrollTop.setToolTipText("Scroll to top");
        
        // Style cho nút
        btnScrollTop.setBorderPainted(false);
        btnScrollTop.setContentAreaFilled(false);
        btnScrollTop.setFocusPainted(false);
        btnScrollTop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnScrollTop.setPreferredSize(new Dimension(40, 40));
        
        // Tạo layered pane để có thể đặt nút lên trên scrollPane
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        
        // Thêm scrollPane vào layered pane ở layer dưới
        layeredPane.add(scrollPane, JLayeredPane.DEFAULT_LAYER);
        
        // Thêm nút vào layer trên
        layeredPane.add(btnScrollTop, JLayeredPane.POPUP_LAYER);
        
        // Thêm layered pane vào contentPane
        getContentPane().add(layeredPane, BorderLayout.CENTER);
        
        // Cập nhật kích thước và vị trí các components khi cửa sổ thay đổi
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Cập nhật kích thước scrollPane
                scrollPane.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                
                // Cập nhật vị trí nút scroll to top
                btnScrollTop.setBounds(
                    layeredPane.getWidth() - btnScrollTop.getPreferredSize().width - 20,
                    layeredPane.getHeight() - btnScrollTop.getPreferredSize().height - 20,
                    btnScrollTop.getPreferredSize().width,
                    btnScrollTop.getPreferredSize().height
                );
            }
        });
        
        // Thêm hover effect
        btnScrollTop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnScrollTop.setContentAreaFilled(true);
                btnScrollTop.setBackground(new Color(52, 152, 219, 200));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnScrollTop.setContentAreaFilled(false);
            }
        });
        
        // Thêm action listener
        btnScrollTop.addActionListener(e -> {
            // Scroll to top với animation
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            new Thread(() -> {
                int currentPosition = verticalBar.getValue();
                int steps = 50;
                int delay = 5;
                int stepSize = currentPosition / steps;
                
                for (int i = 0; i < steps; i++) {
                    try {
                        Thread.sleep(delay);
                        final int step = i;
                        SwingUtilities.invokeLater(() -> {
                            int newValue = currentPosition - (stepSize * (step + 1));
                            verticalBar.setValue(Math.max(0, newValue));
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                SwingUtilities.invokeLater(() -> verticalBar.setValue(0));
            }).start();
        });
        
        // Luôn hiển thị nút
        btnScrollTop.setVisible(true);
    }
    
    private JLabel createAvatarLabel(String username, String avatarUrl) {
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            try {
                // Load ảnh từ URL
                URL url = new URL(avatarUrl);
                BufferedImage originalImage = ImageIO.read(url);
                if (originalImage != null) {
                    // Tạo ảnh tròn
                    BufferedImage circularImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = circularImage.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Vẽ hình tròn làm mask
                    g2.setColor(Color.WHITE);
                    g2.fillOval(0, 0, 39, 39);
                    
                    // Scale và crop ảnh gốc
                    Image scaledImage = originalImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    g2.setClip(new Ellipse2D.Float(0, 0, 39, 39));
                    g2.drawImage(scaledImage, 0, 0, null);
                    
                    g2.dispose();
                    avatarLabel.setIcon(new ImageIcon(circularImage));
                    return avatarLabel;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Fallback to default avatar if URL is invalid or empty
        avatarLabel.setIcon(createCircularAvatar(username.substring(0, 1).toUpperCase()));
        return avatarLabel;
    }
    
    public void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            // Scroll với animation
            new Thread(() -> {
                int currentPosition = verticalBar.getValue();
                int steps = 50;
                int delay = 5;
                int stepSize = currentPosition / steps;
                
                for (int i = 0; i < steps; i++) {
                    try {
                        Thread.sleep(delay);
                        final int step = i;
                        SwingUtilities.invokeLater(() -> {
                            int newValue = currentPosition - (stepSize * (step + 1));
                            verticalBar.setValue(Math.max(0, newValue));
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                SwingUtilities.invokeLater(() -> verticalBar.setValue(0));
            }).start();
        });
    }
    
    private void deletePost(int postId) {
        // Hiển thị dialog xác nhận với icon cảnh báo
        int choice = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this), // Sử dụng parent window
            "Bạn có chắc chắn muốn xóa bài viết này không?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                connection = DBConnection.getConnection();
                
                // Xóa likes
                String deleteLikes = "DELETE FROM tbl_like WHERE post_id = ?";
                PreparedStatement psLikes = connection.prepareStatement(deleteLikes);
                psLikes.setInt(1, postId);
                psLikes.executeUpdate();
                
                // Xóa comments
                String deleteComments = "DELETE FROM tbl_comment WHERE post_id = ?";
                PreparedStatement psComments = connection.prepareStatement(deleteComments);
                psComments.setInt(1, postId);
                psComments.executeUpdate();
                
                // Xóa post
                String deletePost = "DELETE FROM tbl_post WHERE id = ?";
                PreparedStatement psPost = connection.prepareStatement(deletePost);
                psPost.setInt(1, postId);
                psPost.executeUpdate();
                
                // Thông báo thành công trước khi refresh UI
                JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Xóa bài viết thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                // Refresh UI trên EDT
                SwingUtilities.invokeLater(() -> {
                    // Refresh newsfeed
                    loadPosts();
                    // Scroll lên đầu trang
                    scrollToTop();
                    // Revalidate và repaint parent container
                    Container parent = getParent();
                    if (parent != null) {
                        parent.revalidate();
                        parent.repaint();
                    }
                });
                
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Lỗi khi xóa bài viết: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void searchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty() || 
            keyword.equals("Tìm kiếm bài viết...")) {
            loadPosts(); // Load lại tất cả bài viết nếu không có từ khóa
            return;
        }
        
        try {
            connection = DBConnection.getConnection();
            String query = """
                SELECT p.id, u.username, p.title, p.content, p.create_at as post_date,
                    u.avatar, p.hash_img,
                    (SELECT COUNT(*) FROM tbl_like WHERE post_id = p.id) as like_count,
                    (SELECT COUNT(*) FROM tbl_comment WHERE post_id = p.id) as comment_count,
                    EXISTS(SELECT 1 FROM tbl_like WHERE post_id = p.id AND user_id = ?) as user_liked
                FROM tbl_post p 
                JOIN tbl_user u ON p.user_id = u.id 
                WHERE LOWER(p.title) LIKE ? OR LOWER(p.content) LIKE ?
                ORDER BY post_date DESC
            """;
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            
            ResultSet rs = ps.executeQuery();
            
            // Xóa các bài viết hiện tại
            mainPanel.removeAll();
            
            // Panel chứa các bài viết
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            
            boolean hasResults = false;
            
            while (rs.next()) {
                hasResults = true;
                JPanel postPanel = createPostPanel(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("post_date"),
                    rs.getInt("like_count"),
                    rs.getInt("comment_count"),
                    rs.getBoolean("user_liked"),
                    rs.getString("hash_img"),
                    rs.getString("avatar")
                );
                
                // Wrap postPanel trong một panel căn giữa
                JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                wrapperPanel.setOpaque(false);
                postPanel.setPreferredSize(new Dimension(500, postPanel.getPreferredSize().height));
                wrapperPanel.add(postPanel);
                
                centerPanel.add(wrapperPanel);
                centerPanel.add(Box.createVerticalStrut(10));
            }
            
            if (!hasResults) {
                // Hiển thị thông báo không tìm thấy kết quả
                JPanel noResultPanel = new JPanel(new BorderLayout());
                noResultPanel.setOpaque(false);
                noResultPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
                
                JLabel lblNoResult = new JLabel("Không tìm thấy bài viết nào phù hợp", SwingConstants.CENTER);
                lblNoResult.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblNoResult.setForeground(Color.GRAY);
                
                noResultPanel.add(lblNoResult, BorderLayout.CENTER);
                centerPanel.add(noResultPanel);
            }
            
            mainPanel.add(centerPanel);
            mainPanel.revalidate();
            mainPanel.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tìm kiếm: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Thêm phương thức helper để tạo button với icon
    private JButton createHeaderButton(String text, String iconPath) {
        JButton button = new JButton(text);
        try {
            // Load và scale icon
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
            button.setIconTextGap(8); // Khoảng cách giữa icon và text
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Style cho button
        Color buttonColor = new Color(52, 152, 219); // Màu xanh dương
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(buttonColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Thêm padding
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(buttonColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(buttonColor);
            }
        });
        
        return button;
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);
        
        // Combobox thể loại
        cboCategory = new JComboBox<>();
        cboCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboCategory.setPreferredSize(new Dimension(150, 36));
        
        // Thêm item "Tất cả thể loại"
        cboCategory.addItem(new CategoryItem(0, "Tất cả thể loại"));
        
        // Load categories từ database
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT id, title FROM tbl_category ORDER BY title";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                cboCategory.addItem(new CategoryItem(
                    rs.getInt("id"),
                    rs.getString("title")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Xử lý sự kiện khi chọn category
        cboCategory.addActionListener(e -> {
            CategoryItem selected = (CategoryItem) cboCategory.getSelectedItem();
            if (selected != null) {
                filterPostsByCategory(selected.getId());
            }
        });
        
        filterPanel.add(cboCategory);
        return filterPanel;
    }
    
    private void filterPostsByCategory(int categoryId) {
        if (categoryId == 0) {
            loadPosts(); // Load tất cả bài viết
            return;
        }
        
        try {
            connection = DBConnection.getConnection();
            String query = """
                SELECT p.*, u.username, u.avatar,
                    (SELECT COUNT(*) FROM tbl_like WHERE post_id = p.id) as like_count,
                    (SELECT COUNT(*) FROM tbl_comment WHERE post_id = p.id) as comment_count,
                    EXISTS(SELECT 1 FROM tbl_like WHERE post_id = p.id AND user_id = ?) as user_liked,
                    COALESCE(p.create_at, CURRENT_TIMESTAMP) as post_date
                FROM tbl_post p 
                JOIN tbl_user u ON p.user_id = u.id
                JOIN tbl_post_category pc ON p.id = pc.post_id
                WHERE pc.category_id = ?
                ORDER BY post_date DESC
            """;
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ResultSet rs = ps.executeQuery();
            
            // Xóa các bài viết hiện tại
            mainPanel.removeAll();
            
            // Panel chứa các bài viết
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            
            boolean hasResults = false;
            
            while (rs.next()) {
                hasResults = true;
                JPanel postPanel = createPostPanel(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("post_date"),
                    rs.getInt("like_count"),
                    rs.getInt("comment_count"),
                    rs.getBoolean("user_liked"),
                    rs.getString("hash_img"),
                    rs.getString("avatar")
                );
                
                // Wrap postPanel trong một panel căn giữa
                JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                wrapperPanel.setOpaque(false);
                postPanel.setPreferredSize(new Dimension(500, postPanel.getPreferredSize().height));
                wrapperPanel.add(postPanel);
                
                centerPanel.add(wrapperPanel);
                centerPanel.add(Box.createVerticalStrut(10));
            }
            
            if (!hasResults) {
                // Hiển thị thông báo không tìm thấy kết quả
                JPanel noResultPanel = new JPanel(new BorderLayout());
                noResultPanel.setOpaque(false);
                noResultPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
                
                JLabel lblNoResult = new JLabel("Không có bài viết nào trong thể loại này", SwingConstants.CENTER);
                lblNoResult.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblNoResult.setForeground(Color.GRAY);
                
                noResultPanel.add(lblNoResult, BorderLayout.CENTER);
                centerPanel.add(noResultPanel);
            }
            
            mainPanel.add(centerPanel);
            mainPanel.revalidate();
            mainPanel.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi lọc bài viết: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private class CategoryItem {
        private int id;
        private String title;
        
        public CategoryItem(int id, String title) {
            this.id = id;
            this.title = title;
        }
        
        public int getId() { return id; }
        public String getTitle() { return title; }
        
        @Override
        public String toString() {
            return title;
        }
    }
    
    // Thêm phương thức để lấy categories của một post
    private JPanel createCategoryPanel(int postId) {
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        categoryPanel.setOpaque(false);
        
        try {
            connection = DBConnection.getConnection();
            String query = """
                SELECT c.title 
                FROM tbl_category c 
                JOIN tbl_post_category pc ON c.id = pc.category_id 
                WHERE pc.post_id = ?
                ORDER BY c.title
            """;
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                JLabel lblCategory = new JLabel("#" + rs.getString("title"));
                lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblCategory.setForeground(new Color(24, 119, 242)); // Màu xanh Facebook
                categoryPanel.add(lblCategory);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return categoryPanel;
    }
    
    // Thêm phương thức để lấy userId từ username
    private int getUserIdByUsername(String username) {
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT id FROM tbl_user WHERE username = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // Thêm phương thức để xem bài viết của một người dùng cụ thể
    private void viewUserPosts(int authorId, String authorName) {
        try {
            connection = DBConnection.getConnection();
            String query = """
                SELECT p.*, u.username, u.avatar,
                    (SELECT COUNT(*) FROM tbl_like l WHERE l.post_id = p.id) as like_count,
                    (SELECT COUNT(*) FROM tbl_comment c WHERE c.post_id = p.id) as comment_count,
                    EXISTS(SELECT 1 FROM tbl_like l WHERE l.post_id = p.id AND l.user_id = ?) as user_liked
                FROM tbl_post p 
                JOIN tbl_user u ON p.user_id = u.id
                WHERE p.user_id = ?
                ORDER BY p.create_at DESC
            """;
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setInt(2, authorId);
            ResultSet rs = ps.executeQuery();
            
            // Xóa các bài viết hiện tại
            mainPanel.removeAll();
            
            // Thêm tiêu đề
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(Color.WHITE);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Panel cho nút quay lại (bên trái)
            JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            backPanel.setOpaque(false);
            JButton btnBack = new JButton("← Quay lại");
            btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btnBack.setForeground(new Color(24, 119, 242));
            btnBack.setBorderPainted(false);
            btnBack.setContentAreaFilled(false);
            btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnBack.addActionListener(e -> loadPosts());
            backPanel.add(btnBack);
            
            // Panel chứa tiêu đề và thống kê (ở giữa)
            JPanel headerCenterPanel = new JPanel(new BorderLayout());
            headerCenterPanel.setOpaque(false);
            
            // Tiêu đề
            JLabel lblTitle = new JLabel("Bài viết của " + authorName, SwingConstants.CENTER);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            
            // Panel thống kê
            JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            statsPanel.setOpaque(false);
            
            // Lấy thống kê từ database
            String statsQuery = """
                SELECT 
                    COUNT(DISTINCT p.id) as total_posts,
                    (SELECT COUNT(*) FROM tbl_like WHERE post_id IN (SELECT id FROM tbl_post WHERE user_id = ?)) as total_likes,
                    (SELECT COUNT(*) FROM tbl_comment WHERE post_id IN (SELECT id FROM tbl_post WHERE user_id = ?)) as total_comments
                FROM tbl_post p
                WHERE p.user_id = ?
            """;

            PreparedStatement statsPs = connection.prepareStatement(statsQuery);
            statsPs.setInt(1, authorId); // Cho subquery likes
            statsPs.setInt(2, authorId); // Cho subquery comments  
            statsPs.setInt(3, authorId); // Cho FROM clause
            ResultSet statsRs = statsPs.executeQuery();

            if (statsRs.next()) {
                // Tổng số bài viết
                JPanel postStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                postStatsPanel.setOpaque(false);
                ImageIcon postIcon = new ImageIcon(getClass().getResource("/icons/blogs.png"));
                Image postImg = postIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                JLabel lblPostIcon = new JLabel(new ImageIcon(postImg));
                JLabel lblPostCount = new JLabel(statsRs.getInt("total_posts") + " bài viết");
                lblPostCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblPostCount.setForeground(new Color(100, 100, 100));
                postStatsPanel.add(lblPostIcon);
                postStatsPanel.add(lblPostCount);
                
                // Tổng số lượt thích
                JPanel likeStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                likeStatsPanel.setOpaque(false);
                ImageIcon likeIcon = new ImageIcon(getClass().getResource("/icons/like.png"));
                Image likeImg = likeIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                JLabel lblLikeIcon = new JLabel(new ImageIcon(likeImg));
                JLabel lblLikeCount = new JLabel(statsRs.getInt("total_likes") + " lượt thích");
                lblLikeCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblLikeCount.setForeground(new Color(100, 100, 100));
                likeStatsPanel.add(lblLikeIcon);
                likeStatsPanel.add(lblLikeCount);
                
                // Tổng số bình luận
                JPanel commentStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                commentStatsPanel.setOpaque(false);
                ImageIcon commentIcon = new ImageIcon(getClass().getResource("/icons/comment.png"));
                Image commentImg = commentIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                JLabel lblCommentIcon = new JLabel(new ImageIcon(commentImg));
                JLabel lblCommentCount = new JLabel(statsRs.getInt("total_comments") + " bình luận");
                lblCommentCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblCommentCount.setForeground(new Color(100, 100, 100));
                commentStatsPanel.add(lblCommentIcon);
                commentStatsPanel.add(lblCommentCount);
                
                // Thêm vào statsPanel với separator
                statsPanel.add(postStatsPanel);
                statsPanel.add(new JSeparator(JSeparator.VERTICAL) {
                    {
                        setPreferredSize(new Dimension(1, 14));
                        setForeground(new Color(200, 200, 200));
                    }
                });
                statsPanel.add(likeStatsPanel);
                statsPanel.add(new JSeparator(JSeparator.VERTICAL) {
                    {
                        setPreferredSize(new Dimension(1, 14));
                        setForeground(new Color(200, 200, 200));
                    }
                });
                statsPanel.add(commentStatsPanel);
            }

            // Tạo panel mới để chứa title và stats
            JPanel titleAndStatsPanel = new JPanel(new BorderLayout());
            titleAndStatsPanel.setOpaque(false);
            titleAndStatsPanel.add(lblTitle, BorderLayout.NORTH);
            titleAndStatsPanel.add(statsPanel, BorderLayout.CENTER);
            
            // Thêm padding cho titleAndStatsPanel
            titleAndStatsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, btnBack.getPreferredSize().width));
            
            // Thêm các panel vào headerPanel
            headerPanel.add(backPanel, BorderLayout.WEST);
            headerPanel.add(titleAndStatsPanel, BorderLayout.CENTER);
            
            // Panel chứa các bài viết
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            
            boolean hasResults = false;
            
            while (rs.next()) {
                hasResults = true;
                JPanel postPanel = createPostPanel(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("create_at"),
                    rs.getInt("like_count"),
                    rs.getInt("comment_count"),
                    rs.getBoolean("user_liked"),
                    rs.getString("hash_img"),
                    rs.getString("avatar")
                );
                
                // Wrap postPanel trong một panel căn giữa
                JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                wrapperPanel.setOpaque(false);
                postPanel.setPreferredSize(new Dimension(500, postPanel.getPreferredSize().height));
                wrapperPanel.add(postPanel);
                
                centerPanel.add(wrapperPanel);
                centerPanel.add(Box.createVerticalStrut(10));
            }
            
            if (!hasResults) {
                // Hiển thị thông báo không có bài viết
                JPanel noResultPanel = new JPanel(new BorderLayout());
                noResultPanel.setOpaque(false);
                noResultPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
                
                JLabel lblNoResult = new JLabel("Không có bài viết nào", SwingConstants.CENTER);
                lblNoResult.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblNoResult.setForeground(Color.GRAY);
                
                noResultPanel.add(lblNoResult, BorderLayout.CENTER);
                centerPanel.add(noResultPanel);
            }
            
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
            
            // Scroll to top
            scrollToTop();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải bài viết: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 


