package view.manager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import connection.DBConnection;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ListCategory extends JPanel {
    // Khai báo các thành phần UI
    private JTable table;
    private JTextField txtTitle, txtDescription, txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private DefaultTableModel tableModel;
    private Connection connection;
    private int selectedId = -1; // ID của thể loại đang được chọn

    // Constructor - Khởi tạo giao diện quản lý thể loại
    public ListCategory() {
        setLayout(new BorderLayout());

        // Khởi tạo model cho bảng với các cột
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Tiêu đề", "Mô tả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép edit trực tiếp trên bảng
            }
        };
        table = new JTable(tableModel);

        // Tạo panel tìm kiếm
        JPanel searchPanel = new JPanel(new BorderLayout());
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(new JLabel("Tìm kiếm: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(searchPanel, BorderLayout.NORTH);

        // Tùy chỉnh giao diện bảng
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.getTableHeader().setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(70, 130, 180));
        table.setSelectionForeground(Color.WHITE);

        // Tạo thanh cuộn cho bảng
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Thiết lập panel nhập liệu
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel chứa form nhập liệu
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Trường nhập tiêu đề
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tiêu đề:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtTitle = new JTextField(20);
        formPanel.add(txtTitle, gbc);

        // Trường nhập mô tả
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1;
        txtDescription = new JTextField(20);
        formPanel.add(txtDescription, gbc);

        // Panel chứa các nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // Tạo và style các nút
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

        // Thêm các nút vào panel
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        inputPanel.add(formPanel, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.SOUTH);

        // Thêm sự kiện cho các nút
        btnAdd.addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnClear.addActionListener(e -> clearFields());

        // Thêm chức năng tìm kiếm realtime
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchCategories(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchCategories(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchCategories(); }
        });

        // Xử lý sự kiện khi chọn dòng trong bảng
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

        // Tải dữ liệu ban đầu
        loadCategories();
    }

    // Phương thức tạo style cho nút
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));
    }

    // Phương thức tải danh sách thể loại từ database
    private void loadCategories() {
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT * FROM tbl_category ORDER BY id DESC";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0); // Xóa dữ liệu cũ
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

    // Phương thức tìm kiếm thể loại
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

    // Phương thức thêm thể loại mới
    private void addCategory() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        
        // Kiểm tra dữ liệu đầu vào
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

    // Phương thức cập nhật thể loại
    private void updateCategory() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        
        // Kiểm tra dữ liệu đầu vào
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

    // Phương thức xóa thể loại
    private void deleteCategory() {
        // Hiển thị dialog xác nhận xóa
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

    // Phương thức xóa trắng các trường nhập liệu
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