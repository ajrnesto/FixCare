package com.fixcare.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.fixcare.Fragments.BookingsFragment;
import com.fixcare.Fragments.ChatFragment;
import com.fixcare.Objects.Workshop;
import com.fixcare.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class WorkshopDialog extends AppCompatDialogFragment {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    TextView tvName, tvAddress, tvOwner, tvMobile;
    MaterialButton btnChat, btnCall, btnBooking;

    // arguments
    String workshopUid, workshopName, workshopAddress, workshopServices, ownerUid;
    double workshopLatitude, workshopLongitude;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_workshop, null);

        initiate(view);
        loadWorkshopInfo();
        handleButtons();

        builder.setView(view);
        return builder.create();
    }

    private void initiate(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvOwner = view.findViewById(R.id.tvOwner);
        tvMobile = view.findViewById(R.id.tvMobile);
        btnCall = view.findViewById(R.id.btnCall);
        btnChat = view.findViewById(R.id.btnChat);
        btnBooking = view.findViewById(R.id.btnBooking);

        // retrieve argument values
        Bundle workshopArgs = getArguments();
        workshopUid = Objects.requireNonNull(workshopArgs).getString("uid");
        workshopName = workshopArgs.getString("name");
        workshopLatitude = workshopArgs.getDouble("latitude");
        workshopLongitude = workshopArgs.getDouble("longitude");
        workshopAddress = workshopArgs.getString("address");
        workshopServices = workshopArgs.getString("available_services");
        ownerUid = workshopArgs.getString("owner_uid");
    }

    private void loadWorkshopInfo() {
        tvName.setText(workshopName);
        tvAddress.setText(workshopAddress);

        DatabaseReference dbOwner = FIXCARE_DB.getReference("user_"+ownerUid);
        dbOwner.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = Objects.requireNonNull(snapshot.child("firstName").getValue()).toString();
                String lastName = Objects.requireNonNull(snapshot.child("lastName").getValue()).toString();
                String mobile = Objects.requireNonNull(snapshot.child("mobile").getValue()).toString();

                tvOwner.setText(firstName + " " + lastName);
                tvMobile.setText(mobile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleButtons() {
        btnChat.setOnClickListener(view -> {
            Bundle chatArgs = new Bundle();
            chatArgs.putBoolean("user_is_mechanic", false);
            chatArgs.putString("contact_uid", workshopUid);
            chatArgs.putString("contact_name", workshopName);

            ChatFragment chatFragment = new ChatFragment();
            chatFragment.setArguments(chatArgs);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, chatFragment, "CHAT_FRAGMENT")
                    .addToBackStack("CHAT_FRAGMENT")
                    .commit();
            Objects.requireNonNull(getDialog()).dismiss();
        });

        btnBooking.setOnClickListener(view -> {
            Bundle workshopArgs = new Bundle();
            workshopArgs.putString("uid", workshopUid);
            workshopArgs.putString("name", workshopName);
            workshopArgs.putDouble("latitude", workshopLatitude);
            workshopArgs.putDouble("longitude", workshopLongitude);
            workshopArgs.putString("address", workshopAddress);
            workshopArgs.putString("available_services", workshopServices);
            workshopArgs.putString("owner_uid", ownerUid);

            BookingDialog bookingDialog = new BookingDialog();
            bookingDialog.setArguments(workshopArgs);
            bookingDialog.show(requireActivity().getSupportFragmentManager(), "BOOKING_DIALOG");

            Objects.requireNonNull(getDialog()).dismiss();
        });

        btnCall.setOnClickListener(view -> {
            if (tvMobile.getText().toString().isEmpty()) {
                return;
            }

            String encodedPhoneNumber = String.format("tel:%s", Uri.encode(tvMobile.getText().toString()));
            Uri number = Uri.parse(encodedPhoneNumber);
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(callIntent);
        });
    }
}
