package view.manager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import utils.DBConnection;

public class ListUsers extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JTextField txtUsername, txtEmail, txtAddress, txtSearch, txtAvatar;
	private JPasswordField txtPassword;
	private JRadioButton rbtnMale, rbtnFemale;
	private ButtonGroup genderGroup;
	private JButton btnAdd, btnUpdate, btnDelete, btnClear;
	private DefaultTableModel tableModel;
	private Connection connection;

	public ListUsers() {
		setLayout(new BorderLayout());

		// Initialize table model
		tableModel = new DefaultTableModel(
			new Object[]{"ID", "Username", "Email", "Gender", "Address", "Avatar", "Actions"}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 6; // Chỉ cho phép edit cột "Actions"
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
		setupInputPanel();

		// Configure Edit button column
		configureEditButtonColumn();

		// Add search functionality
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

		// Add table row selection listener
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = table.getColumnModel().getColumnIndexAtX(e.getX());
				int row = e.getY() / table.getRowHeight();
				
				if (row < table.getRowCount() && row >= 0 && column == 6) {
					// Click vào nút Edit
					int id = (int) tableModel.getValueAt(row, 0);
					loadUserForEdit(id);
				} else if (row < table.getRowCount() && row >= 0) {
					// Click vào các cột khác để xem chi tiết
					int id = (int) tableModel.getValueAt(row, 0);
					showUserDetailDialog(id);
				}
			}
		});

		// Add button listeners
		btnAdd.addActionListener(e -> addUser());
		btnUpdate.addActionListener(e -> updateUser());
		btnDelete.addActionListener(e -> deleteUser());
		btnClear.addActionListener(e -> clearFields());

		// Load initial data
		loadUsers("");
	}

	private void setupInputPanel() {
		JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
		inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Fields panel
		JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
		
		// Username
		JLabel lblUsername = new JLabel("Tên đăng nhập:");
		lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtUsername = new JTextField();
		txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblUsername);
		fieldsPanel.add(txtUsername);
		
		// Email
		JLabel lblEmail = new JLabel("Email:");
		lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtEmail = new JTextField();
		txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblEmail);
		fieldsPanel.add(txtEmail);
		
		// Password
		JLabel lblPassword = new JLabel("Mật khẩu:");
		lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtPassword = new JPasswordField();
		txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblPassword);
		fieldsPanel.add(txtPassword);
		
		// Address
		JLabel lblAddress = new JLabel("Địa chỉ:");
		lblAddress.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtAddress = new JTextField();
		txtAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblAddress);
		fieldsPanel.add(txtAddress);
		
		// Avatar
		JLabel lblAvatar = new JLabel("Liên kết Avatar:");
		lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtAvatar = new JTextField();
		txtAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblAvatar);
		fieldsPanel.add(txtAvatar);
		
		// Gender
		JLabel lblGender = new JLabel("Giới tính:");
		lblGender.setFont(new Font("Segoe UI", Font.BOLD, 12));
		JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rbtnMale = new JRadioButton("Nam");
		rbtnFemale = new JRadioButton("Nữ");
		genderGroup = new ButtonGroup();
		genderGroup.add(rbtnMale);
		genderGroup.add(rbtnFemale);
		genderPanel.add(rbtnMale);
		genderPanel.add(rbtnFemale);
		fieldsPanel.add(lblGender);
		fieldsPanel.add(genderPanel);
		
		inputPanel.add(fieldsPanel, BorderLayout.CENTER);
		
		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		btnAdd = createStyledButton("Thêm", new Color(46, 204, 113));
		btnUpdate = createStyledButton("Cập nhật", new Color(52, 152, 219));
		btnDelete = createStyledButton("Xóa", new Color(231, 76, 60));
		btnClear = createStyledButton("Xóa trắng", new Color(149, 165, 166));
		
		buttonPanel.add(btnAdd);
		buttonPanel.add(btnUpdate);
		buttonPanel.add(btnDelete);
		buttonPanel.add(btnClear);
		
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(inputPanel, BorderLayout.SOUTH);
	}

	private JButton createStyledButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Segoe UI", Font.BOLD, 12));
		button.setForeground(Color.WHITE);
		button.setBackground(bgColor);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setPreferredSize(new Dimension(100, 35));
		
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

	private void configureEditButtonColumn() {
		// Set preferred width for all columns
		table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
		table.getColumnModel().getColumn(1).setPreferredWidth(150); // Username
		table.getColumnModel().getColumn(2).setPreferredWidth(200); // Email
		table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Gender
		table.getColumnModel().getColumn(4).setPreferredWidth(150); // Address
		table.getColumnModel().getColumn(5).setPreferredWidth(200); // Avatar
		table.getColumnModel().getColumn(6).setPreferredWidth(140); // Actions

		// Configure the Actions column
		table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
				panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
				
				// Create Edit button
				JButton editBtn = new JButton("Edit");
				editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
				editBtn.setBackground(new Color(52, 152, 219));
				editBtn.setForeground(Color.WHITE);
				editBtn.setPreferredSize(new Dimension(55, 24));
				editBtn.setBorderPainted(false);
				editBtn.setFocusPainted(false);
				
				// Create Detail button
				JButton detailBtn = new JButton("Detail");
				detailBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
				detailBtn.setBackground(new Color(46, 204, 113));
				detailBtn.setForeground(Color.WHITE);
				detailBtn.setPreferredSize(new Dimension(55, 24));
				detailBtn.setBorderPainted(false);
				detailBtn.setFocusPainted(false);
				
				panel.add(editBtn);
				panel.add(detailBtn);
				
				return panel;
			}
		});
		
		// Add cell editor for the actions column
		table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
			private JPanel panel;
			private JButton editBtn;
			private JButton detailBtn;
			
			{
				panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
				
				editBtn = new JButton("Edit");
				editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
				editBtn.setBackground(new Color(52, 152, 219));
				editBtn.setForeground(Color.WHITE);
				editBtn.setPreferredSize(new Dimension(55, 24));
				editBtn.setBorderPainted(false);
				editBtn.setFocusPainted(false);
				
				detailBtn = new JButton("Detail");
				detailBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
				detailBtn.setBackground(new Color(46, 204, 113));
				detailBtn.setForeground(Color.WHITE);
				detailBtn.setPreferredSize(new Dimension(55, 24));
				detailBtn.setBorderPainted(false);
				detailBtn.setFocusPainted(false);
				
				panel.add(editBtn);
				panel.add(detailBtn);
				
				editBtn.addActionListener(e -> {
					int row = table.getSelectedRow();
					if (row != -1) {
						int id = (int) tableModel.getValueAt(row, 0);
						loadUserForEdit(id);
					}
					fireEditingStopped();
				});
				
				detailBtn.addActionListener(e -> {
					int row = table.getSelectedRow();
					if (row != -1) {
						int id = (int) tableModel.getValueAt(row, 0);
						showUserDetailDialog(id);
					}
					fireEditingStopped();
				});
			}
			
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value,
					boolean isSelected, int row, int column) {
				panel.setBackground(table.getSelectionBackground());
				return panel;
			}
			
			@Override
			public Object getCellEditorValue() {
				return "Actions";
			}
		});

		// Enable auto resize mode
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	private void loadUsers(String searchQuery) {
		try {
			connection = DBConnection.getConnection();
			String sql = "SELECT * FROM tbl_user WHERE username LIKE ? OR email LIKE ? OR address LIKE ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			String searchPattern = "%" + searchQuery + "%";
			ps.setString(1, searchPattern);
			ps.setString(2, searchPattern);
			ps.setString(3, searchPattern);
			ResultSet rs = ps.executeQuery();

			tableModel.setRowCount(0);

			while (rs.next()) {
				Object[] row = {
					rs.getInt("id"),
					rs.getString("username"),
					rs.getString("email"),
					rs.getString("gender"),
					rs.getString("address"),
					rs.getString("avatar"),
					"Edit"
				};
				tableModel.addRow(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error loading users: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void searchUsers(String query) {
		loadUsers(query);
	}

	private void loadUserForEdit(int id) {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT * FROM tbl_user WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				txtUsername.setText(rs.getString("username"));
				txtEmail.setText(rs.getString("email"));
				txtAddress.setText(rs.getString("address"));
				txtAvatar.setText(rs.getString("avatar"));
				
				String gender = rs.getString("gender");
				if ("Male".equalsIgnoreCase(gender)) {
					rbtnMale.setSelected(true);
				} else {
					rbtnFemale.setSelected(true);
				}
				
				// Select the corresponding row
				for (int i = 0; i < table.getRowCount(); i++) {
					if ((int) table.getValueAt(i, 0) == id) {
						table.setRowSelectionInterval(i, i);
						break;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error loading user for edit: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showUserDetailDialog(int userId) {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT * FROM tbl_user WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				// Create custom dialog
				JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "User Details", true);
				dialog.setLayout(new BorderLayout(10, 10));
				dialog.setSize(400, 500);
				dialog.setLocationRelativeTo(this);

				// Create main panel with padding
				JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

				// Add avatar if exists
				String avatarUrl = rs.getString("avatar");
				if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
					try {
						ImageIcon avatarIcon = new ImageIcon(new URL(avatarUrl));
						Image img = avatarIcon.getImage();
						Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
						JLabel avatarLabel = new JLabel(new ImageIcon(scaledImg));
						avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
						mainPanel.add(avatarLabel);
						mainPanel.add(Box.createVerticalStrut(20));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// Add user details
				addDetailRow(mainPanel, "ID", rs.getString("id"));
				addDetailRow(mainPanel, "Username", rs.getString("username"));
				addDetailRow(mainPanel, "Email", rs.getString("email"));
				addDetailRow(mainPanel, "Gender", rs.getString("gender"));
				addDetailRow(mainPanel, "Address", rs.getString("address"));

				// Add close button
				JButton closeButton = new JButton("Close");
				closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				closeButton.setBackground(new Color(52, 152, 219));
				closeButton.setForeground(Color.WHITE);
				closeButton.setFocusPainted(false);
				closeButton.addActionListener(e -> dialog.dispose());
				
				mainPanel.add(Box.createVerticalStrut(20));
				mainPanel.add(closeButton);

				// Add main panel to dialog
				dialog.add(mainPanel);

				// Show dialog
				dialog.setVisible(true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error loading user details: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addDetailRow(JPanel panel, String label, String value) {
		JPanel rowPanel = new JPanel(new BorderLayout(10, 5));
		rowPanel.setMaximumSize(new Dimension(350, 30));
		
		JLabel lblField = new JLabel(label + ":");
		lblField.setFont(new Font("Segoe UI", Font.BOLD, 12));
		
		JLabel lblValue = new JLabel(value);
		lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		
		rowPanel.add(lblField, BorderLayout.WEST);
		rowPanel.add(lblValue, BorderLayout.CENTER);
		
		panel.add(rowPanel);
		panel.add(Box.createVerticalStrut(10));
	}

	private void addUser() {
		// Validation cho thêm mới (isUpdate = false)
		if (!validateInput(false)) {
			return;
		}

		try {
			connection = DBConnection.getConnection();
			String sql = "INSERT INTO tbl_user (username, email, password, gender, address, avatar) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, txtUsername.getText().trim());
			ps.setString(2, txtEmail.getText().trim());
			ps.setString(3, new String(txtPassword.getPassword()));
			ps.setString(4, rbtnMale.isSelected() ? "Male" : "Female");
			ps.setString(5, txtAddress.getText().trim());
			String avatar = txtAvatar.getText().trim();
			ps.setString(6, avatar.isEmpty() ? null : avatar);

			int result = ps.executeUpdate();
			if (result > 0) {
				JOptionPane.showMessageDialog(this,
					"Thêm người dùng thành công!",
					"Thành công",
					JOptionPane.INFORMATION_MESSAGE);
				loadUsers("");
				clearFields();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error adding user: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateUser() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this,
				"Vui lòng chọn một người dùng để cập nhật!",
				"Cần chọn",
				JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Validation cho cập nhật (isUpdate = true)
		if (!validateInput(true)) {
			return;
		}

		try {
			int id = (int) tableModel.getValueAt(selectedRow, 0);
			connection = DBConnection.getConnection();
			
			// Kiểm tra xem có nhập password mới không
			String password = new String(txtPassword.getPassword());
			String sql;
			PreparedStatement ps;
			
			if (password.isEmpty()) {
				// Nếu không nhập password mới, không cập nhật password
				sql = "UPDATE tbl_user SET username = ?, email = ?, gender = ?, address = ?, avatar = ? WHERE id = ?";
				ps = connection.prepareStatement(sql);
				ps.setString(1, txtUsername.getText().trim());
				ps.setString(2, txtEmail.getText().trim());
				ps.setString(3, rbtnMale.isSelected() ? "Male" : "Female");
				ps.setString(4, txtAddress.getText().trim());
				String avatar = txtAvatar.getText().trim();
				ps.setString(5, avatar.isEmpty() ? null : avatar);
				ps.setInt(6, id);
			} else {
				// Nếu có nhập password mới, cập nhật cả password
				sql = "UPDATE tbl_user SET username = ?, email = ?, password = ?, gender = ?, address = ?, avatar = ? WHERE id = ?";
				ps = connection.prepareStatement(sql);
				ps.setString(1, txtUsername.getText().trim());
				ps.setString(2, txtEmail.getText().trim());
				ps.setString(3, password);
				ps.setString(4, rbtnMale.isSelected() ? "Male" : "Female");
				ps.setString(5, txtAddress.getText().trim());
				String avatar = txtAvatar.getText().trim();
				ps.setString(6, avatar.isEmpty() ? null : avatar);
				ps.setInt(7, id);
			}

			int result = ps.executeUpdate();
			if (result > 0) {
				JOptionPane.showMessageDialog(this,
					"Cập nhật người dùng thành công!",
					"Thành công",
					JOptionPane.INFORMATION_MESSAGE);
				loadUsers("");
				clearFields();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error updating user: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteUser() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this,
				"Vui lòng chọn một người dùng để xóa!",
				"Cần chọn",
				JOptionPane.WARNING_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
			"Bạn có chắc chắn muốn xóa người dùng này?",
			"Xác nhận xóa",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				int id = (int) tableModel.getValueAt(selectedRow, 0);
				connection = DBConnection.getConnection();
				String sql = "DELETE FROM tbl_user WHERE id = ?";
				PreparedStatement ps = connection.prepareStatement(sql);
				ps.setInt(1, id);

				int result = ps.executeUpdate();
				if (result > 0) {
					JOptionPane.showMessageDialog(this,
						"Xóa người dùng thành công!",
						"Thành công",
						JOptionPane.INFORMATION_MESSAGE);
					loadUsers("");
					clearFields();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
					"Error deleting user: " + e.getMessage(),
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private boolean validateInput(boolean isUpdate) {
		String username = txtUsername.getText().trim();
		String email = txtEmail.getText().trim();
		String password = new String(txtPassword.getPassword());
		
		if (username.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Tên đăng nhập là bắt buộc!",
				"Lỗi xác thực",
				JOptionPane.ERROR_MESSAGE);
			txtUsername.requestFocus();
			return false;
		}
		
		if (email.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Email là bắt buộc!",
				"Lỗi xác thực",
				JOptionPane.ERROR_MESSAGE);
			txtEmail.requestFocus();
			return false;
		}
		
		if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
			JOptionPane.showMessageDialog(this,
				"Định dạng email không hợp lệ!",
				"Lỗi xác thực",
				JOptionPane.ERROR_MESSAGE);
			txtEmail.requestFocus();
			return false;
		}
		
		// Chỉ validate password khi thêm mới hoặc khi có nhập password mới
		if (!isUpdate && password.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Mật khẩu là bắt buộc!",
				"Lỗi xác thực",
				JOptionPane.ERROR_MESSAGE);
			txtPassword.requestFocus();
			return false;
		}
		
		if (!rbtnMale.isSelected() && !rbtnFemale.isSelected()) {
			JOptionPane.showMessageDialog(this,
				"Vui lòng chọn giới tính!",
				"Lỗi xác thực",
				JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}

	private void clearFields() {
		txtUsername.setText("");
		txtEmail.setText("");
		txtPassword.setText("");
		txtAddress.setText("");
		txtAvatar.setText("");
		genderGroup.clearSelection();
		table.clearSelection();
	}
}
