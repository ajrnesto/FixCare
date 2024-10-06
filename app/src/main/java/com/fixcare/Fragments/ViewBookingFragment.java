package com.fixcare.Fragments;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class ViewBookingFragment extends Fragment {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference dbUser;

    View view;
    GoogleMap googleMap;
    Location currentLocation;
    TextView tvWorkshopName, tvCustomerName, tvDate, tvVehicleModel, tvVehiclePlateNumber, tvServices, tvStatus;
    AppCompatImageView imgCar, imgMotorcycle;

    String bookingUid;
    String vehicleClass;
    String model;
    String plateNumber;
    double latitude;
    double longitude;
    long schedule;
    String selectedServices;
    String customerUid;
    String workshopUid;
    boolean userIsMechanic;
    int status;

    MaterialButton btnCancel, btnAccept, btnComplete, btnDelete;

    FusedLocationProviderClient fusedLocationProviderClient;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap map) {
            googleMap = map;
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));

            LatLng vehiclePosition = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions();

            if (vehicleClass.equalsIgnoreCase("car")) {
                markerOptions.position(vehiclePosition)
                        .icon(Utils.bitmapDescriptorFromVector(requireContext(), R.drawable.car_marker, 60, 60))
                        .title("Vehicle location")
                        .snippet(model + " - " + plateNumber);
            }
            else {
                markerOptions.position(vehiclePosition)
                        .icon(Utils.bitmapDescriptorFromVector(requireContext(), R.drawable.motorcycle_marker, 60, 60))
                        .title("Vehicle location")
                        .snippet(model + " - " + plateNumber);
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vehiclePosition, 18 ));

            Objects.requireNonNull(googleMap.addMarker(markerOptions)).showInfoWindow();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_booking, container, false);

        initialize();
        loadBookingDetails();
        manageButtons();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        return view;
    }

    private void manageButtons() {
        DatabaseReference dbStatus = FIXCARE_DB.getReference("workshop_"+workshopUid+"_bookings/"+bookingUid+"/status");
        dbStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) { // if snapshot doesn't exist, then the booking must have been deleted
                    MaterialAlertDialogBuilder dialogRecordDeleted = new MaterialAlertDialogBuilder(requireContext());
                    dialogRecordDeleted.setTitle("Booking record deleted");
                    dialogRecordDeleted.setMessage("The booking record you were viewing has been deleted or cancelled. You will be redirected back to your list of bookings.");
                    dialogRecordDeleted.setPositiveButton("Okay", (dialogInterface, i) -> {
                    });
                    dialogRecordDeleted.setOnDismissListener(dialogInterface -> requireActivity().onBackPressed());
                    dialogRecordDeleted.show();
                }
                else {
                    int status = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());

                    if (status == 0) {
                        hideAllButtons();
                        if (userIsMechanic) {
                            btnAccept.setVisibility(View.VISIBLE);
                        }
                        else {
                            btnCancel.setVisibility(View.VISIBLE);
                        }
                    }
                    else if (status == 1) {
                        tvStatus.setText("Accepted");
                        hideAllButtons();
                        if (userIsMechanic) {
                            btnComplete.setVisibility(View.VISIBLE);
                        }
                        // else if user is customer, just don't show any buttons
                    }
                    else if (status == 2) {
                        tvStatus.setText("Completed");
                        hideAllButtons();
                        btnDelete.setVisibility(View.VISIBLE);
                    }
                }
            }

            private void hideAllButtons() {
                btnCancel.setVisibility(View.GONE);
                btnAccept.setVisibility(View.GONE);
                btnComplete.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnCancel.setOnClickListener(view -> {
            MaterialAlertDialogBuilder dialogCancelRecord = new MaterialAlertDialogBuilder(requireContext());
            dialogCancelRecord.setTitle("Cancel booking");
            dialogCancelRecord.setMessage("Did you want to cancel this appointment?");
            dialogCancelRecord.setPositiveButton("Cancel", (dialogInterface, i) -> {
                DatabaseReference dbUserBooking = FIXCARE_DB.getReference("user_"+customerUid+"_bookings/"+bookingUid);
                dbUserBooking.removeValue();

                DatabaseReference dbWorkshopBooking = FIXCARE_DB.getReference("workshop_"+workshopUid+"_bookings/"+bookingUid);
                dbWorkshopBooking.removeValue();
            });
            dialogCancelRecord.setNeutralButton("Back", (dialogInterface, i) -> { });
            dialogCancelRecord.show();
        });

        btnAccept.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Booking has been accepted", Toast.LENGTH_SHORT).show();
            dbStatus.setValue(1);
        });

        btnComplete.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Booking has been marked as completed", Toast.LENGTH_SHORT).show();
            dbStatus.setValue(2);
        });

        btnDelete.setOnClickListener(view -> {
            MaterialAlertDialogBuilder dialogDeleteRecord = new MaterialAlertDialogBuilder(requireContext());
            dialogDeleteRecord.setTitle("Delete booking record");
            dialogDeleteRecord.setMessage("This booking has already been completed and is safe to delete.");
            dialogDeleteRecord.setPositiveButton("Delete", (dialogInterface, i) -> {
                DatabaseReference dbUserBooking = FIXCARE_DB.getReference("user_"+customerUid+"_bookings/"+bookingUid);
                dbUserBooking.removeValue();

                DatabaseReference dbWorkshopBooking = FIXCARE_DB.getReference("workshop_"+workshopUid+"_bookings/"+bookingUid);
                dbWorkshopBooking.removeValue();
            });
            dialogDeleteRecord.setNeutralButton("Cancel", (dialogInterface, i) -> { });
            dialogDeleteRecord.show();
        });
    }

    private void loadBookingDetails() {
        DatabaseReference dbWorkshopName = FIXCARE_DB.getReference("workshops/"+workshopUid+"/name");
        dbWorkshopName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String workshopName = Objects.requireNonNull(snapshot.getValue()).toString();
                tvWorkshopName.setText(workshopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference dbCustomer = FIXCARE_DB.getReference("user_"+customerUid);
        dbCustomer.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue().toString();
                String lastName = snapshot.child("lastName").getValue().toString();
                tvCustomerName.setText(firstName+" "+lastName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        tvDate.setText(sdf.format(schedule));
        tvVehicleModel.setText(model);

        if (vehicleClass.equalsIgnoreCase("car")) {
            imgCar.setVisibility(View.VISIBLE);
            imgMotorcycle.setVisibility(View.GONE);
        }
        else {
            imgCar.setVisibility(View.GONE);
            imgMotorcycle.setVisibility(View.VISIBLE);
        }
        tvVehiclePlateNumber.setText(plateNumber);

        tvServices.setText(loadSelectedServices(selectedServices));

        if (status == 0) {
            tvStatus.setText("This appointment is Pending");
        }
        else if (status == 1) {
            tvStatus.setText("This appointment is in-progress");
        }
        else if (status == 2) {
            tvStatus.setText("This appointment has been Completed");
        }
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

    private void initialize() {
        btnCancel = view.findViewById(R.id.btnCancel);
        btnAccept = view.findViewById(R.id.btnAccept);
        btnComplete = view.findViewById(R.id.btnComplete);
        btnDelete = view.findViewById(R.id.btnDelete);
        tvWorkshopName = view.findViewById(R.id.tvWorkshopName);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvDate = view.findViewById(R.id.tvDate);
        tvVehicleModel = view.findViewById(R.id.tvVehicleModel);
        tvVehiclePlateNumber = view.findViewById(R.id.tvVehiclePlateNumber);
        tvServices = view.findViewById(R.id.tvServices);
        tvStatus = view.findViewById(R.id.tvStatus);
        imgCar = view.findViewById(R.id.imgCar);
        imgMotorcycle = view.findViewById(R.id.imgMotorcycle);

        Bundle bookingArgs = getArguments();
        bookingUid = bookingArgs.getString("booking_uid");
        vehicleClass = bookingArgs.getString("vehicle_class");
        model = bookingArgs.getString("model");
        plateNumber = bookingArgs.getString("plateNumber");
        latitude = bookingArgs.getDouble("latitude");
        longitude = bookingArgs.getDouble("longitude");
        schedule = bookingArgs.getLong("schedule");
        selectedServices = bookingArgs.getString("selected_services");
        customerUid = bookingArgs.getString("customer_uid");
        workshopUid = bookingArgs.getString("workshop_uid");
        userIsMechanic = bookingArgs.getBoolean("user_is_mechanic");
        status = bookingArgs.getInt("status");
    }
}