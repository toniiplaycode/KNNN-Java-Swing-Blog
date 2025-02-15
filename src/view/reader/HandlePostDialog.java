package view.reader;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.awt.image.BufferedImage;
import java.awt.image.Graphics2D;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;
import java.net.URL;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;

public class HandlePostDialog extends JDialog {
    // Khai báo các biến thành viên
    private Connection connection; // Kết nối database
    private int userId; // ID người dùng
    private Runnable onPostAdded; // Callback khi thêm/sửa bài viết
    private int postId; // ID bài viết (khi ở chế độ sửa)
    private boolean isEditMode; // Chế độ sửa/thêm mới
    private JTextField txtTitle; // Trường nhập tiêu đề
    private JTextPane txtContent; // Trường nhập nội dung
    private JTextArea txtImageUrl; // Trường nhập URL ảnh
    private JLabel lblTitle; // Label tiêu đề
    private JLabel lblContent; // Label nội dung
    private JLabel lblImageUrl; // Label URL ảnh
    private JPanel categoryPanel; // Panel chứa danh sách thể loại
    private List<JCheckBox> categoryCheckboxes; // Danh sách checkbox thể loại
    private JLabel lblImagePreview; // Label xem trước ảnh
    private Timer previewTimer; // Timer để delay xem trước ảnh
    
    // Khởi tạo dialog tạo bài viết mới
    public HandlePostDialog(JFrame parent, int userId, Runnable onPostAdded) {
        super(parent, "Tạo bài viết mới", true);
        this.userId = userId;
        this.onPostAdded = onPostAdded;
        this.isEditMode = false;
        initializeUI();
    }
    
    // Khởi tạo dialog chỉnh sửa bài viết
    public HandlePostDialog(JFrame parent, int userId, int postId, String title, 
            String content, String imageUrl, Runnable onPostAdded) {
        super(parent, "Chỉnh sửa bài viết", true);
        this.userId = userId;
        this.postId = postId;
        this.onPostAdded = onPostAdded;
        this.isEditMode = true;
        
        initializeUI();
        
        txtTitle.setText(title);
        txtContent.setText(content);
        txtImageUrl.setText(imageUrl != null ? imageUrl : "");
    }
    
    // Khởi tạo giao diện người dùng
    private void initializeUI() {
        setSize(1000, 800);
        setLocationRelativeTo(null);
        
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(245, 246, 247));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 220, 224)),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));
        
        JLabel titleLabel = new JLabel("Tạo bài viết mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(32, 33, 36));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(new Color(245, 246, 247));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        lblTitle = new JLabel("Tiêu đề");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTitle = new JTextField(20);
        txtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTitle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        lblContent = new JLabel("Nội dung");
        lblContent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtContent = new JTextPane();
        txtContent.setContentType("text/html");
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JToolBar formatToolbar = createFormatToolbar(txtContent);
        formatToolbar.setBackground(Color.WHITE);
        formatToolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JPanel editorPanel = new JPanel(new BorderLayout(0, 10));
        editorPanel.setOpaque(false);
        editorPanel.add(formatToolbar, BorderLayout.NORTH);
        
        JScrollPane scrollContent = new JScrollPane(txtContent);
        scrollContent.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));
        scrollContent.setPreferredSize(new Dimension(0, 300));
        editorPanel.add(scrollContent, BorderLayout.CENTER);
        
        // Label cho URL ảnh
        lblImageUrl = new JLabel("URL ảnh:");
        lblImageUrl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Panel chứa text field và preview
        JPanel imageContentPanel = new JPanel(new BorderLayout(5, 5));
        imageContentPanel.setOpaque(false);
        
        // Text field cho URL
        txtImageUrl = new JTextArea(2, 20);
        txtImageUrl.setLineWrap(true);
        txtImageUrl.setWrapStyleWord(true);
        txtImageUrl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Preview label
        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(200, 200));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Thêm DocumentListener cho txtImageUrl
        previewTimer = new Timer(500, e -> loadImagePreview());
        previewTimer.setRepeats(false);
        
        txtImageUrl.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { restartTimer(); }
            public void removeUpdate(DocumentEvent e) { restartTimer(); }
            public void insertUpdate(DocumentEvent e) { restartTimer(); }
        });
        
        imageContentPanel.add(new JScrollPane(txtImageUrl), BorderLayout.CENTER);
        imageContentPanel.add(lblImagePreview, BorderLayout.EAST);
        
        // Category selection
        JLabel lblCategory = new JLabel("Thể loại");
        lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        categoryPanel.setOpaque(false);
        categoryCheckboxes = new ArrayList<>();
        
        // Load categories từ database
        loadCategories();
        
        // Nếu đang ở chế độ edit, load các category đã chọn
        if (isEditMode) {
            loadSelectedCategories();
        }
        
        JScrollPane categoryScroll = new JScrollPane(categoryPanel);
        categoryScroll.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));
        categoryScroll.setPreferredSize(new Dimension(0, 80));
        
        formPanel.add(createFormGroup(lblTitle, txtTitle));
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(createFormGroup(lblContent, editorPanel));
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(createFormGroup(lblImageUrl, imageContentPanel));
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(createFormGroup(lblCategory, categoryScroll));
        
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 220, 224)),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));
        
        JButton btnCancel = createButton("Hủy", new Color(218, 220, 224), Color.BLACK);
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnPost = createButton("Đăng bài", new Color(24, 119, 242), Color.WHITE);
        btnPost.addActionListener(e -> {
            if (savePost(txtTitle.getText().trim(), 
                        txtContent.getText().trim(), 
                        txtImageUrl.getText().trim())) {
                dispose();
            }
        });
        
        footerPanel.add(btnCancel);
        footerPanel.add(btnPost);
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainContainer);
        setupKeyboardShortcuts(txtContent);
    }
    
    // Tạo nhóm các trường nhập liệu
    private JPanel createFormGroup(JLabel label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    
    // Tạo và tùy chỉnh nút
    private JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 36));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    // Tạo thanh công cụ định dạng văn bản
    private JToolBar createFormatToolbar(JTextPane txtContent) {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        addFormatButton(toolbar, "B", "In đậm (Ctrl+B)", "bold", txtContent);
        addFormatButton(toolbar, "I", "In nghiêng (Ctrl+I)", "italic", txtContent);
        addFormatButton(toolbar, "U", "Gạch chân (Ctrl+U)", "underline", txtContent);
        toolbar.addSeparator();
        
        addListButton(toolbar, "•", "Tạo danh sách", false, txtContent);
        addListButton(toolbar, "1.", "Tạo danh sách đánh số", true, txtContent);
        toolbar.addSeparator();
        
        toolbar.add(new JLabel(" Cỡ chữ: "));
        String[] sizes = {"12", "14", "16", "18", "20", "24", "28", "32"};
        JComboBox<String> cbFontSize = new JComboBox<>(sizes);
        cbFontSize.addActionListener(e -> 
            applyFontSize(txtContent, Integer.parseInt(cbFontSize.getSelectedItem().toString())));
        toolbar.add(cbFontSize);
        
        return toolbar;
    }
    
    // Tùy chỉnh style cho nút
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
    
    // Thiết lập phím tắt cho các chức năng định dạng
    private void setupKeyboardShortcuts(JTextPane textPane) {
        InputMap inputMap = textPane.getInputMap();
        ActionMap actionMap = textPane.getActionMap();
        
        KeyStroke boldKey = KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        inputMap.put(boldKey, "bold-action");
        actionMap.put("bold-action", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                applyFormat(textPane, "bold");
            }
        });
        
        KeyStroke italicKey = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        inputMap.put(italicKey, "italic-action");
        actionMap.put("italic-action", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                applyFormat(textPane, "italic");
            }
        });
        
        KeyStroke underlineKey = KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        inputMap.put(underlineKey, "underline-action");
        actionMap.put("underline-action", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                applyFormat(textPane, "underline");
            }
        });
    }
    
    // Thêm nút định dạng vào toolbar
    private void addFormatButton(JToolBar toolbar, String text, String tooltip, String format, JTextPane textPane) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        if (format.equals("bold")) {
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        } else if (format.equals("italic")) {
            button.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        } else {
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        button.addActionListener(e -> applyFormat(textPane, format));
        toolbar.add(button);
    }
    
    // Thêm nút tạo danh sách vào toolbar
    private void addListButton(JToolBar toolbar, String text, String tooltip, boolean numbered, JTextPane textPane) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.addActionListener(e -> {
            if (numbered) {
                createNumberedList(textPane);
            } else {
                createList(textPane);
            }
        });
        toolbar.add(button);
    }
    
    // Áp dụng định dạng cho văn bản được chọn
    private void applyFormat(JTextPane textPane, String format) {
        StyledEditorKit kit = (StyledEditorKit) textPane.getEditorKit();
        MutableAttributeSet attr = new SimpleAttributeSet();
        
        switch (format) {
            case "bold":
                StyleConstants.setBold(attr, !StyleConstants.isBold(textPane.getCharacterAttributes()));
                break;
            case "italic":
                StyleConstants.setItalic(attr, !StyleConstants.isItalic(textPane.getCharacterAttributes()));
                break;
            case "underline":
                StyleConstants.setUnderline(attr, !StyleConstants.isUnderline(textPane.getCharacterAttributes()));
                break;
        }
        
        kit.getInputAttributes().addAttributes(attr);
        textPane.setCharacterAttributes(attr, false);
    }
    
    // Áp dụng cỡ chữ cho văn bản được chọn
    private void applyFontSize(JTextPane textPane, int size) {
        StyledEditorKit kit = (StyledEditorKit) textPane.getEditorKit();
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontSize(attr, size);
        kit.getInputAttributes().addAttributes(attr);
        textPane.setCharacterAttributes(attr, false);
    }
    
    // Tạo danh sách không đánh số
    private void createList(JTextPane textPane) {
        String selectedText = textPane.getSelectedText();
        if (selectedText != null) {
            String[] lines = selectedText.split("\n");
            StringBuilder listText = new StringBuilder("<ul>");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    listText.append("<li>").append(line.trim()).append("</li>");
                }
            }
            listText.append("</ul>");
            
            try {
                HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
                kit.insertHTML((HTMLDocument) textPane.getDocument(), 
                    textPane.getCaretPosition(), listText.toString(), 0, 0, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Tạo danh sách đánh số
    private void createNumberedList(JTextPane textPane) {
        String selectedText = textPane.getSelectedText();
        if (selectedText != null) {
            String[] lines = selectedText.split("\n");
            StringBuilder listText = new StringBuilder("<ol>");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    listText.append("<li>").append(line.trim()).append("</li>");
                }
            }
            listText.append("</ol>");
            
            try {
                HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
                kit.insertHTML((HTMLDocument) textPane.getDocument(), 
                    textPane.getCaretPosition(), listText.toString(), 0, 0, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Tải danh sách thể loại từ database
    private void loadCategories() {
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT id, title FROM tbl_category ORDER BY title";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                JCheckBox checkbox = new JCheckBox(rs.getString("title"));
                checkbox.putClientProperty("category_id", rs.getInt("id"));
                checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                checkbox.setOpaque(false);
                categoryCheckboxes.add(checkbox);
                categoryPanel.add(checkbox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Tải các thể loại đã chọn (cho chế độ sửa)
    private void loadSelectedCategories() {
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT category_id FROM tbl_post_category WHERE post_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            
            Set<Integer> selectedIds = new HashSet<>();
            while (rs.next()) {
                selectedIds.add(rs.getInt("category_id"));
            }
            
            // Check các checkbox tương ứng
            for (JCheckBox checkbox : categoryCheckboxes) {
                int categoryId = (int) checkbox.getClientProperty("category_id");
                checkbox.setSelected(selectedIds.contains(categoryId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private boolean savePost(String title, String content, String imageUrls) {
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ tiêu đề và nội dung",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false); // Bắt đầu transaction
            
            // Lưu post
            String query;
            PreparedStatement ps;
            int newPostId = 0;
            int result = 0;
            
            // Xử lý nội dung HTML trước khi lưu
            String cleanContent = content
                .replaceAll("(?s)<head\\b[^>]*>.*?</head>", "")
                .replaceAll("(?i)</?html[^>]*>", "")
                .replaceAll("(?i)</?body[^>]*>", "")
                .replaceAll("\\s+", " ")
                .trim();
            
            if (isEditMode) {
                query = "UPDATE tbl_post SET title = ?, content = ?, hash_img = ? WHERE id = ? AND user_id = ?";
                ps = connection.prepareStatement(query);
                ps.setString(1, title);
                ps.setString(2, cleanContent);
                ps.setString(3, imageUrls.isEmpty() ? null : imageUrls);
                ps.setInt(4, postId);
                ps.setInt(5, userId);
                result = ps.executeUpdate();
                newPostId = postId;
            } else {
                query = "INSERT INTO tbl_post (user_id, title, content, hash_img) VALUES (?, ?, ?, ?)";
                ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setString(2, title);
                ps.setString(3, cleanContent);
                ps.setString(4, imageUrls.isEmpty() ? null : imageUrls);
                result = ps.executeUpdate();
                
                // Lấy ID của post vừa tạo
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    newPostId = generatedKeys.getInt(1);
                }
            }
            
            if (result > 0) {
                // Xóa categories cũ nếu là edit mode
                if (isEditMode) {
                    query = "DELETE FROM tbl_post_category WHERE post_id = ?";
                    ps = connection.prepareStatement(query);
                    ps.setInt(1, postId);
                    ps.executeUpdate();
                }
                
                // Lưu categories mới
                query = "INSERT INTO tbl_post_category (post_id, category_id) VALUES (?, ?)";
                ps = connection.prepareStatement(query);
                
                for (JCheckBox checkbox : categoryCheckboxes) {
                    if (checkbox.isSelected()) {
                        int categoryId = (int) checkbox.getClientProperty("category_id");
                        ps.setInt(1, newPostId);
                        ps.setInt(2, categoryId);
                        ps.executeUpdate();
                    }
                }
                
                connection.commit(); // Commit transaction
                
                JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật bài viết thành công!" : "Đăng bài thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Gọi callback để cập nhật UI
                SwingUtilities.invokeLater(() -> {
                    if (onPostAdded != null) {
                        onPostAdded.run();
                    }
                });
                
                return true;
            }
            
            return false;
            
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi " + (isEditMode ? "cập nhật" : "đăng") + " bài: " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void restartTimer() {
        previewTimer.restart();
    }
    
    private void loadImagePreview() {
        String imageUrl = txtImageUrl.getText().trim();
        if (imageUrl.isEmpty()) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("No image");
            return;
        }

        try {
            // Tải ảnh trong thread riêng để không block UI
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    URL url = new URL(imageUrl);
                    BufferedImage originalImage = ImageIO.read(url);
                    if (originalImage == null) return null;
                    
                    // Tăng kích thước target từ 150x150 lên 200x200
                    int targetWidth = 200;
                    int targetHeight = 200;
                    
                    double scale = Math.min(
                        (double) targetWidth / originalImage.getWidth(),
                        (double) targetHeight / originalImage.getHeight()
                    );
                    
                    int scaledWidth = (int) (originalImage.getWidth() * scale);
                    int scaledHeight = (int) (originalImage.getHeight() * scale);
                    
                    BufferedImage scaledImage = new BufferedImage(
                        scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB
                    );
                    
                    Graphics2D g2d = scaledImage.createGraphics();
                    g2d.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR
                    );
                    g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
                    g2d.dispose();
                    
                    return new ImageIcon(scaledImage);
                }
                
                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null) {
                            lblImagePreview.setIcon(icon);
                            lblImagePreview.setText(null);
                        } else {
                            lblImagePreview.setIcon(null);
                            lblImagePreview.setText("Invalid image");
                        }
                    } catch (Exception e) {
                        lblImagePreview.setIcon(null);
                        lblImagePreview.setText("Error loading image");
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
            
        } catch (Exception e) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Invalid URL");
            e.printStackTrace();
        }
    }
} 