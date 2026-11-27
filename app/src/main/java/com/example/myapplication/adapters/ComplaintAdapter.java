package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Complaint;
import com.example.myapplication.utils.Constants;

import java.util.List;

/**
 * ComplaintAdapter binds a list of Complaint objects to item_complaint.xml cards in a RecyclerView.
 * It applies the PRD Section 9 badge colors:
 * - Status: Pending = orange (#F59E0B), Resolved = green (#16A34A)
 * - Priority: Low = green, Medium = orange, High = red
 */
public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    /**
     * Listener interface notified when a user or admin taps a complaint card.
     */
    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    private List<Complaint> complaintList;
    private final boolean showStudentName;
    private final OnComplaintClickListener listener;

    /**
     * Constructor receiving the complaint list, whether to display the student's name (admin view),
     * and the card click listener.
     */
    public ComplaintAdapter(List<Complaint> complaintList, boolean showStudentName,
                            OnComplaintClickListener listener) {
        this.complaintList = complaintList;
        this.showStudentName = showStudentName;
        this.listener = listener;
    }

    /**
     * Replaces the current complaint list and refreshes the RecyclerView.
     */
    public void updateList(List<Complaint> newList) {
        this.complaintList = newList;
        notifyDataSetChanged();
    }

    /**
     * Inflates one item_complaint.xml card and wraps it in a ComplaintViewHolder.
     */
    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    /**
     * Binds a Complaint object's fields and colored status/priority badges to the ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTicketCode.setText(complaint.getTicketCode());
        holder.tvTitle.setText(complaint.getTitle());
        holder.tvDescription.setText(complaint.getDescription());
        holder.tvDate.setText(complaint.getCreatedDate());
        holder.tvCategoryChip.setText(complaint.getCategory());

        if (showStudentName && complaint.getUserName() != null && !complaint.getUserName().isEmpty()) {
            holder.tvSubheader.setText(context.getString(
                    R.string.complaint_card_student_fmt,
                    complaint.getUserName(),
                    complaint.getCategory()
            ));
        } else {
            holder.tvSubheader.setText("• " + complaint.getCategory());
        }

        // Status badge color (PRD Section 9: Pending = orange, Resolved = green)
        boolean isResolved = Constants.STATUS_RESOLVED.equalsIgnoreCase(complaint.getStatus());
        holder.tvStatusBadge.setText(complaint.getStatus());
        if (isResolved) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_resolved);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_resolved));
            holder.viewStatusStripe.setBackgroundColor(ContextCompat.getColor(context, R.color.status_resolved));
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
            holder.viewStatusStripe.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending));
        }

        // Priority text color (PRD Section 9: Low = green, Medium = orange, High = red)
        String priority = complaint.getPriority();
        holder.tvPriorityChip.setText(priority);
        if (Constants.PRIORITY_HIGH.equalsIgnoreCase(priority)) {
            holder.tvPriorityChip.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
        } else if (Constants.PRIORITY_LOW.equalsIgnoreCase(priority)) {
            holder.tvPriorityChip.setTextColor(ContextCompat.getColor(context, R.color.priority_low));
        } else {
            holder.tvPriorityChip.setTextColor(ContextCompat.getColor(context, R.color.priority_medium));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComplaintClick(complaint);
            }
        });
    }

    /**
     * Returns the number of complaints currently in the adapter list.
     */
    @Override
    public int getItemCount() {
        return complaintList != null ? complaintList.size() : 0;
    }

    /**
     * ViewHolder caching child views inside item_complaint.xml.
     */
    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {

        final View viewStatusStripe;
        final TextView tvTicketCode;
        final TextView tvSubheader;
        final TextView tvStatusBadge;
        final TextView tvTitle;
        final TextView tvDescription;
        final TextView tvDate;
        final TextView tvCategoryChip;
        final TextView tvPriorityChip;

        /**
         * Connects fields to views inside item_complaint.xml using findViewById.
         */
        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusStripe = itemView.findViewById(R.id.viewComplaintStatusStripe);
            tvTicketCode = itemView.findViewById(R.id.tvComplaintTicketCode);
            tvSubheader = itemView.findViewById(R.id.tvComplaintSubheader);
            tvStatusBadge = itemView.findViewById(R.id.tvComplaintStatusBadge);
            tvTitle = itemView.findViewById(R.id.tvComplaintTitle);
            tvDescription = itemView.findViewById(R.id.tvComplaintDescription);
            tvDate = itemView.findViewById(R.id.tvComplaintDate);
            tvCategoryChip = itemView.findViewById(R.id.tvComplaintCategoryChip);
            tvPriorityChip = itemView.findViewById(R.id.tvComplaintPriorityChip);
        }
    }
}
