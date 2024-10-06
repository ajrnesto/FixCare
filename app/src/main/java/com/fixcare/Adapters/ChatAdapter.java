package com.fixcare.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.fixcare.Objects.Chat;
import com.fixcare.R;
import com.fixcare.Utils.Utils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.chatViewHolder>{

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    Context context;
    ArrayList<Chat> arrChats;
    private ChatAdapter.OnChatListener mOnChatListener;

    public ChatAdapter(Context context, ArrayList<Chat> arrChats, ChatAdapter.OnChatListener onChatListener) {
        this.context = context;
        this.arrChats = arrChats;
        this.mOnChatListener = onChatListener;
    }

    @NonNull
    @Override
    public ChatAdapter.chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_chat, parent, false);
        return new ChatAdapter.chatViewHolder(view, mOnChatListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.chatViewHolder holder, int position) {
        Chat chat = arrChats.get(position);

        if (Objects.equals(chat.getAuthorUid(), Objects.requireNonNull(USER).getUid())) { // if user is the chat author
            showFirstPerson(holder, chat);
            hideSecondPerson(holder);
        }
        else { // if user is NOT the chat author
            showSecondPerson(holder, chat);
            hideFirstPerson(holder);
        }
    }

    private void showFirstPerson(chatViewHolder holder, Chat chat) {
        holder.cvFirstPerson.setVisibility(View.VISIBLE);
        holder.imgFirstPerson.setVisibility(View.VISIBLE);
        holder.tvFirstPersonMessage.setText(chat.getMessage());

        /*if (userIsMechanic) {
            Picasso.get().load(Utils.getAdminPhotoUrl()).fit().centerCrop().into(holder.imgFirstPerson);
        }
        else {
            Picasso.get().load(authorPhotoUrl).fit().centerCrop().into(holder.imgFirstPerson);
        }*/
    }

    private void hideFirstPerson(chatViewHolder holder) {
        holder.cvFirstPerson.setVisibility(View.INVISIBLE);
        holder.imgFirstPerson.setVisibility(View.INVISIBLE);
    }

    private void showSecondPerson(chatViewHolder holder, Chat chat) {
        holder.cvSecondPerson.setVisibility(View.VISIBLE);
        holder.imgSecondPerson.setVisibility(View.VISIBLE);
        holder.tvSecondPersonMessage.setText(chat.getMessage());

        /*if (userIsMechanic) {
            Picasso.get().load(Utils.getAdminPhotoUrl()).fit().centerCrop().into(holder.imgSecondPerson);
        }
        else {
            Picasso.get().load(authorPhotoUrl).fit().centerCrop().into(holder.imgSecondPerson);
        }*/
    }

    private void hideSecondPerson(chatViewHolder holder) {
        holder.cvSecondPerson.setVisibility(View.INVISIBLE);
        holder.imgSecondPerson.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return arrChats.size();
    }

    public class chatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        AppCompatImageView imgFirstPerson, imgSecondPerson;
        MaterialCardView cvFirstPerson, cvSecondPerson;
        TextView tvFirstPersonMessage, tvSecondPersonMessage;
        ChatAdapter.OnChatListener onChatListener;

        public chatViewHolder(@NonNull View itemView, ChatAdapter.OnChatListener onChatListener) {
            super(itemView);
            imgFirstPerson = itemView.findViewById(R.id.imgFirstPerson);
            cvFirstPerson = itemView.findViewById(R.id.cvFirstPerson);
            tvFirstPersonMessage = itemView.findViewById(R.id.tvFirstPersonMessage);

            imgSecondPerson = itemView.findViewById(R.id.imgSecondPerson);
            cvSecondPerson = itemView.findViewById(R.id.cvSecondPerson);
            tvSecondPersonMessage = itemView.findViewById(R.id.tvSecondPersonMessage);

            this.onChatListener = onChatListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onChatListener.onChatClick(getAdapterPosition());
        }
    }

    public interface OnChatListener{
        void onChatClick(int position);
    }
}
