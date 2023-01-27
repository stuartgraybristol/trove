package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.example.stucollyn.nfc_play.R;

//SplashScreen showing the trove app creators and project partners; first Activity in app
public class SplashScreen extends AppCompatActivity {

    Animation meineck_fade_in, meineck_fade_out, uob_fade_in, uob_fade_out, miine_fade_in,
            miine_fade_out;
    ImageView meineckLogo, uobLogo, miineLogo;
    private static final String TAG = "miine App: ";
    int mode;

//OnCreate method called when Activity begins
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);

        //Initialize animations and views
        InitAnimation();
        InitView();

        //Begin animation sequence with Studio Meineck logo
        animateStudioMeineckLogo();
    }

    //Initialize animations used in Activity
    private void InitAnimation() {

        meineck_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        meineck_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        uob_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        uob_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        miine_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        miine_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
    }

    //Initialize views used in Activity
    private void InitView() {

        meineckLogo = (ImageView) findViewById(R.id.meineck);
        uobLogo = (ImageView) findViewById(R.id.uob);
        miineLogo = (ImageView) findViewById(R.id.miine);
    }

    //Animation sequence for Studio Meineck logo
    private void animateStudioMeineckLogo() {

        //Fade in Studio Meineck logo
        meineckLogo.startAnimation(meineck_fade_in);

        //Listen for Studio Meineck logo to finish fading in, then begin fade out
        meineck_fade_in.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                meineckLogo.startAnimation(meineck_fade_out);
            }
        });

        /*Listen for Studio Meineck logo to finish fading out, then begin University of Bristol
        logo sequence */
        meineck_fade_out.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                meineckLogo.setVisibility(View.INVISIBLE);
                animateUoBLogo();
            }
        });

    }

    //Animation sequence for University of Bristol logo
    private void animateUoBLogo() {

        //Fade in University of Bristol logo
        uobLogo.startAnimation(uob_fade_in);

        //Listen for University of Bristol logo to finish fading in, then begin fade out
        uob_fade_in.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                uobLogo.startAnimation(uob_fade_out);
            }
        });

         /*Listen for University of Bristol logo to finish fading out, then begin trove logo
         sequence */
        uob_fade_out.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                uobLogo.setVisibility(View.INVISIBLE);
//Note, no transition required as trove logo is to remain constantly visible
                Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                intent.putExtra("Orientation", mode);
                SplashScreen.this.startActivity(intent);
                overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        });

    }

    //Animation sequence for trove logo
    private void animateMiineLogo() {

        //Fade in trove logo
        miineLogo.startAnimation(miine_fade_in);

        //Make trove logo visible beyond the animation, to the end of SplashScreen Activity
        miineLogo.setVisibility(View.VISIBLE);

        //When trove has finished fading in, open LoginScreen Activity
        miine_fade_in.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //Note, no transition required as trove logo is to remain constantly visible
                Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                intent.putExtra("Orientation", mode);
                SplashScreen.this.startActivity(intent);

            }
        });


    }

    //When the screen is tapped, skip animations and immediately open LoginScreen Activity
    public void Skip(View view) {

        Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
        intent.putExtra("Orientation", mode);
        SplashScreen.this.startActivity(intent);
        //Activity fade transition
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);

    }

}



//Example thread handler
      /* new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
               /* Intent mainIntent = new Intent(SplashScreen.this, NFCRead.class);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }

        }, SPLASH_DISPLAY_LENGTH); */