package com.ptithcm.waveapp.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptithcm.waveapp.R;
import com.ptithcm.waveapp.util.ImageFileHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminOverviewAdapter extends RecyclerView.Adapter<AdminOverviewAdapter.ViewHolder> {

    public static class AdminOverviewItem {
        public final String id;
        public final String indexLabel;
        public final String title;
        public final String subtitle;
        public final String meta;
        public final String imageUrl;
        public final int placeholderResId;
        public final boolean hidden;
        public final boolean singleLineTitle;
        public final boolean singleLineMeta;

        public AdminOverviewItem(String id, String indexLabel, String title, String subtitle, String meta, String imageUrl, int placeholderResId, boolean hidden, boolean singleLineTitle, boolean singleLineMeta) {
            this.id = id;
            this.indexLabel = indexLabel;
            this.title = title;
            this.subtitle = subtitle;
            this.meta = meta;
            this.imageUrl = imageUrl;
            this.placeholderResId = placeholderResId;
            this.hidden = hidden;
            this.singleLineTitle = singleLineTitle;
            this.singleLineMeta = singleLineMeta;
        }
    }

    public interface OnAdminOverviewActionListener {
        void onViewDetailClick(AdminOverviewItem item);
        void onEditClick(AdminOverviewItem item);
        default void onHideClick(AdminOverviewItem item) {}
        default void onRestoreClick(AdminOverviewItem item) {}
        default void onDeleteClick(AdminOverviewItem item) {}
    }

    public interface OnAdminOverviewItemClickListener {
        void onImageClick(AdminOverviewItem item);
    }

    private final List<AdminOverviewItem> items = new ArrayList<>();
    private OnAdminOverviewActionListener actionListener;
    private OnAdminOverviewItemClickListener itemClickListener;
    private int actionMenuResId = R.menu.admin_item_actions_menu;

    public void setActionMenuResId(int actionMenuResId) {
        this.actionMenuResId = actionMenuResId;
    }

    public void setItems(List<AdminOverviewItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void addItems(List<AdminOverviewItem> moreItems) {
        if (moreItems == null || moreItems.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(start, moreItems.size());
    }

    public void setOnAdminOverviewActionListener(OnAdminOverviewActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setOnAdminOverviewItemClickListener(OnAdminOverviewItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_overview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminOverviewItem item = items.get(position);
        holder.tvIndex.setText(item.indexLabel);
        holder.tvIndex.setVisibility(item.indexLabel == null || item.indexLabel.isEmpty() ? View.GONE : View.VISIBLE);
        holder.tvTitle.setText(item.title);
        holder.tvTitle.setMaxLines(item.singleLineTitle ? 1 : Integer.MAX_VALUE);
        holder.tvTitle.setEllipsize(item.singleLineTitle ? TextUtils.TruncateAt.END : null);
        holder.tvSubtitle.setText(item.subtitle);
        holder.tvMeta.setText(item.meta);
        holder.tvMeta.setMaxLines(item.singleLineMeta ? 1 : Integer.MAX_VALUE);
        holder.tvMeta.setEllipsize(item.singleLineMeta ? TextUtils.TruncateAt.END : null);
        ImageFileHelper.loadIntoImageView(holder.itemView.getContext(), item.imageUrl, holder.ivImage, item.placeholderResId);
        holder.ivImage.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onImageClick(item);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onImageClick(item);
            }
        });
        holder.btnMore.setVisibility(actionListener != null ? View.VISIBLE : View.GONE);
        holder.btnMore.setOnClickListener(v -> {
            if (actionListener == null) {
                return;
            }

            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.btnMore);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(actionMenuResId, popupMenu.getMenu());
            MenuItem hideItem = popupMenu.getMenu().findItem(R.id.action_hide);
            MenuItem restoreItem = popupMenu.getMenu().findItem(R.id.action_restore);
            if (hideItem != null) {
                hideItem.setVisible(!item.hidden);
            }
            if (restoreItem != null) {
                restoreItem.setVisible(item.hidden);
            }
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_view_detail) {
                    actionListener.onViewDetailClick(item);
                    return true;
                }
                if (itemId == R.id.action_edit) {
                    actionListener.onEditClick(item);
                    return true;
                }
                if (itemId == R.id.action_hide) {
                    actionListener.onHideClick(item);
                    return true;
                }
                if (itemId == R.id.action_restore) {
                    actionListener.onRestoreClick(item);
                    return true;
                }
                if (itemId == R.id.action_delete) {
                    actionListener.onDeleteClick(item);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvIndex;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvMeta;
        private final ImageButton btnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvAdminItemIndex);
            ivImage = itemView.findViewById(R.id.ivAdminItemImage);
            tvTitle = itemView.findViewById(R.id.tvAdminItemTitle);
            tvSubtitle = itemView.findViewById(R.id.tvAdminItemSubtitle);
            tvMeta = itemView.findViewById(R.id.tvAdminItemMeta);
            btnMore = itemView.findViewById(R.id.btnAdminItemMore);
        }
    }
}
