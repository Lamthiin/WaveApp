package com.ptithcm.waveapp;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ptithcm.waveapp.adapter.UserAdminAdapter;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.SearchNormalizer;
import java.util.ArrayList;
import java.util.List;

public class AdminUserManagementActivity extends BaseAdminActivity {

    private EditText etSearchUser;
    private TextView filterAll, filterAdmin, filterUser;
    private TextView tvAdminAvatar;
    private RecyclerView rvUsers;

    private UserAdminAdapter adapter;
    private UserRepository userRepository;

    // Danh sách gốc chứa tất cả user từ Database
    private List<User> allUsersList = new ArrayList<>();

    // Biến lưu trạng thái bộ lọc hiện tại
    private String currentRoleFilter = "ALL"; // "ALL", "ADMIN", "USER"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // Khởi tạo Database và TokenManager
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        userRepository = new UserRepository(dbHelper);

        initViews();
        setupAdminChrome(
                R.id.tvHeaderTitle,
                R.id.tvAdminAvatar,
                R.id.bottomAdminNavigation,
                R.id.nav_admin_users,
                "Quản lý người dùng"
        );
        setupRecyclerView();
        setupFilters();
        setupSearch();

        // Tải dữ liệu lần đầu từ SQLite
        loadDataFromDatabase();
    }

    private void initViews() {
        etSearchUser = findViewById(R.id.etSearchUser);
        filterAll = findViewById(R.id.filterAll);
        filterAdmin = findViewById(R.id.filterAdmin);
        filterUser = findViewById(R.id.filterUser);
        tvAdminAvatar = findViewById(R.id.tvAdminAvatar);
        rvUsers = findViewById(R.id.rvUsers);
    }

    private void setupRecyclerView() {
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdminAdapter(new UserAdminAdapter.OnUserActionListener() {
            @Override
            public void onEditClick(User user) {
                showEditRoleDialog(user);
            }

            @Override
            public void onDeleteClick(User user) {
                showDeleteConfirmDialog(user);
            }
        });

        rvUsers.setAdapter(adapter);
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> {
            updateFilterUI(filterAll, filterAdmin, filterUser);
            currentRoleFilter = "ALL";
            applySearchAndFilter(etSearchUser.getText().toString());
        });

        filterAdmin.setOnClickListener(v -> {
            updateFilterUI(filterAdmin, filterAll, filterUser);
            currentRoleFilter = "ADMIN";
            applySearchAndFilter(etSearchUser.getText().toString());
        });

        filterUser.setOnClickListener(v -> {
            updateFilterUI(filterUser, filterAll, filterAdmin);
            currentRoleFilter = "USER";
            applySearchAndFilter(etSearchUser.getText().toString());
        });
    }

    private void setupSearch() {
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi gõ chữ, áp dụng ngay tìm kiếm và lọc
                applySearchAndFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Đổi màu sắc giao diện (xanh lá cho nút đang chọn, xám cho nút không chọn)
    private void updateFilterUI(TextView active, TextView inactive1, TextView inactive2) {
        active.setBackgroundColor(Color.parseColor("#1DB954"));
        active.setTextColor(Color.parseColor("#FFFFFF"));

        inactive1.setBackgroundColor(Color.parseColor("#282828"));
        inactive1.setTextColor(Color.parseColor("#B3B3B3"));

        inactive2.setBackgroundColor(Color.parseColor("#282828"));
        inactive2.setTextColor(Color.parseColor("#B3B3B3"));
    }

    // Lấy dữ liệu thật từ Database
    private void loadDataFromDatabase() {
        allUsersList = userRepository.getAllUsers();
        applySearchAndFilter(etSearchUser.getText().toString());
    }

    // Xử lý đồng thời cả việc gõ chữ Tìm Kiếm và bấm nút Lọc
    private void applySearchAndFilter(String query) {
        List<User> filteredList = new ArrayList<>();

        for (User user : allUsersList) {
            // Kiểm tra an toàn cho tên và email null
            String name = user.getName() != null ? user.getName() : "";
            String email = user.getEmail() != null ? user.getEmail() : "";

            // 1. Kiểm tra điều kiện tìm kiếm (Tên hoặc Email)
            boolean matchesSearch = SearchNormalizer.containsNormalized(name, query)
                    || SearchNormalizer.containsNormalized(email, query);

            // 2. Kiểm tra điều kiện Role
            boolean matchesRole = currentRoleFilter.equals("ALL") ||
                    (user.getRole() != null && user.getRole().equalsIgnoreCase(currentRoleFilter));

            // Nếu thỏa mãn cả 2 thì đưa vào danh sách hiển thị
            if (matchesSearch && matchesRole) {
                filteredList.add(user);
            }
        }

        // Cập nhật lên RecyclerView
        adapter.setUsers(filteredList);
    }

    // --- CÁC HÀM XỬ LÝ DIALOG SỬA / XÓA ---

    private void showEditRoleDialog(User user) {
        String[] roles = {"USER", "ADMIN"};
        // Xác định vị trí mặc định đang được chọn
        int checkedItem = (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN")) ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật quyền cho " + user.getName())
                .setSingleChoiceItems(roles, checkedItem, (dialog, which) -> {
                    String newRole = roles[which];

                    // Gọi hàm cập nhật từ UserRepository
                    boolean isUpdated = userRepository.updateUserRole(user.getId(), newRole);

                    if (isUpdated) {
                        Toast.makeText(this, "Đã cập nhật quyền thành " + newRole, Toast.LENGTH_SHORT).show();
                        loadDataFromDatabase(); // Load lại danh sách ngay lập tức
                    } else {
                        Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss(); // Đóng hộp thoại
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmDialog(User user) {
        // Lấy ID của Admin đang đăng nhập (để tránh tự xóa chính mình)
        // Giả định bạn có hàm getUserId() trong TokenManager. Nếu là tên khác, hãy đổi lại cho khớp nhé!
        String currentAdminId = tokenManager.getUserId();

        if (user.getId().equals(currentAdminId)) {
            Toast.makeText(this, "Bạn không thể tự xóa tài khoản của chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng " + user.getName() + " không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {

                    // Gọi hàm xóa từ UserRepository
                    boolean isDeleted = userRepository.deleteUser(user.getId());

                    if (isDeleted) {
                        Toast.makeText(this, "Đã xóa " + user.getName(), Toast.LENGTH_SHORT).show();
                        loadDataFromDatabase(); // Load lại danh sách
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa người dùng!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
