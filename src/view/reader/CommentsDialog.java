package view.reader;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.sql.*;
import java.text.SimpleDateFormat;
import utils.DBConnection;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.awt.Image;
import java.awt.geom.Ellipse2D;

// Đảm bảo import đầy đủ các class cần thiết
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class CommentsDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private int postId;
    private int userId;
    private JPanel commentsPanel;
    private JTextField txtComment;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Runnable onCommentAdded; // Callback khi thêm comment
    private JPanel selectedCommentPanel; // Panel của comment đang được reply
    private Map<Integer, Boolean> repliesVisible = new HashMap<>(); // commentId -> isVisible
    private Map<Integer, List<Component>> loadedReplies = new HashMap<>();
    
    public CommentsDialog(JFrame parent, String postTitle, int postId, int userId, Runnable onCommentAdded) {
        super(parent, "Comments - " + postTitle, true);
        this.postId = postId;
        this.userId = userId;
        this.onCommentAdded = onCommentAdded;
        
        setLayout(new BorderLayout(10, 10));
        setSize(500, 600);
        setLocationRelativeTo(parent);
        
        // Comment input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtComment = new JTextField();
        txtComment.setPreferredSize(new Dimension(0, 35));
        
        JButton btnComment = new JButton("Comment");
        styleButton(btnComment, new Color(52, 152, 219));
        btnComment.addActionListener(e -> {
            String content = txtComment.getText().trim();
            if (!content.isEmpty()) {
                addComment(content);
                txtComment.setText("");
            }
        });
        
        inputPanel.add(txtComment, BorderLayout.CENTER);
        inputPanel.add(btnComment, BorderLayout.EAST);
        
        // Comments panel
        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(commentsPanel);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        loadComments();
    }
    
    private void loadComments() {
        try {
            connection = DBConnection.getConnection();
            String query = """
                SELECT c.*, u.username, u.avatar,
                    (SELECT COUNT(*) FROM tbl_comment WHERE comment_id = c.id) as reply_count
                FROM tbl_comment c 
                JOIN tbl_user u ON c.user_id = u.id 
                WHERE c.post_id = ? AND c.comment_id IS NULL
                ORDER BY c.create_at DESC
            """;
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            
            commentsPanel.removeAll();
            
            while (rs.next()) {
                JPanel commentPanel = createCommentPanel(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("content"),
                    rs.getTimestamp("create_at"),
                    rs.getInt("reply_count"),
                    0, // Indent level
                    rs.getString("avatar") // Thêm avatar
                );
                commentsPanel.add(commentPanel);
                commentsPanel.add(Box.createVerticalStrut(10));
            }
            
            commentsPanel.revalidate();
            commentsPanel.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading comments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createCommentPanel(int commentId, String username, String content, 
            Timestamp createAt, int replyCount, int indentLevel, String avatar) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(Color.WHITE);
        
        // Tạo 2 border riêng biệt
        Border lineBorder = new LineBorder(new Color(240, 240, 240));
        Border emptyBorder = new EmptyBorder(10, indentLevel * 20, 10, 10);
        panel.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));
        
        // Thêm tag để nhận biết level của comment
        panel.putClientProperty("comment_level", indentLevel);
        panel.putClientProperty("comment_id", commentId);
        
        // User info panel
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userInfoPanel.setOpaque(false);
        
        // Avatar
        JLabel avatarLabel = createAvatarLabel(username, avatar);
        userInfoPanel.add(avatarLabel);
        
        // Username and date
        JPanel userDatePanel = new JPanel();
        userDatePanel.setLayout(new BoxLayout(userDatePanel, BoxLayout.Y_AXIS));
        userDatePanel.setOpaque(false);
        
        JLabel lblUsername = new JLabel(username);
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel lblDate = new JLabel(sdf.format(createAt));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(Color.GRAY);
        
        userDatePanel.add(lblUsername);
        userDatePanel.add(lblDate);
        
        userInfoPanel.add(userDatePanel);
        
        // Comment content
        JTextArea txtContent = new JTextArea(content);
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setOpaque(false);
        
        panel.add(userInfoPanel, BorderLayout.NORTH);
        panel.add(txtContent, BorderLayout.CENTER);
        
        // Add reply button
        JButton btnReply = new JButton("Reply");
        btnReply.setBorderPainted(false);
        btnReply.setContentAreaFilled(false);
        btnReply.setForeground(new Color(52, 152, 219));
        btnReply.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Show replies button if has replies
        if (replyCount > 0) {
            JButton btnShowReplies = new JButton(replyCount + " replies");
            btnShowReplies.setBorderPainted(false);
            btnShowReplies.setContentAreaFilled(false);
            btnShowReplies.setForeground(new Color(100, 100, 100));
            
            // Thêm action để toggle replies
            btnShowReplies.addActionListener(e -> toggleReplies(commentId, panel, indentLevel, btnShowReplies));
            panel.add(btnShowReplies, BorderLayout.SOUTH);
        }
        
        btnReply.addActionListener(e -> showReplyForm(commentId, panel));
        
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setOpaque(false);
        actionsPanel.add(btnReply);
        
        panel.add(actionsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void toggleReplies(int parentId, JPanel parentPanel, int currentIndent, JButton toggleButton) {
        boolean isVisible = repliesVisible.getOrDefault(parentId, false);
        
        if (isVisible) {
            // Hide replies - xóa khỏi panel nhưng giữ lại trong loadedReplies
            List<Component> replies = loadedReplies.get(parentId);
            if (replies != null) {
                for (Component comp : replies) {
                    commentsPanel.remove(comp);
                }
            }
            toggleButton.setText(getRepliesCount(parentId) + " replies");
            repliesVisible.put(parentId, false);
        } else {
            // Show replies
            List<Component> replies = loadedReplies.get(parentId);
            if (replies == null) {
                // Nếu chưa load thì load mới
                loadReplies(parentId, parentPanel, currentIndent);
            } else {
                // Nếu đã load rồi thì hiển thị lại từ cache
                int insertIndex = commentsPanel.getComponentZOrder(parentPanel) + 1;
                for (Component comp : replies) {
                    commentsPanel.add(comp, insertIndex++);
                }
            }
            toggleButton.setText("Hide replies");
            repliesVisible.put(parentId, true);
        }
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }
    
    private void loadReplies(int parentId, JPanel parentPanel, int currentIndent) {
        try {
            String query = """
                SELECT c.*, u.username, u.avatar,
                    (SELECT COUNT(*) FROM tbl_comment WHERE comment_id = c.id) as reply_count
                FROM tbl_comment c 
                JOIN tbl_user u ON c.user_id = u.id 
                WHERE c.comment_id = ?
                ORDER BY c.create_at ASC
            """;
            
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            
            int parentIndex = commentsPanel.getComponentZOrder(parentPanel);
            List<Component> replies = new ArrayList<>();
            
            if (parentIndex != -1) {
                int insertIndex = parentIndex + 1;
                while (rs.next()) {
                    JPanel replyPanel = createCommentPanel(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getTimestamp("create_at"),
                        rs.getInt("reply_count"),
                        currentIndent + 1,
                        rs.getString("avatar")
                    );
                    commentsPanel.add(replyPanel, insertIndex++);
                    replies.add(replyPanel);
                    
                    Component strut = Box.createVerticalStrut(5);
                    commentsPanel.add(strut, insertIndex++);
                    replies.add(strut);
                }
                
                // Lưu replies vào cache
                loadedReplies.put(parentId, replies);
            }
            
            commentsPanel.revalidate();
            commentsPanel.repaint();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading replies: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showReplyForm(int parentId, JPanel parentPanel) {
        // Create reply form
        JPanel replyForm = new JPanel(new BorderLayout(5, 5));
        replyForm.setBorder(new EmptyBorder(10, 20, 10, 10));
        
        JTextArea txtReply = new JTextArea(2, 20);
        txtReply.setLineWrap(true);
        txtReply.setWrapStyleWord(true);
        
        JButton btnSubmit = new JButton("Reply");
        styleButton(btnSubmit, new Color(52, 152, 219));
        
        btnSubmit.addActionListener(e -> {
            String content = txtReply.getText().trim();
            if (!content.isEmpty()) {
                addReply(parentId, content);
                // Remove reply form
                commentsPanel.remove(replyForm);
                commentsPanel.revalidate();
                commentsPanel.repaint();
            }
        });
        
        replyForm.add(new JScrollPane(txtReply), BorderLayout.CENTER);
        replyForm.add(btnSubmit, BorderLayout.EAST);
        
        // Add reply form below parent comment
        int index = commentsPanel.getComponentZOrder(parentPanel);
        if (index != -1) {
            commentsPanel.add(replyForm, index + 1);
            commentsPanel.revalidate();
            commentsPanel.repaint();
        }
    }
    
    private void addReply(int parentId, String content) {
        try {
            String query = "INSERT INTO tbl_comment (post_id, user_id, content, comment_id) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.setInt(4, parentId);
            ps.executeUpdate();
            
            // Chỉ xóa cache của replies cho parent comment này
            loadedReplies.remove(parentId);
            
            // Giữ nguyên trạng thái hiển thị của replies
            boolean wasVisible = repliesVisible.getOrDefault(parentId, false);
            
            // Reload tất cả comments
            loadComments();
            
            // Khôi phục trạng thái hiển thị của replies nếu đang được hiển thị
            if (wasVisible) {
                // Tìm parent comment panel
                for (Component comp : commentsPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        Integer commentId = (Integer) panel.getClientProperty("comment_id");
                        if (commentId != null && commentId == parentId) {
                            // Lấy indent level của parent comment
                            Integer level = (Integer) panel.getClientProperty("comment_level");
                            if (level != null) {
                                // Load và hiển thị lại replies với indent level phù hợp
                                loadReplies(parentId, panel, level + 1);
                                repliesVisible.put(parentId, true);
                            }
                            break;
                        }
                    }
                }
            }
            
            // Notify parent
            if (onCommentAdded != null) {
                onCommentAdded.run();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error adding reply: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addComment(String content) {
        if (content.isEmpty()) return;
        
        try {
            connection = DBConnection.getConnection();
            String query = "INSERT INTO tbl_comment (post_id, user_id, content) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.executeUpdate();
            
            // Refresh danh sách comments
            loadComments();
            
            // Cập nhật số lượng comment trong NewsFeed
            if (onCommentAdded != null) {
                onCommentAdded.run();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error adding comment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Helper methods
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(color);
            }
        });
    }
    
    private ImageIcon createCircularAvatar(String letter) {
        int size = 30;
        BufferedImage image = new BufferedImage(size, size, TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(new Color(52, 152, 219));
        g2.fillOval(0, 0, size - 1, size - 1);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(letter)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(letter, x, y);
        
        g2.dispose();
        return new ImageIcon(image);
    }
    
    private JLabel createAvatarLabel(String username, String avatarUrl) {
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(30, 30));
        
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            try {
                // Load ảnh từ URL
                URL url = new URL(avatarUrl);
                BufferedImage originalImage = ImageIO.read(url);
                if (originalImage != null) {
                    // Tạo ảnh tròn
                    BufferedImage circularImage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = circularImage.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Vẽ hình tròn làm mask
                    g2.setColor(Color.WHITE);
                    g2.fillOval(0, 0, 29, 29);
                    
                    // Scale và crop ảnh gốc
                    Image scaledImage = originalImage.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    g2.setClip(new Ellipse2D.Float(0, 0, 29, 29));
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
    
    private int getRepliesCount(int parentId) {
        try {
            String query = "SELECT COUNT(*) as count FROM tbl_comment WHERE comment_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
} 