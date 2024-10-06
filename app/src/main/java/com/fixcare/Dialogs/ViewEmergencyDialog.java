package com.fixcare.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fixcare.Fragments.ChatFragment;
import com.fixcare.Objects.Emergency;
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ViewEmergencyDialog extends AppCompatDialogFragment {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    TextView tvCustomerName, tvMobile, tvVehicleModel, tvVehiclePlateNumber, tvServices;
    AppCompatImageView imgCar, imgMotorcycle;
    MaterialButton btnCall, btnChat;

    // arguments
    String customerUid;
    String vehicleClass;
    String model;
    String plateNumber;
    String selectedServices;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_view_emergency, null);

        initialize(view);
        loadEmergencyDetails(view);
        handleButtons();

        builder.setView(view);
        return builder.create();
    }

    private void initialize(View view) {
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvMobile = view.findViewById(R.id.tvMobile);
        tvVehicleModel = view.findViewById(R.id.tvVehicleModel);
        tvVehiclePlateNumber = view.findViewById(R.id.tvVehiclePlateNumber);
        tvServices = view.findViewById(R.id.tvServices);
        imgCar = view.findViewById(R.id.imgCar);
        imgMotorcycle = view.findViewById(R.id.imgMotorcycle);
        btnCall = view.findViewById(R.id.btnCall);
        btnChat = view.findViewById(R.id.btnChat);

        // retrieve argument values
        Bundle args = getArguments();
        customerUid = Objects.requireNonNull(args).getString("customer_uid");
        vehicleClass = Objects.requireNonNull(args).getString("vehicle_class");
        model = Objects.requireNonNull(args).getString("model");
        plateNumber = Objects.requireNonNull(args).getString("plateNumber");
        selectedServices = Objects.requireNonNull(args).getString("selected_services");
    }

    private void loadEmergencyDetails(View view) {
        // customer info
        DatabaseReference dbCustomer = FIXCARE_DB.getReference("user_"+customerUid);
        dbCustomer.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = Objects.requireNonNull(snapshot.child("firstName").getValue()).toString();
                String lastName = Objects.requireNonNull(snapshot.child("lastName").getValue()).toString();
                String mobile = Objects.requireNonNull(snapshot.child("mobile").getValue()).toString();

                tvCustomerName.setText(firstName+" "+lastName);
                tvMobile.setText(mobile);

                // handle chat button
                btnChat.setOnClickListener(view -> {
                    Bundle chatArgs = new Bundle();
                    chatArgs.putBoolean("user_is_mechanic", true);
                    chatArgs.putString("contact_uid", customerUid);
                    chatArgs.putString("contact_name", firstName+" "+lastName);

                    ChatFragment chatFragment = new ChatFragment();
                    chatFragment.setArguments(chatArgs);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, chatFragment, "CHAT_FRAGMENT")
                            .addToBackStack("CHAT_FRAGMENT")
                            .commit();
                    Objects.requireNonNull(getDialog()).dismiss();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // vehicle info
        if (vehicleClass.equalsIgnoreCase("car")) {
            imgCar.setVisibility(View.VISIBLE);
            imgMotorcycle.setVisibility(View.GONE);
        }
        else {
            imgMotorcycle.setVisibility(View.VISIBLE);
            imgCar.setVisibility(View.GONE);
        }
        tvVehicleModel.setText(model);
        tvVehiclePlateNumber.setText(plateNumber);

        // services info
        tvServices.setText(loadSelectedServices(selectedServices));
    }

    private void handleButtons() {
        // btnChat is handled in loading customer info

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

    private String loadSelectedServices(String selectedServices) {
        StringBuilder servicesBuilder = new StringBuilder();

        if (selectedServices.charAt(0) == '1') { servicesBuilder.append("Body Repairs, "); }
        if (selectedServices.charAt(1) == '1') { servicesBuilder.append("Car Wash & Detailing, "); }
        if (selectedServices.charAt(2) == '1') { servicesBuilder.append("Engine Maintenance, "); }
        if (selectedServices.charAt(3) == '1') { servicesBuilder.append("Emergency Assistance, "); }
        if (selectedServices.charAt(4) == '1') { servicesBuilder.append("Electrical & Battery, "); }
        if (selectedServices.charAt(5) == '1') { servicesBuilder.append("Full System Check, "); }
        if (selectedServices.charAt(6) == '1') { servicesBuilder.append("Gas & Fuel Delivery, "); }
        if (selectedServices.charAt(7) == '1') { servicesBuilder.append("Spare Parts Replacement, "); }
        if (selectedServices.charAt(8) == '1') { servicesBuilder.append("Wheels & Tires, "); }
        if (selectedServices.charAt(9) == '1') { servicesBuilder.append("24/7 Assistance, "); }

        servicesBuilder.delete(servicesBuilder.length() - 2, servicesBuilder.length());
        return servicesBuilder.toString();
    }
}
