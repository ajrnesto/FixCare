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

import com.fixcare.Adapters.InboxAdapter;
import com.fixcare.Objects.Chat;
import com.fixcare.Objects.ChatReference;
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

public class InboxFragment extends Fragment implements InboxAdapter.OnInboxListener {

    private FirebaseDatabase FIXCARE_DB;
    private FirebaseUser USER;
    DatabaseReference dbInbox;
    ValueEventListener velInbox;

    CircularProgressIndicator loadingBar;
    RecyclerView rvInbox;
    TextView tvEmpty;

    View view;
    boolean userIsMechanic;

    ArrayList<ChatReference> arrChatReferences;
    ArrayList<Chat> arrChat;
    InboxAdapter inboxAdapter;
    InboxAdapter.OnInboxListener onBookingListener = this;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_inbox, container, false);

        initialize();
        loadInbox();

        return view;
    }

    private void initialize() {
        rvInbox = view.findViewById(R.id.rvInbox);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        FIXCARE_DB = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void loadInbox() {
        DatabaseReference dbUserType = FIXCARE_DB.getReference("user_"+ Objects.requireNonNull(USER).getUid()+"/userType");
        dbUserType.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIsMechanic = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString()) == 1;

                // load inbox
                if (userIsMechanic) {
                    useWorkshopChatReference();
                }
                else {
                    useCustomerChatReference();
                }
            }

            private void useWorkshopChatReference() {
                arrChatReferences = new ArrayList<>();
                rvInbox = view.findViewById(R.id.rvInbox);
                loadingBar = view.findViewById(R.id.loadingBar);
                rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));

                DatabaseReference dbWorkshopUid = FIXCARE_DB.getReference("user_"+USER.getUid()+"/workshopUid");
                dbWorkshopUid.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String workshopUid = Objects.requireNonNull(snapshot.getValue()).toString();

                        DatabaseReference dbInbox = FIXCARE_DB.getReference("workshop_"+workshopUid+"_chat");
                        velInbox = dbInbox.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                arrChatReferences.clear();

                                if (!snapshot.exists()) {
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    rvInbox.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    tvEmpty.setVisibility(View.GONE);
                                    rvInbox.setVisibility(View.VISIBLE);
                                }

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    ChatReference chatReference = dataSnapshot.getValue(ChatReference.class);
                                    arrChatReferences.add(chatReference);
                                }

                                loadingBar.hide();

                                inboxAdapter = new InboxAdapter(getContext(), arrChatReferences, onBookingListener, userIsMechanic);
                                rvInbox.setAdapter(inboxAdapter);
                                inboxAdapter.notifyDataSetChanged();
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

            private void useCustomerChatReference() {
                arrChatReferences = new ArrayList<>();
                rvInbox = view.findViewById(R.id.rvInbox);
                loadingBar = view.findViewById(R.id.loadingBar);
                rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));

                DatabaseReference dbInbox = FIXCARE_DB.getReference("user_"+USER.getUid()+"_chat");
                velInbox = dbInbox.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrChatReferences.clear();

                        if (!snapshot.exists()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvInbox.setVisibility(View.INVISIBLE);
                        }
                        else {
                            tvEmpty.setVisibility(View.GONE);
                            rvInbox.setVisibility(View.VISIBLE);
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ChatReference chatReference = dataSnapshot.getValue(ChatReference.class);
                            arrChatReferences.add(chatReference);
                        }

                        loadingBar.hide();

                        inboxAdapter = new InboxAdapter(getContext(), arrChatReferences, onBookingListener, userIsMechanic);
                        rvInbox.setAdapter(inboxAdapter);
                        inboxAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onInboxClick(int position) {
        if (userIsMechanic) {
            String customerUid = arrChatReferences.get(position).getContactUid();

            DatabaseReference customerName = FIXCARE_DB.getReference("user_"+customerUid);
            customerName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String firstName = snapshot.child("firstName").getValue().toString();
                    String lastName = snapshot.child("lastName").getValue().toString();

                    DatabaseReference dbWorkshopUid = FIXCARE_DB.getReference("user_"+USER.getUid()+"/workshopUid");
                    dbWorkshopUid.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String workshopUid = snapshot.getValue().toString();

                            Bundle chatArgs = new Bundle();
                            chatArgs.putBoolean("user_is_mechanic", true);
                            chatArgs.putString("contact_uid", customerUid);
                            chatArgs.putString("contact_name", firstName+" "+lastName);
                            chatArgs.putString("workshop_uid", workshopUid);

                            ChatFragment chatFragment = new ChatFragment();
                            chatFragment.setArguments(chatArgs);
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.frameLayout, chatFragment, "CHAT_FRAGMENT")
                                    .addToBackStack("CHAT_FRAGMENT")
                                    .commit();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
        else {
            String workshopUid = arrChatReferences.get(position).getContactUid();

            DatabaseReference dbWorkshopName = FIXCARE_DB.getReference("workshops/"+workshopUid+"/name");
            dbWorkshopName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String workshopName = Objects.requireNonNull(snapshot.getValue()).toString();

                    Bundle chatArgs = new Bundle();
                    chatArgs.putBoolean("user_is_mechanic", false);
                    chatArgs.putString("contact_uid", workshopUid);
                    chatArgs.putString("contact_name", workshopName);

                    ChatFragment chatFragment = new ChatFragment();
                    chatFragment.setArguments(chatArgs);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, chatFragment, "CHAT_FRAGMENT")
                            .addToBackStack("CHAT_FRAGMENT")
                            .commit();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}