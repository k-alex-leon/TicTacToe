package com.example.tictactoe.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tictactoe.databinding.ActivityGameBinding;
import com.example.tictactoe.models.Move;
import com.example.tictactoe.models.User;
import com.example.tictactoe.providers.AuthProvider;
import com.example.tictactoe.providers.GamesProvider;
import com.example.tictactoe.providers.UsersProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tictactoe.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityGameBinding binding;

    GamesProvider mGameProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    List<ImageView> cells;
    TextView tvPlayer1, tvPlayer2;

    String idGame, playerOneName, playerTwoName, idWinner;
    boolean turn;
    Move game;
    User userPlayerOne, userPlayerTwo;

    // inicia con valor nulo para evitar errores
    ListenerRegistration listenerMoves = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_game);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mGameProvider = new GamesProvider();
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        tvPlayer1 = findViewById(R.id.txtVPlayer1);
        tvPlayer2 = findViewById(R.id.txtVPlayer2);

        cells = new ArrayList<>();
        cells.add((ImageView) findViewById(R.id.imgV0));
        cells.add((ImageView) findViewById(R.id.imgV1));
        cells.add((ImageView) findViewById(R.id.imgV2));
        cells.add((ImageView) findViewById(R.id.imgV3));
        cells.add((ImageView) findViewById(R.id.imgV4));
        cells.add((ImageView) findViewById(R.id.imgV5));
        cells.add((ImageView) findViewById(R.id.imgV6));
        cells.add((ImageView) findViewById(R.id.imgV7));
        cells.add((ImageView) findViewById(R.id.imgV8));

        Bundle extra = getIntent().getExtras();
        idGame = extra.getString("idGame");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_game);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        gameListener();
    }

    private void gameListener() {
        listenerMoves = mGameProvider.getGame(idGame).addSnapshotListener(GameActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Toast.makeText(GameActivity.this, "Error with game data", Toast.LENGTH_SHORT).show();
                    return;
                }
                // verificando si el cambio es local o de servidor

                String source = value != null && value.getMetadata().hasPendingWrites() ? "local" : "server";

                if (value != null){
                    // convierte docsnapshot a objeto jugada
                    game = value.toObject(Move.class);
                    if (playerOneName == null || playerTwoName == null){
                        getPlayerNames();
                    }
                    
                    updateUI();
                }
                
                changePlayerColor();
            }
        });
    }

    // cambiando color de jugador segun el turno
    private void changePlayerColor() {
        if (turn){
            tvPlayer2.setTextColor(Color.GRAY);
            tvPlayer1.setTextColor(Color.GREEN);
        }else{
            tvPlayer1.setTextColor(Color.GRAY);
            tvPlayer2.setTextColor(Color.GREEN);
        }

        if (game.getIdWinner() != null){
            idWinner = game.getIdWinner();
            showAlertDialog();
        }

    }

    // refrescar y mostrar cambios en la interfaz
    private void updateUI() {
        //obteniendo los valores de las celdas
        for (int i = 0; i<9; i++){

            int cell = game.getSelectCells().get(i);
            ImageView imgVCell = cells.get(i);

            // cambiando img dependiendo el valor
            if (cell == 0){
                imgVCell.setImageResource(R.drawable.ic_square);
            }else if (cell == 1){
                imgVCell.setImageResource(R.drawable.ic_x);
            }else if (cell == 2){
                imgVCell.setImageResource(R.drawable.ic_o);
            }
        }

    }

    // obteniendo data de los jugadores
    private void getPlayerNames() {

        // obtener player 1

        mUsersProvider.getUser(game.getGamer1id())
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // obteniendo en objeto user todos los datos del usuario
                userPlayerOne = documentSnapshot.toObject(User.class);
                playerOneName = documentSnapshot.get("username").toString();
                tvPlayer1.setText(playerOneName);
            }
        });

        // obtener player 2

        mUsersProvider.getUser(game.getGamer2id())
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // obteniendo en objeto user todos los datos del usuario
                userPlayerTwo = documentSnapshot.toObject(User.class);
                playerTwoName = documentSnapshot.get("username").toString();
                tvPlayer2.setText(playerTwoName);
            }
        });
    }


    // si la activity es pausada se elimina el listener
    @Override
    protected void onStop() {
        if (listenerMoves != null){
            listenerMoves.remove();
        }
        super.onStop();
    }

    public void cellClicked(View view) {
        if (game.getIdWinner() != null){
            Toast.makeText(this, "Game over", Toast.LENGTH_SHORT).show();
        }else{
            turn = game.getGameTurn();
            if (turn && game.getGamer1id().equals(mAuthProvider.getUid())){
                // juega player 1
                //llamaos al tag de los imageview seleccionados
                updateGame(view.getTag().toString());
            }
            else if(!turn && game.getGamer2id().equals(mAuthProvider.getUid())){
                // juega player 2
                updateGame(view.getTag().toString());
            }
            else{
                Toast.makeText(this, "Wait you're turn.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateGame(String cellNumber) {
        int cellPosition = Integer.parseInt(cellNumber);

        if (game.getSelectCells().get(cellPosition) != 0){
            Toast.makeText(this, "Select a free cell", Toast.LENGTH_SHORT).show();
        }else {

            if (turn) {
                cells.get(cellPosition).setImageResource(R.drawable.ic_x);
                // pasamos valor de 1 al campo seleccionado por player 1
                game.getSelectCells().set(cellPosition, 1);
            } else {
                cells.get(cellPosition).setImageResource(R.drawable.ic_o);
                // pasamos valor de 1 al campo seleccionado por player 1
                game.getSelectCells().set(cellPosition, 2);
            }

            if (testresult()){
                game.setIdWinner(mAuthProvider.getUid());
            }else if (testTie()){
                game.setIdWinner("TIE");
            }


            game.setGameTurn(!turn);

            // actualizar datos de partida

            mGameProvider.setGame(idGame, game).addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            });

        }
    }

    public boolean testTie(){
        boolean existResult = false;
        //examinar empate
        boolean emptyCell = false;
        for (int i = 0; i<9; i++){
            if (game.getSelectCells().get(i) == 0){
                emptyCell = true;
                break;
            }
        }
        if (!emptyCell){
            existResult = true;
        }

        return existResult;
    }

    // verificando si ya existe un ganador
    public boolean testresult(){
        boolean existResult = false;


            List<Integer> selectCells = game.getSelectCells();

            // 0-1-2
            if (selectCells.get(0) == selectCells.get(1)
                && selectCells.get(1) == selectCells.get(2)
                && selectCells.get(2) != 0){
                existResult = true;
            }
            // 3-4-5
            else if(selectCells.get(3) == selectCells.get(4)
                    && selectCells.get(4) == selectCells.get(5)
                    && selectCells.get(5) != 0){
                existResult = true;
            }
            //6-7-8
            else if(selectCells.get(6) == selectCells.get(7)
                    && selectCells.get(7) == selectCells.get(8)
                    && selectCells.get(8) != 0){
                existResult = true;
            }
            //0-4-8
            else if(selectCells.get(0) == selectCells.get(4)
                    && selectCells.get(4) == selectCells.get(8)
                    && selectCells.get(8) != 0){
                existResult = true;
            }
            //3-5-6
            else if(selectCells.get(2) == selectCells.get(4)
                    && selectCells.get(4) == selectCells.get(6)
                    && selectCells.get(6) != 0){
                existResult = true;
            }
            //0-3-6
            else if(selectCells.get(0) == selectCells.get(3)
                    && selectCells.get(3) == selectCells.get(6)
                    && selectCells.get(6) != 0){
                existResult = true;
            }
            //1-4-7
            else if(selectCells.get(1) == selectCells.get(4)
                    && selectCells.get(4) == selectCells.get(7)
                    && selectCells.get(7) != 0){
                existResult = true;
            }
            //2-5-8
            else if(selectCells.get(2) == selectCells.get(5)
                    && selectCells.get(5) == selectCells.get(8)
                    && selectCells.get(8) != 0){
                existResult = true;
            }

        return existResult;
    }

    public void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);

        View view = getLayoutInflater().inflate(R.layout.gameover_dialog, null);
        // obteniendo referencias del layout
        TextView tvPoints = view.findViewById(R.id.txtVPlayerPoints);
        TextView tvPlayerInfo = view.findViewById(R.id.txtVPlayerInfo);
        LottieAnimationView gameOverAnimation = view.findViewById(R.id.animationGameOver);

        builder.setTitle("GAME OVER");
        // no permite que el usuario pueda quitar la alerta
        builder.setCancelable(false);
        // pasamos el layout que ve el usuario
        builder.setView(view);

        //si la partida resulta en empate
        if (idWinner.equals("TIE")){
            updatePlayerPoints(1);
            gameOverAnimation.setAnimation("action_sword.json");
            tvPoints.setText("+1 points");
            if (game.getGamer1id().equals(mAuthProvider.getUid())){
                tvPlayerInfo.setText(playerOneName +" tie with "+ playerTwoName);
            }else{
                tvPlayerInfo.setText(playerTwoName +" tie with "+ playerOneName);
            }

        }
        // jugador ganador
        else if (idWinner.equals(mAuthProvider.getUid())){
            updatePlayerPoints(3);
            tvPlayerInfo.setText("You win!");
            tvPoints.setText("+3 points");
        }
        // jugador perdedor
        else{
            updatePlayerPoints(0);
            tvPlayerInfo.setText("You lose!");
            tvPoints.setText("0 points");
            gameOverAnimation.setAnimation("warning.json");
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updatePlayerPoints(int points) {
        User updatePlayer = null;
        if (mAuthProvider.getUid().equals(userPlayerOne.getId())){
            // incrementando los puntos del usuario
            userPlayerOne.setPoints(userPlayerOne.getPoints() + points);
            userPlayerOne.setGames(userPlayerOne.getGames() + 1);
            updatePlayer = userPlayerOne;
        }
        else if (mAuthProvider.getUid().equals(userPlayerTwo.getId())){

            userPlayerTwo.setPoints(userPlayerTwo.getPoints() + points);
            userPlayerTwo.setGames(userPlayerTwo.getGames() + 1);
            updatePlayer = userPlayerTwo;
        }

        mUsersProvider.setPoints(updatePlayer).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                // Por ahora no es necesario realizar ninguna accion

            }
        });

    }


}