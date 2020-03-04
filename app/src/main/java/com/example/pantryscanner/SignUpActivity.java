package com.example.pantryscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private TextView account;

    private Button signUpBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password1);
        confirmPassword = findViewById(R.id.password2);

        signUpBtn = findViewById(R.id.confirmButton);
        account = findViewById(R.id.preexisting_account);

        auth = FirebaseAuth.getInstance();

        final Context context = this;
        final Activity activity = this;

        //register the user when they sign up
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emails = email.getText().toString();
                String password1 = password.getText().toString();
                String password2 = confirmPassword.getText().toString();

                if(emails.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show();
                }
                else if(!password1.equals(password2)) {
                    Toast.makeText(context, "The passwords do not match", Toast.LENGTH_LONG).show();
                }
                else {
                    auth.createUserWithEmailAndPassword(emails, password1).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(context, "Signed up!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                    }).addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                                // If sign in fails, display a message to the user.
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(context, "Invalid password", Toast.LENGTH_LONG).show();
                            } else if (e instanceof FirebaseAuthInvalidUserException) {
                                Toast.makeText(context, "Incorrect email address", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();                            }
                        }
                    });
                }
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}
