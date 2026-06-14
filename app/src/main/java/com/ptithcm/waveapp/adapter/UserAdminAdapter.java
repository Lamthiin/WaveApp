package com.ptithcm.waveapp.adapter;

import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    public UserAdminAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

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

        String usernameRole = "@" + user.getUsername() + " • " + user.getRole();
        holder.tvUsernameRole.setText(usernameRole);

        holder.btnMoreUser.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.btnMoreUser);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.user_item_actions_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_edit) {
                    listener.onEditClick(user);
                    return true;
                }
                if (itemId == R.id.action_delete) {
                    listener.onDeleteClick(user);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvUsernameRole, tvEmail;
        ImageButton btnMoreUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvUsernameRole = itemView.findViewById(R.id.tvUsernameRole);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnMoreUser = itemView.findViewById(R.id.btnMoreUser);
        }
    }
}
