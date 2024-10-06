package com.fixcare.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fixcare.Fragments.ChatFragment;
import com.fixcare.Objects.Booking;
import com.fixcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.bookingViewHolder>{

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    Context context;
    ArrayList<Booking> arrBooking = new ArrayList<>();
    private BookingAdapter.OnBookingListener mOnBookingListener;
    boolean userIsMechanic;

    public BookingAdapter(Context context, ArrayList<Booking> arrBooking, BookingAdapter.OnBookingListener onBookingListener, boolean userIsMechanic) {
        this.context = context;
        this.arrBooking = arrBooking;
        this.mOnBookingListener = onBookingListener;
        this.userIsMechanic = userIsMechanic;
    }

    @NonNull
    @Override
    public BookingAdapter.bookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_booking, parent, false);
        return new BookingAdapter.bookingViewHolder(view, mOnBookingListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingAdapter.bookingViewHolder holder, int position) {
        Booking booking = arrBooking.get(position);

        loadTitle(holder, booking);
        loadSchedule(holder, booking);
        loadVehicle(holder, booking);
        loadSelectedServices(holder, booking);
    }

    private void loadTitle(bookingViewHolder holder, Booking booking) {
        if (userIsMechanic) {
            DatabaseReference dbCustomerName = FIXCARE_DB.getReference("user_"+booking.getCustomerUid());
            dbCustomerName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String firstName = Objects.requireNonNull(snapshot.child("firstName").getValue()).toString();
                    String lastName = Objects.requireNonNull(snapshot.child("lastName").getValue()).toString();

                    holder.tvName.setText(firstName+" "+lastName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            DatabaseReference dbWorkshopName = FIXCARE_DB.getReference("workshops/"+booking.getWorkshopUid()+"/name");
            dbWorkshopName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String workshopName = Objects.requireNonNull(snapshot.getValue()).toString();
                    holder.tvName.setText(workshopName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void loadSchedule(bookingViewHolder holder, Booking booking) {
        long schedule = booking.getSchedule();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        holder.tvDate.setText(sdf.format(schedule));
    }

    private void loadVehicle(bookingViewHolder holder, Booking booking) {
        String vehicleClass = booking.getVehicleClass();
        if (vehicleClass.equalsIgnoreCase("Car")) {
            holder.imgCar.setVisibility(View.VISIBLE);
            holder.imgMotorcycle.setVisibility(View.GONE);
        }
        else {
            holder.imgCar.setVisibility(View.GONE);
            holder.imgMotorcycle.setVisibility(View.VISIBLE);
        }

        String vehicleModel = booking.getModel();
        String vehiclePlateNumber = booking.getPlateNumber();
        holder.tvVehicleInfo.setText(vehicleModel+" - "+vehiclePlateNumber);
    }

    private void loadSelectedServices(bookingViewHolder holder, Booking booking) {
        String services = booking.getSelectedServices();
        StringBuilder servicesBuilder = new StringBuilder();

        if (services.charAt(0) == '1') { servicesBuilder.append("Body Repairs, "); }
        if (services.charAt(1) == '1') { servicesBuilder.append("Car Wash & Detailing, "); }
        if (services.charAt(2) == '1') { servicesBuilder.append("Engine Maintenance, "); }
        if (services.charAt(3) == '1') { servicesBuilder.append("Emergency Assistance, "); }
        if (services.charAt(4) == '1') { servicesBuilder.append("Electrical & Battery, "); }
        if (services.charAt(5) == '1') { servicesBuilder.append("Full System Check, "); }
        if (services.charAt(6) == '1') { servicesBuilder.append("Gas & Fuel Delivery, "); }
        if (services.charAt(7) == '1') { servicesBuilder.append("Spare Parts Replacement, "); }
        if (services.charAt(8) == '1') { servicesBuilder.append("Wheels & Tires, "); }
        if (services.charAt(9) == '1') { servicesBuilder.append("24/7 Assistance, "); }

        servicesBuilder.delete(servicesBuilder.length() - 2, servicesBuilder.length());
        holder.tvServices.setText(servicesBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return arrBooking.size();
    }

    public class bookingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvName, tvDate, tvVehicleInfo, tvServices;
        AppCompatImageView imgCar, imgMotorcycle;
        BookingAdapter.OnBookingListener onBookingListener;

        public bookingViewHolder(@NonNull View itemView, BookingAdapter.OnBookingListener onBookingListener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvVehicleInfo = itemView.findViewById(R.id.tvVehicleInfo);
            tvServices = itemView.findViewById(R.id.tvServices);
            imgCar = itemView.findViewById(R.id.imgCar);
            imgMotorcycle = itemView.findViewById(R.id.imgMotorcycle);

            this.onBookingListener = onBookingListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onBookingListener.onBookingClick(getAdapterPosition());
        }
    }

    public interface OnBookingListener{
        void onBookingClick(int position);
    }
}