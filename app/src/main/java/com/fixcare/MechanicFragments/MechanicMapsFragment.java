package com.fixcare.MechanicFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fixcare.CustomerFragments.CustomerMapsFragment;
import com.fixcare.Dialogs.ViewEmergencyDialog;
import com.fixcare.Dialogs.WorkshopDialog;
import com.fixcare.Objects.Emergency;
import com.fixcare.Objects.Workshop;
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MechanicMapsFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    View view;
    TextView cords;
    GoogleMap googleMap;
    Location currentLocation;

    FusedLocationProviderClient fusedLocationProviderClient;
    
    // emergency markers
    ArrayList<Emergency> arrEmergencies;
    ArrayList<Marker> arrMarkers;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap map) {
            googleMap = map;
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
            getCurrentLocation();
            renderEmergencies();

            DatabaseReference dbWorkshopUid = FIXCARE_DB.getReference("user_"+USER.getUid()+"/workshopUid");
            dbWorkshopUid.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String workshopUid = snapshot.getValue().toString();

                    DatabaseReference dbWorkshop = FIXCARE_DB.getReference("workshops/"+workshopUid);
                    dbWorkshop.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("latitude").getValue()).toString());
                            double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("longitude").getValue()).toString());

                            LatLng workshopLocation = new LatLng(latitude, longitude);
                            Objects.requireNonNull(googleMap.addMarker(new MarkerOptions()
                                    .position(workshopLocation)
                                    .icon(Utils.bitmapDescriptorFromVector(requireContext(), R.drawable.car_mechanic_24, 60, 60))
                                    .title("Your workshop")
                                    .snippet(name))).showInfoWindow();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void renderEmergencies() {
            arrEmergencies = new ArrayList<>();
            arrMarkers = new ArrayList<>();

            DatabaseReference dbWorkshops = FIXCARE_DB.getReference("emergencies");
            dbWorkshops.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    arrEmergencies.clear();
                    arrMarkers.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        arrEmergencies.add(dataSnapshot.getValue(Emergency.class));
                    }

                    int workshopCount = arrEmergencies.size();
                    for (int i = 0; i < workshopCount; i++) {
                        Emergency emergency = arrEmergencies.get(i);

                        LatLng emergencyPosition = new LatLng(emergency.getLatitude(), emergency.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(emergencyPosition)
                                .icon(Utils.bitmapDescriptorFromVector(requireContext(), R.drawable.engine_warning_marker, 60, 60))
                                .title(emergency.getModel())
                                .snippet("Click here to view details");

                        arrMarkers.add(googleMap.addMarker(markerOptions));

                        googleMap.setOnInfoWindowClickListener(MechanicMapsFragment.this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        return view;
    }

    private void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 100);
            return;
        }

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Toast.makeText(getContext()," location result is  " + locationResult, Toast.LENGTH_LONG).show();

                if (locationResult == null) {
                    Toast.makeText(getContext(),"current location is null ", Toast.LENGTH_LONG).show();

                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Toast.makeText(getContext(),"current location is " + location.getLongitude(), Toast.LENGTH_LONG).show();

                        //TODO: UI updates.
                    }
                }
            }
        };

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;

                LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                googleMap.setMyLocationEnabled(true);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        int emergencyIndex = arrMarkers.indexOf(marker);

        Emergency clickedEmergency = arrEmergencies.get(emergencyIndex);
        Bundle emergencyArgs = new Bundle();
        emergencyArgs.putString("customer_uid", clickedEmergency.getCustomerUid());
        emergencyArgs.putString("vehicle_class", clickedEmergency.getVehicleClass());
        emergencyArgs.putString("model", clickedEmergency.getModel());
        emergencyArgs.putString("plateNumber", clickedEmergency.getPlateNumber());
        emergencyArgs.putString("selected_services", clickedEmergency.getSelectedServices());

        ViewEmergencyDialog viewEmergencyDialog = new ViewEmergencyDialog();
        viewEmergencyDialog.setArguments(emergencyArgs);
        viewEmergencyDialog.show(requireActivity().getSupportFragmentManager(), "WORKSHOP_DIALOG");
    }
}