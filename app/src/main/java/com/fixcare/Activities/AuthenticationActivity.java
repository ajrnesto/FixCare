package com.fixcare.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fixcare.CustomerActivities.CustomerActivity;
import com.fixcare.MechanicActivities.MechanicActivity;
import com.fixcare.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.fixcare.Utils.Utils;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

public class AuthenticationActivity extends AppCompatActivity {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private DatabaseReference refUser;

    MaterialCardView cvLogin, cvSignup;
    RoundedImageView imgFixcareLogo;
    RadioGroup radioGroup;
    MaterialRadioButton radioCustomer, radioWorkshop;
    TextInputEditText etLoginEmail, etLoginPassword;
    TextInputEditText etSignupFirstName, etSignupLastName, etSignupEmail, etSignupMobile, etSignupPassword;
    MaterialButton btnLogin, btnShowSignup, btnSignup, btnShowLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.gray, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.gray));
        }
        setContentView(R.layout.activity_authentication);

        initialize();
        softKeyboardListener();

        btnSignup.setOnClickListener(view -> {
            registerUser();
        });

        btnShowLogin.setOnClickListener(view -> {
            cvLogin.setVisibility(View.VISIBLE);
            cvSignup.setVisibility(View.GONE);
        });

        btnShowSignup.setOnClickListener(view -> {
            cvLogin.setVisibility(View.GONE);
            cvSignup.setVisibility(View.VISIBLE);
        });

        btnLogin.setOnClickListener(view -> {
            loginUser();
        });

        btnShowSignup.setOnClickListener(view -> {
            cvLogin.setVisibility(View.GONE);
            cvSignup.setVisibility(View.VISIBLE);
        });
    }

    private void softKeyboardListener() {
        final View activityRootView = findViewById(R.id.activityRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > Utils.dpToPx(AuthenticationActivity.this, 200)) {
                    // if keyboard visible
                    imgFixcareLogo.setVisibility(View.GONE);
                }
                else {
                    imgFixcareLogo.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }

    private void initialize() {
        imgFixcareLogo = findViewById(R.id.imgFixcareLogo);
        // login
        cvLogin = findViewById(R.id.cvLogin);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnShowSignup = findViewById(R.id.btnShowSignup);
        // sign up
        cvSignup = findViewById(R.id.cvSignup);
        etSignupFirstName = findViewById(R.id.etSignupFirstName);
        etSignupLastName = findViewById(R.id.etSignupLastName);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupMobile = findViewById(R.id.etMobile);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        radioGroup = findViewById(R.id.radioGroup);
        radioCustomer = findViewById(R.id.radioCustomer);
        radioWorkshop = findViewById(R.id.radioWorkshop);
        btnSignup = findViewById(R.id.btnSignup);
        btnShowLogin = findViewById(R.id.btnShowLogin);
    }

    private void registerUser() {
        btnSignup.setEnabled(false);
        String firstName = Objects.requireNonNull(etSignupFirstName.getText()).toString();
        String lastName = Objects.requireNonNull(etSignupLastName.getText()).toString();
        String mobile = Objects.requireNonNull(etSignupMobile.getText()).toString();
        String email = Objects.requireNonNull(etSignupEmail.getText()).toString();
        String password = Objects.requireNonNull(etSignupPassword.getText()).toString();
        int checkedUserTypeId = radioGroup.getCheckedRadioButtonId();
        int userType;

        if (TextUtils.isEmpty(firstName) ||
                TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(mobile) ||
                TextUtils.isEmpty(password)) {
            Utils.basicDialog(this, "All fields are required!", "Okay");
            btnSignup.setEnabled(true);
            return;
        }

        if (password.length() < 6) {
            Utils.basicDialog(this, "The password should be at least 6 characters", "Okay");
            btnSignup.setEnabled(true);
            return;
        }

        if (checkedUserTypeId == radioCustomer.getId()) {
            userType = 0;
        }
        else if (checkedUserTypeId == radioWorkshop.getId()) {
            userType = 1;
        }
        else {
            Utils.basicDialog(this, "Please select a user type", "Okay");
            btnSignup.setEnabled(true);
            return;
        }

        int finalUserType = userType;
        AUTH.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        refUser = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(AUTH.getCurrentUser()).getUid());
                        refUser.child("uid").setValue(AUTH.getCurrentUser().getUid());
                        refUser.child("firstName").setValue(firstName);
                        refUser.child("lastName").setValue(lastName);
                        refUser.child("mobile").setValue(mobile);
                        refUser.child("userType").setValue(finalUserType);
                        Toast.makeText(AuthenticationActivity.this, "Creating account...", Toast.LENGTH_SHORT).show();

                        if (finalUserType == 0) {
                            startActivity(new Intent(AuthenticationActivity.this, CustomerActivity.class));
                            finish();
                        }
                        else {
                            startActivity(new Intent(AuthenticationActivity.this, MechanicActivity.class));
                            finish();
                        }
                    }
                    else {
                        Utils.basicDialog(this, "Something went wrong! Please try again.", "Try again");
                        btnSignup.setEnabled(true);
                    }
                });
    }

    private void loginUser() {
        btnLogin.setEnabled(false);
        String email = Objects.requireNonNull(etLoginEmail.getText()).toString();
        String password = Objects.requireNonNull(etLoginPassword.getText()).toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Utils.basicDialog(this, "All fields are required!", "Okay");
            btnLogin.setEnabled(true);
            return;
        }

        AUTH.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        refUser = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(AUTH.getCurrentUser()).getUid()).child("userType");
                        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Toast.makeText(AuthenticationActivity.this, "Signed in as "+email, Toast.LENGTH_SHORT).show();
                                if (Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString()) == 0) {
                                    startActivity(new Intent(AuthenticationActivity.this, CustomerActivity.class));
                                    finish();
                                }
                                else {
                                    startActivity(new Intent(AuthenticationActivity.this, MechanicActivity.class));
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    else {
                        Utils.basicDialog(this, "Email or password is incorrect", "Try again");
                        btnLogin.setEnabled(true);
                    }
                });
    }
}