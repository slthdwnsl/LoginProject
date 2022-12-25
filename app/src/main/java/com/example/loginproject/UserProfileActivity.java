package com.example.loginproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private TextView textViewWelcome, textViewFullname, textViewEmail, textViewDoB, textViewGender, textViewMobile;
    private ProgressBar progressBar;
    private String fullName, email, doB, gender, mobile;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setTitle("홈");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewFullname = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewDoB = findViewById(R.id.textView_show_dob);
        textViewGender = findViewById(R.id.textView_show_gender);
        textViewMobile = findViewById(R.id.textView_show_mobile);
        progressBar = findViewById(R.id.progressBar);

        imageView = findViewById(R.id.imageView_profile_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfileActivity.this, UploadProfileActivity.class));
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser == null){
            Toast.makeText(UserProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
        }else {
            checkifEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }
    }

    private void swipeToRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    private void checkifEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
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

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB = readUserDetails.doB;
                    gender = readUserDetails.gender;
                    mobile = readUserDetails.mobile;

                    textViewWelcome.setText("환영합니다," + fullName + "!");
                    textViewFullname.setText(fullName);
                    textViewEmail.setText(email);
                    textViewDoB.setText(doB);
                    textViewGender.setText(gender);
                    textViewMobile.setText(mobile);

                    Uri uri = firebaseUser.getPhotoUrl();

                    Picasso.with(UserProfileActivity.this).load(uri).into(imageView);
                }else {
                    Toast.makeText(UserProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
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

        if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(UserProfileActivity.this);
        }
        else if (id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        }else if (id == R.id.menu_update_profile){
            startActivity(new Intent(UserProfileActivity.this, UpdateProfileActivity.class));
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(UserProfileActivity.this, UpdateEmailActivity.class));
        }else if (id == R.id.menu_settings){
            Toast.makeText(UserProfileActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(UserProfileActivity.this, ChangePasswordActivity.class));
            finish();
        }else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(UserProfileActivity.this, DeleteProfileActivity.class));
            finish();
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UserProfileActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(UserProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
