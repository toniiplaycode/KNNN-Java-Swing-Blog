package view.reader;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;

public class HandlePostDialog extends JDialog {
    private Connection connection;
    private int userId;
    private Runnable onPostAdded;
    private int postId;
    private boolean isEditMode;
    private JTextField txtTitle;
    private JTextPane txtContent;
    private JTextArea txtImageUrl;
    private JLabel lblTitle;
    private JLabel lblContent;
    private JLabel lblImageUrl;
    
    public HandlePostDialog(JFrame parent, int userId, Runnable onPostAdded) {
        super(parent, "Tạo bài viết mới", true);
        this.userId = userId;
        this.onPostAdded = onPostAdded;
        this.isEditMode = false;
        initializeUI();
    }
    
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
    
    private void initializeUI() {
        setSize(1000, 700);
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
        
        lblImageUrl = new JLabel("URL ảnh");
        lblImageUrl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtImageUrl = new JTextArea(4, 20);
        txtImageUrl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtImageUrl.setLineWrap(true);
        JScrollPane scrollImage = new JScrollPane(txtImageUrl);
        scrollImage.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));
        
        formPanel.add(createFormGroup(lblTitle, txtTitle));
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(createFormGroup(lblContent, editorPanel));
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(createFormGroup(lblImageUrl, scrollImage));
        
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
    
    private JPanel createFormGroup(JLabel label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    
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
    
    private void applyFontSize(JTextPane textPane, int size) {
        StyledEditorKit kit = (StyledEditorKit) textPane.getEditorKit();
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontSize(attr, size);
        kit.getInputAttributes().addAttributes(attr);
        textPane.setCharacterAttributes(attr, false);
    }
    
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
            String query;
            PreparedStatement ps;
            
            // Xử lý nội dung HTML trước khi lưu
            String cleanContent = content
                .replaceAll("(?s)<head\\b[^>]*>.*?</head>", "") // Xóa thẻ head và nội dung bên trong
                .replaceAll("(?i)</?html[^>]*>", "") // Xóa thẻ html mở và đóng
                .replaceAll("(?i)</?body[^>]*>", "") // Xóa thẻ body mở và đóng
                .replaceAll("\\s+", " ") // Thay thế nhiều khoảng trắng bằng 1 khoảng trắng
                .trim(); // Xóa khoảng trắng đầu và cuối
            
            if (isEditMode) {
                query = "UPDATE tbl_post SET title = ?, content = ?, hash_img = ? WHERE id = ? AND user_id = ?";
                ps = connection.prepareStatement(query);
                ps.setString(1, title);
                ps.setString(2, cleanContent);
                ps.setString(3, imageUrls.isEmpty() ? null : imageUrls);
                ps.setInt(4, postId);
                ps.setInt(5, userId);
            } else {
                query = "INSERT INTO tbl_post (user_id, title, content, hash_img) VALUES (?, ?, ?, ?)";
                ps = connection.prepareStatement(query);
                ps.setInt(1, userId);
                ps.setString(2, title);
                ps.setString(3, cleanContent);
                ps.setString(4, imageUrls.isEmpty() ? null : imageUrls);
            }
            
            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật bài viết thành công!" : "Đăng bài thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                if (onPostAdded != null) {
                    onPostAdded.run();
                }
                return true;
            }
            return false;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi " + (isEditMode ? "cập nhật" : "đăng") + " bài: " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
} 