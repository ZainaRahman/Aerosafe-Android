package com.example.aerotutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aerotutorial.R;
import com.example.aerotutorial.adapters.UsersAdapter;
import com.example.aerotutorial.models.User;
import com.example.aerotutorial.repository.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private RecyclerView rvUsers;
    private LinearLayout llEmptyState;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton btnUsers, btnResearchers, btnAdmins;

    private UsersAdapter adapter;
    private List<User> usersList;
    private AuthRepository authRepository;
    private String currentFilter = "user";

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
        btnUsers = view.findViewById(R.id.btnUsers);
        btnResearchers = view.findViewById(R.id.btnResearchers);
        btnAdmins = view.findViewById(R.id.btnAdmins);
    }

    private void setupRecyclerView() {
        usersList = new ArrayList<>();
        adapter = new UsersAdapter(usersList);

        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnUsers) {
                    currentFilter = "user";
                } else if (checkedId == R.id.btnResearchers) {
                    currentFilter = "researcher";
                } else if (checkedId == R.id.btnAdmins) {
                    currentFilter = "admin";
                }
                loadUsers();
            }
        });
    }

    private void loadUsers() {
        authRepository.getUsersByRole(currentFilter).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(userSnapshot.getKey());
                        usersList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                    "Failed to load users: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (usersList.isEmpty()) {
            rvUsers.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
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
