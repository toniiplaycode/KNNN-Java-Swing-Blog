package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import utils.DBConnection; // Assuming this class handles DB connection

public class ListUsers extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private JTextField txtUsername, txtEmail, txtAddress, txtSearch; // Add txtSearch for search input
    private JPasswordField txtPassword;
    private JRadioButton rbtnMale, rbtnFemale;
    private ButtonGroup genderGroup;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private Connection connection;

    public ListUsers() {
        setLayout(new BorderLayout());

        // Create search field
        JPanel searchPanel = new JPanel(new BorderLayout());
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(new JLabel("Search "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // top, left, bottom, right margin
        add(searchPanel, BorderLayout.NORTH);  // Add search panel at the top

        // Initialize table
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Styling for the table
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(56, 142, 60));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Zebra striping for rows
        table.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // Create input panel for user data
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        add(inputPanel, BorderLayout.SOUTH);

        inputPanel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        inputPanel.add(txtUsername);

        inputPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        inputPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        inputPanel.add(txtPassword);

        inputPanel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        inputPanel.add(txtAddress);

        inputPanel.add(new JLabel("Gender:"));
        JPanel genderPanel = new JPanel();
        rbtnMale = new JRadioButton("Male");
        rbtnFemale = new JRadioButton("Female");
        genderGroup = new ButtonGroup();
        genderGroup.add(rbtnMale);
        genderGroup.add(rbtnFemale);
        genderPanel.add(rbtnMale);
        genderPanel.add(rbtnFemale);
        inputPanel.add(genderPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        inputPanel.add(buttonPanel);

        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // Add action listeners for buttons
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearFields());

        // Initialize DB connection and load users
        connectToDB();
        loadUsers("");

        // Add DocumentListener to search field
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchUsers(txtSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchUsers(txtSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchUsers(txtSearch.getText());
            }
        });
    }

    private void connectToDB() {
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers(String searchQuery) {
        try {
            String sql = "SELECT * FROM tbl_user WHERE username LIKE ? OR email LIKE ? OR address LIKE ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            String searchPattern = "%" + searchQuery + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            ResultSet resultSet = statement.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Username");
            model.addColumn("Email");
            model.addColumn("Gender");
            model.addColumn("Address");

            while (resultSet.next()) {
                model.addRow(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("gender"),
                        resultSet.getString("address")
                });
            }

            table.setModel(model);
            for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                TableColumn column = table.getColumnModel().getColumn(columnIndex);
                column.setPreferredWidth(150);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to filter users based on search input
    private void searchUsers(String query) {
        loadUsers(query);
    }

    private void addUser() {
        try {
            String username = txtUsername.getText();
            String email = txtEmail.getText();
            String password = new String(txtPassword.getPassword());  // Get password from JPasswordField
            String gender = "";

            // Check if a gender is selected
            if (rbtnMale.isSelected()) {
                gender = "Male";  // Male
            } else if (rbtnFemale.isSelected()) {
                gender = "Female";  // Female
            } else {
                JOptionPane.showMessageDialog(this, "Please select a gender.");
                return;  // Exit the method if no gender is selected
            }

            String address = txtAddress.getText();

            String sql = "INSERT INTO tbl_user (username, email, password, gender, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, gender);
            statement.setString(5, address);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                loadUsers("");  // Reload the users to update the table
                clearFields();  // Clear input fields after adding
            } else {
                JOptionPane.showMessageDialog(this, "Error adding user.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateUser() {
        // Step 1: Get selected row from table
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to update.");
            return;
        }

        // Get user ID from the selected row (assuming ID is in the first column)
        int userId = (int) table.getValueAt(selectedRow, 0);  

        // Step 2: Pre-fill text fields with current data from the selected row
        String currentUsername = (String) table.getValueAt(selectedRow, 1);
        String currentEmail = (String) table.getValueAt(selectedRow, 2);
        String currentGender = (String) table.getValueAt(selectedRow, 3);
        String currentAddress = (String) table.getValueAt(selectedRow, 4);

        // Fill input fields with current data
        txtUsername.setText(currentUsername);
        txtEmail.setText(currentEmail);
        txtAddress.setText(currentAddress);

        // Select gender radio button based on current data
        if ("Male".equals(currentGender)) {
            rbtnMale.setSelected(true);
        } else {
            rbtnFemale.setSelected(true);
        }

        // Step 3: When update is clicked again, get the new data from input fields and update
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Capture the new data from input fields
                String username = txtUsername.getText();
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());  // Get password from JPasswordField
                String gender = rbtnMale.isSelected() ? "Male" : "Female";
                String address = txtAddress.getText();

                // If the password field is empty, keep the old password
                if (password.isEmpty()) {
                    password = (String) table.getValueAt(selectedRow, 2); // Retrieve old password from table
                }

                // Update the database
                try {
                    String sql = "UPDATE tbl_user SET username = ?, email = ?, password = ?, gender = ?, address = ? WHERE id = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, username);
                    statement.setString(2, email);
                    statement.setString(3, password);
                    statement.setString(4, gender);
                    statement.setString(5, address);
                    statement.setInt(6, userId);

                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(ListUsers.this, "User updated successfully!");
                        loadUsers("");  // Reload the users to update the table
                        clearFields();  // Clear input fields after updating
                    } else {
                        JOptionPane.showMessageDialog(ListUsers.this, "Error updating user.");
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ListUsers.this, "Error: " + ex.getMessage());
                }
            }
        });
    }

    private void deleteUser() {
        // Step 1: Get selected row from table
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        // Get user ID from the selected row
        int userId = (int) table.getValueAt(selectedRow, 0);

        // Step 2: Confirm deletion with the user
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", 
                                                     "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Step 3: Delete the user from the database
            try {
                String sql = "DELETE FROM tbl_user WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, userId);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully!");
                    loadUsers("");  // Reload the users to update the table
                } else {
                    JOptionPane.showMessageDialog(this, "Error deleting user.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        txtUsername.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        genderGroup.clearSelection();
        txtAddress.setText("");
    }

    private static class ZebraTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (row % 2 == 0) {
                component.setBackground(new Color(245, 245, 245));
            } else {
                component.setBackground(Color.WHITE);
            }

            if (isSelected) {
                component.setBackground(new Color(56, 142, 60));
                component.setForeground(Color.WHITE);
            } else {
                component.setForeground(Color.BLACK);
            }

            return component;
        }
    }
}
