package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.Constants;

import java.util.List;
import java.util.Locale;

/**
 * UserListAdapter is a custom BaseAdapter that populates the ListView of registered users
 * on the Admin Users tab (PRD Section 3 & Section 5.3 D8).
 */
public class UserListAdapter extends BaseAdapter {

    /**
     * Listener notified when the admin taps the Delete icon on a student user row.
     */
    public interface OnUserDeleteListener {
        void onDeleteUserClick(User user);
    }

    private final Context context;
    private List<User> userList;
    private final OnUserDeleteListener deleteListener;

    /**
     * Constructor receiving the Context, initial list of users, and delete click listener.
     */
    public UserListAdapter(Context context, List<User> userList, OnUserDeleteListener deleteListener) {
        this.context = context;
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    /**
     * Replaces the current user list and refreshes the ListView.
     */
    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    /**
     * Returns the number of users in the list.
     */
    @Override
    public int getCount() {
        return userList != null ? userList.size() : 0;
    }

    /**
     * Returns the User object at the given position.
     */
    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    /**
     * Returns the user's database ID as the row ID.
     */
    @Override
    public long getItemId(int position) {
        return userList.get(position).getId();
    }

    /**
     * Inflates or reuses item_user.xml and populates the user's initials, name, email, role badge,
     * and delete button (hiding the delete button for the protected admin account).
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        }

        User user = userList.get(position);

        View frameAvatar = view.findViewById(R.id.frameUserAvatar);
        TextView tvInitials = view.findViewById(R.id.tvUserInitials);
        TextView tvName = view.findViewById(R.id.tvItemUserName);
        TextView tvRoleBadge = view.findViewById(R.id.tvItemUserRoleBadge);
        TextView tvEmail = view.findViewById(R.id.tvItemUserEmail);
        View btnDelete = view.findViewById(R.id.btnDeleteUser);

        tvInitials.setText(extractInitials(user.getName()));
        tvName.setText(user.getName());
        tvEmail.setText(user.getEmail());

        boolean isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole());
        if (isAdmin) {
            tvRoleBadge.setText(R.string.user_role_badge_admin);
            tvRoleBadge.setBackgroundResource(R.drawable.bg_chip_pink);
            tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.crimson_dark));
            frameAvatar.setBackgroundResource(R.drawable.bg_circle_crimson);
            tvInitials.setTextColor(ContextCompat.getColor(context, R.color.white));
            // Admin account cannot be deleted (PRD D8)
            btnDelete.setVisibility(View.INVISIBLE);
            btnDelete.setOnClickListener(null);
        } else {
            tvRoleBadge.setText(R.string.user_role_badge_student);
            tvRoleBadge.setBackgroundResource(R.drawable.bg_chip_blue);
            tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.midnight));
            frameAvatar.setBackgroundResource(R.drawable.bg_circle_blue);
            tvInitials.setTextColor(ContextCompat.getColor(context, R.color.midnight));
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteUserClick(user);
                }
            });
        }

        return view;
    }

    /**
     * Extracts up to 2 uppercase initials from a full name (e.g. "Hamza Ali" -> "HA").
     */
    private String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.US);
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.US);
    }
}
