package com.dsce.eventmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{

    EditText emailId, password;
    Button btnLogin;
    TextView tvSignUp;
    TextView tvForgotPassword;
    ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    int FLAG=1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvSignUp = findViewById(R.id.textViewRegister);
        tvForgotPassword = findViewById(R.id.textViewForgotPassword);
        progressBar=findViewById(R.id.LoginProgressBar);

        mAuthStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if(FLAG==1){
                    FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                    if( mFirebaseUser != null )
                    {
                        if(mFirebaseUser.isEmailVerified()){
                            //Toast.makeText(LoginActivity.this,"You are logged in",Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(LoginActivity.this, EmailVerificationActivity.class);
                            startActivity(i);
                            LoginActivity.this.finish();
                        }
                        else{
                            Toast.makeText(LoginActivity.this,"Please create and verify your mail.\nIf mail has already been created, please wait for it to get verified.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,"Please Login",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                String pwd = password.getText().toString();
                if(email.isEmpty()){
                    emailId.setError("Please enter a email id");
                    emailId.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailId.setError("Please enter a valid email id");
                    emailId.requestFocus();
                }
                else  if(pwd.isEmpty()){
                    password.setError("Please enter a password");
                    password.requestFocus();
                }
                else if(pwd.length()<6){
                    password.setError("Please enter a 6+ char password");
                    password.requestFocus();
                }
                else  if(email.isEmpty() && pwd.isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"Fields Are Empty!",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    FLAG=0;
                    progressBar.setVisibility(View.VISIBLE);
                    mFirebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            progressBar.setVisibility(View.INVISIBLE);
                            if(!task.isSuccessful())
                            {
                                if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                    Toast.makeText(LoginActivity.this,"User does not exist.",Toast.LENGTH_SHORT).show();
                                }
                                else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                    Toast.makeText(LoginActivity.this,"Incorrect password.",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(LoginActivity.this,"Login Error, Please Login Again",Toast.LENGTH_SHORT).show();

                                }
                            }
                            else
                            {
                                Intent intToHome = new Intent(LoginActivity.this,EmailVerificationActivity.class);
                                startActivity(intToHome);
                                LoginActivity.this.finish();
                            }
                        }
                    });
                }

            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intSignUp = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intSignUp);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intSignUp = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intSignUp);
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

}
