package com.ptithcm.waveapp.adapter; // Đổi lại package name cho đúng với thư mục của bạn

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.User; // Đảm bảo import đúng model User của bạn

import java.util.ArrayList;
import java.util.List;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final OnUserActionListener listener;

    // Interface để bắt sự kiện Sửa/Xóa truyền ngược lại Activity
    public interface OnUserActionListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    public UserAdminAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    // Hàm dùng để cập nhật lại danh sách khi tìm kiếm hoặc lọc
    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        // Hiển thị Username và Role
        String usernameRole = "@" + user.getUsername() + " • " + user.getRole();
        holder.tvUsernameRole.setText(usernameRole);

        // TODO: Load avatar bằng Glide hoặc thư viện hình ảnh của bạn
        // Glide.with(holder.itemView.getContext()).load(user.getAvatar()).into(holder.ivAvatar);

        // Bắt sự kiện bấm nút
        holder.btnEditUser.setOnClickListener(v -> listener.onEditClick(user));
        holder.btnDeleteUser.setOnClickListener(v -> listener.onDeleteClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvUsernameRole, tvEmail;
        ImageButton btnEditUser, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvUsernameRole = itemView.findViewById(R.id.tvUsernameRole);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}