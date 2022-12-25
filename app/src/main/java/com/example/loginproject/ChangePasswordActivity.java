package com.example.loginproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText editTextPwdCurr, editTextPwdNew, editTextPwdConfirmNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userPwdCurr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("비밀번호 변경");

        editTextPwdNew = findViewById(R.id.editText_change_pwd_new);
        editTextPwdCurr = findViewById(R.id.editText_change_pwd_current);
        editTextPwdConfirmNew = findViewById(R.id.editText_change_pwd_new_confirm);
        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticated);
        progressBar = findViewById(R.id.progressBar);
        buttonReAuthenticate = findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd = findViewById(R.id.button_change_pwd);

        editTextPwdNew.setEnabled(false);
        editTextPwdConfirmNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        ImageView imageViewShowHideCureentPwd = findViewById(R.id.imageView_show_hide_new_pwd);
        imageViewShowHideCureentPwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHideCureentPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextPwdCurr.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    editTextPwdCurr.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHideCureentPwd.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextPwdCurr.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHideCureentPwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        ImageView imageViewShowNewPwd = findViewById(R.id.imageView_show_hide_new_pwd);
        imageViewShowNewPwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowNewPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextPwdNew.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    editTextPwdNew.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowNewPwd.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextPwdNew.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowNewPwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        ImageView imageViewShowNewPwdConfirm = findViewById(R.id.imageView_show_hide_new_pwd_confirm);
        imageViewShowNewPwdConfirm.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowNewPwdConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextPwdConfirmNew.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    editTextPwdConfirmNew.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowNewPwdConfirm.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextPwdConfirmNew.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowNewPwdConfirm.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        if(firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this, "오류", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ChangePasswordActivity.this, UserProfileActivity.class));
            finish();
        }else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwdCurr = editTextPwdCurr.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this, "비밀번호가 필요합니다", Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("인증을 위해 현재 비밀번호를 입력해주세요");
                    editTextPwdCurr.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                editTextPwdCurr.setEnabled(true);
                                editTextPwdNew.setEnabled(true);
                                editTextPwdConfirmNew.setEnabled(true);

                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePwd.setEnabled(true);

                                textViewAuthenticated.setText("인증되었습니다" + "\n"+ "비밀번호를 변경할 수 있습니다");
                                Toast.makeText(ChangePasswordActivity.this, "인증되었습니다" +"\n"+ "비밀번호를 변경할 수 있습니다", Toast.LENGTH_SHORT).show();

                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePasswordActivity.this, R.color.dark_green));

                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }else {
                                try{
                                    throw task.getException();
                                }catch (Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = editTextPwdConfirmNew.getText().toString();

        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "새 비밀번호가 필요합니다", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("새 비밀번호가 필요합니다");
            editTextPwdNew.requestFocus();
        }else if(TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this, "비밀번호를 한번 더 입력해주세요", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("비밀번호를 한번 더 입력해주세요");
            editTextPwdConfirmNew.requestFocus();
        }else if (!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("비밀번호가 일치하지 않습니다");
            editTextPwdConfirmNew.requestFocus();
            }else if(userPwdCurr.matches(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "이전 비밀번호와 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("이전 비밀번호와 일치하지 않습니다");
            editTextPwdConfirmNew.requestFocus();
        }else {
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ChangePasswordActivity.this, UserProfileActivity.class));
                        finish();
                    }else {
                        try{
                            task.getException();
                        }catch (Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        }else if (id == R.id.menu_update_profile){
            startActivity(new Intent(ChangePasswordActivity.this, UpdateProfileActivity.class));
            finish();
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(ChangePasswordActivity.this, UpdateEmailActivity.class));
        }else if (id == R.id.menu_settings){
            Toast.makeText(ChangePasswordActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class));
            finish();
        }else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(ChangePasswordActivity.this, DeleteProfileActivity.class));
            finish();
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(ChangePasswordActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(ChangePasswordActivity.this, "오류", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
