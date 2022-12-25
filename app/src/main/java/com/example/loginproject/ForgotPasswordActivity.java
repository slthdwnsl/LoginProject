package com.example.loginproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button buttonPwdReset;
    private EditText editTextPwdResetEmail;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().setTitle("비밀번호 찾기");

        editTextPwdResetEmail = findViewById(R.id.editText_password_reset_email);
        buttonPwdReset = findViewById(R.id.button_password_reset);
        progressBar = findViewById(R.id.progressBar);

        buttonPwdReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextPwdResetEmail.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(ForgotPasswordActivity.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmail.setError("이메일을 입력해주세요");
                    editTextPwdResetEmail.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(ForgotPasswordActivity.this, "유효한 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmail.setError("유효한 이메일을 입력해주세요");
                    editTextPwdResetEmail.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "이메일로 링크가 전송되었습니다.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        editTextPwdResetEmail.setError("이메일이 존재하지 않거나 유효하지 않습니다, 다시 등록해주세요");
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
