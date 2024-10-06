package com.fixcare.Fragments;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixcare.Adapters.ChatAdapter;
import com.fixcare.Objects.Chat;
import com.fixcare.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ChatFragment extends Fragment implements ChatAdapter.OnChatListener {

    private FirebaseDatabase FIXCARE_DB;
    private FirebaseUser USER;
    private DatabaseReference dbChat;
    private ValueEventListener velChat;

    RecyclerView rvChat;
    TextView tvEmpty;
    MaterialButton btnSend;
    TextInputEditText etChatBox;
    CircularProgressIndicator loadingBar;

    // top action bar elements
    MaterialButton btnBack;
    TextView tvActivityTitle;

    ArrayList<Chat> arrChat;
    ChatAdapter chatAdapter;
    ChatAdapter.OnChatListener onChatListener = this;

    String contactUid, contactName;
    boolean userIsMechanic;

    String chat = "";

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        initialize();
        initializeTopActionBar(view);
        loadRecyclerView();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat = Objects.requireNonNull(etChatBox.getText()).toString().trim();
                if (chat.isEmpty()) {
                    return;
                }
                sendMessage();
                etChatBox.getText().clear();
            }

            private void sendMessage() {
                if (userIsMechanic){
                    String customerUid = contactUid;

                    DatabaseReference dbWorkshopUid = FIXCARE_DB.getReference("user_"+USER.getUid()+"/workshopUid");
                    dbWorkshopUid.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String workshopUid = Objects.requireNonNull(snapshot.getValue()).toString();

                            DatabaseReference dbChat = FIXCARE_DB.getReference("chat_"+workshopUid+"_"+customerUid).push();
                            Chat newChat = new Chat(dbChat.getKey(), chat, USER.getUid(), System.currentTimeMillis());
                            dbChat.setValue(newChat);

                            DatabaseReference dbChatUserReference = FIXCARE_DB.getReference("user_"+customerUid+"_chat/"+workshopUid);
                            dbChatUserReference.setValue(newChat);
                            dbChatUserReference.child("contactUid").setValue(workshopUid);

                            DatabaseReference dbChatWorkshopReference = FIXCARE_DB.getReference("workshop_"+workshopUid+"_chat/"+customerUid);
                            dbChatWorkshopReference.setValue(newChat);
                            dbChatWorkshopReference.child("contactUid").setValue(customerUid);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else {
                    String workshopUid = contactUid;

                    DatabaseReference dbChat = FIXCARE_DB.getReference("chat_"+workshopUid+"_"+USER.getUid()).push();
                    Chat newChat = new Chat(dbChat.getKey(), chat, USER.getUid(), System.currentTimeMillis());
                    dbChat.setValue(newChat);

                    DatabaseReference dbChatUserReference = FIXCARE_DB.getReference("user_"+USER.getUid()+"_chat/"+workshopUid);
                    dbChatUserReference.setValue(newChat);
                    dbChatUserReference.child("contactUid").setValue(workshopUid);

                    DatabaseReference dbChatWorkshopReference = FIXCARE_DB.getReference("workshop_"+workshopUid+"_chat/"+USER.getUid());
                    dbChatWorkshopReference.setValue(newChat);
                    dbChatWorkshopReference.child("contactUid").setValue(USER.getUid());
                }
            }
        });

        return view;
    }

    private void initialize() {
        rvChat = view.findViewById(R.id.rvChat);
        etChatBox = view.findViewById(R.id.etChatBox);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnSend = view.findViewById(R.id.btnSend);
        loadingBar = view.findViewById(R.id.loadingBar);

        Bundle chatArgs = getArguments();
        contactUid = Objects.requireNonNull(chatArgs).getString("contact_uid");
        userIsMechanic = chatArgs.getBoolean("user_is_mechanic");
        contactName = chatArgs.getString("contact_name", "Chat");

        FIXCARE_DB = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initializeTopActionBar(View view) {
        tvActivityTitle = view.findViewById(R.id.tvActivityTitle);
        btnBack = view.findViewById(R.id.btnActionBar);

        tvActivityTitle.setText(contactName);
        btnBack.setOnClickListener(view1 -> {
            requireActivity().onBackPressed();
        });
    }

    private void loadRecyclerView() {
        if (userIsMechanic) {
            arrChat = new ArrayList<>();
            rvChat = view.findViewById(R.id.rvChat);
            rvChat.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setStackFromEnd(true);
            rvChat.setLayoutManager(linearLayoutManager);

            // reference = chat_workshopUid_customerUid
            DatabaseReference dbWorkshopUid = FIXCARE_DB.getReference("user_"+USER.getUid()+"/workshopUid");
            dbWorkshopUid.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String workshopUid = snapshot.getValue().toString();

                    dbChat = FIXCARE_DB.getReference("chat_"+workshopUid+"_"+contactUid);
                    velChat = dbChat.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            arrChat.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Chat chat = dataSnapshot.getValue(Chat.class);
                                arrChat.add(chat);
                                chatAdapter.notifyDataSetChanged();
                                rvChat.scrollToPosition(arrChat.size() - 1);
                            }

                            loadingBar.hide();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    chatAdapter = new ChatAdapter(getContext(), arrChat, onChatListener);
                    rvChat.setAdapter(chatAdapter);
                    chatAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            arrChat = new ArrayList<>();
            rvChat = view.findViewById(R.id.rvChat);
            rvChat.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setStackFromEnd(true);
            rvChat.setLayoutManager(linearLayoutManager);

            // reference = chat_workshopUid_customerUid
            dbChat = FIXCARE_DB.getReference("chat_"+contactUid+"_"+USER.getUid());
            velChat = dbChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    arrChat.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        arrChat.add(chat);
                        chatAdapter.notifyDataSetChanged();
                        rvChat.scrollToPosition(arrChat.size() - 1);
                    }

                    loadingBar.hide();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            chatAdapter = new ChatAdapter(getContext(), arrChat, onChatListener);
            rvChat.setAdapter(chatAdapter);
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onChatClick(int position) {

    }
}