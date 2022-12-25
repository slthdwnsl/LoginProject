package com.example.loginproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextLoginEmail, editTextLoginPwd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("로그인");

        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextLoginPwd = findViewById(R.id.editText_login_pwd);
        progressBar = findViewById(R.id.progressBar);

        authProfile = FirebaseAuth.getInstance();

        TextView textViewLinkResetPwd = findViewById(R.id.textView_forgot_password_link);
        textViewLinkResetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "비밀번호를 재설정 할 수 있습니다", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

        TextView textViewLinkRegister = findViewById(R.id.textView_register_link);
        textViewLinkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        ImageView imageViewShowHidePwd = findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextLoginPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    editTextLoginPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextLoginPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextLoginPwd.getText().toString();

                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("이메일을 입력해주세요");
                    editTextLoginEmail.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(LoginActivity.this, "이메일을 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("이메일을 다시 입력해주세요");
                    editTextLoginEmail.requestFocus();
                }else if(TextUtils.isEmpty(textPwd)){
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("비밀번호를 입력해주세요");
                    editTextLoginEmail.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail, textPwd);
                }
            }
        });
    }

    private void loginUser(String email, String pwd) {
        authProfile.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginActivity.this, "로그인 완료", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
                        finish();
                    }else {
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        showAlertDialog();
                    }
                }else {
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        editTextLoginEmail.setError("사용자가 존재하지 않거나 유효하지 않습니다. 다시 회원가입 하십시오");
                        editTextLoginEmail.requestFocus();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        editTextLoginEmail.setError("확인 후 재입력해주세요");
                        editTextLoginEmail.requestFocus();
                    }catch (Exception e){
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(LoginActivity.this, "로그인 오류", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("이메일이 유효하지 않습니다");
        builder.setMessage("이메일이 유효한지 확인하십시오, 유효하지 않으면 로그인 할 수 없습니다.");

        builder.setPositiveButton("계속", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(authProfile.getCurrentUser() != null){
            Toast.makeText(LoginActivity.this, "이미 로그인 되어있습니다", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish();
        }else {
            Toast.makeText(LoginActivity.this, "이제 로그인 할 수 있습니다", Toast.LENGTH_SHORT).show();
        }
    }
}
