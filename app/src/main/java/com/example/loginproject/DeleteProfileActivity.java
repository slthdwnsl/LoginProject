package com.example.loginproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private ProgressBar progressBar;
    private String userPwd;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private static final String TAG = "DeleteProfileActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        getSupportActionBar().setTitle("프로필 삭제");

        progressBar = findViewById(R.id.progressBar);
        editTextUserPwd = findViewById(R.id.editText_delete_user_pwd);
        textViewAuthenticated = findViewById(R.id.textView_delete_user_authenticated);
        buttonDeleteUser = findViewById(R.id.button_delete_user);
        buttonReAuthenticate = findViewById(R.id.button_delete_user_authenticate);

        buttonDeleteUser.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        ImageView imageViewShowHideCureentPwd = findViewById(R.id.imageView_show_hide_current_pwd);
        imageViewShowHideCureentPwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHideCureentPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextUserPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    editTextUserPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHideCureentPwd.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextUserPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHideCureentPwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        if (firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "오류", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DeleteProfileActivity.this, UserProfileActivity.class));
            finish();
        }else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwd = editTextUserPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteProfileActivity.this, "비밀번호가 필요합니다", Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("인증을 위해 현재 비밀번호를 입력해주세요");
                    editTextUserPwd.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                editTextUserPwd.setEnabled(true);

                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                textViewAuthenticated.setText("인증되었습니다" + ","+ "프로필을 삭제할 수 있습니다");
                                Toast.makeText(DeleteProfileActivity.this, "인증되었습니다" +"\n"+ "프로필을 삭제할 수 있습니다", Toast.LENGTH_SHORT).show();

                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this, R.color.dark_green));

                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showAlertDialog();
                                    }
                                });
                            }else {
                                try{
                                    throw task.getException();
                                }catch (Exception e){
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("정말로 삭제하시겠습니까?");
        builder.setMessage("삭제된 데이터는 복구할 수 없습니다"+"\n"+"정말로 삭제하시겠습니까?");

        builder.setPositiveButton("계속", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteUser();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(DeleteProfileActivity.this, UserProfileActivity.class));
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });
        alertDialog.show();
    }

    private void deleteUser() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "성공적으로 삭제되었습니다", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DeleteProfileActivity.this, MainActivity.class));
                    finish();
                }else {
                    try{
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void deleteUserData(FirebaseUser firebaseUser) {
        if (firebaseUser.getPhotoUrl() != null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess: Photo Deleted");

                    deleteUser();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(DeleteProfileActivity.this, UpdateProfileActivity.class));
            finish();
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(DeleteProfileActivity.this, UpdateEmailActivity.class));
        }else if (id == R.id.menu_settings){
            Toast.makeText(DeleteProfileActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class));
            finish();
        }else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(DeleteProfileActivity.this, DeleteProfileActivity.class));
            finish();
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(DeleteProfileActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(DeleteProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
