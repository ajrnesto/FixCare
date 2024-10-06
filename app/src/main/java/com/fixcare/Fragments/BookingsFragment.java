package com.fixcare.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixcare.Adapters.BookingAdapter;
import com.fixcare.Dialogs.BookingReference;
import com.fixcare.Objects.Booking;
import com.fixcare.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class BookingsFragment extends Fragment implements BookingAdapter.OnBookingListener {

    private FirebaseDatabase FIXCARE_DB;
    private FirebaseUser USER;
    DatabaseReference dbUserBookings, dbWorkshopBookings;
    ValueEventListener velBookings;
    boolean userIsMechanic;

    CircularProgressIndicator loadingBar;
    RecyclerView rvBookings;
    TextView tvEmpty;

    ArrayList<Booking> arrBookings;
    ArrayList<BookingReference> arrBookingReferences;
    BookingAdapter bookingAdapter;
    BookingAdapter.OnBookingListener onBookingListener = this;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bookings, container, false);

        initialize();
        checkUserType();

        return view;
    }

    private void initialize() {
        rvBookings = view.findViewById(R.id.rvBookings);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        FIXCARE_DB = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void checkUserType() {
        DatabaseReference dbUserType = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(USER).getUid()+"/userType");
        dbUserType.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIsMechanic = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString()) == 1;

                if (userIsMechanic) {
                    loadWorkshopBookings();
                }
                else {
                    loadUserBooking();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadWorkshopBookings() {
        arrBookings = new ArrayList<>();
        arrBookingReferences = new ArrayList<>();
        rvBookings = view.findViewById(R.id.rvBookings);
        loadingBar = view.findViewById(R.id.loadingBar);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseReference dbWorkshopUid = FIXCARE_DB.getReference("user_"+USER.getUid()+"/workshopUid");
        dbWorkshopUid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String workshopUid = snapshot.getValue().toString();

                dbWorkshopBookings = FIXCARE_DB.getReference("workshop_"+workshopUid+"_bookings");
                velBookings = dbWorkshopBookings.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrBookings.clear();
                        arrBookingReferences.clear();

                        if (!snapshot.exists()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvBookings.setVisibility(View.INVISIBLE);
                        }
                        else {
                            tvEmpty.setVisibility(View.GONE);
                            rvBookings.setVisibility(View.VISIBLE);
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            BookingReference bookingReference = dataSnapshot.getValue(BookingReference.class);
                            arrBookingReferences.add(bookingReference);
                        }

                        for (BookingReference bookingReference : arrBookingReferences) {
                            DatabaseReference dbWorkshopBooking = FIXCARE_DB.getReference("workshop_"+bookingReference.getWorkshopUid()+"_bookings/"+bookingReference.getUid());
                            dbWorkshopBooking.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Booking booking = snapshot.getValue(Booking.class);
                                    arrBookings.add(booking);
                                    bookingAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                        loadingBar.hide();

                        bookingAdapter = new BookingAdapter(getContext(), arrBookings, onBookingListener, userIsMechanic);
                        rvBookings.setAdapter(bookingAdapter);
                        bookingAdapter.notifyDataSetChanged();
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

    private void loadUserBooking() {
        arrBookings = new ArrayList<>();
        arrBookingReferences = new ArrayList<>();
        rvBookings = view.findViewById(R.id.rvBookings);
        loadingBar = view.findViewById(R.id.loadingBar);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        dbUserBookings = FIXCARE_DB.getReference("user_"+USER.getUid()+"_bookings");
        velBookings = dbUserBookings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrBookings.clear();
                arrBookingReferences.clear();

                if (!snapshot.exists()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvBookings.setVisibility(View.INVISIBLE);
                }
                else {
                    tvEmpty.setVisibility(View.GONE);
                    rvBookings.setVisibility(View.VISIBLE);
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BookingReference bookingReference = dataSnapshot.getValue(BookingReference.class);
                    arrBookingReferences.add(bookingReference);
                }

                for (BookingReference bookingReference : arrBookingReferences) {
                    DatabaseReference dbWorkshopBooking = FIXCARE_DB.getReference("workshop_"+bookingReference.getWorkshopUid()+"_bookings/"+bookingReference.getUid());
                    dbWorkshopBooking.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Booking booking = snapshot.getValue(Booking.class);
                            arrBookings.add(booking);
                            bookingAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                loadingBar.hide();

                bookingAdapter = new BookingAdapter(getContext(), arrBookings, onBookingListener, userIsMechanic);
                rvBookings.setAdapter(bookingAdapter);
                bookingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBookingClick(int position) {
        // get user type
        DatabaseReference dbUserType = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(USER).getUid()+"/userType");
        dbUserType.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIsMechanic = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString()) == 1;
                Booking booking = arrBookings.get(position);

                String uid = booking.getUid();
                String vehicleClass = booking.getVehicleClass();
                String model = booking.getModel();
                String plateNumber = booking.getPlateNumber();
                double latitude = booking.getLatitude();
                double longitude = booking.getLongitude();
                long schedule = booking.getSchedule();
                String selectedServices = booking.getSelectedServices();
                String customerUid = booking.getCustomerUid();
                String workshopUid = booking.getWorkshopUid();
                int status = booking.getStatus();

                Bundle bookingArgs = new Bundle();
                bookingArgs.putString("booking_uid", uid);
                bookingArgs.putString("vehicle_class", vehicleClass);
                bookingArgs.putString("model", model);
                bookingArgs.putString("plateNumber", plateNumber);
                bookingArgs.putDouble("latitude", latitude);
                bookingArgs.putDouble("longitude", longitude);
                bookingArgs.putLong("schedule", schedule);
                bookingArgs.putString("selected_services", selectedServices);
                bookingArgs.putString("customer_uid", customerUid);
                bookingArgs.putString("workshop_uid", workshopUid);
                bookingArgs.putBoolean("user_is_mechanic", userIsMechanic);
                bookingArgs.putInt("status", status);

                ViewBookingFragment viewBookingFragment = new ViewBookingFragment();
                viewBookingFragment.setArguments(bookingArgs);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, viewBookingFragment, "VIEW_BOOKING_FRAGMENT")
                        .addToBackStack("VIEW_BOOKING_FRAGMENT")
                        .commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}