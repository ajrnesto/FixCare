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
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class BookingDialog extends AppCompatDialogFragment {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    TextView tvName, tvTotal;
    TextInputLayout tilVehicle;
    TextInputEditText etModel, etPlateNumber, etSchedule, etLocation;
    MaterialButton btnCancel, btnBookNow;
    ConstraintLayout clMore;

    // checkboxes
    CheckBox chkBodyRepairs, chkCarWash, chkEngineMaintenance, chkEmergencyAssistance, chkElectrical, chkSystemCheck, chkFuelDelivery, chkSparePartsReplacement, chkWheels, chk247Assistance;
    /*MutableLiveData<String> servicesLiveData = new MutableLiveData<>();*/
    double total = 0;

    // vehicle class spinner
    String[] vehicles;
    ArrayAdapter<String> adapterVehicle;
    AutoCompleteTextView menuVehicle;

    // builder date picker
    MaterialDatePicker.Builder<Long> builderDatePicker;
    MaterialDatePicker<Long> datePicker;

    // arguments
    String workshopUid, workshopName, workshopAddress, workshopServices, ownerUid;
    double workshopLatitude, workshopLongitude;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_booking, null);

        initialize(view);
        loadWorkshopInfoAndServices();
        initializeSpinner(view);
        initializeDatePicker();

        etLocation.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), SelectLocationActivity.class)));
        etSchedule.setOnClickListener(view1 -> {
            etSchedule.setEnabled(false);
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        btnBookNow.setOnClickListener(view1 -> {
            submitForm(view);
        });

        btnCancel.setOnClickListener(view1 -> Objects.requireNonNull(getDialog()).dismiss());

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        double latitude = Utils.Cache.getDouble(requireContext(), "selected_latitude");
        double longitude = Utils.Cache.getDouble(requireContext(), "selected_longitude");
        String locality = Utils.Cache.getString(requireContext(), "locality");
        String subAdminArea = Utils.Cache.getString(requireContext(), "subAdminArea");

        if (latitude == 0 || longitude == 0) {
            Objects.requireNonNull(etLocation.getText()).clear();
            return;
        }

        etLocation.setText(Utils.addressBuilder(locality, subAdminArea));
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
        etLocation = view.findViewById(R.id.etLocation);
        etSchedule = view.findViewById(R.id.etSchedule);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvTotal.setText(Utils.DoubleFormatter.currencyFormat(total));
        btnBookNow = view.findViewById(R.id.btnBookNow);
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
        Bundle workshopArgs = getArguments();
        workshopUid = Objects.requireNonNull(workshopArgs).getString("uid");
        workshopName = workshopArgs.getString("name");
        workshopLatitude = workshopArgs.getDouble("latitude");
        workshopLongitude = workshopArgs.getDouble("longitude");
        workshopAddress = workshopArgs.getString("address");
        workshopServices = workshopArgs.getString("available_services");
        ownerUid = workshopArgs.getString("owner_uid");
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

    private void initializeDatePicker() {
        builderDatePicker = MaterialDatePicker.Builder.datePicker();
        builderDatePicker.setTitleText("Birthdate")
                .setSelection(System.currentTimeMillis());
        //datePicker = builderDatePicker.setTheme(R.style.ThemeOverlay_App_MaterialCalendar).build();
        datePicker = builderDatePicker.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            etSchedule.setText(sdf.format(datePicker.getSelection()));
            etSchedule.setEnabled(true);
        });
        datePicker.addOnNegativeButtonClickListener(view -> {
            etSchedule.setEnabled(true);
        });
        datePicker.addOnCancelListener(dialogInterface -> {
            etSchedule.setEnabled(true);
        });

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
    }

    private void loadWorkshopInfoAndServices() {
        tvName.setText(workshopName);

        if (workshopServices.charAt(0) == '1') {
            chkBodyRepairs.setVisibility(View.VISIBLE);
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
        }
        else {
            chkBodyRepairs.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(1) == '1') {
            chkCarWash.setVisibility(View.VISIBLE);
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
        }
        else {
            chkCarWash.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(2) == '1') {
            chkEngineMaintenance.setVisibility(View.VISIBLE);
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
        }
        else {
            chkEngineMaintenance.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(3) == '1') {
            chkEmergencyAssistance.setVisibility(View.VISIBLE);
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
        }
        else {
            chkEmergencyAssistance.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(4) == '1') {
            chkElectrical.setVisibility(View.VISIBLE);
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
        }
        else {
            chkElectrical.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(5) == '1') {
            chkSystemCheck.setVisibility(View.VISIBLE);
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
        }
        else {
            chkSystemCheck.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(6) == '1') {
            chkFuelDelivery.setVisibility(View.VISIBLE);
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
        }
        else {
            chkFuelDelivery.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(7) == '1') {
            chkSparePartsReplacement.setVisibility(View.VISIBLE);
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
        }
        else {
            chkSparePartsReplacement.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(8) == '1') {
            chkWheels.setVisibility(View.VISIBLE);
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
        }
        else {
            chkWheels.setVisibility(View.GONE);
        }

        if (workshopServices.charAt(9) == '1') {
            chk247Assistance.setVisibility(View.VISIBLE);
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
        else {
            chk247Assistance.setVisibility(View.GONE);
        }
    }

    private void submitForm(View view) {
        // retrieve form data
        String vehicleClass = menuVehicle.getText().toString();
        String model = Objects.requireNonNull(etModel.getText()).toString().trim();
        String plateNumber = Objects.requireNonNull(etPlateNumber.getText()).toString().trim();
        double latitude = Utils.Cache.getDouble(requireContext(), "selected_latitude");
        double longitude = Utils.Cache.getDouble(requireContext(), "selected_longitude");
        String locality = Utils.Cache.getString(requireContext(), "locality");
        String subAdminArea = Utils.Cache.getString(requireContext(), "subAdminArea");
        long schedule = datePicker.getSelection();
        String selectedServices = buildSelectedServicesNotation();

        // validate form
        if (vehicleClass.isEmpty() ||
                model.isEmpty() ||
                plateNumber.isEmpty() ||
                locality.isEmpty() ) {
            Utils.basicDialog(requireContext(), "Please fill in all fields", "Okay");
            return;
        }
        if (selectedServices.equals("0000000000")) {
            Utils.basicDialog(requireContext(), "At least 1 service must be selected", "Okay");
            return;
        }

        // submit booking
        DatabaseReference dbWorkshopBooking = FIXCARE_DB.getReference("workshop_"+workshopUid+"_bookings");
        String bookingUid = dbWorkshopBooking.push().getKey();
        Booking newBooking = new Booking(bookingUid, vehicleClass, model, plateNumber, latitude,
                longitude, schedule, selectedServices,
                Objects.requireNonNull(USER).getUid(),
                workshopUid, 0);
        dbWorkshopBooking.child(Objects.requireNonNull(bookingUid)).setValue(newBooking);
        // save reference for user
        DatabaseReference dbUserBooking = FIXCARE_DB.getReference("user_"+Objects.requireNonNull(USER).getUid()+"_bookings").child(bookingUid);
        dbUserBooking.child("uid").setValue(bookingUid);
        dbUserBooking.child("workshopUid").setValue(workshopUid);

        Objects.requireNonNull(getDialog()).dismiss();

        Fragment bookingsFragment = new BookingsFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, bookingsFragment, "BOOKINGS_FRAGMENT");
        fragmentTransaction.addToBackStack("BOOKINGS_FRAGMENT");
        fragmentTransaction.commit();
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
