package com.sulitous.mtc;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView mText1,mText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mImageView = findViewById(R.id.splash_logo);
        mText1 = findViewById(R.id.splash_text);
        mText2 = findViewById(R.id.splash_text1);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.image_anim);
        mImageView.startAnimation(animation);
        mText1.setAnimation(animation);
        mText2.setAnimation(animation);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 2000);
    }
}
