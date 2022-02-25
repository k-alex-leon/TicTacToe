package com.example.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tictactoe.R;
import com.example.tictactoe.models.User;
import com.example.tictactoe.providers.AuthProvider;
import com.example.tictactoe.providers.UsersProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    ImageView imgVGoBack;
    Button btnRegisterUser;
    TextInputEditText txtUsername, txtEmail, txtPassword, txtConfirmPassword;
    ProgressBar pbRegisterUser;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Obtenemos medidas de window y asignamos el tamaño de popup
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int heigth = metrics.heightPixels;
        getWindow().setLayout((int)(width * 0.9), (int)(heigth*0.8));
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //providers
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        imgVGoBack = findViewById(R.id.imgGoBack);

        txtUsername = findViewById(R.id.etxtUsername);
        txtEmail = findViewById(R.id.etxtEmail);
        txtPassword = findViewById(R.id.etxtPassword);
        txtConfirmPassword = findViewById(R.id.etxtConfirmPassword);
        pbRegisterUser = findViewById(R.id.pbRegister);

        // click en el boton de registro == validateData();
        btnRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        imgVGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // validando los datos registrados por el usuario
    private void validateData() {
        String username = txtUsername.getText().toString();
        String email = txtEmail.getText().toString();
        String pass = txtPassword.getText().toString();
        String confirmPass = txtConfirmPassword.getText().toString();

        if (!username.isEmpty() && !email.isEmpty() &&
                !pass.isEmpty() && !confirmPass.isEmpty()){

            //validacion de correo
            if (isEmailValid(email)){
                // validacion de password semejante
                if(pass.contentEquals(confirmPass)){
                    //validando longitud de pass
                    if (pass.length() > 6 && confirmPass.length() > 6){
                        createUser(username, email, pass);
                    }else{
                        txtPassword.setError("Too short");
                        txtConfirmPassword.setError("Too short");
                    }

                }else{
                    txtPassword.setError("Must be the same");
                    txtConfirmPassword.setError("Must be the same");
                }
            }
            else{
                txtEmail.setError("Wrong email.");
            }


        }
        else{
            Toast.makeText(this, "Cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    // creando usuario despues de validacion
    private void createUser(String username, String email, String pass) {

        // primero llamar al auth para crear la autenticacion luego el user provider para crear coleccion
        mAuthProvider.register(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    User user = new User();
                    user.setId(mAuthProvider.getUid());
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setTimestamp(new Date().getTime());

                    mUsersProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                pbRegisterUser.setVisibility(View.VISIBLE);
                                Toast.makeText(RegisterActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, FindGameActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(RegisterActivity.this, "Error creating user", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                }
                else{
                    Toast.makeText(RegisterActivity.this, "Error creating user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // validacion de un correo
    private boolean isEmailValid(String emailUser) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emailUser);
        return matcher.matches();
    }
}