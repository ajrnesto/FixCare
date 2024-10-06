package com.fixcare.MechanicActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fixcare.MechanicActivities.MechanicActivity;
import com.fixcare.MechanicFragments.MechanicMapsFragment;
import com.fixcare.MechanicFragments.MechanicMenuFragment;
import com.fixcare.Fragments.BookingsFragment;
import com.fixcare.Fragments.ChatFragment;
import com.fixcare.Fragments.InboxFragment;
import com.fixcare.MechanicFragments.MechanicMapsFragment;
import com.fixcare.MechanicFragments.MechanicMenuFragment;
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MechanicActivity extends AppCompatActivity {

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    BottomNavigationView bottom_navbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic);

        FIXCARE_DB.getReference("user_"+USER.getUid()).child("workshopUid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    startActivity(new Intent(MechanicActivity.this, SetupWorkshopActivity.class));
                    finish();
                }
                else {
                    initialize();

                    bottom_navbar.setOnItemSelectedListener(item -> {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
                        switch (item.getItemId()) {
                            case R.id.miMap:
                                Fragment mapsFragment = new MechanicMapsFragment();
                                fragmentTransaction.replace(R.id.frameLayout, mapsFragment,"MAPS_FRAGMENT");
                                fragmentTransaction.addToBackStack("MAPS_FRAGMENT");
                                fragmentTransaction.commit();
                                break;
                            case R.id.miMenu:
                                Fragment menuFragment = new MechanicMenuFragment();
                                fragmentTransaction.replace(R.id.frameLayout, menuFragment, "MENU_FRAGMENT");
                                fragmentTransaction.addToBackStack("MENU_FRAGMENT");
                                fragmentTransaction.commit();
                                break;
                            case R.id.miChat:
                                Fragment inboxFragment = new InboxFragment();
                                fragmentTransaction.replace(R.id.frameLayout, inboxFragment, "INBOX_FRAGMENT");
                                fragmentTransaction.addToBackStack("INBOX_FRAGMENT");
                                fragmentTransaction.commit();
                                break;
                            case R.id.miBookings:
                                Fragment bookingsFragment = new BookingsFragment();
                                fragmentTransaction.replace(R.id.frameLayout, bookingsFragment, "BOOKINGS_FRAGMENT");
                                fragmentTransaction.addToBackStack("BOOKINGS_FRAGMENT");
                                fragmentTransaction.commit();
                                break;
                        }
                        return true;
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initialize() {
        bottom_navbar = findViewById(R.id.bottom_navbar);

        bottomNavbarManager();

        Fragment mapsFragment = new MechanicMapsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, mapsFragment, "MAPS_FRAGMENT");
        fragmentTransaction.addToBackStack("MAPS_FRAGMENT");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // get previous fragment
        int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
        FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
        String tag = backEntry.getName();
        Fragment prevFragment = getSupportFragmentManager().findFragmentByTag(tag);

        // check if previous fragment is maps
        if (Objects.requireNonNull(prevFragment).getClass() == MechanicMapsFragment.class) {
            Fragment mapsFragment = new MechanicMapsFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, mapsFragment, "MAPS_FRAGMENT");
            fragmentTransaction.addToBackStack("MAPS_FRAGMENT");
            fragmentTransaction.commit();
        }
    }

    private void bottomNavbarManager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            MechanicMenuFragment menuFragment = (MechanicMenuFragment) getSupportFragmentManager().findFragmentByTag("MENU_FRAGMENT");
            MechanicMapsFragment mapsFragment = (MechanicMapsFragment) getSupportFragmentManager().findFragmentByTag("MAPS_FRAGMENT");
            InboxFragment inboxFragment = (InboxFragment) getSupportFragmentManager().findFragmentByTag("INBOX_FRAGMENT");
            ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag("CHAT_FRAGMENT");
            BookingsFragment bookingsFragment = (BookingsFragment) getSupportFragmentManager().findFragmentByTag("BOOKINGS_FRAGMENT");

            if (menuFragment != null && menuFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(0).setChecked(true);
            }
            else if (mapsFragment != null && mapsFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(1).setChecked(true);
            }
            else if (inboxFragment != null && inboxFragment.isVisible()) {
                bottom_navbar.getMenu().getItem(2).setChecked(true);
            }
            else if (chatFragment != null && chatFragment.isVisible()) {
                softKeyboardListener();
                bottom_navbar.getMenu().getItem(2).setChecked(true);
            }
            else if (bookingsFragment != null && bookingsFragment.isVisible()) {
                softKeyboardListener();
                bottom_navbar.getMenu().getItem(3).setChecked(true);
            }
        });
    }

    private void softKeyboardListener() {
        final View activityRootView = findViewById(R.id.activityRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > Utils.dpToPx(MechanicActivity.this, 200)) {
                    // if keyboard visible
                    bottom_navbar.setVisibility(View.GONE);
                }
                else {
                    bottom_navbar.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}