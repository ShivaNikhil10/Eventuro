package com.dsce.eventmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {


    EditText email;
    Button VerifyBtn;
    Button Cancel;

    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mFirebaseAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.editTextVerifyEmail);
        VerifyBtn = findViewById(R.id.buttonVerify);
        Cancel = findViewById(R.id.buttonCancelRegister);

        if(mFirebaseAuth.getCurrentUser()==null){
            Toast.makeText(EmailVerificationActivity.this,"Please Login/Register.",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(EmailVerificationActivity.this, MainActivity.class);
            startActivity(i);
            EmailVerificationActivity.this.finish();
        }
        else{
            email.setText(mFirebaseAuth.getCurrentUser().getEmail());
        }
        if(mFirebaseAuth.getCurrentUser().isEmailVerified()){
            //Toast.makeText(EmailVerificationActivity.this,"Email verified :auto",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(EmailVerificationActivity.this, HomeActivity.class);
            startActivity(i);
            EmailVerificationActivity.this.finish();
        }
        else {
            Toast.makeText(EmailVerificationActivity.this,"Please read the conditions and verify your mail.",Toast.LENGTH_SHORT).show();
        }

        VerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFirebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                boolean verified=user.isEmailVerified();
                if(user.isEmailVerified()){
                    //Toast.makeText(EmailVerificationActivity.this,"Email Verified",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(EmailVerificationActivity.this, HomeActivity.class);
                    startActivity(i);
                    EmailVerificationActivity.this.finish();
                }
                else{
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(EmailVerificationActivity.this,"Verification email sent.",Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                            startActivity(i);
                            EmailVerificationActivity.this.finish();
                        }
                    });
                }

            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog= new AlertDialog.Builder(EmailVerificationActivity.this).create();
                alertDialog.setTitle("Registration");
                alertDialog.setMessage("User cannot sign in without being verified.Press cancel to verify this account.Please verify this account or create an account that you would like to verify.You can login using an already verified account directly.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        //Toast.makeText(EmailVerificationActivity.this,"User has been successfully yeeted.",Toast.LENGTH_SHORT).show();
                        Intent intToMain = new Intent(EmailVerificationActivity.this, MainActivity.class);
                        startActivity(intToMain);
                        EmailVerificationActivity.this.finish();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EmailVerificationActivity.this,"You have cancelled your cancellation :)",Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        });

    }
}
