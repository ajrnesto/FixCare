package com.fixcare.MechanicFragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.fixcare.Activities.AuthenticationActivity;
import com.fixcare.Activities.SelectLocationActivity;
import com.fixcare.MechanicActivities.MechanicActivity;
import com.fixcare.MechanicActivities.SetupWorkshopActivity;
import com.fixcare.Objects.Workshop;
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MechanicMenuFragment extends Fragment {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    View view;
    MaterialButton btnLogout;

    // profile
    TextView tvEmail;
    TextInputEditText etFirstName, etLastName, etMobile;
    MaterialButton btnSaveProfile;

    // workshop details
    MaterialButton btnWorkshopDetails, btnSaveWorkshopDetails;
    ConstraintLayout clWorkshopDetails;
    TextInputEditText etWorkshopName, etLocation;
    CheckBox chkBodyRepairs, chkCarWash, chkEngineMaintenance, chkEmergencyAssistance, chkElectrical, chkSystemCheck, chkFuelDelivery, chkSparePartsReplacement, chkWheels, chk247Assistance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mechanic_menu, container, false);

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

    @Override
    public void onResume() {
        super.onResume();
        double latitude = Utils.Cache.getDouble(requireContext(), "selected_latitude");
        double longitude = Utils.Cache.getDouble(requireContext(), "selected_longitude");
        String locality = Utils.Cache.getString(requireContext(), "locality");
        String subAdminArea = Utils.Cache.getString(requireContext(), "subAdminArea");

        if (latitude == 0 || longitude == 0) {
            return;
        }

        Objects.requireNonNull(etLocation.getText()).clear();
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

    private void initialize() {
        btnLogout = view.findViewById(R.id.btnLogout);
        // profile
        tvEmail = view.findViewById(R.id.tvEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etMobile = view.findViewById(R.id.etMobile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        // workshop details
        btnWorkshopDetails = view.findViewById(R.id.btnWorkshopDetails);
        clWorkshopDetails = view.findViewById(R.id.clWorkshopDetails);
        etWorkshopName = view.findViewById(R.id.etWorkshopName);
        etLocation = view.findViewById(R.id.etLocation);
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
        btnSaveWorkshopDetails = view.findViewById(R.id.btnSaveWorkshopDetails);
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
                String workshopUid = Objects.requireNonNull(snapshot.child("workshopUid").getValue()).toString();

                tvEmail.setText(email);
                etFirstName.setText(firstName);
                etLastName.setText(lastName);
                etMobile.setText(mobile);

                handleProfileChanges(firstName, lastName, mobile);
                loadWorkshopInformation(workshopUid);
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

                btnSaveProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference dbUser = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(USER).getUid());
                        dbUser.child("firstName").setValue(Objects.requireNonNull(etFirstName.getText()).toString().trim());
                        dbUser.child("lastName").setValue(Objects.requireNonNull(etLastName.getText()).toString().trim());
                        dbUser.child("mobile").setValue(Objects.requireNonNull(etMobile.getText()).toString().trim());
                        Toast.makeText(requireContext(), "Updated user information.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadWorkshopInformation(String workshopUid) {
        btnWorkshopDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clWorkshopDetails.getVisibility() == View.VISIBLE) {
                    clWorkshopDetails.setVisibility(View.GONE);
                }
                else {
                    clWorkshopDetails.setVisibility(View.VISIBLE);
                }
            }
        });

        DatabaseReference dbWorkshop = FIXCARE_DB.getReference("workshops/"+workshopUid);
        dbWorkshop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Workshop workshop = snapshot.getValue(Workshop.class);

                etWorkshopName.setText(Objects.requireNonNull(workshop).getName());
                loadLocation(workshop);
                loadServices(workshop);
                // handleWorkshopChanges(workshop);

                btnSaveWorkshopDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // check if there location was changed
                        if (Utils.Cache.getString(requireContext(), "locality").isEmpty()) {
                            // if location was not changed
                            Workshop updatedWorkshop = new Workshop(
                                    workshopUid,
                                    Objects.requireNonNull(etWorkshopName.getText()).toString().trim(),
                                    workshop.getLatitude(),
                                    workshop.getLongitude(),
                                    workshop.getAddress(),
                                    getAvailableServices(),
                                    Objects.requireNonNull(USER).getUid()
                            );
                            dbWorkshop.setValue(updatedWorkshop);
                        }
                        else {
                            // else if location was changed
                            String locality = Utils.Cache.getString(requireContext(), "locality");
                            String subAdminArea = Utils.Cache.getString(requireContext(), "subAdminArea");
                            Workshop updatedWorkshop = new Workshop(
                                    workshopUid,
                                    Objects.requireNonNull(etWorkshopName.getText()).toString().trim(),
                                    Utils.Cache.getDouble(requireContext(), "selected_latitude"),
                                    Utils.Cache.getDouble(requireContext(), "selected_longitude"),
                                    locality+", "+subAdminArea,
                                    getAvailableServices(),
                                    Objects.requireNonNull(USER).getUid()
                            );
                            dbWorkshop.setValue(updatedWorkshop);
                        }

                        Toast.makeText(requireContext(), "Updated workshop details", Toast.LENGTH_SHORT).show();
                        clWorkshopDetails.setVisibility(View.GONE);
                    }
                });
            }

            private void loadLocation(Workshop workshop) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(workshop.getLatitude(), workshop.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String locality = address.getLocality();
                        String subAdminArea = address.getSubAdminArea();

                        etLocation.setText(Utils.addressBuilder(locality, subAdminArea));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                etLocation.setOnClickListener(view -> startActivity(new Intent(requireContext(), SelectLocationActivity.class)));
            }

            private void loadServices(Workshop workshop) {
                String services = workshop.getAvailableServices();
                chkBodyRepairs.setChecked(services.charAt(0) == '1');
                chkCarWash.setChecked(services.charAt(1) == '1');
                chkEngineMaintenance.setChecked(services.charAt(2) == '1');
                chkEmergencyAssistance.setChecked(services.charAt(3) == '1');
                chkElectrical.setChecked(services.charAt(4) == '1');
                chkSystemCheck.setChecked(services.charAt(5) == '1');
                chkFuelDelivery.setChecked(services.charAt(6) == '1');
                chkSparePartsReplacement.setChecked(services.charAt(7) == '1');
                chkWheels.setChecked(services.charAt(8) == '1');
                chk247Assistance.setChecked(services.charAt(9) == '1');
            }

            private String getAvailableServices() {
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

            /*private void handleWorkshopChanges(Workshop workshop) {
                String locationWasChanged = "false";
                String availableServices = getAvailableServices();
                // workshop name
                etWorkshopName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (!Objects.requireNonNull(etWorkshopName.getText()).toString().trim().equals(workshop.getName()) ||
                                !locationWasChanged.equals(false) ||
                                !availableServices.equals(workshop.getAvailableServices())) {
                            btnSaveWorkshopDetails.setVisibility(View.VISIBLE);
                        }
                        else {
                            btnSaveWorkshopDetails.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

                // location
                etLocation.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        locationWasChanged = "true";
                        btnSaveWorkshopDetails.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

                // checkboxes
            }*/

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}