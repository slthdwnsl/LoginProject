package com.example.loginproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("프로필 설정");

        progressBar = findViewById(R.id.progressBar);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        showProfile(firebaseUser);

        TextView textViewUploadProfilePic = findViewById(R.id.textView_profile_upload_pic);
        textViewUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdateProfileActivity.this, UploadProfileActivity.class));
                finish();
            }
        });

        TextView textViewUpdateEmail = findViewById(R.id.textView_profile_update_email);
        textViewUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class));
                finish();
            }
        });

        ImageView imageViewDatePicker = findViewById(R.id.imageView_date_picker);
        imageViewDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textSADoB[] = textDoB.split("/");

                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1]) - 1;
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(year + "/" + (month + 1) + "/" + dayOfMonth);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(firebaseUser);
            }
        });
    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        String mobileRegex = "[0][0-9]{10}";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobile);

        if(TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfileActivity.this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            editTextUpdateName.setError("이름을 입력해주세요.");
            editTextUpdateName.requestFocus();
        }else if(TextUtils.isEmpty(textDoB)){
            Toast.makeText(UpdateProfileActivity.this, "생년월일을 입력해주세요", Toast.LENGTH_SHORT).show();
            editTextUpdateDoB.setError("생년월일을 입력해주세요.");
            editTextUpdateDoB.requestFocus();
        }else if(TextUtils.isEmpty(textMobile)){
            Toast.makeText(UpdateProfileActivity.this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("전화번호를 입력해주세요.");
            editTextUpdateMobile.requestFocus();
        }else if(textMobile.length() != 11){
            Toast.makeText(UpdateProfileActivity.this, "전화번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("전화번호를 다시 입력해주세요.");
            editTextUpdateMobile.requestFocus();
        }else if(!mobileMatcher.find()){
            Toast.makeText(UpdateProfileActivity.this, "전화번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("전화번호를 다시 입력해주세요.");
            editTextUpdateMobile.requestFocus();
        }else if(TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "성을 선택해주세요", Toast.LENGTH_SHORT).show();
            radioButtonUpdateGenderSelected.setError("성을 선택해주세요.");
            radioButtonUpdateGenderSelected.requestFocus();
        }else{
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textDoB, textGender, textMobile);

            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "업데이트 성공", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        try{
                            throw task.getException();
                        }catch (Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null){
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = readUserDetails.doB;
                    textGender = readUserDetails.gender;
                    textMobile = readUserDetails.mobile;

                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);

                    if (textGender.equals("남성")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    } else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                }else {
                    Toast.makeText(UpdateProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
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
            startActivity(new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class));
            finish();
        }else if (id == R.id.menu_update_email){
            startActivity(new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class));
        }else if (id == R.id.menu_settings){
            Toast.makeText(UpdateProfileActivity.this, "menu_settings", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password){
            startActivity(new Intent(UpdateProfileActivity.this, ChangePasswordActivity.class));
            finish();
        }else if (id == R.id.menu_delete_profile){
            startActivity(new Intent(UpdateProfileActivity.this, DeleteProfileActivity.class));
            finish();
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(UpdateProfileActivity.this, "오류", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
