package com.sulitous.mtc;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private Switch mPaymentView;
    private RatingBar mDietView;
    private EditText mAnmView,mMtcView;
    private FirebaseFirestore mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mPaymentView = findViewById(R.id.feedback_payment);
        mDietView = findViewById(R.id.feedback_diet_rating);
        mAnmView = findViewById(R.id.feedback_anm);
        mMtcView = findViewById(R.id.feedback_mtc);
        Button mFeedbackSubmitView = findViewById(R.id.feedback_submit);
        mFeedbackSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });
        mRootRef = FirebaseFirestore.getInstance();
    }

    private void checkData() {
        boolean payment = mPaymentView.isChecked();
        double diet = mDietView.getNumStars();
        String anm = mAnmView.getText().toString().trim();
        String mtc = mMtcView.getText().toString().trim();


        if (TextUtils.isEmpty(anm)){
            anm = "Nothing";

        }

        if (TextUtils.isEmpty(mtc)){
            mtc = "Nothing";
        }

        mAnmView.setText("");
        mMtcView.setText("");
        onSubmit(payment,diet,anm,mtc);
    }

    private void onSubmit(boolean payment, double diet, String anm, String mtc) {
        final ShowDialog showDialog = new ShowDialog(FeedbackActivity.this);
        showDialog.setTitle("Feedback");
        showDialog.setMessage("Uploading Feedback");
        showDialog.show();
        Map<String, Object> feedbackHashMap = new HashMap<>();
        feedbackHashMap.put("payment", payment);
        feedbackHashMap.put("diet",diet);
        feedbackHashMap.put("anm",anm);
        feedbackHashMap.put("mtc",mtc);
        feedbackHashMap.put("timeStamp",FieldValue.serverTimestamp());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        // TODO add user display name
        feedbackHashMap.put("LS","Gourav Karwasara");
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE);
        String centre = sharedPreferences.getString(getString(R.string.centre_key_shared),"");
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        mRootRef.collection("Feedback").document(centre).collection(timeStamp).add(feedbackHashMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                showDialog.cancel();
                Toast.makeText(FeedbackActivity.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showDialog.cancel();
                Toast.makeText(FeedbackActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
