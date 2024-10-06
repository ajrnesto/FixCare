package com.fixcare.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fixcare.Objects.Chat;
import com.fixcare.Objects.ChatReference;
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

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.inboxViewHolder>{

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    Context context;
    ArrayList<ChatReference> arrInbox = new ArrayList<>();
    private InboxAdapter.OnInboxListener mOnInboxListener;
    boolean userIsMechanic;

    public InboxAdapter(Context context, ArrayList<ChatReference> arrInbox, InboxAdapter.OnInboxListener onInboxListener, boolean userIsMechanic) {
        this.context = context;
        this.arrInbox = arrInbox;
        this.mOnInboxListener = onInboxListener;
        this.userIsMechanic = userIsMechanic;
    }

    @NonNull
    @Override
    public InboxAdapter.inboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_inbox, parent, false);
        return new InboxAdapter.inboxViewHolder(view, mOnInboxListener);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxAdapter.inboxViewHolder holder, int position) {
        ChatReference chatReference = arrInbox.get(position);

        loadContactName(holder, chatReference);
        loadAuthorName(holder, chatReference);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy - hh:mm aa");
        holder.tvTimestamp.setText(sdf.format(chatReference.getTimestamp()));
    }

    private void loadContactName(inboxViewHolder holder, ChatReference chatReference) {
        if (userIsMechanic) {
            DatabaseReference dbContactName = FIXCARE_DB.getReference("user_"+ chatReference.getContactUid());
            dbContactName.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String firstName = Objects.requireNonNull(snapshot.child("firstName").getValue()).toString();
                        String lastName = Objects.requireNonNull(snapshot.child("lastName").getValue()).toString();

                        holder.tvAuthorName.setText(firstName + " " + lastName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            DatabaseReference dbContactName = FIXCARE_DB.getReference("workshops/"+ chatReference.getContactUid()+"/name");
            dbContactName.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = Objects.requireNonNull(snapshot.getValue()).toString();

                    holder.tvAuthorName.setText(name);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void loadAuthorName(inboxViewHolder holder, ChatReference chatReference) {
        if (Objects.equals(chatReference.getAuthorUid(), Objects.requireNonNull(USER).getUid())) {
            holder.tvLastMessage.setText("You: "+chatReference.getMessage());
        }
        else {
            holder.tvLastMessage.setText(chatReference.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return arrInbox.size();
    }

    public class inboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvAuthorName, tvLastMessage, tvTimestamp;
        InboxAdapter.OnInboxListener onInboxListener;

        public inboxViewHolder(@NonNull View itemView, InboxAdapter.OnInboxListener onInboxListener) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);

            this.onInboxListener = onInboxListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onInboxListener.onInboxClick(getAdapterPosition());
        }
    }

    public interface OnInboxListener{
        void onInboxClick(int position);
    }
}