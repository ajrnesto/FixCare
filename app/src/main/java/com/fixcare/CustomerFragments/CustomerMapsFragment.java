package com.fixcare.CustomerFragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fixcare.Dialogs.EmergencyDialog;
import com.fixcare.Dialogs.WorkshopDialog;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CustomerMapsFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    View view;
    GoogleMap googleMap;
    Location currentLocation;
    
    MaterialButton btnHelp, btnStopHelp;
    TextView tvLocationWarning;

    FusedLocationProviderClient fusedLocationProviderClient;

    // workshop markers
    ArrayList<Workshop> arrWorkshops;
    ArrayList<Marker> arrMarkers;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap map) {
            googleMap = map;
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            getCurrentLocation();
            renderWorkshops();

            btnHelp.setOnClickListener(view -> {

                LatLng currentPosition = googleMap.getCameraPosition().target;

                Bundle args = new Bundle();
                args.putDouble("latitude", currentPosition.latitude);
                args.putDouble("longitude", currentPosition.longitude);

                EmergencyDialog emergencyDialog = new EmergencyDialog();
                emergencyDialog.setArguments(args);
                emergencyDialog.show(requireActivity().getSupportFragmentManager(), "EMERGENCY_DIALOG");
            });

            btnStopHelp.setOnClickListener(view -> {
                DatabaseReference dbMyEmergency = FIXCARE_DB.getReference("emergencies/"+ Objects.requireNonNull(USER).getUid());
                dbMyEmergency.removeValue();
            });
        }

        private void renderWorkshops() {
            arrWorkshops = new ArrayList<>();
            arrMarkers = new ArrayList<>();

            DatabaseReference dbWorkshops = FIXCARE_DB.getReference("workshops");
            dbWorkshops.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    arrWorkshops.clear();
                    arrMarkers.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        arrWorkshops.add(dataSnapshot.getValue(Workshop.class));
                    }

                    int workshopCount = arrWorkshops.size();
                    for (int i = 0; i < workshopCount; i++) {
                        Workshop workshop = arrWorkshops.get(i);

                        LatLng workshopLatLng = new LatLng(workshop.getLatitude(), workshop.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(workshopLatLng)
                                .icon(Utils.bitmapDescriptorFromVector(requireContext(), R.drawable.car_mechanic_24, 60, 60))
                                .title(workshop.getName())
                                .snippet("Click here to visit shop");

                        arrMarkers.add(googleMap.addMarker(markerOptions));

                        googleMap.setOnInfoWindowClickListener(CustomerMapsFragment.this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);
        
        initialize();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        return view;
    }

    private void initialize() {
        DatabaseReference dbMyEmergency = FIXCARE_DB.getReference("emergencies/"+ Objects.requireNonNull(USER).getUid());
        dbMyEmergency.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) { // if user is calling for help
                    btnStopHelp.setVisibility(View.VISIBLE);
                    tvLocationWarning.setVisibility(View.VISIBLE);
                    btnHelp.setVisibility(View.GONE);
                }
                else { // if user is NOT calling for help
                    btnHelp.setVisibility(View.VISIBLE);
                    btnStopHelp.setVisibility(View.GONE);
                    tvLocationWarning.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btnHelp = view.findViewById(R.id.btnHelp);
        btnStopHelp = view.findViewById(R.id.btnStopHelp);
        tvLocationWarning = view.findViewById(R.id.tvLocationWarning);
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
                /*googleMap.addMarker(new MarkerOptions()
                                .position(myLocation)
                                .title("Sakto ni nga location?"));*/
                googleMap.setMyLocationEnabled(true);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
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
        int workshopIndex = arrMarkers.indexOf(marker);

        Workshop clickedWorkshop = arrWorkshops.get(workshopIndex);
        Bundle workshopArgs = new Bundle();
        workshopArgs.putString("uid", clickedWorkshop.getUid());
        workshopArgs.putString("name", clickedWorkshop.getName());
        workshopArgs.putDouble("latitude", clickedWorkshop.getLatitude());
        workshopArgs.putDouble("longitude", clickedWorkshop.getLongitude());
        workshopArgs.putString("address", clickedWorkshop.getAddress());
        workshopArgs.putString("available_services", clickedWorkshop.getAvailableServices());
        workshopArgs.putString("owner_uid", clickedWorkshop.getOwnerUid());

        WorkshopDialog workshopDialog = new WorkshopDialog();
        workshopDialog.setArguments(workshopArgs);
        workshopDialog.show(requireActivity().getSupportFragmentManager(), "WORKSHOP_DIALOG");
    }
}