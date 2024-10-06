package com.fixcare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.fixcare.CustomerActivities.CustomerActivity;
import com.fixcare.MechanicActivities.MechanicActivity;
import com.fixcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class StartupActivity extends AppCompatActivity {

    private static final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private static final FirebaseUser USER = AUTH.getCurrentUser();

    @Override
    protected void onStart() {
        super.onStart();

        if (USER != null) {
            FirebaseDatabase.getInstance().getReference("user_"+USER.getUid()).child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString()) == 0) {
                        startActivity(new Intent(StartupActivity.this, CustomerActivity.class));
                        finish();
                    }
                    else {
                        startActivity(new Intent(StartupActivity.this, MechanicActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            startActivity(new Intent(this, AuthenticationActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }
}