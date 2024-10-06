package com.fixcare.MechanicActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.fixcare.Activities.SelectLocationActivity;
import com.fixcare.Objects.Workshop;
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SetupWorkshopActivity extends AppCompatActivity {

    FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    Context context;
    TextInputEditText etWorkshopName, etLocation;
    CheckBox chkBodyRepairs, chkCarWash, chkEngineMaintenance,
            chkEmergencyAssistance, chkElectrical, chkSystemCheck,
            chkFuelDelivery, chkSparePartsReplacement,
            chkWheels, chk247Assistance;
    MaterialButton btnSave;

    // top action bar elements
    MaterialButton btnBack;
    TextView tvActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_workshop);

        initialize();
        initializeTopActionBar();

        etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SetupWorkshopActivity.this, SelectLocationActivity.class));
            }
        });
        btnSave.setOnClickListener(view -> saveWorkshopDetails());
    }

    @Override
    protected void onResume() {
        super.onResume();

        double latitude = Utils.Cache.getDouble(context, "selected_latitude");
        double longitude = Utils.Cache.getDouble(context, "selected_longitude");
        String locality = Utils.Cache.getString(context, "locality");
        String subAdminArea = Utils.Cache.getString(context, "subAdminArea");

        if (latitude == 0 || longitude == 0) {
            Objects.requireNonNull(etLocation.getText()).clear();
            return;
        }

        etLocation.setText(Utils.addressBuilder(locality, subAdminArea));
    }

    private void initialize() {
        context = this;
        etWorkshopName = findViewById(R.id.etWorkshopName);
        etLocation = findViewById(R.id.etLocation);
        chkBodyRepairs = findViewById(R.id.chkBodyRepairs);
        chkCarWash = findViewById(R.id.chkCarWash);
        chkEngineMaintenance = findViewById(R.id.chkEngineMaintenance);
        chkEmergencyAssistance = findViewById(R.id.chkEmergencyAssistance);
        chkElectrical = findViewById(R.id.chkElectrical);
        chkSystemCheck = findViewById(R.id.chkSystemCheck);
        chkFuelDelivery = findViewById(R.id.chkFuelDelivery);
        chkSparePartsReplacement = findViewById(R.id.chkSparePartsReplacement);
        chkWheels = findViewById(R.id.chkWheels);
        chk247Assistance = findViewById(R.id.chk247Assistance);
        btnSave = findViewById(R.id.btnSave);
    }

    private void initializeTopActionBar() {
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        btnBack = findViewById(R.id.btnActionBar);

        tvActivityTitle.setText("Set up Workshop");
        /*btnBack.setOnClickListener(view -> {
            startActivity(new Intent(SetupWorkshopActivity.this, MechanicActivity.class));
            finish();
        });*/
        btnBack.setVisibility(View.GONE);
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

    private void saveWorkshopDetails() {
        String workshopName = Objects.requireNonNull(etWorkshopName.getText()).toString().trim();
        double latitude = Utils.Cache.getDouble(context, "selected_latitude");
        double longitude = Utils.Cache.getDouble(context, "selected_longitude");
        String locality = Utils.Cache.getString(context, "locality");
        String subAdminArea = Utils.Cache.getString(context, "subAdminArea");
        String availableServices = buildSelectedServicesNotation();

        if (latitude == 0 || longitude == 0) {
            Utils.basicDialog(context, "Please specify your workshop's location", "Okay");
            return;
        }

        if (workshopName.isEmpty()) {
            Utils.basicDialog(context, "Please indicate your workshop's name", "Okay");
            return;
        }

        if (availableServices.equals("0000000000")) {
            Utils.basicDialog(context, "At least 1 service must be selected", "Okay");
            return;
        }

        // generate new UID
        String workshopUid = FIXCARE_DB.getReference().push().getKey();

        // update user's workshop uid
        DatabaseReference dbUserWorkshopUid = FIXCARE_DB.getReference("user_"+USER.getUid()+"/workshopUid");
        dbUserWorkshopUid.setValue(workshopUid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // save new workshop node
                DatabaseReference dbNewWorkshop = FIXCARE_DB.getReference("workshops/"+workshopUid);
                Workshop workshop = new Workshop(
                        workshopUid,
                        workshopName,
                        latitude,
                        longitude,
                        locality+", "+subAdminArea,
                        availableServices,
                        USER.getUid()
                );
                dbNewWorkshop.setValue(workshop).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Utils.Cache.removeKey(context, "selected_latitude");
                        Utils.Cache.removeKey(context, "selected_longitude");
                        Utils.Cache.removeKey(context, "locality");
                        Utils.Cache.removeKey(context, "subAdminArea");

                        startActivity(new Intent(SetupWorkshopActivity.this, MechanicActivity.class));
                        finish();
                    }
                });
            }
        });
    }
}