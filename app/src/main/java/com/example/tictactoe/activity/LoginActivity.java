package com.example.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.tictactoe.R;
import com.example.tictactoe.providers.AuthProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText txtEmailAdress, txtPassword;
    private Button btnLogin, btnRegister;
    private ScrollView formLogin;
    private ProgressBar pbLogin;

    AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuthProvider = new AuthProvider();

        txtEmailAdress = findViewById(R.id.etxtEmail);
        txtPassword = findViewById(R.id.etxtPassword);

        formLogin = findViewById(R.id.formLogin);

        pbLogin = findViewById(R.id.pbLogin);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLogin();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // SI EL USUARIO INICIO SESION SALTA EL LOGINACTIVITY
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthProvider.getUserSession() != null){
            Intent intent = new Intent(LoginActivity.this, FindGameActivity.class);
            // esto limpia el historial de acciones del usuario
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void validateLogin() {
        String email = txtEmailAdress.getText().toString();
        String password = txtPassword.getText().toString();

        if (!email.isEmpty()){
            if (isEmailValid(email)){
                if (!password.isEmpty()){

                    login(false, email, password);

                }else{
                    txtPassword.setError("Cannot be empty!");
                }
            }else{
                txtEmailAdress.setError("Wrong email!");
            }

        }else {
            txtPassword.setError("Cannot be empty!");
        }

    }

    private void login(boolean showForm, String email, String password) {

        if (!email.equals("")){
            if (!password.equals("")){
                mAuthProvider.login(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(LoginActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                                    pbLogin.setVisibility(showForm ? View.GONE : View.VISIBLE);
                                    formLogin.setVisibility(showForm ? View.VISIBLE : View.GONE);
                                    Intent intent = new Intent(LoginActivity.this, FindGameActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(LoginActivity.this, "Wrong email or password!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }else{
                txtPassword.setError("Cannot be empty");
            }
        }else{
            txtEmailAdress.setError("Cannot be empty");
        }

    }

    // validacion de un correo
    private boolean isEmailValid(String emailUser) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emailUser);
        return matcher.matches();
    }


}