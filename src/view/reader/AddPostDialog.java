package view.reader;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;

public class AddPostDialog extends JDialog {
    private Connection connection;
    private int userId;
    private Runnable onPostAdded; // Callback khi thêm bài viết thành công
    
    public AddPostDialog(JFrame parent, int userId, Runnable onPostAdded) {
        super(parent, "Tạo bài viết mới", true);
        this.userId = userId;
        this.onPostAdded = onPostAdded;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Tiêu đề
        JLabel lblTitle = new JLabel("Tiêu đề");
        JTextField txtTitle = new JTextField(20);
        txtTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        // Nội dung với định dạng
        JLabel lblContent = new JLabel("Nội dung");
        JTextPane txtContent = new JTextPane();
        txtContent.setContentType("text/html");
        
        // Toolbar cho định dạng văn bản
        JToolBar formatToolbar = createFormatToolbar(txtContent);
        
        // Panel chứa toolbar và editor
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(formatToolbar, BorderLayout.NORTH);
        editorPanel.add(new JScrollPane(txtContent), BorderLayout.CENTER);
        
        // URL ảnh
        JLabel lblImageUrl = new JLabel("URL ảnh");
        JTextArea txtImageUrl = new JTextArea(4, 20);
        txtImageUrl.setLineWrap(true);
        JScrollPane scrollImage = new JScrollPane(txtImageUrl);
        
        // Thêm các components vào form
        formPanel.add(lblTitle);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(txtTitle);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(lblContent);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(editorPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        formPanel.add(lblImageUrl);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(scrollImage);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton btnCancel = new JButton("Hủy");
        styleButton(btnCancel, new Color(231, 76, 60));
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnPost = new JButton("Đăng bài");
        styleButton(btnPost, new Color(52, 152, 219));
        btnPost.addActionListener(e -> {
            if (savePost(txtTitle.getText().trim(), 
                        txtContent.getText().trim(), 
                        txtImageUrl.getText().trim())) {
                dispose();
            }
        });
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnPost);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setupKeyboardShortcuts(txtContent);
    }
    
    private JToolBar createFormatToolbar(JTextPane txtContent) {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // Các nút định dạng
        addFormatButton(toolbar, "B", "In đậm (Ctrl+B)", "bold", txtContent);
        addFormatButton(toolbar, "I", "In nghiêng (Ctrl+I)", "italic", txtContent);
        addFormatButton(toolbar, "U", "Gạch chân (Ctrl+U)", "underline", txtContent);
        toolbar.addSeparator();
        
        // Nút danh sách
        addListButton(toolbar, "•", "Tạo danh sách", false, txtContent);
        addListButton(toolbar, "1.", "Tạo danh sách đánh số", true, txtContent);
        toolbar.addSeparator();
        
        // Font size
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
        
        // Ctrl+B cho in đậm
        KeyStroke boldKey = KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        inputMap.put(boldKey, "bold-action");
        actionMap.put("bold-action", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                applyFormat(textPane, "bold");
            }
        });
        
        // Ctrl+I cho in nghiêng
        KeyStroke italicKey = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        inputMap.put(italicKey, "italic-action");
        actionMap.put("italic-action", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                applyFormat(textPane, "italic");
            }
        });
        
        // Ctrl+U cho gạch chân
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
            String query = "INSERT INTO tbl_post (user_id, title, content, hash_img) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setString(2, title);
            
            // Loại bỏ các thẻ HTML bao ngoài và khoảng trắng thừa
            String cleanContent = content
                .replaceAll("(?s)<head\\b[^>]*>.*?</head>", "") // Xóa toàn bộ phần head và nội dung bên trong
                .replaceAll("(?i)</?html[^>]*>", "") // Xóa thẻ html mở và đóng
                .replaceAll("(?i)</?body[^>]*>", "") // Xóa thẻ body mở và đóng
                .replaceAll("\\s+", " ") // Thay thế nhiều khoảng trắng bằng 1 khoảng trắng
                .trim(); // Xóa khoảng trắng đầu và cuối
                
            ps.setString(3, cleanContent);
            ps.setString(4, imageUrls);
            ps.executeUpdate();
            
            // Gọi callback để refresh danh sách bài viết
            if (onPostAdded != null) {
                onPostAdded.run();
            }
            
            JOptionPane.showMessageDialog(this,
                "Đăng bài thành công!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
                
            return true;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi đăng bài: " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
} 