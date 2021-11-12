package com.example.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.tictactoe.R;
import com.example.tictactoe.models.Move;
import com.example.tictactoe.providers.AuthProvider;
import com.example.tictactoe.providers.GamesProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class FindGameActivity extends AppCompatActivity {

    TextView txtLoadingGame;
    ProgressBar progressBar;
    Toolbar toolbar;
    AuthProvider mAuthProvider;
    GamesProvider mGamesProvider;
    LinearLayout layoutLoading, layoutMenu;

    ListenerRegistration listenerRegistration = null;

    Button btnGame, btnScore, btnCancel;

    String idGame = null, idGamer1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);

        mAuthProvider = new AuthProvider();
        mGamesProvider = new GamesProvider();

        txtLoadingGame = findViewById(R.id.tvLoadingGame);
        progressBar = findViewById(R.id.pbLoadingGame);

        layoutLoading = findViewById(R.id.contentLoading);
        layoutMenu = findViewById(R.id.contentLobbyGame);

        //declaracion del toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // buscar partida a jugar
        btnGame = findViewById(R.id.btnFindGame);
        btnGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeVisibility(false);
                searchFreeGame();
            }
        });

        btnScore = findViewById(R.id.btnSeeScore);
        btnScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FindGameActivity.this, "falta por trabajar", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel = findViewById(R.id.btnCancelGame);
        btnCancel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cancelGame();
                return false;
            }
        });


        startProgressBar();
    }


    private void searchFreeGame() {
        txtLoadingGame.setText("Looking for a game...");


        mGamesProvider.getGameByPlayers().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().size() == 0){

                    createGame();

                }else{
                    txtLoadingGame.setText("Adding to game...");
                    DocumentSnapshot docGame = task.getResult().getDocuments().get(0);
                    idGame = docGame.getId();

                    Move game = docGame.toObject(Move.class);
                    game.setGamer2id(mAuthProvider.getUid());
                    game.setIdGame(idGame);
                    idGamer1 = game.getGamer1id();

                    // buscando partida con un id user distinto al creador
                    if (!idGamer1.equals(mAuthProvider.getUid())){

                        mGamesProvider.addPlayer2(idGame, game).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                startGame();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                changeVisibility(true);
                                Toast.makeText(FindGameActivity.this, "Error adding player to game!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }else{
                        // si no encuentra partida, crea una
                        txtLoadingGame.setText("Could not find a game");
                        createGame();
                    }

                }
            }
        });
    }

    private void createGame() {

        txtLoadingGame.setText("Creating game...");

        Move game = new Move();
        game.setGamer1id(mAuthProvider.getUid());
        ArrayList<Integer> cells = new ArrayList<>();
        for(int i = 0; i<9; i++){
            cells.add(new Integer(0));
        }
        game.setSelectCells(cells);
        game.setTimestamp(new Date().getTime());
        game.setGameTurn(true);
        mGamesProvider.createGame(game).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                idGame = documentReference.getId();
                waitPlayer();
            }
        });
    }

    private void waitPlayer() {
        txtLoadingGame.setText("Waiting for another player...");

        listenerRegistration = (ListenerRegistration) mGamesProvider.getGame(idGame).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.get("gamer2id") != null){
                    txtLoadingGame.setText("player 2 added");
                    startGame();
                }
            }
        });
    }

    private void startGame() {

        if (listenerRegistration != null){
            listenerRegistration.remove();
        }

        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra("idGame", idGame);
        startActivity(i);

        idGame = null;
    }

    private void cancelGame() {

        txtLoadingGame.setText("Cancel");
        mGamesProvider.deleteGame(idGame).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                changeVisibility(true);
            }
        });
    }

    // valor asignado para que gire al iniciar el activity
    private void startProgressBar() {
        progressBar.setIndeterminate(true);
        txtLoadingGame.setText("Loading game...");

        changeVisibility(true);
    }

    private void changeVisibility(boolean showMenu) {
        layoutLoading.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        layoutMenu.setVisibility(showMenu ? View.VISIBLE : View.GONE);
    }

    //metodo que se ejecuta despues de pausar un activity
    @Override
    protected void onResume() {
        super.onResume();

       if (idGame != null){
           changeVisibility(false);
           searchFreeGame();
       }else{
           changeVisibility(true);
       }
    }

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
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null){
            listenerRegistration.remove();
        }

        if (idGame != null){
            mGamesProvider.deleteGame(idGame).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    idGame = "";
                }
            });
        }
    }
}