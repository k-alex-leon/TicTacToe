package com.example.tictactoe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tictactoe.R;
import com.example.tictactoe.models.Move;
import com.example.tictactoe.providers.AuthProvider;
import com.example.tictactoe.providers.GamesProvider;
import com.example.tictactoe.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;


public class GameAdapter extends FirestoreRecyclerAdapter<Move, GameAdapter.ViewHolder> {

    Context context;
    GamesProvider mGamesProvider;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;

    public GameAdapter(@NonNull FirestoreRecyclerOptions<Move> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull GameAdapter.ViewHolder holder, int position, @NonNull Move model) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        holder.mTxtDateGame.setText(format.format(model.getTimestamp()));

        String tie = "TIE";

        if (mAuthProvider.getUid().equals(model.getIdWinner())){
            holder.mImgVStatus.setImageResource(R.drawable.ic_cup_win);
        }else if (tie.equals(model.getIdWinner())){
            holder.mImgVStatus.setImageResource(R.drawable.ic_tie);
        }else{
            holder.mImgVStatus.setImageResource(R.drawable.ic_lose);
        }

        if (model.getGamer1id().equals(mAuthProvider.getUid())){
            mUsersProvider.getUser(model.getGamer2id()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    holder.mTxtVPlayerName.setText(documentSnapshot.getString("username"));
                }
            });
        }else{
            mUsersProvider.getUser(model.getGamer1id()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    holder.mTxtVPlayerName.setText(documentSnapshot.getString("username"));
                }
            });
        }
    }

    @NonNull
    @Override
    public GameAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_game,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTxtVPlayerName, mTxtDateGame;
        ImageView mImgVStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mGamesProvider = new GamesProvider();
            mUsersProvider = new UsersProvider();
            mAuthProvider = new AuthProvider();

            mTxtVPlayerName = itemView.findViewById(R.id.txtVPlayerName);
            mTxtDateGame = itemView.findViewById(R.id.txtVDateGame);
            mImgVStatus = itemView.findViewById(R.id.imgVStatusGame);
        }
    }
}
