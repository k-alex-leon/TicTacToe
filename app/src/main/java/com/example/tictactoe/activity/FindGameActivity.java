package com.example.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.tictactoe.R;
import com.example.tictactoe.adapters.GameAdapter;
import com.example.tictactoe.models.Move;
import com.example.tictactoe.providers.AuthProvider;
import com.example.tictactoe.providers.GamesProvider;
import com.example.tictactoe.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindGameActivity extends AppCompatActivity {

    Toolbar toolbar;
    AuthProvider mAuthProvider;
    GamesProvider mGamesProvider;
    UsersProvider mUserProvider;

    GameAdapter mGameAdapter;
    LinearLayout layoutLoading, layoutMenu;
    TextView txtVTitleGameDialog;
    AlertDialog mAlertDialog;

    Query query;

    ListenerRegistration listenerRegistration = null;

    Button mBtnGame, mBtnScore;
    TextView mTxtVPoints;

    String mIdGame = null, mIdGamer1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);

        mAuthProvider = new AuthProvider();
        mGamesProvider = new GamesProvider();
        mUserProvider = new UsersProvider();

        layoutLoading = findViewById(R.id.contentLoading);
        layoutMenu = findViewById(R.id.contentLobbyGame);

        mTxtVPoints = findViewById(R.id.txtVPoints);

        //declaracion del toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // buscar partida a jugar
        mBtnGame = findViewById(R.id.btnFindGame);
        mBtnGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLookingGameDialog();
            }
        });

        mBtnScore = findViewById(R.id.btnSeeScore);
        mBtnScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListGamesDialog();
            }
        });

        getPointsUser();

    }

    private void getPointsUser() {
        mUserProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists() && documentSnapshot.contains("points")){
                    mTxtVPoints.setText(String.valueOf(documentSnapshot.get("points")));
                }
            }
        });
    }

    /*
     *   GAME
     * */

    // mientras busca una partida libre muestra un alertDialog
    private void showLookingGameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.looking_game_dialog, null);

        builder.setView(view);
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();

        ProgressBar pbGameDialog;
        Button btnCancelGameDialog;

        txtVTitleGameDialog = view.findViewById(R.id.tvLoadingGame);
        pbGameDialog = view.findViewById(R.id.pbLoadingGame);
        btnCancelGameDialog = view.findViewById(R.id.btnCancelGame);

        pbGameDialog.setIndeterminate(true);
        btnCancelGameDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbGameDialog.setIndeterminate(false);
                mAlertDialog.dismiss();
                cancelGame(txtVTitleGameDialog);
            }
        });

        searchFreeGame();
    }


    private void searchFreeGame() {
        txtVTitleGameDialog.setText("Looking for a game...");

        mGamesProvider.getGameByPlayers().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().size() == 0){

                    createGame();

                }else{
                    txtVTitleGameDialog.setText("Adding to game...");
                    DocumentSnapshot docGame = task.getResult().getDocuments().get(0);
                    mIdGame = docGame.getId();

                    Move game = docGame.toObject(Move.class);
                    game.setGamer2id(mAuthProvider.getUid());
                    game.setIdGame(mIdGame);
                    mIdGamer1 = game.getGamer1id();

                    // buscando partida con un id user distinto al creador
                    if (!mIdGamer1.equals(mAuthProvider.getUid())){

                        mGamesProvider.addPlayer2(mIdGame, game).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                startGame();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FindGameActivity.this, "Error adding player to game!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }else{
                        // si no encuentra partida, crea una
                        txtVTitleGameDialog.setText("Could not find a game");
                        createGame();
                    }

                }
            }
        });
    }

    private void createGame() {

        txtVTitleGameDialog.setText("Creating game...");

        Move game = new Move();
        game.setGamer1id(mAuthProvider.getUid());

        ArrayList<Integer> cells = new ArrayList<>();
        for(int i = 0; i<9; i++){
            cells.add(0);
        }

        game.setSelectCells(cells);
        game.setTimestamp(new Date().getTime());
        game.setGameTurn(true);

        mGamesProvider.createGame(game).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                mIdGame = documentReference.getId();
                waitPlayer();
            }
        });
    }

    private void waitPlayer() {
        txtVTitleGameDialog.setText("Waiting for another player...");

        listenerRegistration = (ListenerRegistration) mGamesProvider.getGame(mIdGame).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.get("gamer2id") != null){
                    txtVTitleGameDialog.setText("player 2 added");
                    startGame();
                }
            }
        });
    }

    private void startGame() {
        mAlertDialog.dismiss();
        if (listenerRegistration != null){
            listenerRegistration.remove();
        }

        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra("idGame", mIdGame);
        startActivity(i);

        mIdGame = null;
    }

    private void cancelGame(TextView txtVTitleGameDialog) {

        txtVTitleGameDialog.setText("Cancel...");
        mGamesProvider.deleteGame(mIdGame).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(FindGameActivity.this, "Game deleted.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*
     *   LIST
     * */

    private void showListGamesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.list_games_dialog, null);
        builder.setView(view);

        AlertDialog alertDialogList = builder.create();
        alertDialogList.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogList.setCancelable(false);
        alertDialogList.show();

        CircleImageView cImgVTieGames, cImgVWinGames,
                cImgVLoseGames;

        ImageView imgVCloseListDialog, imgVRefreshListDialog;

        RecyclerView recyclerViewListGames;

        cImgVTieGames = view.findViewById(R.id.cImgVTieGames);
        cImgVWinGames = view.findViewById(R.id.cImgVWinGames);
        cImgVLoseGames = view.findViewById(R.id.cImgVLoseGames);

        imgVCloseListDialog = view.findViewById(R.id.imgVCloseListDialog);
        imgVRefreshListDialog = view.findViewById(R.id.imgVRefreshListDialog);

        recyclerViewListGames = view.findViewById(R.id.recyclerListGames);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewListGames.setLayoutManager(linearLayoutManager);

        cImgVTieGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query firstQuery = mGamesProvider.getGameTieIdPlayer2(mAuthProvider.getUid());

                firstQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() >= 1){
                            searchGames(firstQuery, recyclerViewListGames);
                        }else{
                            Query secondQuery = mGamesProvider.getGameTieIdPlayer1(mAuthProvider.getUid());
                            searchGames(secondQuery, recyclerViewListGames);
                        }
                    }
                });
            }
        });

        cImgVWinGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query = mGamesProvider.getGameWinId(mAuthProvider.getUid());
                searchGames(query, recyclerViewListGames);
            }
        });

        cImgVLoseGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                query = mGamesProvider.getGameLoseId(mAuthProvider.getUid());
                searchGames(query, recyclerViewListGames);

            }
        });

        imgVCloseListDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGameAdapter.stopListening();
                alertDialogList.dismiss();
            }
        });

        imgVRefreshListDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query firstQuery = mGamesProvider.getGameIdPlayer2(mAuthProvider.getUid());

                firstQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() >= 1){
                            searchGames(firstQuery, recyclerViewListGames);
                        }else{
                            Query secondQuery = mGamesProvider.getGameIdPlayer1(mAuthProvider.getUid());
                            searchGames(secondQuery, recyclerViewListGames);
                        }
                    }
                });

            }
        });

        Query firstQuery = mGamesProvider.getGameIdPlayer2(mAuthProvider.getUid());

        firstQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.size() >= 1){
                    searchGames(firstQuery, recyclerViewListGames);
                }else{
                    Query secondQuery = mGamesProvider.getGameIdPlayer1(mAuthProvider.getUid());
                    searchGames(secondQuery, recyclerViewListGames);
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchGames(Query query, RecyclerView recyclerViewListGames) {

        FirestoreRecyclerOptions<Move> options =
                new FirestoreRecyclerOptions.Builder<Move>()
                        .setQuery(query, Move.class)
                        .build();

        mGameAdapter = new GameAdapter(options,this);
        mGameAdapter.notifyDataSetChanged();
        recyclerViewListGames.setAdapter(mGameAdapter);
        // esto escucha los cambios que se hagan en la db
        mGameAdapter.startListening();

    }

    /*
    *   TOOLBAR
    * */

    // metodo para configuarar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // metodo para asignar acciones a los botones del menu

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOut:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(FindGameActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (listenerRegistration != null){
            listenerRegistration.remove();
        }
        if (mIdGame != null){
            mGamesProvider.deleteGame(mIdGame).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mIdGame = null;
                }
            });
        }
    }

    //metodo que se ejecuta despues de pausar un activity
    @Override
    protected void onResume() {
        super.onResume();

        if(mIdGame != null){
            mAlertDialog.show();
        }
    }
}