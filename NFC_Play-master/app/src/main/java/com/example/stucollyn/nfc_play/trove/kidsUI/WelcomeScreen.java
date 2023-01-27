package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import com.example.stucollyn.nfc_play.R;
import java.io.File;
import java.util.HashMap;

/*
This activity is the opening animation sequence for the trove app and the first activity run on launch.
 */

public class WelcomeScreen extends AppCompatActivity {

    //The ImageViews displayed on the activity layout
    ImageView backgroundShapes, zigzag1, zigzag2, zigzag3, zigzag4, star, moon, shell, book, key,
    leaf, umbrella, tear, teddy, halfcircle, heart, trove, back;

    //The animations used on the ImageViews
    Animation blink, bounce;

    //Arrays for grouping specific views together
    ImageView zigzagArray[], largeItemArray[], allViews[];

    //Hash map which couples each image view with an image resource. This is used later in the activity to load image recources into corresponding ImageViews.
    HashMap<ImageView, Integer> IvDrawable;

    //Handlers, runnables, and logical components governing the timing and repetition of animations
    Handler startupZigZagHandler, startupLargeObjectsHandler, idleZigZagHandler,
    idleLargeObjectsHandler, idleTroveHandler;
    Runnable TroveRunnable;
    int zigzagInt = 0;
    int largeObjectsInt = 0;
    boolean startupZigZagAnimationComplete = false, startupLargeObjectsAnimationComplete = false;

    //onCreate is called when the activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Layout associated with this activity
        setContentView(R.layout.activity_welcome_screen_kids_ui);
        //Ensure screen always stays on and never dims
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Initialize the ImageViews for programmatic use
        initializeViews();
        //Check whether the internal storage directory folders exist. If they do not, create them.
        setupStoryLocation();
        //Ensure all the large items the painted same colour - light blue/grey
        paintViews();
        //triggerAnimationSequence
        animationStartSequence();
    }

    void initializeViews() {
        //Initialize views from the associated layout
        backgroundShapes = findViewById(R.id.small_shapes);
        zigzag1 = findViewById(R.id.zigzag_1);
        zigzag2 = findViewById(R.id.zigzag_2);
        zigzag3 = findViewById(R.id.zigzag_3);
        zigzag4 = findViewById(R.id.zigzag_4);
        star = findViewById(R.id.star);
        moon = findViewById(R.id.moon);
        shell = findViewById(R.id.shell);
        book = findViewById(R.id.book);
        key = findViewById(R.id.key);
        leaf = findViewById(R.id.leaf);
        umbrella = findViewById(R.id.umbrella);
        tear = findViewById(R.id.tear);
        teddy = findViewById(R.id.teddy);
        heart = findViewById(R.id.heart);
        halfcircle = findViewById(R.id.circle);
        trove = findViewById(R.id.trove);
        back = findViewById(R.id.back);

        //Initialize array which stores all animated vector drawables - the zigzag views and the back button
        zigzagArray = new ImageView[5];
        zigzagArray[0] = zigzag1;
        zigzagArray[1] = zigzag2;
        zigzagArray[2] = zigzag3;
        zigzagArray[3] = zigzag4;
        zigzagArray[4] = back;

        //Initialize array which stores all of the large background objects
        largeItemArray = new ImageView[11];
        largeItemArray[0] = star;
        largeItemArray[1] = moon;
        largeItemArray[2] = shell;
        largeItemArray[3] = book;
        largeItemArray[4] = key;
        largeItemArray[5] = leaf;
        largeItemArray[6] = umbrella;
        largeItemArray[7] = tear;
        largeItemArray[8] = teddy;
        largeItemArray[9] = heart;
        largeItemArray[10] = halfcircle;

        //Initialize array which stores all the views in the layout
        allViews = new ImageView[16];
        allViews[0] = zigzag1;
        allViews[1] = zigzag2;
        allViews[2] = zigzag3;
        allViews[3] = zigzag4;
        allViews[4] = back;
        allViews[5] = star;
        allViews[6] = moon;
        allViews[7] = shell;
        allViews[8] = book;
        allViews[9] = key;
        allViews[10] = leaf;
        allViews[11] = umbrella;
        allViews[12] = tear;
        allViews[13] = teddy;
        allViews[14] = heart;
        allViews[15] = halfcircle;

        //Initialize hash map, which couples every image view with an image resource
        IvDrawable = new HashMap<>();
        IvDrawable.put(star, R.drawable.kids_ui_star);
        IvDrawable.put(moon, R.drawable.kids_ui_moon);
        IvDrawable.put(shell, R.drawable.kids_ui_shell);
        IvDrawable.put(book, R.drawable.kids_ui_book);
        IvDrawable.put(key, R.drawable.kids_ui_key);
        IvDrawable.put(leaf, R.drawable.kids_ui_leaf);
        IvDrawable.put(umbrella, R.drawable.kids_ui_umbrella);
        IvDrawable.put(tear, R.drawable.kids_ui_tear);
        IvDrawable.put(teddy, R.drawable.kids_ui_teddy);
        IvDrawable.put(heart,  R.drawable.kids_ui_heart);
        IvDrawable.put(halfcircle,  R.drawable.kids_ui_halfcircle);
        IvDrawable.put(zigzag1,  R.drawable.kids_ui_zigzag);
        IvDrawable.put(zigzag2,  R.drawable.kids_ui_zigzag);
        IvDrawable.put(zigzag3,  R.drawable.kids_ui_zigzag);
        IvDrawable.put(zigzag4,  R.drawable.kids_ui_zigzag);
        IvDrawable.put(back,  R.drawable.kids_ui_back);
        IvDrawable.put(trove,  R.drawable.kids_ui_trove);

        //Initialize animations
        blink = AnimationUtils.loadAnimation(this, R.anim.blink);
        bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
    }

    //Setup internal file storage folders
    private void setupStoryLocation() {


        /*Create a series of internal folder directories.
        * Inside the 'Stories' directory, every specific object is given its own directory. The title of this directory is a unique object ID. Inside these object directories we store individual story
        * validStoryFolders (images, audio, video) about the object. Inside the 'Tag' directory, every specific object is given its own directory, in the same way as with the Stories folder - using the same unique
        * object ID. However, there are only ever two validStoryFolders within each object directory - an audio file and an image (or video) file. When NFC tags are read, the currently associated validStoryFolders are read from
        * the Tag folder. Insider the 'Covers' directory, every specific object is also given its own directory. However, there is only ever the very first image associated with the object in each
        * directory. The archive activity read from this folder when populating gallery items with images. Inside the 'Cloud' directory every specific object is also given its own directory, upon being
        * downloaded from the cloud. The Cloud directory contents is deleted every time the user logs out of trove.
        */
        File story_directory = new File (getFilesDir() + File.separator + "Stories");
        File tag_directory = new File (getFilesDir() + File.separator + "Tag");
        File cover_directory = new File (getFilesDir() + File.separator + "Covers");
        File cloud_directory = new File (getFilesDir() + File.separator + "Cloud");

        //Check if the Stories, Tag, Covers, and Cloud directories exist. If not, create them. This is important because the archive activities will attempt to read from them later.
        if (! story_directory.exists()){
            story_directory.mkdir();
        }

        if (! tag_directory.exists()){
            tag_directory.mkdir();
        }

        if (! cover_directory.exists()){
            cover_directory.mkdir();
        }

        if (! cloud_directory.exists()){
            cloud_directory.mkdir();
        }
    }

    //Ensure all the large items the painted same colour - light blue/grey
    void paintViews() {

        //For all the large items, which are painted different colours at later stages of the app, ensure they are repainted their original colour.
        for(int i=0; i<largeItemArray.length; i++) {

            //Retrieve vector drawable, then wrap it in a specific RGB colour value. Next apply that to the ImageView.
            Drawable d = VectorDrawableCompat.create(getResources(), IvDrawable.get(largeItemArray[i]), null);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, android.graphics.Color.rgb(111,133,226));
            largeItemArray[i].setImageDrawable(d);
        }
    }


    void animationIdleSequence() {

        idleZigZagHandler = new Handler();
        idleLargeObjectsHandler = new Handler();
        idleTroveHandler = new Handler();

        //Runnable to handle idle trove animation
        TroveRunnable = new Runnable() {

            @Override
            public void run() {
                idleTroveAnimation();
                idleTroveHandler.postDelayed(this, 2000);
            }
        };

        //Runnable to handle the zig zag drawings
        Runnable AnimationRunnable = new Runnable() {

            @Override
            public void run() {
//                zigZagAnimation();
                idleZigZagHandler.postDelayed(this, 2000);
            }
        };

        //Runnable to handle random animation sequence of larger objects
        Runnable LargeItemRunnable = new Runnable() {

            @Override
            public void run() {
                idleLargeItemAnimation();
                idleZigZagHandler.postDelayed(this, 8000);
            }
        };

        idleZigZagHandler.post(AnimationRunnable);
        idleLargeObjectsHandler.post(LargeItemRunnable);
        idleTroveHandler.post(TroveRunnable);


    }

    void idleTroveAnimation() {

        //Bounce animate the trove logo
        trove.startAnimation(bounce);
    }

    void idleLargeItemAnimation() {

        //This commented-out code can be used if trying to make random large items blink if desired
        /*Random random = new Random();
        int i = random.nextInt(9);
        passCodeItemArray[i].startAnimation(blink);*/

        //Blink animate the background shapes
        backgroundShapes.startAnimation(blink);
    }

    //triggerAnimationSequence
    void animationStartSequence() {

        //Create two UI Handlers. One handler to manage the timing and animation of the zig zag animated vectors. Another handler to manage the timing and animation of the larger background objects.
        //Although presented in the opposite order, the larger objects handler is actually the one which is called and completed first.
        startupZigZagHandler = new Handler();
        startupLargeObjectsHandler = new Handler();

        //Runnable to handle the zig zag animated vectors drawings. A new zig zag vector should appear and animate every second until all 4 zig zags are complete.
        final Runnable zigZagRunnable = new Runnable() {

            @Override
            public void run() {

                //Call zig zag animation method.
                startupZigZagAnimation();

                //After one second, if not all of the 4 zig zags have been completed, run the runnable again.
                if(!startupZigZagAnimationComplete) {

                    startupZigZagHandler.postDelayed(this, 1000);
                }

                //When all 4 zig zags have completed, remove call backs to the zig zag handler (i.e. end zig zag animations), and begin to animate the trove logo as well as the idle animation sequence.
                else {

                    startupZigZagHandler.removeCallbacks(this);
                    startupTroveLogoAnimation();
                    animationIdleSequence();
                }
            }
        };

        //Runnable to handle and time the fade in animations of the large background items.
        final Runnable largeObjectsRunnable = new Runnable() {

            @Override
            public void run() {

                //Call large item animation method.
                startupLargeItemAnimation();

                //Begin to fade in a new large background object every second until all of these objects are visible.
                if(!startupLargeObjectsAnimationComplete) {

                    startupLargeObjectsHandler.postDelayed(this, 1000);
                }

                //When all of the objects are visible, remove call backs and begin the zig zag animation sequence.
                else {

                    startupLargeObjectsHandler.removeCallbacks(this);

                    //Call the zig zag handler to begin zig zag animations for the first time.
                    startupZigZagHandler.post(zigZagRunnable);
                }
            }
        };

        //Begin the large objects handler UI thread.
        startupLargeObjectsHandler.post(largeObjectsRunnable);
    }

    //Initial fade in animation of the trove logo.
    void startupTroveLogoAnimation() {

        //Initialize animation, start it, and ensure the ImageView remains visible upon completion.
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        trove.startAnimation(fadein);
        trove.setVisibility(View.VISIBLE);
    }

    //Initial fade in animations of every large background object.
    void startupLargeItemAnimation() {

        //Initialize animation. For each large background object, apply the animation, start it, and ensure each ImageView remains visible upon completion.
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);

        //If the counter largeObjectsInt is remains less than the total number of ImageViews we require to animate, select the next ImageView.
        if(largeObjectsInt<11) {

            largeItemArray[largeObjectsInt].startAnimation(fadein);

            //Listen for the current animation to finish, before the next object is eligible to be animated.
            fadein.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    largeItemArray[largeObjectsInt].setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    //add one to the counter
                    largeObjectsInt += 1;
                }
            });
        }

        //When all large background objects are loaded, begin to animate the background shapes
        else {

            largeObjectsInt=0;
            startupLargeObjectsAnimationComplete= true;
            backgroundShapes.startAnimation(fadein);
            backgroundShapes.setVisibility(View.VISIBLE);
        }

    }

    void startupZigZagAnimation(){

        if(zigzagInt<5) {

            zigzagArray[zigzagInt].setVisibility(View.VISIBLE);
            Drawable d = zigzagArray[zigzagInt].getDrawable();
            final AnimatedVectorDrawable zigzaganim = (AnimatedVectorDrawable) d;
            zigzaganim.start();
            zigzagInt+=1;
        }

        else {

            zigzagInt=0;
            startupZigZagAnimationComplete = true;
        }
    }

    //If the user presses the star ImageView (the first image visible), skip the animations and continue to the Login activity.
    public void Skip(View view) {

        Intent intent = new Intent(WelcomeScreen.this, Login.class);
        intent.putExtra("PreviousActivity", "WelcomeScreen");
        WelcomeScreen.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //If the user presses the trove button (visible after all animations have completed), continue to the Login activity.
    public void Continue(View view) {

        idleTroveHandler.removeCallbacks(TroveRunnable);
        trove.clearAnimation();
        Intent intent = new Intent(WelcomeScreen.this, Login.class);
        intent.putExtra("PreviousActivity", "WelcomeScreen");
        WelcomeScreen.this.startActivity(intent);
    }

}
