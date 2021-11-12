package com.example.tictactoe.providers;

import com.example.tictactoe.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class UsersProvider {

    private CollectionReference mCollection;

    public UsersProvider(){ mCollection = FirebaseFirestore.getInstance().collection("Users");}

    public Task<Void> create(User user){
        return mCollection.document(user.getId()).set(user);
    }

    public Task<DocumentSnapshot> getUser(String idUser){
        return mCollection.document(idUser).get();
    }

    public Task<Void> setPoints(User user){
        return mCollection.document(user.getId()).set(user);
    }
}
