package com.sulitous.mtc;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private String centre,phone;
    private TextView mUnderTreatmentCount,mWaitingListCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView mLsName = findViewById(R.id.ls_name);
        TextView mLsEmail = findViewById(R.id.ls_email);
        TextView mLsPhone = findViewById(R.id.ls_phone);
        TextView mLsCentre = findViewById(R.id.ls_centre);
        mUnderTreatmentCount = findViewById(R.id.total_under_treatment);
        mWaitingListCount = findViewById(R.id.total_waiting_list);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE);
        centre = sharedPreferences.getString(getString(R.string.centre_key_shared),"");
//        phone = sharedPreferences.getString(getString(R.string.phone),"");

        mLsName.setText(mUser.getDisplayName());
        mLsEmail.setText(mUser.getEmail());
        mLsPhone.setText("7023211995");
        mLsCentre.setText(centre);
    }
}
