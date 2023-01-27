package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.transition.Explode;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stucollyn.nfc_play.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/*
The AboutAndLogout fragment activity is activated when the user presses the hamburger screen button from any other activity which features the button in the app.
The activity displays some information about the trove project and allows the user to logout at short notice. There is an inbuilt function in this activity which
allows us to clear all the internal file storage directories - tapping the leaf image 5+ times.
 */
public class AboutAndLogout extends FragmentActivity {

    //The View group and ImageViews displayed on the activity layout
    ViewGroup mRootView;
    ImageView backgroundShapes, zigzag1, zigzag2, zigzag3, zigzag4, star, moon, shell, book, key,
            leaf, umbrella, tear, teddy, heart, back, back2, halfcircle;

    //The animations used on the ImageViews
    Animation bounce, fadein, alpha, fadeout;
    AnimatedVectorDrawable backRetrace;

    //trove Text
    TextView troveTitle, troveBody;

    //Arrays for grouping specific views together
    ImageView allViews[];

    //Hash map which couples each image view with an image resource. This is used later in the activity to load image recources into corresponding ImageViews.
    HashMap<ImageView, Integer> IvDrawable;

    //Handlers, runnables, and logical components governing the appearance, timing and repetition of animations
    Integer[] paintColourArray;

    //NFC components and variables
    NFCInteraction nfcInteraction;
    Tag mytag;
    boolean newStoryReady = false;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter readTagFilters[];

    //Networking
    boolean authenticated = false;

    //Story playback components and variables
    ShowStoryContent showStoryContent;
    CommentaryInstruction commentaryInstruction;

    //Other Variables
    String previousActivity;
    ImageView hamburgerButton;
    Class targetClass;
    int deleteCounter = 0; //Used to reset the app story storage folders

    //onCreate is called when the activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Layout associated with this activity
        setContentView(R.layout.activity_about_and_logout_kids_ui);
        //Ensure screen always stays on and never dims
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Initialize views
        initializeViews();
        //Paint background imageView
        paintViews();
        //Initialize commentary instructions
        initializeCommentary();
        //Check for data connection before allowing user to sign in via cloud account
        checkConnection();
        //Setup and initialize NFC functionality
        nfcSetup();
        //Check what the previous activity was and what action is required
        checkPreviousActivity();
    }

    //Check what the previous activity was and what action is required
    void checkPreviousActivity() {

        previousActivity = (String) getIntent().getExtras().get("PreviousActivity");

        try {
            targetClass = Class.forName("com.example.stucollyn.nfc_play.trove.kidsUI."+previousActivity);
        }
        catch (Exception e) {

        }
    }

    //Setup nfc components
    void nfcSetup() {

        nfcInteraction = new NFCInteraction(this, this);
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        readTagFilters = new IntentFilter[] {tagDetected };
    }

    //Initialize commentary instructions
    void initializeCommentary() {

        commentaryInstruction = new CommentaryInstruction(this, this, false, authenticated);
    }

    //Check for data connection before allowing user to sign in via cloud account.
    void checkConnection() {

        //Call method which checks for connection.
        boolean isNetworkConnected = isNetworkConnected();

        //If there is a data connection currently available for use, attempt to authenticate login details with Firebase,
        // allow user to login offline but without a profile and access only to local storage.
        if(isNetworkConnected) {

            //If there is an active data connection, set authenticated value to true
            authenticated = true;
        }

        else {

            //If there is an active data connection, set authenticated value to false
            authenticated = false;
        }
    }

    //Check whether a network connection is present.
    private boolean isNetworkConnected() {

        //Use Android connectivity manager to get the status of whether connected to a data connection.
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    //Initialize views
    void initializeViews(){

        //Initialize the root group of image views to control multiple views in a single command
        mRootView = (ViewGroup) findViewById(R.id.activity_hamburger_kids_ui);

        //Initialize the four different colours used to denote background object image views.
        paintColourArray = new Integer[4];
        paintColourArray[0] = Color.rgb(255, 157, 0);
        paintColourArray[1] = Color.rgb(253, 195, 204);
        paintColourArray[2] = Color.rgb(0, 235, 205);
        paintColourArray[3] = Color.WHITE;

        //Initialize the image views themselves.
        hamburgerButton = findViewById(R.id.drawer_button);
        backgroundShapes = (ImageView) findViewById(R.id.small_shapes);
        zigzag1 = (ImageView) findViewById(R.id.zigzag_1);
        zigzag2 = (ImageView) findViewById(R.id.zigzag_2);
        zigzag3 = (ImageView) findViewById(R.id.zigzag_3);
        zigzag4 = (ImageView) findViewById(R.id.zigzag_4);
        star = (ImageView) findViewById(R.id.star);
        moon = (ImageView) findViewById(R.id.moon);
        shell = (ImageView) findViewById(R.id.shell);
        book = (ImageView) findViewById(R.id.book);
        key = (ImageView) findViewById(R.id.key);
        leaf = (ImageView) findViewById(R.id.leaf);
        umbrella = (ImageView) findViewById(R.id.umbrella);
        tear = (ImageView) findViewById(R.id.tear);
        teddy = (ImageView) findViewById(R.id.teddy);
        heart = (ImageView) findViewById(R.id.heart);
        halfcircle = (ImageView) findViewById(R.id.circle);
        back = (ImageView) findViewById(R.id.back);
        back2 = (ImageView) findViewById(R.id.back2);
        backRetrace = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_back_anim_retrace);
        troveTitle = (TextView) findViewById(R.id.trove_title);
        troveBody = (TextView) findViewById(R.id.trove_body);

        //Hashmap which we can use to reference the image resource of each imageview.
        IvDrawable = new HashMap<ImageView, Integer>();
        IvDrawable.put(star, R.drawable.kids_ui_star);
        IvDrawable.put(moon, R.drawable.kids_ui_moon);
        IvDrawable.put(shell, R.drawable.kids_ui_shell);
        IvDrawable.put(book, R.drawable.kids_ui_book);
        IvDrawable.put(key, R.drawable.key_log_out);
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
        IvDrawable.put(back2,  R.drawable.kids_ui_back);

        //Initialize array of all image views present in the activity.
        allViews = new ImageView[17];
        allViews[0] = zigzag2;
        allViews[1] = zigzag1;
        allViews[2] = zigzag4;
        allViews[3] = zigzag3;
        allViews[4] = star;
        allViews[5] = halfcircle;
        allViews[6] = leaf;
        allViews[7] = moon;
        allViews[8] = book;
        allViews[9] = back;
        allViews[10] = shell;
        allViews[11] = teddy;
        allViews[12] = tear;
        allViews[13] = umbrella;
        allViews[14] = heart;
        allViews[15] = back2;
        allViews[16] = key;

        //Initialize animations
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_infinite);
    }

    //When the hamburger button is pressed, do the following
    public void Hamburger(View view){

        disableViewClickability();
        commentaryInstruction.stopPlaying();
        Intent intent = new Intent(AboutAndLogout.this, targetClass);
        intent.putExtra("Authenticated", authenticated);
        intent.putExtra("PreviousActivity", "AboutAndLogout");
        AboutAndLogout.this.startActivity(intent);
        overridePendingTransition(R.anim.right_to_left_slide_in_activity, R.anim.right_to_left_slide_out_activity);
    }

    //Play a story stored on an NFC tag immediately after it is scanned.
    void playStory(File[] filesOnTag) {

        //If a story is already being played, stop it.
        commentaryInstruction.stopPlaying();

        //If there is already a story content fragment open, close it.
        if(showStoryContent!=null) {

            //Get a currently open dialog fragment and close it.
            ShowStoryContentDialog currentDialog = showStoryContent.returnDialog();
            showStoryContent.closeOpenFragments(currentDialog);
        }

        //Show story content for a newly scanned tag using a showStoryContent object.
        showStoryContent = new ShowStoryContent(commentaryInstruction.getmPlayer(), this, this, filesOnTag);
        showStoryContent.checkFilesOnTag();
    }

    //When a new intent is generated by an NFC scanning operation, read its content and take a course of action.
    @Override
    protected void onNewIntent(Intent intent) {

        //If the new intent matches the filtered NFC intent, read in the tag's raw data.
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //Display a visible notification showing the object has been found.
            Toast.makeText(this, "Object found.", Toast.LENGTH_LONG).show();

            //Try and read and decode the tag's content if there is content to read.
            try {
                //If the tag is null, play an audio message and written text to show that requested tag is empty.
                // Else, use the tag's contained reference to find the corresponding files in device internal storage.
                //The containing files will be in the internal directory [package name]\Tags\[tag name]\
                if (mytag == null) {

                    commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.emptytag), false, null, "HomeScreen");
                    Toast.makeText(this, "This object has no story.", Toast.LENGTH_LONG).show();
                }

                else {
                    //Get the name of the current package, which we then use to help us to locate our files.
                    PackageManager m = getPackageManager();
                    String packageName = getPackageName();
                    File[] filesOnTag = nfcInteraction.read(mytag, m, packageName);

                    //If the referenced internal directory exists and there are files within, play the story.
                    if (filesOnTag != null) {

                        playStory(filesOnTag);
                    }
                }

            }
            //Catch various exceptions which may arise from failures in the scanning process or with regards to content data.
            catch (IOException e) {
                // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                System.out.println("Fail 1");
            } catch (FormatException e) {
                // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                System.out.println("Fail 2");
            } catch (IndexOutOfBoundsException e) {
                // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                System.out.println("Fail 3");
            } catch (NullPointerException e) {
                // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                System.out.println("Fail 4");
                Toast.makeText(this, "This object has no story.", Toast.LENGTH_LONG).show();
                Uri audioFileUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.empty);
                commentaryInstruction.onPlay(audioFileUri, false, null, "HomeScreen");
            }
        }
    }

    //Paint all the background image views one of three possible colours.
    void paintViews() {

        //Cycle through each image view in IvDrawable and set each a default colour.
        for(int i=0; i<16; i++) {

            Drawable d = VectorDrawableCompat.create(getResources(), IvDrawable.get(allViews[i]), null);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, android.graphics.Color.rgb(111,133,226));
            allViews[i].setImageDrawable(d);
        }
    }

    //Toggle the visibility of submitted image views on and off.
    private static void toggleVisibility(View... views) {
        for (View view : views) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    //Log out of the app
    //To do: delete all temporarily downloaded cloud files on logout
    public void LogOut(View view) {

        //Stop clickability of other imageViews
        disableViewClickability();
        //Play logout message
        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.goodbye), false, HomeScreen.class, "HomeScreen");
        //Undertake explode animation and return to welcome screen
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 1s = 1000ms

        Transition explode = new Explode();

        explode.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {

                Intent intent = new Intent(AboutAndLogout.this, WelcomeScreen.class);
                AboutAndLogout.this.startActivity(intent);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(mRootView, explode);
        toggleVisibility(backgroundShapes, zigzag1, zigzag2, zigzag3, zigzag4, star, moon, shell, book, key,
                leaf, umbrella, tear, teddy, halfcircle, heart, back, back2, troveTitle, troveBody, hamburgerButton);

            }
        }, 1000);

    }

    //Make all views in this activity unclickable.
    void disableViewClickability() {

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.activity_hamburger_kids_ui);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setClickable(false);
        }
    }

    //Reset folder delete counter
    public void Reset(View view) {

        deleteCounter++;
        deleteDirectories();
    }

    //Delete story directory folders
    void deleteDirectories() {


        if(deleteCounter>5) {

            Toast.makeText(this, "Resetting archive", Toast.LENGTH_LONG).show();

            File story_directory = new File (getFilesDir() + File.separator + "Stories");
            File tag_directory = new File (getFilesDir() + File.separator + "Tag");
            File cover_directory = new File (getFilesDir() + File.separator + "Covers");
            File cloud_directory = new File (getFilesDir() + File.separator + "Cloud");

            if (story_directory != null && !newStoryReady) {
                deleteStoryDirectory(story_directory);
            }

            if (tag_directory != null && !newStoryReady) {
                deleteTagDirectory(tag_directory);
            }

            if (cover_directory != null && !newStoryReady) {
                deleteCoverDirectory(cover_directory);
            }
            if (cloud_directory != null && !newStoryReady) {
                deleteCloudDirectory(cloud_directory);
            }
        }
    }

    //Delete cloud directory folder
    void deleteCloudDirectory(File cloud_directory) {

        try {

            FileUtils.cleanDirectory(cloud_directory);
        }

        catch (IOException e) {

        }

    }

    //Delete story directory folder
    void deleteStoryDirectory(File story_directory) {

        try {

            FileUtils.cleanDirectory(story_directory);
        }

        catch (IOException e) {

        }

    }

    //Delete tag directory folder
    void deleteTagDirectory(File tag_directory) {

        try {

            FileUtils.cleanDirectory(tag_directory);
        }

        catch (IOException e) {

        }

    }

    //Delete cover directory folder
    void deleteCoverDirectory(File cover_directory) {

        try {

            FileUtils.cleanDirectory(cover_directory);
        }

        catch (IOException e) {

        }

    }

    //onBackPressed
    @Override
    public void onBackPressed() {

        back.setClickable(false);
        back.setImageDrawable(backRetrace);
        backRetrace.start();
        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.goodbye), false, AboutAndLogout.class, "HomeScreen");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                Intent intent = new Intent(AboutAndLogout.this, WelcomeScreen.class);
                AboutAndLogout.this.startActivity(intent);
            }
        }, 1000);

    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
//        nfcInteraction.ReadModeOff(adapter);
    }

    @Override
    public void onPause(){
        super.onPause();
        nfcInteraction.ReadModeOff(adapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        nfcInteraction.ReadModeOn(adapter, pendingIntent, readTagFilters);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        authenticated = savedInstanceState.getBoolean("Authenticated");
        previousActivity = savedInstanceState.getString("PreviousActivity");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean("Authenticated", authenticated);
        savedInstanceState.putString("PreviousActivity", previousActivity);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}
