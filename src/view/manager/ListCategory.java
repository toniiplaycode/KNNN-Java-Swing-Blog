package view.manager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import utils.DBConnection;

public class ListCategory extends JPanel {
    private JTable table;
    private JTextField txtTitle, txtDescription, txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private DefaultTableModel tableModel;
    private Connection connection;
    private int selectedId = -1;

    public ListCategory() {
        setLayout(new BorderLayout());

        // Initialize table model
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Tiêu đề", "Mô tả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép edit trực tiếp trên bảng
            }
        };
        table = new JTable(tableModel);

        // Create search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(new JLabel("Tìm kiếm: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(searchPanel, BorderLayout.NORTH);

        // Style the table
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.getTableHeader().setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(70, 130, 180));
        table.setSelectionForeground(Color.WHITE);

        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Setup input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tiêu đề:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtTitle = new JTextField(20);
        formPanel.add(txtTitle, gbc);

        // Description field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1;
        txtDescription = new JTextField(20);
        formPanel.add(txtDescription, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        btnAdd = new JButton("Thêm");
        styleButton(btnAdd, new Color(46, 204, 113));
        
        btnUpdate = new JButton("Cập nhật");
        styleButton(btnUpdate, new Color(52, 152, 219));
        btnUpdate.setEnabled(false);
        
        btnDelete = new JButton("Xóa");
        styleButton(btnDelete, new Color(231, 76, 60));
        btnDelete.setEnabled(false);
        
        btnClear = new JButton("Làm mới");
        styleButton(btnClear, new Color(149, 165, 166));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        inputPanel.add(formPanel, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.SOUTH);

        // Add action listeners
        btnAdd.addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnClear.addActionListener(e -> clearFields());

        // Search functionality
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchCategories(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchCategories(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchCategories(); }
        });

        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedId = (int) table.getValueAt(selectedRow, 0);
                    txtTitle.setText((String) table.getValueAt(selectedRow, 1));
                    txtDescription.setText((String) table.getValueAt(selectedRow, 2));
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                    btnAdd.setEnabled(false);
                } else {
                    clearFields();
                }
            }
        });

        loadCategories();
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));
    }

    private void loadCategories() {
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT * FROM tbl_category ORDER BY id DESC";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải danh sách thể loại: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCategories() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT * FROM tbl_category WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ? ORDER BY id DESC";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, "%" + searchText + "%");
            ps.setString(2, "%" + searchText + "%");
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCategory() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tiêu đề thể loại",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            connection = DBConnection.getConnection();
            String query = "INSERT INTO tbl_category (title, description) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, title);
            ps.setString(2, description);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Thêm thể loại thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadCategories();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi thêm thể loại: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCategory() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tiêu đề thể loại",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            connection = DBConnection.getConnection();
            String query = "UPDATE tbl_category SET title = ?, description = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, selectedId);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật thể loại thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadCategories();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi cập nhật thể loại: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategory() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa thể loại này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            try {
                connection = DBConnection.getConnection();
                
                // Xóa các liên kết trong bảng tbl_post_category trước
                String deleteLinksQuery = "DELETE FROM tbl_post_category WHERE category_id = ?";
                PreparedStatement psLinks = connection.prepareStatement(deleteLinksQuery);
                psLinks.setInt(1, selectedId);
                psLinks.executeUpdate();
                
                // Sau đó xóa category
                String query = "DELETE FROM tbl_category WHERE id = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, selectedId);
                
                int result = ps.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Xóa thể loại thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadCategories();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xóa thể loại: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        txtTitle.setText("");
        txtDescription.setText("");
        selectedId = -1;
        table.clearSelection();
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
} 