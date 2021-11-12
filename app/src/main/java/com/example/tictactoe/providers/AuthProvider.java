package com.example.tictactoe.providers;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthProvider {

    private FirebaseAuth mAuth;

    public AuthProvider() {
        mAuth = FirebaseAuth.getInstance();
    }


    //retorna para el RegisterActivity la cuenta registrada
    public Task<AuthResult> register(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    //retorna para el loginActivity la cuenta registrada
    public Task<AuthResult> login(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    // retornar el id del usuario para el registerActivity
    public String getUid() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        } else {
            return null;
        }
    }

    public FirebaseUser getUserSession(){
        if (mAuth.getCurrentUser() != null){
            return mAuth.getCurrentUser();
        }else{
            return null;
        }

    }

    public  String getEmail(){
        if (mAuth.getCurrentUser() != null){
            return mAuth.getCurrentUser().getEmail();
        }else{
            return null;
        }
    }

    public  void logout(){
        if (mAuth != null){
            mAuth.signOut();
        }else{

        }

    }
}