package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import utils.DBConnection;

public class ListUsers extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JTextField txtUsername, txtEmail, txtAddress, txtSearch, txtAvatar;
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
		searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(searchPanel, BorderLayout.NORTH);

		// Initialize table
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Styling for the table
		table.setFillsViewportHeight(true);
		table.setSelectionBackground(new Color(70, 130, 180));
		table.setSelectionForeground(Color.WHITE);
		table.setRowHeight(30);
		table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);

		// Create input panel for user data
		// Create input panel for user data
		JPanel inputPanel = new JPanel(new GridBagLayout());
		add(inputPanel, BorderLayout.SOUTH);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5); // Padding
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Add Username Label and TextField
		gbc.gridx = 0; // Column 0
		gbc.gridy = 0; // Row 0
		gbc.weightx = 0.2; // 20% of width
		inputPanel.add(new JLabel("Username:"), gbc);

		gbc.gridx = 1; // Column 1
		gbc.weightx = 0.8; // 80% of width
		txtUsername = new JTextField();
		inputPanel.add(txtUsername, gbc);

		// Add Email Label and TextField
		gbc.gridx = 0; 
		gbc.gridy = 1;
		gbc.weightx = 0.2;
		inputPanel.add(new JLabel("Email:"), gbc);

		gbc.gridx = 1; 
		gbc.weightx = 0.8;
		txtEmail = new JTextField();
		inputPanel.add(txtEmail, gbc);

		// Add Password Label and TextField
		gbc.gridx = 0; 
		gbc.gridy = 2;
		gbc.weightx = 0.2;
		inputPanel.add(new JLabel("Password:"), gbc);

		gbc.gridx = 1; 
		gbc.weightx = 0.8;
		txtPassword = new JPasswordField();
		inputPanel.add(txtPassword, gbc);

		// Add Address Label and TextField
		gbc.gridx = 0; 
		gbc.gridy = 3;
		gbc.weightx = 0.2;
		inputPanel.add(new JLabel("Address:"), gbc);

		gbc.gridx = 1; 
		gbc.weightx = 0.8;
		txtAddress = new JTextField();
		inputPanel.add(txtAddress, gbc);

		// Add Avatar Link Label and TextField
		gbc.gridx = 0; 
		gbc.gridy = 4;
		gbc.weightx = 0.2;
		inputPanel.add(new JLabel("Avatar Link:"), gbc);

		gbc.gridx = 1; 
		gbc.weightx = 0.8;
		txtAvatar = new JTextField();
		inputPanel.add(txtAvatar, gbc);

		// Add Gender Label and Radio Buttons
		gbc.gridx = 0; 
		gbc.gridy = 5;
		gbc.weightx = 0.2;
		inputPanel.add(new JLabel("Gender:"), gbc);

		gbc.gridx = 1; 
		gbc.weightx = 0.8;
		JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rbtnMale = new JRadioButton("Male");
		rbtnFemale = new JRadioButton("Female");
		genderGroup = new ButtonGroup();
		genderGroup.add(rbtnMale);
		genderGroup.add(rbtnFemale);
		genderPanel.add(rbtnMale);
		genderPanel.add(rbtnFemale);
		inputPanel.add(genderPanel, gbc);

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
		
		btnAdd.setBackground(new Color(60, 179, 113)); // Green background
		btnAdd.setForeground(Color.WHITE);            // White text

		btnUpdate.setBackground(new Color(30, 144, 255)); // Blue background
		btnUpdate.setForeground(Color.WHITE);

		btnDelete.setBackground(new Color(220, 20, 60)); // Red background
		btnDelete.setForeground(Color.WHITE);

		btnClear.setBackground(new Color(255, 165, 0));  // Orange background
		btnClear.setForeground(Color.WHITE);


		// Add action listeners for buttons
		btnAdd.addActionListener(e -> addUser());
		btnUpdate.addActionListener(e -> updateUser());
		btnDelete.addActionListener(e -> deleteUser());
		btnClear.addActionListener(e -> clearFields());

		// Set button visibility based on table selection
		btnUpdate.setVisible(false);
		btnDelete.setVisible(false);
		btnClear.setVisible(false);

		table.getSelectionModel().addListSelectionListener(e -> {
			boolean rowSelected = table.getSelectedRow() != -1;
			btnAdd.setVisible(!rowSelected);
			btnClear.setVisible(!rowSelected); // Nút Clear chỉ hiển thị cùng nút Add
			btnUpdate.setVisible(rowSelected);
			btnDelete.setVisible(rowSelected);
		});

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

		// Add MouseListener to table
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow != -1) {
					txtUsername.setText(table.getValueAt(selectedRow, 1).toString());
					txtAvatar.setText(
							table.getValueAt(selectedRow, 2) != null ? table.getValueAt(selectedRow, 2).toString()
									: "");
					txtEmail.setText(table.getValueAt(selectedRow, 3).toString());
					txtAddress.setText(table.getValueAt(selectedRow, 5).toString());

					String gender = table.getValueAt(selectedRow, 4).toString();
					if (gender.equalsIgnoreCase("Male")) {
						rbtnMale.setSelected(true);
					} else {
						rbtnFemale.setSelected(true);
					}
				}
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

			DefaultTableModel model = new DefaultTableModel() {
				@Override
				public boolean isCellEditable(int row, int column) {
					return column == 6; // Chỉ cho phép chỉnh sửa cột "Detail"
				}
			};

			model.addColumn("ID");
			model.addColumn("Username");
			model.addColumn("Avatar Link");
			model.addColumn("Email");
			model.addColumn("Gender");
			model.addColumn("Address");
			model.addColumn("Detail");

			while (resultSet.next()) {
				String avatar = resultSet.getString("avatar");
				model.addRow(new Object[] { resultSet.getInt("id"), resultSet.getString("username"),
						avatar != null ? avatar : "empty", // Kiểm tra null và thay bằng "empty"
						resultSet.getString("email"), resultSet.getString("gender"), resultSet.getString("address"),
						"Detail" });
			}

			table.setModel(model);
			table.getColumn("Detail").setCellRenderer(new ButtonRenderer());
			table.getColumn("Detail").setCellEditor(new ButtonEditor(new JCheckBox()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void searchUsers(String query) {
		loadUsers(query);
	}

	private void addUser() {
		try {
			String username = txtUsername.getText();
			String email = txtEmail.getText();
			String password = new String(txtPassword.getPassword());
			String gender = rbtnMale.isSelected() ? "Male" : "Female";
			String address = txtAddress.getText();
			String avatar = txtAvatar.getText();

			String sql = "INSERT INTO tbl_user (username, email, password, gender, address, avatar) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			statement.setString(2, email);
			statement.setString(3, password);
			statement.setString(4, gender);
			statement.setString(5, address);
			statement.setString(6, avatar.isEmpty() ? null : avatar);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected > 0) {
				JOptionPane.showMessageDialog(this, "User added successfully!");
				loadUsers("");
				clearFields();
			} else {
				JOptionPane.showMessageDialog(this, "Error adding user.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
		}
	}

	private void updateUser() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a user to update.");
			return;
		}

		int userId = (int) table.getValueAt(selectedRow, 0);
		try {
			String username = txtUsername.getText();
			String email = txtEmail.getText();
			String password = new String(txtPassword.getPassword());
			String gender = rbtnMale.isSelected() ? "Male" : "Female";
			String address = txtAddress.getText();
			String avatar = txtAvatar.getText();

			String sql = "UPDATE tbl_user SET username = ?, email = ?, password = ?, gender = ?, address = ?, avatar = ? WHERE id = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			statement.setString(2, email);
			statement.setString(3, password);
			statement.setString(4, gender);
			statement.setString(5, address);
			statement.setString(6, avatar.isEmpty() ? null : avatar);
			statement.setInt(7, userId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected > 0) {
				JOptionPane.showMessageDialog(this, "User updated successfully!");
				loadUsers("");
				clearFields();
			} else {
				JOptionPane.showMessageDialog(this, "Error updating user.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
		}
	}

	private void deleteUser() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a user to delete.");
			return;
		}

		int userId = (int) table.getValueAt(selectedRow, 0);
		try {
			String sql = "DELETE FROM tbl_user WHERE id = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);

			int rowsAffected = statement.executeUpdate();
			if (rowsAffected > 0) {
				JOptionPane.showMessageDialog(this, "User deleted successfully!");
				loadUsers("");
				clearFields();
			} else {
				JOptionPane.showMessageDialog(this, "Error deleting user.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
		}
	}

	private void clearFields() {
		txtUsername.setText("");
		txtEmail.setText("");
		txtPassword.setText("");
		txtAddress.setText("");
		txtAvatar.setText("");
		genderGroup.clearSelection();
	}

	// ButtonRenderer class to render button in table
	class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
		public ButtonRenderer() {
			setOpaque(true);
			try {
				// Load icon and scale it to 30x30
				ImageIcon icon = new ImageIcon(getClass().getResource("/icons/view.png"));
				Image scaledIcon = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
				setIcon(new ImageIcon(scaledIcon));
			} catch (Exception e) {
				e.printStackTrace();
				setText("Detail"); // Fallback if icon is missing
			}
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setText(""); // Hide text since we are using an icon
			return this;
		}
	}

	// ButtonEditor class to handle button click events
	class ButtonEditor extends DefaultCellEditor {
		private JButton button;
		private String label;
		private boolean clicked;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);

			button.addActionListener(e -> fireEditingStopped());
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			label = (value == null) ? "Detail" : value.toString();
			button.setText(label);
			clicked = true;
			return button;
		}

		@Override
		public Object getCellEditorValue() {
			if (clicked) {
				int row = table.getSelectedRow();
				String avatarLink = (table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "empty");
				String userDetails = "ID: " + table.getValueAt(row, 0) + "\n" + "Username: " + table.getValueAt(row, 1)
						+ "\n" + "Email: " + table.getValueAt(row, 3) + "\n" + "Gender: " + table.getValueAt(row, 4)
						+ "\n" + "Address: " + table.getValueAt(row, 5);

				// Create a dialog
				JDialog dialog = new JDialog((Frame) null, "User Details", true);
				dialog.setLayout(new BorderLayout());

				// Add user details text
				JTextArea textArea = new JTextArea(userDetails);
				textArea.setEditable(false);
				textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
				dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);

				// Add avatar image if available
				if (!avatarLink.equals("empty")) {
					try {
						ImageIcon avatarIcon = new ImageIcon(new URL(avatarLink));
						Image avatarImage = avatarIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
						JLabel avatarLabel = new JLabel(new ImageIcon(avatarImage));
						avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
						dialog.add(avatarLabel, BorderLayout.NORTH);
					} catch (Exception e) {
						// Log or handle invalid URL or image loading error
						JLabel errorLabel = new JLabel("Avatar not available", SwingConstants.CENTER);
						dialog.add(errorLabel, BorderLayout.NORTH);
					}
				}

				// Set dialog properties
				dialog.setSize(300, 300);
				dialog.setLocationRelativeTo(null); // Center on screen
				dialog.setVisible(true);
			}
			clicked = false;
			return label;
		}

		@Override
		public boolean stopCellEditing() {
			clicked = false;
			return super.stopCellEditing();
		}

		@Override
		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}
}
