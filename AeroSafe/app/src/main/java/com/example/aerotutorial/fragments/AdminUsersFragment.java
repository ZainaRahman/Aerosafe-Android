package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.aerotutorial.R;
import com.example.aerotutorial.adapters.UsersAdapter;
import com.example.aerotutorial.models.User;
import com.example.aerotutorial.repository.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private RecyclerView rvUsers;
    private LinearLayout llEmptyState;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnAllUsers, btnUsers, btnResearchers, btnAdmins;
    private TextView tvUserCount, tvEmptyMessage;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    private UsersAdapter adapter;
    private List<User> usersList;
    private AuthRepository authRepository;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        authRepository = new AuthRepository();

        loadUsers();
    }

    private void initViews(View view) {
        rvUsers = view.findViewById(R.id.rvUsers);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        toggleGroup = view.findViewById(R.id.toggleGroup);
        btnAllUsers = view.findViewById(R.id.btnAllUsers);
        btnUsers = view.findViewById(R.id.btnUsers);
        btnResearchers = view.findViewById(R.id.btnResearchers);
        btnAdmins = view.findViewById(R.id.btnAdmins);
        tvUserCount = view.findViewById(R.id.tvUserCount);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadUsers);
            swipeRefresh.setColorSchemeResources(R.color.primary);
        }
    }

    private void setupRecyclerView() {
        usersList = new ArrayList<>();
        adapter = new UsersAdapter(usersList);

        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        // Ensure "All Users" is checked by default
        if (toggleGroup != null && btnAllUsers != null) {
            toggleGroup.check(R.id.btnAllUsers);
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                android.util.Log.d("AdminUsersFragment", "Button checked: " + checkedId + ", isChecked: " + isChecked);

                if (checkedId == R.id.btnAllUsers) {
                    currentFilter = "all";
                    android.util.Log.d("AdminUsersFragment", "Filter changed to: ALL");
                } else if (checkedId == R.id.btnUsers) {
                    currentFilter = "user";
                    android.util.Log.d("AdminUsersFragment", "Filter changed to: USER");
                } else if (checkedId == R.id.btnResearchers) {
                    currentFilter = "researcher";
                    android.util.Log.d("AdminUsersFragment", "Filter changed to: RESEARCHER");
                } else if (checkedId == R.id.btnAdmins) {
                    currentFilter = "admin";
                    android.util.Log.d("AdminUsersFragment", "Filter changed to: ADMIN");
                }

                android.util.Log.d("AdminUsersFragment", "Loading users with filter: " + currentFilter);
                loadUsers();
            }
        });
    }

    private void loadUsers() {
        android.util.Log.d("AdminUsersFragment", "loadUsers called with filter: " + currentFilter);

        if (progressBar != null && swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        try {
            if ("all".equals(currentFilter)) {
                // Load all users
                DatabaseReference usersRef = authRepository.getAllUsers();
                android.util.Log.d("AdminUsersFragment", "Loading ALL users from database");

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        android.util.Log.d("AdminUsersFragment", "Data received from Firebase. Count: " + snapshot.getChildrenCount());

                        usersList.clear();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                user.setId(userSnapshot.getKey());
                                usersList.add(user);
                                String username = user.getUsername() != null ? user.getUsername() : user.getEmail();
                                android.util.Log.d("AdminUsersFragment", "Added user: " + username + " (Role: " + user.getRole() + ")");
                            }
                        }

                        android.util.Log.d("AdminUsersFragment", "Total users loaded: " + usersList.size());
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        updateUserCount();
                        hideLoading();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("AdminUsersFragment", "Failed to load users: " + error.getMessage());
                        Toast.makeText(requireContext(),
                            "Failed to load users: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                        hideLoading();
                    }
                });
            } else {
                // Load users by role - getUsersByRole returns Query, not DatabaseReference
                com.google.firebase.database.Query usersQuery = authRepository.getUsersByRole(currentFilter);
                android.util.Log.d("AdminUsersFragment", "Loading users with role: " + currentFilter);

                usersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        android.util.Log.d("AdminUsersFragment", "Data received from Firebase. Count: " + snapshot.getChildrenCount());

                        usersList.clear();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                user.setId(userSnapshot.getKey());
                                usersList.add(user);
                                String username = user.getUsername() != null ? user.getUsername() : user.getEmail();
                                android.util.Log.d("AdminUsersFragment", "Added user: " + username + " (Role: " + user.getRole() + ")");
                            }
                        }

                        android.util.Log.d("AdminUsersFragment", "Total users loaded: " + usersList.size());
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        updateUserCount();
                        hideLoading();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("AdminUsersFragment", "Failed to load users: " + error.getMessage());
                        Toast.makeText(requireContext(),
                            "Failed to load users: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                        hideLoading();
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("AdminUsersFragment", "Exception in loadUsers: " + e.getMessage(), e);
            Toast.makeText(requireContext(),
                "Error loading users: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
            hideLoading();
        }
    }

    private void updateUserCount() {
        if (tvUserCount != null) {
            String filterText = getFilterDisplayName();
            tvUserCount.setText(usersList.size() + " " + filterText + " found");
        }
    }

    private String getFilterDisplayName() {
        switch (currentFilter) {
            case "user":
                return "user" + (usersList.size() != 1 ? "s" : "");
            case "researcher":
                return "researcher" + (usersList.size() != 1 ? "s" : "");
            case "admin":
                return "admin" + (usersList.size() != 1 ? "s" : "");
            default:
                return "user" + (usersList.size() != 1 ? "s" : "");
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void updateEmptyState() {
        if (usersList.isEmpty()) {
            rvUsers.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);

            if (tvEmptyMessage != null) {
                String message;
                switch (currentFilter) {
                    case "user":
                        message = "No regular users found";
                        break;
                    case "researcher":
                        message = "No researchers found";
                        break;
                    case "admin":
                        message = "No admins found";
                        break;
                    default:
                        message = "No users found";
                }
                tvEmptyMessage.setText(message);
            }
        } else {
            rvUsers.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }
}
