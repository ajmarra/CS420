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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText email;
    private EditText password;
    private TextView forgotPassword;

    private Button registerBtn;
    private Button loginBtn;
    private SignInButton googleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgotPassword = findViewById(R.id.forgot_password);

        registerBtn = findViewById(R.id.register_button);
        loginBtn = findViewById(R.id.login_button);

        final Context context = this;
        final Activity activity = this;

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            // user already logged in, switch to main activity
            Toast.makeText(context, "Logged in", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, PantryActivity.class);
            startActivity(intent);
            finish();
        }

        /*googleBtn = findViewById(R.id.sign_in_button);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button:
                        Intent signInIntent = client.getSignInIntent();
                        startActivityForResult(signInIntent, RC_SIGN_IN);
                        break;
                    // ...
                }
            }
        });*/

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emails = email.getText().toString();
                String password1 = password.getText().toString();

                //email signin
                if(emails.isEmpty() || password1.isEmpty()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show();
                }
                else {
                    auth.signInWithEmailAndPassword(emails, password1).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(context, "Logged in", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                                finish();

                                FirebaseUser user = auth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(context, "Login failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                    });
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ResetPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


}
