package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Notice;

import java.util.List;

/**
 * NoticeAdapter binds a list of Notice objects to item_notice.xml cards in a RecyclerView.
 * It works in two modes:
 * - Admin mode (isAdmin = true): shows the Edit and Delete icon buttons on each card.
 * - User mode (isAdmin = false): hides the Edit and Delete buttons so students only read/tap notices.
 */
public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    /**
     * Listener interface for card clicks, edit clicks, and delete clicks.
     */
    public interface OnNoticeActionListener {
        void onNoticeClick(Notice notice);

        void onEditClick(Notice notice);

        void onDeleteClick(Notice notice);
    }

    private List<Notice> noticeList;
    private final boolean isAdmin;
    private final OnNoticeActionListener listener;

    /**
     * Constructor that receives the initial notice list, whether the viewer is admin, and the click listener.
     */
    public NoticeAdapter(List<Notice> noticeList, boolean isAdmin, OnNoticeActionListener listener) {
        this.noticeList = noticeList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    /**
     * Replaces the current notice list (for example after a search or database refresh) and updates the UI.
     */
    public void updateList(List<Notice> newList) {
        this.noticeList = newList;
        notifyDataSetChanged();
    }

    /**
     * Inflates one item_notice.xml row layout and wraps it in a NoticeViewHolder.
     */
    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    /**
     * Fills a NoticeViewHolder with the title, date, content, and code of the Notice at position.
     */
    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);

        holder.tvNoticeCode.setText(notice.getNoticeCode());
        holder.tvNoticeDate.setText(notice.getCreatedDate());
        holder.tvNoticeTitle.setText(notice.getTitle());
        holder.tvNoticeContent.setText(notice.getContent());

        if (isAdmin) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(notice);
                }
            });
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(notice);
                }
            });
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoticeClick(notice);
            }
        });
    }

    /**
     * Returns the total number of notices currently in the adapter list.
     */
    @Override
    public int getItemCount() {
        return noticeList != null ? noticeList.size() : 0;
    }

    /**
     * ViewHolder class that caches findViewById references for one notice card.
     */
    public static class NoticeViewHolder extends RecyclerView.ViewHolder {

        final TextView tvNoticeCode;
        final TextView tvNoticeDate;
        final TextView tvNoticeTitle;
        final TextView tvNoticeContent;
        final View layoutAdminActions;
        final View btnEdit;
        final View btnDelete;

        /**
         * Finds and stores all child views inside item_notice.xml.
         */
        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoticeCode = itemView.findViewById(R.id.tvNoticeCode);
            tvNoticeDate = itemView.findViewById(R.id.tvNoticeDate);
            tvNoticeTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvNoticeContent = itemView.findViewById(R.id.tvNoticeContent);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminNoticeActions);
            btnEdit = itemView.findViewById(R.id.btnEditNotice);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotice);
        }
    }
}
