package com.example.myapplication.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.adapters.UserListAdapter;
import com.example.myapplication.admin.AdminDashboardActivity;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * UsersFragment displays all registered users in a ListView with a custom BaseAdapter (PRD Section 3 & D8).
 * The admin can tap the trash button or long-press a student row to delete that student.
 * The admin account cannot be deleted, and deleting a student also deletes their complaints
 * via SQLite ON DELETE CASCADE.
 */
public class UsersFragment extends Fragment implements UserListAdapter.OnUserDeleteListener {

    private TextView tvCountBadge;
    private ListView lvUsers;
    private TextView tvEmpty;

    private DatabaseHelper dbHelper;
    private UserListAdapter userListAdapter;
    private List<User> users = new ArrayList<>();

    /**
     * Inflates fragment_users.xml, initializes the ListView with UserListAdapter, and wires long-press.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvCountBadge = view.findViewById(R.id.tvUsersCountBadge);
        lvUsers = view.findViewById(R.id.lvUsers);
        tvEmpty = view.findViewById(R.id.tvEmptyUsers);

        userListAdapter = new UserListAdapter(requireContext(), new ArrayList<>(), this);
        lvUsers.setAdapter(userListAdapter);

        // Also support long-pressing any row in the ListView to delete (PRD D8)
        lvUsers.setOnItemLongClickListener((parent, rowView, position, id) -> {
            if (position >= 0 && position < users.size()) {
                onDeleteUserClick(users.get(position));
            }
            return true;
        });

        return view;
    }

    /**
     * Refreshes the users ListView whenever the tab becomes visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    /**
     * Reads all users from SQLite and updates the ListView and count badge.
     */
    private void loadUsers() {
        users = dbHelper.getAllUsers();
        tvCountBadge.setText(getString(R.string.admin_users_count_fmt, users.size()));
        userListAdapter.updateList(users);

        if (users.isEmpty()) {
            lvUsers.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            lvUsers.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Protects the admin account and shows a confirmation AlertDialog before deleting a student (PRD D8).
     */
    @Override
    public void onDeleteUserClick(User user) {
        if (Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            Toast.makeText(requireContext(), R.string.admin_user_protected_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.admin_user_delete_title)
                .setMessage(getString(R.string.admin_user_delete_message, user.getName(), user.getEmail()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    boolean deleted = dbHelper.deleteUser(user.getId());
                    if (deleted) {
                        Toast.makeText(requireContext(), R.string.admin_user_deleted_toast, Toast.LENGTH_SHORT).show();
                        loadUsers();
                        if (getActivity() instanceof AdminDashboardActivity) {
                            ((AdminDashboardActivity) getActivity()).updateHeaderSummary();
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
