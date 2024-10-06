package com.fixcare.CustomerFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fixcare.Activities.AuthenticationActivity;
import com.fixcare.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class CustomerMenuFragment extends Fragment {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    View view;
    MaterialButton btnLogout;

    // profile
    TextView tvEmail;
    TextInputEditText etFirstName, etLastName, etMobile;
    MaterialButton btnSaveProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_customer_menu, container, false);

        initialize();
        loadUserInformation();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(requireContext(), AuthenticationActivity.class));
                requireActivity().finish();
            }
        });
        return view;
    }

    private void initialize() {
        btnLogout = view.findViewById(R.id.btnLogout);
        // profile
        tvEmail = view.findViewById(R.id.tvEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMobile = view.findViewById(R.id.etMobile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
    }

    private void loadUserInformation() {
        DatabaseReference dbUser = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(USER).getUid());
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = USER.getEmail();
                String firstName = Objects.requireNonNull(snapshot.child("firstName").getValue()).toString();
                String lastName = Objects.requireNonNull(snapshot.child("lastName").getValue()).toString();
                String mobile = Objects.requireNonNull(snapshot.child("mobile").getValue()).toString();

                tvEmail.setText(email);
                etFirstName.setText(firstName);
                etLastName.setText(lastName);
                etMobile.setText(mobile);

                handleProfileChanges(firstName, lastName, mobile);
            }

            private void handleProfileChanges(String firstName, String lastName, String mobile) {
                etFirstName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (!etFirstName.getText().toString().trim().equals(firstName) ||
                                !etLastName.getText().toString().trim().equals(lastName) ||
                                !etMobile.getText().toString().trim().equals(mobile)) {
                            btnSaveProfile.setVisibility(View.VISIBLE);
                        }
                        else {
                            btnSaveProfile.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });

                etLastName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (!etFirstName.getText().toString().trim().equals(firstName) ||
                                !etLastName.getText().toString().trim().equals(lastName) ||
                                !etMobile.getText().toString().trim().equals(mobile)) {
                            btnSaveProfile.setVisibility(View.VISIBLE);
                        }
                        else {
                            btnSaveProfile.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });

                etMobile.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (!etFirstName.getText().toString().trim().equals(firstName) ||
                                !etLastName.getText().toString().trim().equals(lastName) ||
                                !etMobile.getText().toString().trim().equals(mobile)) {
                            btnSaveProfile.setVisibility(View.VISIBLE);
                        }
                        else {
                            btnSaveProfile.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                btnSaveProfile.setOnClickListener(view -> {
                    DatabaseReference dbUser1 = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(USER).getUid());
                    dbUser1.child("firstName").setValue(Objects.requireNonNull(etFirstName.getText()).toString().trim());
                    dbUser1.child("lastName").setValue(Objects.requireNonNull(etLastName.getText()).toString().trim());
                    dbUser1.child("mobile").setValue(Objects.requireNonNull(etMobile.getText()).toString().trim());
                    Toast.makeText(requireContext(), "Updated user information.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}