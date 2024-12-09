package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import utils.DBConnection; // Assuming this class handles DB connection

public class ListUsers extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private JTextField txtUsername, txtEmail, txtGender, txtAddress, txtPassword;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    // Database connection
    private Connection connection;

    public ListUsers() {
        // Set the layout for the main panel as BorderLayout to allow the table to take up all space
        setLayout(new BorderLayout());

        // Initialize the table with default model for users
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ensure all columns resize automatically

        // Make the table take full width and height of the container
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Add the scrollPane to the center of the panel, so it will expand to fill the space
        add(scrollPane, BorderLayout.CENTER);

        // Create the input fields panel (for adding/updating users)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2, 10, 10));  // 6 rows, 2 columns for input fields
        add(inputPanel, BorderLayout.SOUTH);  // Add at the bottom

        // Input fields for user information
        inputPanel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        inputPanel.add(txtUsername);

        inputPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        inputPanel.add(new JLabel("Password:"));
        txtPassword = new JTextField();
        inputPanel.add(txtPassword);

        inputPanel.add(new JLabel("Gender:"));
        txtGender = new JTextField();
        inputPanel.add(txtGender);

        inputPanel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        inputPanel.add(txtAddress);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        inputPanel.add(buttonPanel);

        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // Add ActionListeners for buttons
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUser();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });

        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        // Initialize DB connection and load users
        connectToDB();
        loadUsers();
    }

    private void connectToDB() {
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            String sql = "SELECT * FROM tbl_user";
            PreparedStatement statement = connection.prepareStatement(sql);
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

            table.setModel(model); // Set the table model

            // Ensure the columns are resized
            for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                TableColumn column = table.getColumnModel().getColumn(columnIndex);
                column.setPreferredWidth(150); // You can set column widths here if necessary
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUser() {
        // Implementation for adding a user
    }

    private void updateUser() {
        // Implementation for updating a user
    }

    private void deleteUser() {
        // Implementation for deleting a user
    }

    private void clearFields() {
        // Implementation for clearing fields
    }
}
