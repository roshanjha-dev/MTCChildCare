package com.sulitous.mtc;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserEmailView, mUserPassView;
    private TextInputLayout mUserEmailLayout,mUserPassLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserEmailView = findViewById(R.id.login_email);
        mUserPassView = findViewById(R.id.login_pass);
        mUserEmailLayout = findViewById(R.id.login_email_layout);
        mUserPassLayout = findViewById(R.id.login_pass_layout);
        TextView forgotView = findViewById(R.id.forgot_password);
        Button mLoginButton = findViewById(R.id.login_button);
        mRootRef = FirebaseFirestore.getInstance();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            switchToMainActivity();
        }
        forgotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showEmailDialog();
            }
        });
    }

    private void showEmailDialog() {
        Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.email_dialog);
        Button sendLink = dialog.findViewById(R.id.email_link_button);
        final TextInputLayout emailLayout = dialog.findViewById(R.id.email_link_layout);
        final TextInputEditText emailView = dialog.findViewById(R.id.email_link_view);

        sendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailView.getText().toString();

                if(TextUtils.isEmpty(email)){
                    emailLayout.setError(getString(R.string.field_required));
                }else if (isEmailValid(email)){
                    emailLayout.setError(getString(R.string.invalid_email));
                }else {
                    emailLayout.setError(null);
                }

                if (emailLayout.getError() == null) {
                    hideKeyboard();
                    ActionCodeSettings actionCodeSettings =
                            ActionCodeSettings.newBuilder()
                                    // URL you want to redirect back to. The domain (www.example.com) for this
                                    // URL must be whitelisted in the Firebase Console.
                                    .setUrl("https://www.towaso.com/")
                                    // This must be true
                                    .setHandleCodeInApp(true)
                                    .setAndroidPackageName(
                                            "com.sulitous.mtc",
                                            true, /* installIfNotAvailable */
                                            String.valueOf(16)    /* minimumVersion */)
                                    .build();

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.sendSignInLinkToEmail(email, actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                switchToMainActivity();
                            }
                        }
                    });
                }else {
                    Toast.makeText(LoginActivity.this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    private void switchToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void checkData() {
        mUserEmailView.setError(null);
        mUserPassView.setError(null);

        String password = mUserPassView.getText().toString();
        String email = mUserEmailView.getText().toString();

        if (TextUtils.isEmpty(password)) {
            mUserPassLayout.setError(getString(R.string.field_required));
        }else if (!isPasswordValid(password)){
            mUserPassLayout.setError(getString(R.string.invalid_password));
        }else {
            mUserPassLayout.setError(null);
        }

        if(TextUtils.isEmpty(email)){
            mUserEmailLayout.setError(getString(R.string.field_required));
        }else if (isEmailValid(email)){
            mUserEmailLayout.setError(getString(R.string.invalid_email));
        }else {
            mUserEmailLayout.setError(null);
        }

        if (mUserEmailLayout.getError() == null && mUserPassLayout.getError() == null) {
            hideKeyboard();
            login(email,password);
        }else {
            Toast.makeText(this, R.string.login_input_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String email, String password) {
        final ShowDialog showDialog = new ShowDialog(LoginActivity.this);
        showDialog.setTitle("Logging");
        showDialog.setMessage("Checking credits");
        showDialog.show();
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(final AuthResult authResult) {
                mRootRef.collection("LS").document(authResult.getUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            LS ls = documentSnapshot.toObject(LS.class);
                            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.name_key_shared),ls.getName());
                            editor.putString(getString(R.string.state_key_shared),ls.getState());
                            editor.putString(getString(R.string.district_key_shared),ls.getDistrict());
                            editor.putString(getString(R.string.centre_key_shared),ls.getCentre());
                            editor.putString(getString(R.string.block_key_shared),ls.getBlock());
                            editor.putString(getString(R.string.address_key_shared),ls.getAddress());
                            editor.putString(getString(R.string.uid_key_shared),authResult.getUser().getUid());
                            editor.apply();
                            showDialog.cancel();
                            switchToMainActivity();
                        }else {
                            mAuth.signOut();
                            showDialog.cancel();
                            Toast.makeText(LoginActivity.this, R.string.not_ls, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mAuth.signOut();
                        showDialog.cancel();
                        Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showDialog.cancel();
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(mUserEmailView.getWindowToken(), 0);
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*" + "@"
                + "\\w+([-.]\\w+)*" + "\\." + "\\w+([-.]\\w+)*");
        Matcher matcher = pattern.matcher(email);
        return !matcher.matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}
