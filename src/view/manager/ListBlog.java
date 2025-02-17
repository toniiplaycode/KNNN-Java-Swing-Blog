package view.manager;
import com.formdev.flatlaf.FlatLightLaf; // Import FlatLaf
import com.toedter.calendar.JDateChooser;

import connection.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.ArrayList;
import java.util.Date;

public class ListBlog extends JPanel {

	// Khai báo các thành phần UI
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JTextField txtTitle, txtContent, txtImgLink, txtSearch;
	private JButton btnAdd, btnUpdate, btnDelete, btnClear;
	private DefaultTableModel tableModel;
	private JPanel detailPanel; // Panel hiển thị chi tiết bài viết
	private Connection connection;
	private JComboBox<Author> cboAuthor;
	private JComboBox<String> cboMonth, cboYear, cboSort;
	private JTextField txtDate;
	private JButton btnFilter, btnClearFilter;
	private JPanel searchPanel;

	// Class lưu trữ thông tin tác giả
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

	// Constructor - Khởi tạo giao diện
	public ListBlog() {
		// Thiết lập giao diện FlatLaf
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		setLayout(new BorderLayout());

		// Khởi tạo model cho bảng với các cột
		tableModel = new DefaultTableModel(
				new Object[] { "ID", "User ID", "Tiêu đề", "Nội dung", "Link ảnh", "Ngày tạo", "Thao tác" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 6; // Chỉ cho phép edit cột "Thao tác"
			}
		};
		table = new JTable(tableModel);

		// Tạo panel tìm kiếm
		searchPanel = new JPanel(new BorderLayout());
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

		// Tạo panel chi tiết bài viết
		detailPanel = new JPanel();
		detailPanel.setLayout(new BorderLayout());

		// Tạo SplitPane để chia màn hình thành 2 phần
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailPanel);
		splitPane.setDividerLocation(600);
		add(splitPane, BorderLayout.CENTER);

		// Thêm chức năng tìm kiếm realtime
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

		// Thiết lập panel nhập liệu
		setupInputPanel();

		// Thêm sự kiện click cho bảng
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

		// Cấu hình cột Edit
		configureEditButtonColumn();

		// Load dữ liệu ban đầu
		loadtbl_post();

		// Thêm sự kiện cho các nút
		btnAdd.addActionListener(e -> addBlog());
		btnUpdate.addActionListener(e -> updateBlog());
		btnDelete.addActionListener(e -> deleteBlog());
		btnClear.addActionListener(e -> clearFields());

		// Thiết lập panel lọc
		setupFilterPanel();
	}

	// Phương thức load tất cả bài viết từ database
	private void loadtbl_post() {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT p.id, p.user_id, p.title, p.content, p.hash_img, p.create_at FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();

			tableModel.setRowCount(0); // Xóa dữ liệu cũ

			// Thêm từng dòng dữ liệu vào bảng
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
				"Lỗi khi tải bài viết: " + e.getMessage(),
				"Lỗi cơ sở dữ liệu",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// Phương thức tìm kiếm bài viết theo từ khóa
	private void searchBlogs(String searchQuery) {
		try {
			connection = DBConnection.getConnection();
			// Tạo câu query tìm kiếm với nhiều điều kiện
			StringBuilder sql = new StringBuilder(
				"SELECT p.id, p.user_id, p.title, p.content, p.hash_img, p.create_at, u.username " +
				"FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id WHERE 1=1"
			);
			ArrayList<Object> params = new ArrayList<>();

			// Thêm điều kiện tìm kiếm theo từ khóa
			if (!searchQuery.isEmpty()) {
				sql.append(" AND (p.title LIKE ? OR p.content LIKE ?)");
				params.add("%" + searchQuery + "%");
				params.add("%" + searchQuery + "%");
			}

			// Lọc theo tác giả
			Author selectedAuthor = (Author) cboAuthor.getSelectedItem();
			if (selectedAuthor != null && selectedAuthor.getId() != 0) {
				sql.append(" AND u.id = ?");
				params.add(selectedAuthor.getId());
			}

			// Lọc theo tháng
			String selectedMonth = (String) cboMonth.getSelectedItem();
			if (selectedMonth != null && !selectedMonth.equals("All Months")) {
				sql.append(" AND MONTH(p.create_at) = ?");
				params.add(cboMonth.getSelectedIndex());
			}

			// Lọc theo năm
			String selectedYear = (String) cboYear.getSelectedItem();
			if (selectedYear != null && !selectedYear.equals("All Years")) {
				sql.append(" AND YEAR(p.create_at) = ?");
				params.add(Integer.parseInt(selectedYear));
			}

			// Sắp xếp kết quả
			sql.append(" ORDER BY p.create_at ");
			sql.append(cboSort.getSelectedIndex() == 0 ? "DESC" : "ASC");

			// Thực thi query
			PreparedStatement ps = connection.prepareStatement(sql.toString());
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}

			ResultSet rs = ps.executeQuery();
			tableModel.setRowCount(0);

			// Hiển thị kết quả tìm kiếm
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
				"Lỗi khi tìm kiếm bài viết: " + e.getMessage(),
				"Lỗi cơ sở dữ liệu",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// Phương thức thêm bài viết mới
	private void addBlog() {
		String title = txtTitle.getText().trim();
		String content = txtContent.getText().trim();
		String imgLink = txtImgLink.getText().trim();
		
		// Kiểm tra dữ liệu đầu vào
		if (title.isEmpty() || content.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Tiêu đề và nội dung là bắt buộc!",
				"Lỗi xác thực",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			connection = DBConnection.getConnection();
			String query = "INSERT INTO tbl_post (user_id, title, content, hash_img) VALUES (?, ?, ?, ?)";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, 1); // Giả sử user_id = 1, cần thay đổi theo logic thực tế
			ps.setString(2, title);
			ps.setString(3, content);
			ps.setString(4, imgLink);
			
			int result = ps.executeUpdate();
			if (result > 0) {
				JOptionPane.showMessageDialog(this,
					"Thêm bài viết thành công!",
					"Thành công",
					JOptionPane.INFORMATION_MESSAGE);
				loadtbl_post();
				clearFields();
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,
				"Lỗi khi thêm bài viết: " + e.getMessage(),
				"Lỗi cơ sở dữ liệu",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// Phương thức cập nhật bài viết
	private void updateBlog() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this,
				"Vui lòng chọn một bài viết để cập nhật!",
				"Cần chọn",
				JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String title = txtTitle.getText().trim();
		String content = txtContent.getText().trim();
		String imgLink = txtImgLink.getText().trim();
		
		// Kiểm tra dữ liệu đầu vào
		if (title.isEmpty() || content.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Tiêu đề và nội dung là bắt buộc!",
				"Lỗi xác thực",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			// Lấy ID bài viết từ dòng được chọn
			int id = (int) tableModel.getValueAt(selectedRow, 0);
			connection = DBConnection.getConnection();
			
			// Cập nhật thông tin bài viết vào database
			String query = "UPDATE tbl_post SET title = ?, content = ?, hash_img = ? WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, title);
			ps.setString(2, content);
			ps.setString(3, imgLink);
			ps.setInt(4, id);
			
			int result = ps.executeUpdate();
			if (result > 0) {
				JOptionPane.showMessageDialog(this,
					"Cập nhật bài viết thành công!",
					"Thành công",
					JOptionPane.INFORMATION_MESSAGE);
				loadtbl_post(); // Tải lại danh sách bài viết
				clearFields(); // Xóa trắng các trường nhập liệu
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,
				"Lỗi khi cập nhật bài viết: " + e.getMessage(),
				"Lỗi cơ sở dữ liệu",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// Phương thức xóa bài viết
	private void deleteBlog() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this,
				"Vui lòng chọn một bài viết để xóa!",
				"Cần chọn",
				JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// Hiển thị dialog xác nhận xóa
		int confirm = JOptionPane.showConfirmDialog(this,
			"Bạn có chắc chắn muốn xóa bài viết này?",
			"Xác nhận xóa",
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
						"Xóa bài viết thành công!",
						"Thành công",
						JOptionPane.INFORMATION_MESSAGE);
					loadtbl_post();
					clearFields();
				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this,
					"Lỗi khi xóa bài viết: " + e.getMessage(),
					"Lỗi cơ sở dữ liệu",
					JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	// Phương thức xóa trắng các trường nhập liệu
	private void clearFields() {
		txtTitle.setText("");
		txtContent.setText("");
		txtImgLink.setText("");
	}

	// Phương thức tải chi tiết bài viết khi click vào một dòng trong bảng
	private void loadBlogDetails(int id) {
		// Hiển thị dialog loading
		JDialog loadingDialog = new JDialog((Frame) null, "Loading", true);
		JPanel loadingPanel = new JPanel(new BorderLayout());
		JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
		loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		loadingPanel.add(loadingLabel, BorderLayout.CENTER);
		loadingDialog.add(loadingPanel);
		loadingDialog.setSize(150, 100);
		loadingDialog.setLocationRelativeTo(this);

		// Tạo worker để tải dữ liệu không block UI
		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					// Tải dữ liệu từ database
					connection = DBConnection.getConnection();
					String query = "SELECT p.id, p.title, p.content, p.hash_img, u.username, u.email, u.gender, u.address FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id WHERE p.id = ?";
					PreparedStatement ps = connection.prepareStatement(query);
					ps.setInt(1, id);
					ResultSet rs = ps.executeQuery();

					if (rs.next()) {
						// Lấy dữ liệu chi tiết
						String title = rs.getString("title");
						String content = rs.getString("content");
						String imgLink = rs.getString("hash_img");
						String username = rs.getString("username");
						String email = rs.getString("email");
						String gender = rs.getString("gender");
						String address = rs.getString("address");

						// Cập nhật UI trong EDT
						SwingUtilities.invokeLater(() -> {
							detailPanel.removeAll();
							detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));

							// Hiển thị tiêu đề
							JPanel titlePanel = new JPanel();
							JLabel lblTitle = new JLabel("Tiêu đề: " + title);
							lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
							titlePanel.add(lblTitle);

							// Hiển thị thông tin tác giả
							JPanel userPanel = new JPanel();
							JLabel lblUser = new JLabel("Tác giả: " + username);
							lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblUser);

							JLabel lblEmail = new JLabel("Email: " + email);
							lblEmail.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblEmail);

							JLabel lblGender = new JLabel("Giới tính: " + gender);
							lblGender.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblGender);

							JLabel lblAddress = new JLabel("Địa chỉ: " + address);
							lblAddress.setFont(new Font("Arial", Font.ITALIC, 12));
							userPanel.add(lblAddress);

							// Hiển thị nội dung
							JPanel contentPanel = new JPanel();
							JLabel lblContent = new JLabel("<html>Nội dung: " + content + "</html>");
							lblContent.setFont(new Font("Arial", Font.PLAIN, 12));
							contentPanel.add(lblContent);

							// Thêm các panel vào detail panel
							detailPanel.add(titlePanel);
							detailPanel.add(userPanel);
							detailPanel.add(contentPanel);

							// Hiển thị ảnh nếu có
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
				loadingDialog.dispose(); // Đóng dialog loading
			}
		};

		// Hiển thị dialog và bắt đầu worker
		SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
		worker.execute();
	}

	// Phương thức tải bài viết để chỉnh sửa
	private void loadBlogForEdit(int id) {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT title, content, hash_img FROM tbl_post WHERE id = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				// Điền dữ liệu vào các trường nhập liệu
				txtTitle.setText(rs.getString("title"));
				txtContent.setText(rs.getString("content"));
				txtImgLink.setText(rs.getString("hash_img"));
				
				// Chọn dòng tương ứng trong bảng
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
				"Lỗi khi tải bài viết để chỉnh sửa: " + e.getMessage(),
				"Lỗi cơ sở dữ liệu",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// Thay đổi phần input panel để có giao diện đẹp hơn
	private void setupInputPanel() {
		JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
		inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Panel chứa các trường nhập liệu
		JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		
		// Trường nhập tiêu đề
		JLabel lblTitle = new JLabel("Tiêu đề:");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtTitle = new JTextField();
		txtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblTitle);
		fieldsPanel.add(txtTitle);
		
		// Trường nhập nội dung
		JLabel lblContent = new JLabel("Nội dung:");
		lblContent.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtContent = new JTextField();
		txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblContent);
		fieldsPanel.add(txtContent);
		
		// Trường nhập link ảnh
		JLabel lblImgLink = new JLabel("Liên kết hình ảnh:");
		lblImgLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
		txtImgLink = new JTextField();
		txtImgLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		fieldsPanel.add(lblImgLink);
		fieldsPanel.add(txtImgLink);
		
		inputPanel.add(fieldsPanel, BorderLayout.CENTER);
		
		// Panel chứa các nút chức năng
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		// Tạo các nút với style tương ứng
		btnAdd = createStyledButton("Thêm", new Color(46, 204, 113));
		btnUpdate = createStyledButton("Cập nhật", new Color(52, 152, 219));
		btnDelete = createStyledButton("Xóa", new Color(231, 76, 60));
		btnClear = createStyledButton("Xóa trắng", new Color(149, 165, 166));
		
		// Thêm các nút vào panel
		buttonPanel.add(btnAdd);
		buttonPanel.add(btnUpdate);
		buttonPanel.add(btnDelete);
		buttonPanel.add(btnClear);
		
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		add(inputPanel, BorderLayout.SOUTH);
	}

	// Phương thức tạo nút với style tùy chỉnh
	private JButton createStyledButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setFont(new Font("Segoe UI", Font.BOLD, 12));
		button.setForeground(Color.WHITE);
		button.setBackground(bgColor);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setPreferredSize(new Dimension(100, 35));
		
		// Thêm hiệu ứng hover cho nút
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

	// Phương thức cấu hình cột Edit trong bảng
	private void configureEditButtonColumn() {
		// Thiết lập độ rộng cho các cột
		table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
		table.getColumnModel().getColumn(1).setPreferredWidth(150); // User ID
		table.getColumnModel().getColumn(2).setPreferredWidth(200); // Tiêu đề
		table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Nội dung
		table.getColumnModel().getColumn(4).setPreferredWidth(150); // Link ảnh
		table.getColumnModel().getColumn(5).setPreferredWidth(200); // Ngày tạo
		table.getColumnModel().getColumn(6).setPreferredWidth(140); // Thao tác

		// Cấu hình renderer cho cột thao tác
		table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				// Tạo panel chứa các nút
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
				panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
				
				// Tạo nút Edit
				JButton editBtn = new JButton("Edit");
				editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
				editBtn.setBackground(new Color(52, 152, 219));
				editBtn.setForeground(Color.WHITE);
				editBtn.setPreferredSize(new Dimension(55, 24));
				editBtn.setBorderPainted(false);
				editBtn.setFocusPainted(false);
				
				// Tạo nút Detail
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
		
		// Thêm cell editor cho cột thao tác
		table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
			private JPanel panel;
			private JButton editBtn;
			private JButton detailBtn;
			
			{
				// Khởi tạo các thành phần UI
				panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
				
				editBtn = new JButton("Edit");
				styleActionButton(editBtn, new Color(52, 152, 219));
				
				detailBtn = new JButton("Detail");
				styleActionButton(detailBtn, new Color(46, 204, 113));
				
				panel.add(editBtn);
				panel.add(detailBtn);
				
				// Thêm sự kiện cho nút Edit
				editBtn.addActionListener(e -> {
					int row = table.getSelectedRow();
					if (row != -1) {
						int id = (int) tableModel.getValueAt(row, 0);
						loadBlogForEdit(id);
					}
					fireEditingStopped();
				});
				
				// Thêm sự kiện cho nút Detail
				detailBtn.addActionListener(e -> {
					int row = table.getSelectedRow();
					if (row != -1) {
						int id = (int) tableModel.getValueAt(row, 0);
						loadBlogDetails(id);
					}
					fireEditingStopped();
				});
			}
			
			// Helper method để style các nút thao tác
			private void styleActionButton(JButton btn, Color bgColor) {
				btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
				btn.setBackground(bgColor);
				btn.setForeground(Color.WHITE);
				btn.setPreferredSize(new Dimension(55, 24));
				btn.setBorderPainted(false);
				btn.setFocusPainted(false);
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

		// Cho phép tự động điều chỉnh kích thước cột
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
		// Tạo panel chứa các thành phần lọc
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		filterPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
		
		// Lọc theo tác giả
		JLabel lblAuthor = new JLabel("Tác giả:");
		lblAuthor.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboAuthor = new JComboBox<>();
		cboAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboAuthor.setPreferredSize(new Dimension(150, 25));
		loadAuthors(); // Tải danh sách tác giả
		
		// Lọc theo tháng
		JLabel lblMonth = new JLabel("Tháng:");
		lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboMonth = new JComboBox<>(new String[] {
			"All Months", "January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December"
		});
		cboMonth.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboMonth.setPreferredSize(new Dimension(100, 25));
		
		// Lọc theo năm
		JLabel lblYear = new JLabel("Năm:");
		lblYear.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboYear = new JComboBox<>();
		cboYear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboYear.setPreferredSize(new Dimension(80, 25));
		loadYears(); // Tải danh sách năm
		
		// Tùy chọn sắp xếp
		JLabel lblSort = new JLabel("Sắp xếp:");
		lblSort.setFont(new Font("Segoe UI", Font.BOLD, 12));
		cboSort = new JComboBox<>(new String[] {"Newest First", "Oldest First"});
		cboSort.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cboSort.setPreferredSize(new Dimension(100, 25));
		
		// Nút lọc và xóa bộ lọc
		btnFilter = createStyledButton("Lọc", new Color(52, 152, 219));
		btnFilter.setPreferredSize(new Dimension(80, 25));
		btnFilter.addActionListener(e -> applyFilters());
		
		btnClearFilter = createStyledButton("Xóa", new Color(149, 165, 166));
		btnClearFilter.setPreferredSize(new Dimension(80, 25));
		btnClearFilter.addActionListener(e -> clearFilters());
		
		// Thêm các thành phần vào panel lọc
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
		
		// Tạo panel chứa cả search và filter
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(searchPanel, BorderLayout.NORTH);
		topPanel.add(filterPanel, BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
	}

	// Phương thức tải danh sách tác giả từ database
	private void loadAuthors() {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT DISTINCT u.id, u.username FROM tbl_user u JOIN tbl_post p ON u.id = p.user_id ORDER BY u.username";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			
			// Xóa danh sách cũ và thêm lựa chọn mặc định
			cboAuthor.removeAllItems();
			cboAuthor.addItem(new Author(0, "All Authors")); 
			
			// Thêm các tác giả vào combobox
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

	// Phương thức tải danh sách năm từ database
	private void loadYears() {
		try {
			connection = DBConnection.getConnection();
			String query = "SELECT DISTINCT YEAR(create_at) as year FROM tbl_post ORDER BY year DESC";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			
			// Xóa danh sách cũ và thêm lựa chọn mặc định
			cboYear.removeAllItems();
			cboYear.addItem("All Years");
			
			// Thêm các năm vào combobox
			while (rs.next()) {
				cboYear.addItem(String.valueOf(rs.getInt("year")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Phương thức áp dụng các bộ lọc đã chọn
	private void applyFilters() {
		searchBlogs(txtSearch.getText());
	}

	// Phương thức xóa tất cả các bộ lọc
	private void clearFilters() {
		txtSearch.setText("");
		cboAuthor.setSelectedIndex(0);     // Chọn "All Authors"
		cboMonth.setSelectedItem("All Months");
		cboYear.setSelectedItem("All Years");
		cboSort.setSelectedIndex(0);        // Chọn "Newest First"
		searchBlogs("");                    // Tải lại danh sách không có bộ lọc
	}

}
