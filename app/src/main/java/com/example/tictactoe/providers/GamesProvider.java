package com.example.tictactoe.providers;

import com.example.tictactoe.models.Move;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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

    public Query getByIdGame(String idGame){
        return mCollection.whereEqualTo(FieldPath.documentId(), idGame);
    }

    public Query getGameByPlayers(){
        return mCollection.whereEqualTo("gamer2id", null);
    }
}
