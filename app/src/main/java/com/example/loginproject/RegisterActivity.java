package com.example.loginproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterDoB, editTextRegisterMobile, editTextRegisterPwd, editTextRegisterConfirmPwd;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;
    private CheckBox checkBoxtermsconditions;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("회원가입");

        Toast.makeText(RegisterActivity.this, "회원가입 하기", Toast.LENGTH_SHORT).show();

        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_mobile);
        editTextRegisterPwd = findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPwd = findViewById(R.id.editText_register_password_confirm);
        progressBar = findViewById(R.id.progressBar);
        checkBoxtermsconditions = findViewById(R.id.checkBox_terms_conditions);

        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        ImageView imageViewShowHidePwd = findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextRegisterPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    editTextRegisterPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextRegisterPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        ImageView imageViewShowHideConfirmPwd = findViewById(R.id.imageView_show_hide_pwd_confirm);
        imageViewShowHideConfirmPwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHideConfirmPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextRegisterConfirmPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    editTextRegisterConfirmPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHideConfirmPwd.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextRegisterConfirmPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHideConfirmPwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        ImageView imageDatePicker = findViewById(R.id.imageView_date_picker);
        imageDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(year + "/" + (month + 1) + "/" + dayOfMonth);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                String textFullName = editTextRegisterFullName.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textMobile = editTextRegisterMobile.getText().toString();
                String textPwd = editTextRegisterPwd.getText().toString();
                String textConfirmPwd = editTextRegisterConfirmPwd.getText().toString();
                String textGender;

                String mobileRegex = "[0][0-9]{10}";
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);

                if(TextUtils.isEmpty(textFullName)){
                    Toast.makeText(RegisterActivity.this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterFullName.setError("이름을 입력해주세요.");
                    editTextRegisterFullName.requestFocus();
                }else if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(RegisterActivity.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterEmail.setError("이메일을 입력해주세요.");
                    editTextRegisterEmail.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(RegisterActivity.this, "이메일을 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterEmail.setError("이메일을 다시 입력해주세요.");
                    editTextRegisterEmail.requestFocus();
                }else if(TextUtils.isEmpty(textDoB)){
                    Toast.makeText(RegisterActivity.this, "생년월일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterDoB.setError("생년월일을 입력해주세요.");
                    editTextRegisterDoB.requestFocus();
                }else if(TextUtils.isEmpty(textMobile)){
                    Toast.makeText(RegisterActivity.this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterMobile.setError("전화번호를 입력해주세요.");
                    editTextRegisterMobile.requestFocus();
                }else if(textMobile.length() != 11){
                    Toast.makeText(RegisterActivity.this, "전화번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterMobile.setError("전화번호를 다시 입력해주세요.");
                    editTextRegisterMobile.requestFocus();
                }else if(!mobileMatcher.find()){
                    Toast.makeText(RegisterActivity.this, "전화번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterMobile.setError("전화번호를 다시 입력해주세요.");
                    editTextRegisterMobile.requestFocus();
                }else if(TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterActivity.this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterPwd.setError("비밀번호를 입력해주세요.");
                    editTextRegisterPwd.requestFocus();
                }else if(textPwd.length() < 6){
                    Toast.makeText(RegisterActivity.this, "비밀번호는 6자리 이상으로 입력해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterPwd.setError("비밀번호 약함");
                    editTextRegisterPwd.requestFocus();
                }else if(radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "성을 선택해주세요", Toast.LENGTH_SHORT).show();
                    radioButtonRegisterGenderSelected.setError("성을 선택해주세요.");
                    radioButtonRegisterGenderSelected.requestFocus();
                }else if(TextUtils.isEmpty(textConfirmPwd)){
                    Toast.makeText(RegisterActivity.this, "비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterConfirmPwd.setError("비밀번호를 확인해주세요");
                    editTextRegisterConfirmPwd.requestFocus();
                }else if (!textPwd.equals(textConfirmPwd)){
                    Toast.makeText(RegisterActivity.this, "비밀번호를 다시 확인해주세요", Toast.LENGTH_SHORT).show();
                    editTextRegisterConfirmPwd.setError("비밀번호를 다시 확인해주세요");
                    editTextRegisterConfirmPwd.requestFocus();
                    editTextRegisterPwd.clearComposingText();
                    editTextRegisterConfirmPwd.clearComposingText();
                }else if(!checkBoxtermsconditions.isChecked()){
                    Toast.makeText(RegisterActivity.this, "개인정보 이용 동의에 체크해주세요", Toast.LENGTH_SHORT).show();
                    checkBoxtermsconditions.setError("개인정보 이용 동의에 체크해주세요");
                    checkBoxtermsconditions.requestFocus();
                }else{
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textFullName, textEmail, textDoB, textGender, textMobile, textPwd);
                }
            }
        });
    }

    private void registerUser(String textFullName, String textEmail, String textDoB, String textGender, String textMobile, String textPwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textDoB, textGender, textMobile);

                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(RegisterActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            }else {
                                Toast.makeText(RegisterActivity.this, "회원가입 실패, 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }else {
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        editTextRegisterPwd.setError("비밀번호가 약합니다. 알파벳과 숫자 특수문자를 섞어주세요");
                        editTextRegisterPwd.requestFocus();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        editTextRegisterEmail.setError("이메일이 중복되었거나 유효하지 않습니다. 다시 입력해주세요");
                        editTextRegisterEmail.requestFocus();
                    }catch (FirebaseAuthUserCollisionException e){
                        editTextRegisterEmail.setError("이미 가입된 이메일입니다. 다른 이메일로 시도해주세요");
                        editTextRegisterEmail.requestFocus();
                    }catch (Exception e){
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
