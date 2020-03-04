package com.example.pantryscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText email;
    private Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();

        reset = findViewById(R.id.reset);
        email = findViewById(R.id.email);

        final String email = getIntent().getStringExtra("FORGOT_PASSWORD_STRING");

        final Context context = this;
        final Activity activity = this;

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.isEmpty()) {
                    Toast.makeText(context, "Enter email", Toast.LENGTH_LONG).show();
                }
            else

            {
                auth.sendPasswordResetEmail(email);
                Toast.makeText(context, "Email sent", Toast.LENGTH_LONG).show();
                        /*.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Intent intent = new Intent(activity, LoginActivity.class);
                            startActivity(intent);
                            finish();

                            FirebaseUser user = auth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(context, "Unable to send reset mail", Toast.LENGTH_LONG).show();
                        }
                    }

                });*/
            }
            }
        });
    }
}
