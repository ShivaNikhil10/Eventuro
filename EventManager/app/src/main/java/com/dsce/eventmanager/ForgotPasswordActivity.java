package com.dsce.eventmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailId;
    Button btnForgot;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.editTextForgetMail);
        btnForgot = findViewById(R.id.btnForgot);

        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                if(email.isEmpty())
                {
                    emailId.setError("Please enter an email ID.");
                    emailId.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailId.setError("Please enter a valid email ID.");
                    emailId.requestFocus();
                }
                else
                {
                    mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            AlertDialog alertDialog= new AlertDialog.Builder(ForgotPasswordActivity.this).create();
                            alertDialog.setTitle("Processing Request");
                            alertDialog.setMessage("This might take some time.Please check your mail.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent backtoLogin=new Intent(ForgotPasswordActivity.this,LoginActivity.class);
                                    startActivity(backtoLogin);
                                }
                            });
                            alertDialog.show();

                            if(task.isSuccessful()){
                                Toast.makeText(ForgotPasswordActivity.this,"Reset password mail sent.",Toast.LENGTH_SHORT).show();
                                Intent backtoLogin = new Intent(ForgotPasswordActivity.this,LoginActivity.class);
                                startActivity(backtoLogin);
                            }
                            else{
                                Toast.makeText(ForgotPasswordActivity.this,"Could not send mail,please try again",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}
