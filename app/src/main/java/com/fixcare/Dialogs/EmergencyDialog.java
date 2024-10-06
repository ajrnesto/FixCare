package com.fixcare.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.fixcare.Activities.SelectLocationActivity;
import com.fixcare.Fragments.BookingsFragment;
import com.fixcare.Fragments.LocationSelectionMapFragment;
import com.fixcare.MechanicActivities.SetupWorkshopActivity;
import com.fixcare.Objects.Booking;
import com.fixcare.Objects.Emergency;
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class EmergencyDialog extends AppCompatDialogFragment {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    TextView tvName, tvTotal;
    TextInputLayout tilVehicle;
    TextInputEditText etModel, etPlateNumber;
    MaterialButton btnCancel, btnConfirm;
    ConstraintLayout clMore;

    // checkboxes
    CheckBox chkBodyRepairs, chkCarWash, chkEngineMaintenance, chkEmergencyAssistance, chkElectrical, chkSystemCheck, chkFuelDelivery, chkSparePartsReplacement, chkWheels, chk247Assistance;
    double total = 0;

    // vehicle class spinner
    String[] vehicles;
    ArrayAdapter<String> adapterVehicle;
    AutoCompleteTextView menuVehicle;

    // builder date picker
    MaterialDatePicker.Builder<Long> builderDatePicker;
    MaterialDatePicker<Long> datePicker;

    // arguments
    double currentLatitude, currentLongitude;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_emergency, null);

        initialize(view);
        initializeSpinner(view);
        handleServicesCheckboxes();

        btnConfirm.setOnClickListener(view1 -> {
            submitForm(view);
        });

        btnCancel.setOnClickListener(view1 -> Objects.requireNonNull(getDialog()).dismiss());

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();

        Utils.Cache.removeKey(requireContext(), "selected_latitude");
        Utils.Cache.removeKey(requireContext(), "selected_longitude");
        Utils.Cache.removeKey(requireContext(), "locality");
        Utils.Cache.removeKey(requireContext(), "subAdminArea");
    }

    private void initialize(View view) {
        clMore = view.findViewById(R.id.clMore);
        tvName = view.findViewById(R.id.tvName);
        etModel = view.findViewById(R.id.etModel);
        etPlateNumber = view.findViewById(R.id.etPlateNumber);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        btnConfirm = view.findViewById(R.id.btnConfirm);
        // checkboxes
        chkBodyRepairs = view.findViewById(R.id.chkBodyRepairs);
        chkCarWash = view.findViewById(R.id.chkCarWash);
        chkEngineMaintenance = view.findViewById(R.id.chkEngineMaintenance);
        chkEmergencyAssistance = view.findViewById(R.id.chkEmergencyAssistance);
        chkElectrical = view.findViewById(R.id.chkElectrical);
        chkSystemCheck = view.findViewById(R.id.chkSystemCheck);
        chkFuelDelivery = view.findViewById(R.id.chkFuelDelivery);
        chkSparePartsReplacement = view.findViewById(R.id.chkSparePartsReplacement);
        chkWheels = view.findViewById(R.id.chkWheels);
        chk247Assistance = view.findViewById(R.id.chk247Assistance);

        // retrieve argument values
        Bundle args = getArguments();
        currentLatitude = Objects.requireNonNull(args).getDouble("latitude");
        currentLongitude = Objects.requireNonNull(args).getDouble("longitude");
    }

    private void initializeSpinner(View view) {
        vehicles = new String[] {"Car", "Motorcycle"};
        adapterVehicle = new ArrayAdapter<>(getContext(), R.layout.list_item, vehicles);
        tilVehicle = view.findViewById(R.id.tilVehicle);
        menuVehicle = view.findViewById(R.id.menuVehicle);
        menuVehicle.setAdapter(adapterVehicle);
        menuVehicle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tilVehicle.setVisibility(View.GONE);
                clMore.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleServicesCheckboxes() {
        chkBodyRepairs.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 10000;
                }
                else {
                    total += 2000;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 10000;
                }
                else {
                    total -= 2000;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkCarWash.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 200;
                }
                else {
                    total += 50;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 200;
                }
                else {
                    total -= 50;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkEngineMaintenance.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 1000;
                }
                else {
                    total += 600;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 1000;
                }
                else {
                    total -= 600;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkEmergencyAssistance.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 2000;
                }
                else {
                    total += 500;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 2000;
                }
                else {
                    total -= 500;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkElectrical.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 5000;
                }
                else {
                    total += 1000;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 5000;
                }
                else {
                    total -= 1000;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkSystemCheck.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 1000;
                }
                else {
                    total += 800;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 1000;
                }
                else {
                    total -= 800;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkFuelDelivery.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 3000;
                }
                else {
                    total += 800;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 3000;
                }
                else {
                    total -= 800;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkSparePartsReplacement.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 5000;
                }
                else {
                    total += 1000;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 5000;
                }
                else {
                    total -= 1000;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chkWheels.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 3000;
                }
                else {
                    total += 3000;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 3000;
                }
                else {
                    total -= 3000;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });

        chk247Assistance.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total += 10000;
                }
                else {
                    total += 2000;
                }
            }
            else {
                if (menuVehicle.getText().toString().equals("Car")) {
                    total -= 10000;
                }
                else {
                    total -= 2000;
                }
            }

            tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        });
    }

    private void submitForm(View view) {
        // retrieve form data
        String customerUid = Objects.requireNonNull(USER).getUid();
        String vehicleClass = menuVehicle.getText().toString();
        String model = Objects.requireNonNull(etModel.getText()).toString().trim();
        String plateNumber = Objects.requireNonNull(etPlateNumber.getText()).toString().trim();
        double latitude = currentLatitude;
        double longitude = currentLongitude;
        String selectedServices = buildSelectedServicesNotation();

        // validate form
        if (vehicleClass.isEmpty() ||
                model.isEmpty() ||
                plateNumber.isEmpty()) {
            Utils.basicDialog(requireContext(), "Please fill in all fields", "Okay");
            return;
        }
        if (selectedServices.equals("0000000000")) {
            Utils.basicDialog(requireContext(), "At least 1 service must be selected", "Okay");
            return;
        }

        // submit booking
        DatabaseReference dbEmergencies = FIXCARE_DB.getReference("emergencies").child(customerUid);
        Emergency newEmergency = new Emergency(customerUid, vehicleClass, model, plateNumber, latitude, longitude, selectedServices);
        dbEmergencies.setValue(newEmergency);

        Objects.requireNonNull(getDialog()).dismiss();
    }

    private String buildSelectedServicesNotation() {
        StringBuilder servicesNotationBuilder = new StringBuilder();

        if (chkBodyRepairs.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkCarWash.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkEngineMaintenance.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkEmergencyAssistance.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkElectrical.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkSystemCheck.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkFuelDelivery.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkSparePartsReplacement.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chkWheels.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        if (chk247Assistance.isChecked()) {
            servicesNotationBuilder.append("1");
        }
        else {
            servicesNotationBuilder.append("0");
        }

        return servicesNotationBuilder.toString();
    }
}
