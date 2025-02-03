package view.manager;
import com.formdev.flatlaf.FlatLightLaf; // Import FlatLaf
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import utils.DBConnection; // Assuming this class handles DB connection
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.ArrayList;
import java.util.Date;

public class ListBlog extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JTextField txtTitle, txtContent, txtImgLink, txtSearch; // Add txtSearch for search input
	private JButton btnAdd, btnUpdate, btnDelete, btnClear;
	private DefaultTableModel tableModel;
	private JPanel detailPanel; // Panel to show blog details
	private Connection connection;
	private JComboBox<Author> cboAuthor;
	private JComboBox<String> cboMonth, cboYear, cboSort;
	private JTextField txtDate; // Thay thế JDateChooser
	private JButton btnFilter, btnClearFilter;
	private JPanel searchPanel; // Thêm biến instance

	// Thêm class Author để lưu trữ thông tin tác giả
	private class Author {
		private int id;
		private String username;
		
		public Author(int id, String username) {
			this.id = id;
			this.username = username;
		}
		
		@Override
		public String toString() {
			return id == 0 ? username : username + " (ID: " + id + ")";
		}
		
		public int getId() {
			return id;
		}
		
		public String getUsername() {
			return username;
		}
	}

	public ListBlog() {
		// Set FlatLaf look and feel for the UI
		try {
			UIManager.setLookAndFeel(new FlatLightLaf()); // Use FlatLightLaf theme
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		setLayout(new BorderLayout()); // Use BorderLayout for resizing components

		// Initialize table with columns: id, user_id, title, content, image link,
		// created_at, and Edit
		tableModel = new DefaultTableModel(
				new Object[] { "ID", "User ID", "Title", "Content", "Image Link", "Created At", "Edit" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 6; // Chỉ cho phép edit cột "Edit"
			}
		};
		table = new JTable(tableModel);

		// Create Search panel above the table (top position)
		searchPanel = new JPanel(new BorderLayout());
		txtSearch = new JTextField();
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
		searchPanel.add(txtSearch, BorderLayout.CENTER);
		searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(searchPanel, BorderLayout.NORTH); // Move the search panel to the top (NORTH)

		// Apply FlatLaf look and feel for JTable
		table.setRowHeight(30); // Adjust row height for readability
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Single row selection
		table.setFillsViewportHeight(true);
		table.setBackground(Color.WHITE); // White background for table
		table.getTableHeader().setBackground(Color.WHITE); // Table header background color

		// Set row selection background color to green
		table.setSelectionBackground(new Color(70, 130, 180)); // Green when row is selected
		table.setSelectionForeground(Color.WHITE); // White text for selected row

		// Create JScrollPane to wrap the JTable
		JScrollPane scrollPane = new JScrollPane(table);

		// Create the detail panel where blog details will be shown
		detailPanel = new JPanel();
		detailPanel.setLayout(new BorderLayout());

		// Create a JSplitPane to separate table and details panel
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailPanel);
		splitPane.setDividerLocation(600); // Adjust the divider to fit your needs
		add(splitPane, BorderLayout.CENTER);

		// Add DocumentListener to the search input field
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				searchBlogs(txtSearch.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				searchBlogs(txtSearch.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				searchBlogs(txtSearch.getText());
			}
		});

		// Thay đổi phần input panel để có giao diện đẹp hơn
		setupInputPanel();

		// Add MouseListener for table row selection
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = table.getColumnModel().getColumnIndexAtX(e.getX());
				int row = e.getY() / table.getRowHeight();
				
				if (row < table.getRowCount() && row >= 0 && column == 6) {
					// Click vào nút Edit
					int id = (int) tableModel.getValueAt(row, 0);
					loadBlogForEdit(id);
				} else if (row < table.getRowCount() && row >= 0) {
					// Click vào các cột khác để xem chi tiết
					int id = (int) tableModel.getValueAt(row, 0);
					loadBlogDetails(id);
				}
			}
		});

		// Thêm TableCellRenderer cho cột Edit
		configureEditButtonColumn();

		// Load all blog entries from the database
		loadtbl_post();

		// Thêm action listeners cho các nút
		btnAdd.addActionListener(e -> addBlog());
		btnUpdate.addActionListener(e -> updateBlog());
		btnDelete.addActionListener(e -> deleteBlog());
		btnClear.addActionListener(e -> clearFields());

		// Thêm phương thức setupFilterPanel() và gọi nó trong constructor sau phần search panel
		setupFilterPanel();
	}

	// Method to load all tbl_post into the table
	private void loadtbl_post() {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT p.id, p.user_id, p.title, p.content, p.hash_img, p.create_at FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();

			tableModel.setRowCount(0);

			while (rs.next()) {
				Object[] row = {
					rs.getInt("id"),
					rs.getInt("user_id"),
					rs.getString("title"),
					rs.getString("content"),
					rs.getString("hash_img"),
					rs.getTimestamp("create_at"),
					"Edit" // Thay "Show Detail" bằng "Edit"
				};
				tableModel.addRow(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error loading blogs: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// Method to filter the blogs based on the search input
	private void searchBlogs(String searchQuery) {
		try {
			connection = DBConnection.getConnection();
			StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.user_id, p.title, p.content, p.hash_img, p.create_at, u.username " +
				"FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id WHERE 1=1"
			);
			ArrayList<Object> params = new ArrayList<>();

			// Thêm điều kiện tìm kiếm
			if (!searchQuery.isEmpty()) {
				sql.append(" AND (p.title LIKE ? OR p.content LIKE ?)");
				params.add("%" + searchQuery + "%");
				params.add("%" + searchQuery + "%");
			}

			// Lọc theo tác giả
			Author selectedAuthor = (Author) cboAuthor.getSelectedItem();
			if (selectedAuthor != null && selectedAuthor.getId() != 0) { // 0 là ID của "All Authors"
				sql.append(" AND u.id = ?");
				params.add(selectedAuthor.getId());
			}

			// Lọc theo tháng
			String selectedMonth = (String) cboMonth.getSelectedItem();
			if (selectedMonth != null && !selectedMonth.equals("All Months")) {
				sql.append(" AND MONTH(p.create_at) = ?");
				params.add(cboMonth.getSelectedIndex()); // Index 1-12 tương ứng với các tháng
			}

			// Lọc theo năm
			String selectedYear = (String) cboYear.getSelectedItem();
			if (selectedYear != null && !selectedYear.equals("All Years")) {
				sql.append(" AND YEAR(p.create_at) = ?");
				params.add(Integer.parseInt(selectedYear));
			}

			// Sắp xếp theo thời gian
			sql.append(" ORDER BY p.create_at ");
			sql.append(cboSort.getSelectedIndex() == 0 ? "DESC" : "ASC");

			PreparedStatement ps = connection.prepareStatement(sql.toString());
			
			// Set parameters
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}

			ResultSet rs = ps.executeQuery();
			tableModel.setRowCount(0);

			while (rs.next()) {
				Object[] row = {
					rs.getInt("id"),
					rs.getInt("user_id"),
					rs.getString("title"),
					rs.getString("content"),
					rs.getString("hash_img"),
					rs.getTimestamp("create_at"),
					"Edit"
				};
				tableModel.addRow(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error searching blogs: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// Method to add a new blog
	private void addBlog() {
		String title = txtTitle.getText().trim();
		String content = txtContent.getText().trim();
		String imgLink = txtImgLink.getText().trim();
		
		// Validation
		if (title.isEmpty() || content.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Title and Content are required!",
				"Validation Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			connection = DBConnection.getConnection();
			String query = "INSERT INTO tbl_post (user_id, title, content, hash_img) VALUES (?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, 1); // Assuming default user_id = 1, modify as needed
			ps.setString(2, title);
			ps.setString(3, content);
			ps.setString(4, imgLink);
			
			int result = ps.executeUpdate();
			if (result > 0) {
				JOptionPane.showMessageDialog(this,
					"Blog added successfully!",
					"Success",
					JOptionPane.INFORMATION_MESSAGE);
				loadtbl_post();
				clearFields();
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,
				"Error adding blog: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// Method to update a blog
	private void updateBlog() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this,
				"Please select a blog to update!",
				"Selection Required",
				JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String title = txtTitle.getText().trim();
		String content = txtContent.getText().trim();
		String imgLink = txtImgLink.getText().trim();
		
		// Validation
		if (title.isEmpty() || content.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Title and Content are required!",
				"Validation Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			int id = (int) tableModel.getValueAt(selectedRow, 0);
			connection = DBConnection.getConnection();
			String query = "UPDATE tbl_post SET title = ?, content = ?, hash_img = ? WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, title);
			ps.setString(2, content);
			ps.setString(3, imgLink);
			ps.setInt(4, id);
			
			int result = ps.executeUpdate();
			if (result > 0) {
				JOptionPane.showMessageDialog(this,
					"Blog updated successfully!",
					"Success",
					JOptionPane.INFORMATION_MESSAGE);
				loadtbl_post();
				clearFields();
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,
				"Error updating blog: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// Method to delete a blog
	private void deleteBlog() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this,
				"Please select a blog to delete!",
				"Selection Required",
				JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		int confirm = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to delete this blog?",
			"Confirm Delete",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);
			
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				int id = (int) tableModel.getValueAt(selectedRow, 0);
				connection = DBConnection.getConnection();
				String query = "DELETE FROM tbl_post WHERE id = ?";
				PreparedStatement ps = connection.prepareStatement(query);
				ps.setInt(1, id);
				
				int result = ps.executeUpdate();
				if (result > 0) {
					JOptionPane.showMessageDialog(this,
						"Blog deleted successfully!",
						"Success",
						JOptionPane.INFORMATION_MESSAGE);
					loadtbl_post();
					clearFields();
				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this,
					"Error deleting blog: " + e.getMessage(),
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	// Method to clear the input fields
	private void clearFields() {
		txtTitle.setText("");
		txtContent.setText("");
		txtImgLink.setText("");
		txtDate.setText("");
	}

	// Method to load blog details into the detail panel when a blog is clicked in
	// the table
	private void loadBlogDetails(int id) {
		// Hiển thị loading dialog
		JDialog loadingDialog = new JDialog((Frame) null, "Loading", true);
		JPanel loadingPanel = new JPanel(new BorderLayout());
		JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
		loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		loadingPanel.add(loadingLabel, BorderLayout.CENTER);
		loadingDialog.add(loadingPanel);
		loadingDialog.setSize(150, 100);
		loadingDialog.setLocationRelativeTo(this);

		// Tạo một worker để tải dữ liệu và tránh làm đông cứng UI
		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					// Tải dữ liệu từ cơ sở dữ liệu
					connection = DBConnection.getConnection();
					String query = "SELECT p.id, p.title, p.content, p.hash_img, u.username, u.email, u.gender, u.address FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id WHERE p.id = ?";
					PreparedStatement ps = connection.prepareStatement(query);
					ps.setInt(1, id);
					ResultSet rs = ps.executeQuery();

					if (rs.next()) {
						// Lấy dữ liệu chi tiết từ database
						String title = rs.getString("title");
						String content = rs.getString("content");
						String imgLink = rs.getString("hash_img");
						String username = rs.getString("username");
						String email = rs.getString("email");
						String gender = rs.getString("gender");
						String address = rs.getString("address");

						// Cập nhật giao diện detailPanel
						SwingUtilities.invokeLater(() -> {
							detailPanel.removeAll();

							detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));

							JPanel titlePanel = new JPanel();
							JLabel lblTitle = new JLabel("Title: " + title);
							lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
							titlePanel.add(lblTitle);

							JPanel userPanel = new JPanel();
							JLabel lblUser = new JLabel("Author: " + username);
							lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblUser);

							JLabel lblEmail = new JLabel("Email: " + email);
							lblEmail.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblEmail);

							JLabel lblGender = new JLabel("Gender: " + gender);
							lblGender.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblGender);

							JLabel lblAddress = new JLabel("Address: " + address);
							lblAddress.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblAddress);

							JPanel contentPanel = new JPanel();
							JLabel lblContent = new JLabel("<html>Content: " + content + "</html>");
							lblContent.setFont(new Font("Arial", Font.PLAIN, 12));
							contentPanel.add(lblContent);

							detailPanel.add(titlePanel);
							detailPanel.add(userPanel);
							detailPanel.add(contentPanel);

							if (imgLink != null && !imgLink.trim().isEmpty()) {
								JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

								try {
									ImageIcon imgIcon = new ImageIcon(new URL(imgLink));
									Image img = imgIcon.getImage();
									Image scaledImg = img.getScaledInstance(700, 400, Image.SCALE_SMOOTH);
									imgIcon = new ImageIcon(scaledImg);
									JLabel imgLabel = new JLabel(imgIcon);

									imagePanel.add(imgLabel);
									detailPanel.add(imagePanel);
								} catch (java.net.MalformedURLException e) {
									e.printStackTrace();
								}
							}

							detailPanel.revalidate();
							detailPanel.repaint();
						});
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {
				loadingDialog.dispose(); // Đóng loading dialog
			}
		};

		// Hiển thị dialog và bắt đầu worker
		SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
		worker.execute();
	}

	// Thêm phương thức mới để load blog cho việc edit
	private void loadBlogForEdit(int id) {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT title, content, hash_img FROM tbl_post WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				txtTitle.setText(rs.getString("title"));
				txtContent.setText(rs.getString("content"));
				txtImgLink.setText(rs.getString("hash_img"));
				
				// Chọn row tương ứng trong table
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
				"Error loading blog for edit: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// Thay đổi phần input panel để có giao diện đẹp hơn
	private void setupInputPanel() {
		JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
		inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Panel chứa các trường nhập liệu
		JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		
		// Title field
		JLabel lblTitle = new JLabel("Title:");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtTitle = new JTextField();
		txtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblTitle);
		fieldsPanel.add(txtTitle);
		
		// Content field
		JLabel lblContent = new JLabel("Content:");
		lblContent.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtContent = new JTextField();
		txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblContent);
		fieldsPanel.add(txtContent);
		
		// Image Link field
		JLabel lblImgLink = new JLabel("Image Link:");
		lblImgLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtImgLink = new JTextField();
		txtImgLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblImgLink);
		fieldsPanel.add(txtImgLink);
		
		inputPanel.add(fieldsPanel, BorderLayout.CENTER);
		
		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		btnAdd = createStyledButton("Add", new Color(46, 204, 113));
		btnUpdate = createStyledButton("Update", new Color(52, 152, 219));
		btnDelete = createStyledButton("Delete", new Color(231, 76, 60));
		btnClear = createStyledButton("Clear", new Color(149, 165, 166));
		
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
		
		// Hover effect
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

	// Thêm phương thức mới để cấu hình cột Edit
	private void configureEditButtonColumn() {
		table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JButton button = new JButton("Edit");
				button.setBackground(new Color(52, 152, 219));
				button.setForeground(Color.WHITE);
				return button;
			}
		});
	}

	// Thêm phương thức mới để setupFilterPanel
	private void setupFilterPanel() {
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		filterPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
		
		// Author filter
		JLabel lblAuthor = new JLabel("Author:");
		lblAuthor.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboAuthor = new JComboBox<>();
		cboAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboAuthor.setPreferredSize(new Dimension(150, 25));
		loadAuthors();
		
		// Month filter
		JLabel lblMonth = new JLabel("Month:");
		lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboMonth = new JComboBox<>(new String[] {
			"All Months", "January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December"
		});
		cboMonth.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboMonth.setPreferredSize(new Dimension(100, 25));
		
		// Year filter
		JLabel lblYear = new JLabel("Year:");
		lblYear.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboYear = new JComboBox<>();
		cboYear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboYear.setPreferredSize(new Dimension(80, 25));
		loadYears();
		
		// Sort order
		JLabel lblSort = new JLabel("Sort:");
		lblSort.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboSort = new JComboBox<>(new String[] {"Newest First", "Oldest First"});
		cboSort.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboSort.setPreferredSize(new Dimension(100, 25));
		
		// Filter buttons
		btnFilter = createStyledButton("Filter", new Color(52, 152, 219));
		btnFilter.setPreferredSize(new Dimension(80, 25));
		btnFilter.addActionListener(e -> applyFilters());
		
		btnClearFilter = createStyledButton("Clear", new Color(149, 165, 166));
		btnClearFilter.setPreferredSize(new Dimension(80, 25));
		btnClearFilter.addActionListener(e -> clearFilters());
		
		filterPanel.add(lblAuthor);
		filterPanel.add(cboAuthor);
		filterPanel.add(lblMonth);
		filterPanel.add(cboMonth);
		filterPanel.add(lblYear);
		filterPanel.add(cboYear);
		filterPanel.add(lblSort);
		filterPanel.add(cboSort);
		filterPanel.add(btnFilter);
		filterPanel.add(btnClearFilter);
		
		// Thêm filter panel vào phía trên bảng, dưới search panel
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(searchPanel, BorderLayout.NORTH);
		topPanel.add(filterPanel, BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
	}

	// Thêm phương thức để load danh sách tác giả
	private void loadAuthors() {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT DISTINCT u.id, u.username FROM tbl_user u JOIN tbl_post p ON u.id = p.user_id ORDER BY u.username";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			
			cboAuthor.removeAllItems();
			cboAuthor.addItem(new Author(0, "All Authors")); // ID = 0 cho All Authors
			
			while (rs.next()) {
				cboAuthor.addItem(new Author(
					rs.getInt("id"),
					rs.getString("username")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Thêm phương thức để load các năm từ database
	private void loadYears() {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT DISTINCT YEAR(create_at) as year FROM tbl_post ORDER BY year DESC";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			
			cboYear.removeAllItems();
			cboYear.addItem("All Years");
			
			while (rs.next()) {
				cboYear.addItem(String.valueOf(rs.getInt("year")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Thêm phương thức để áp dụng filter
	private void applyFilters() {
		searchBlogs(txtSearch.getText());
	}

	// Thêm phương thức để xóa filter
	private void clearFilters() {
		txtSearch.setText("");
		cboAuthor.setSelectedIndex(0); // Select "All Authors"
		cboMonth.setSelectedItem("All Months");
		cboYear.setSelectedItem("All Years");
		cboSort.setSelectedIndex(0); // Newest First
		searchBlogs("");
	}

}
