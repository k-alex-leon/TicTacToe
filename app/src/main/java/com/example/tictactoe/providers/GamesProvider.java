package com.example.tictactoe.providers;

import com.example.tictactoe.models.Move;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GamesProvider {

    private CollectionReference mCollection;

    public GamesProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("Games");
    }

    public Task<DocumentReference> createGame(Move move){
        return mCollection.add(move);
    }

    public Task<Void> deleteGame(String idGame){
        return mCollection.document(idGame).delete();
    }

    public Task<Void> addPlayer2(String idGame, Move move ){
        return mCollection.document(idGame).set(move);
    }

    public Task<Void> setGame(String idGame, Move move){
        return mCollection.document(idGame).set(move);
    }
    public DocumentReference getGame(String idGame){
        return mCollection.document(idGame);
    }


    public Query getGameTieIdPlayer1(String idPlayer){
        return mCollection.whereEqualTo("gamer1id", idPlayer).whereEqualTo("idWinner", "TIE");
    }

    public Query getGameTieIdPlayer2(String idPlayer){
        return mCollection.whereEqualTo("gamer2id", idPlayer).whereEqualTo("idWinner", "TIE");
    }

    public Query getGameWinId(String idPlayer){
        return mCollection.whereEqualTo("idWinner", idPlayer);
    }

    public Query getGameLoseId(String idPlayer){
        return mCollection.whereEqualTo("idLoser", idPlayer);
    }

    public Query getGameByPlayers(){
        return mCollection.whereEqualTo("gamer2id", null);
    }

    public Task<Void> updateLeavePlayer(String idGame, String idPlayerLeave, String idWinner){
        Map<String,Object> map = new HashMap<>();
        map.put("idExitGame" , idPlayerLeave);
        map.put("idLoser", idPlayerLeave);
        map.put("idWinner", idWinner);

     return mCollection.document(idGame).update(map);
    }
}
