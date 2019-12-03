package com.sulitous.mtc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mUser;
    private FirebaseFirestore mRootRef;
    private ChildAdapter mChildAdapter;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO 4 add Offline support
        mAuth = FirebaseAuth.getInstance();
        authWithFireBase();
        mRootRef = FirebaseFirestore.getInstance();
        findViewById(R.id.add_child_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToAddChildActivity();
            }
        });

        RecyclerView waitingList = findViewById(R.id.waiting_list);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        waitingList.setLayoutManager(manager);
        mChildAdapter = new ChildAdapter(MainActivity.this,mRootRef,0);
        waitingList.setAdapter(mChildAdapter);
    }

    private void switchToAddChildActivity() {
        Intent addChildIntent = new Intent(MainActivity.this,AddChildActivity.class);
        startActivity(addChildIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            return true;
        }else if (id == R.id.action_underTreatment){
            Intent underTreatmentIntent = new Intent(MainActivity.this,UnderTreatmentActivity.class);
            startActivity(underTreatmentIntent);
            return true;
        }else if (id == R.id.action_profile){
            Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
            startActivity(profileIntent);
        }else if (id == R.id.action_feedback){
            Intent feedbackIntent = new Intent(MainActivity.this,FeedbackActivity.class);
            startActivity(feedbackIntent);
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUser != null){
            if (registration == null) {
                mChildAdapter.clear();
                registration = mRootRef.collection("WaitingList").document(mUser.getUid()).addSnapshotListener(mChildAdapter);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (registration != null){
            mChildAdapter.clear();
            registration.remove();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mUser != null){
            if (registration == null) {
                mChildAdapter.clear();
                registration = mRootRef.collection("WaitingList").document(mUser.getUid()).addSnapshotListener(mChildAdapter);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (registration != null){
            mChildAdapter.clear();
            registration.remove();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (registration != null){
            registration.remove();
        }
    }

    private void authWithFireBase(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if (mUser == null) {
                    switchToLoginActivity();
                }else {
                    mChildAdapter.clear();
                    if (registration == null) {
                        mChildAdapter.clear();
                        registration = mRootRef.collection("WaitingList").document(mUser.getUid()).addSnapshotListener(mChildAdapter);
                    }
                }
            }
        };
    }

    private void switchToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
