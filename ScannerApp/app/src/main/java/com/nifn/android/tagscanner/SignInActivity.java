package com.nifn.android.tagscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPassword;

    private Button mSignIn_btn;
    private Button mRegister_btn;
    private Button mBack_btn;

    private ProgressBar mLoadingProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email_editTxt);
        mPassword = findViewById(R.id.password_editTxt);

        mSignIn_btn = findViewById(R.id.signin_btn);
        mSignIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEmpty()) return;
                inProgress(true);
                mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String userId = mAuth.getCurrentUser().getUid();
                                Toast.makeText(SignInActivity.this, "User signed in " + userId, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish(); return;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        inProgress(false);
                        Toast.makeText(SignInActivity.this, "Sign in failed!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        mRegister_btn = findViewById(R.id.register_btn);
        mRegister_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEmpty()) return;
                inProgress(true);
                mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(SignInActivity.this, "User registered successfully", Toast.LENGTH_LONG).show();
                                inProgress(false);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        inProgress(false);
                        Toast.makeText(SignInActivity.this, "Registration failed!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        mBack_btn = findViewById(R.id.back_btn);
        mBack_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); return;
            }
        });

        mLoadingProgress = findViewById(R.id.loading_progressBar);
    }

    private void inProgress(boolean inProgress){
        if(inProgress){
            mLoadingProgress.setVisibility(View.VISIBLE);
            mBack_btn.setEnabled(false);
            mRegister_btn.setEnabled(false);
            mSignIn_btn.setEnabled(false);
        } else {
            mLoadingProgress.setVisibility(View.GONE);
            mBack_btn.setEnabled(true);
            mRegister_btn.setEnabled(true);
            mSignIn_btn.setEnabled(true);
        }
    }

    private boolean isEmpty(){
        if(TextUtils.isEmpty(mEmail.getText().toString())){
            mEmail.setError("REQUIRED!");
            return true;
        }
        if(TextUtils.isEmpty(mPassword.getText().toString())){
            mPassword.setError("REQUIRED!");
            return true;
        }
        return false;
    }
}

