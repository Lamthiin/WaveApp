package com.ptithcm.waveapp;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.ptithcm.waveapp.adapter.UserAdminAdapter;
import com.ptithcm.waveapp.database.DatabaseHelper;
import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import com.ptithcm.waveapp.util.SearchNormalizer;
import java.util.ArrayList;
import java.util.List;

public class AdminUserManagementActivity extends BaseAdminActivity {

    private EditText etSearchUser;
    private ImageButton btnClearSearchUser;
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
        btnClearSearchUser = findViewById(R.id.btnClearSearchAdmin);
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
                if (btnClearSearchUser != null) {
                    btnClearSearchUser.setVisibility(s != null && s.length() > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
                }
                // Khi gõ chữ, áp dụng ngay tìm kiếm và lọc
                applySearchAndFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (btnClearSearchUser != null) {
            btnClearSearchUser.setOnClickListener(v -> {
                etSearchUser.setText("");
                etSearchUser.clearFocus();
                btnClearSearchUser.setVisibility(android.view.View.GONE);
            });
        }
    }

    @Override
    protected void clearAdminSearchIfPresent() {
        if (etSearchUser != null) {
            etSearchUser.setText("");
            etSearchUser.clearFocus();
        }
        if (btnClearSearchUser != null) {
            btnClearSearchUser.setVisibility(android.view.View.GONE);
        }
    }

    // Đổi màu sắc giao diện (xanh lá cho nút đang chọn, xám cho nút không chọn)
    private void updateFilterUI(TextView active, TextView inactive1, TextView inactive2) {
        active.setBackgroundResource(R.drawable.bg_admin_tab_active);
        active.setTextColor(Color.parseColor("#FFFFFF"));

        inactive1.setBackgroundResource(R.drawable.bg_admin_tab_inactive);
        inactive1.setTextColor(Color.parseColor("#B3B3B3"));

        inactive2.setBackgroundResource(R.drawable.bg_admin_tab_inactive);
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_user_role, null, false);
        TextView tvTitle = dialogView.findViewById(R.id.tvUserRoleDialogTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvUserRoleDialogSubtitle);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupUserRole);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelUserRoleDialog);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveUserRoleDialog);

        tvTitle.setText("Cập nhật quyền");
        tvSubtitle.setText("Chọn quyền cho " + user.getName());

        if (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN")) {
            radioGroup.check(R.id.radioUserRoleAdmin);
        } else {
            radioGroup.check(R.id.radioUserRoleUser);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newRole = radioGroup.getCheckedRadioButtonId() == R.id.radioUserRoleAdmin ? "ADMIN" : "USER";

            boolean isUpdated = userRepository.updateUserRole(user.getId(), newRole);
            if (isUpdated) {
                Toast.makeText(this, "Đã cập nhật quyền thành " + newRole, Toast.LENGTH_SHORT).show();
                loadDataFromDatabase();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
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
