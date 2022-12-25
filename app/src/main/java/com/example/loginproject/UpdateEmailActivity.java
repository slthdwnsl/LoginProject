package com.example.loginproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String userOldEmail, userNewEmail, userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail, editTextPwd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("이메일 변경");

        progressBar = findViewById(R.id.progressBar);
        editTextPwd = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);
        textViewAuthenticated = findViewById(R.id.textView_update_email_authenticated);
        buttonUpdateEmail = findViewById(R.id.button_update_email);

        buttonUpdateEmail.setEnabled(false);
        editTextNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textView_update_email_old);
        textViewOldEmail.setText(userOldEmail);

        if(firebaseUser.equals("")){
            Toast.makeText(UpdateEmailActivity.this, "오류", Toast.LENGTH_LONG).show();
        }else {
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.button_authenticate_user);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwd = editTextPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmailActivity.this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("패스워드를 입력해주세요");
                    editTextPwd.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(UpdateEmailActivity.this, "인증되었습니다" + "이메일을 업데이트 할 수 있습니다", Toast.LENGTH_LONG).show();

                                textViewAuthenticated.setText("인증되었습니다, 이메일을 업데이트 할 수 있습니다");

                                editTextNewEmail.setEnabled(true);
                                editTextPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,R.color.dark_green));

                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewEmail = editTextNewEmail.getText().toString();
                                        if (TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "새 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("새 이메일을 입력해주세요");
                                            editTextNewEmail.requestFocus();
                                        }else if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this, "유효한 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("유효한 이메일을 입력해주세요");
                                            editTextNewEmail.requestFocus();
                                        }else if(userOldEmail.matches(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "전 이메일과 중복됩니다, 새로운 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("전 이메일과 중복됩니다, 새로운 이메일을 입력해주세요");
                                            editTextNewEmail.requestFocus();
                                        }else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }else {
                                try{
                                    throw task.getException();
                                }catch (Exception e){
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    firebaseUser.sendEmailVerification();

                    Toast.makeText(UpdateEmailActivity.this, "이메일이 업데이트 되었습니다, 새 이메일을 확인해주세요", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdateEmailActivity.this, UserProfileActivity.class));
                    finish();
                }else{
                    try{
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
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
            startActivity(new Intent(UpdateEmailActivity.this, UpdateProfileActivity.class));
            finish();
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(UpdateEmailActivity.this, UpdateEmailActivity.class));
        }else if (id == R.id.menu_settings){
            Toast.makeText(UpdateEmailActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(UpdateEmailActivity.this, ChangePasswordActivity.class));
            finish();
        }else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(UpdateEmailActivity.this, DeleteProfileActivity.class));
            finish();
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateEmailActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateEmailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(UpdateEmailActivity.this, "오류", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
