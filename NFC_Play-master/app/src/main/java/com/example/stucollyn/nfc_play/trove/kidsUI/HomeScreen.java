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
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/*
This activity acts as the app's home page. Within this activity users are able to scan object's NFC tags to be read, with their content being displayed on screen.
This is also the first activity to incorporate the hamburger menu where users can find out additional information about the app and log out.
 */

public class HomeScreen extends FragmentActivity {

    //The View group and ImageViews displayed on the activity layout
    ViewGroup mRootView;
    ImageView backgroundShapes, zigzag1, zigzag2, zigzag3, zigzag4, star, moon, shell, book, key,
            leaf, umbrella, tear, teddy, heart, trove, back, halfcircle;

    //The animations used on the ImageViews
    Animation bounce, fadein, alpha, fadeout;
    AnimatedVectorDrawable backRetrace;

    //Arrays for grouping specific views together
    ImageView allViews[];

    //Hash map which couples each image view with an image resource. This is used later in the activity to load image recources into corresponding ImageViews.
    HashMap<ImageView, Integer> IvDrawable;

    //Handlers, runnables, and logical components governing the appearance, timing and repetition of animations
    Integer[] paintColourArray;
    int paintColourArrayInt = 0;

    //NFC components and variables
    NFCInteraction nfcInteraction; //Object to handle nfc interactions
    Tag mytag;  //Used to identify individual NFC tags
    NfcAdapter adapter; //Adapter used to receive data from tag
    PendingIntent pendingIntent;    //Intent given to NFC software external to the application to launch new intent in activity
    IntentFilter readTagFilters[];  //The intent filter handling tag reading

    //Story playback components and variables
    ShowStoryContent showStoryContent;
    CommentaryInstruction commentaryInstruction;

    //Firebase - networked activities
    boolean authenticated = false; //if connected to a network, authenticated is set to true

    //Hamburger menu control
    ImageView hamburgerButton;
    NavigationView navigationView;

    //Activity governance
    String previousActivity;

    //onCreate is called when the activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Layout associated with this activity
        setContentView(R.layout.activity_home_screen_kids_ui);
        //Ensure screen always stays on and never dims
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Initialize views
        initializeViews();
        //Check for data connection before allowing user to sign in via cloud account
        checkConnection();
//        authenticated = (Boolean) getIntent().getExtras().get("Authenticated"); //This can alternatively be used if the value of authenticated is passed from another activity
        //Initialize commentary instructions for trove's voice
        commentaryInstruction = new CommentaryInstruction(this, this, false, authenticated);
        //Animate the image views
        animateViews();
        //Check what the previous activity was and what action is required
        checkPreviousActivity();
        //Setup and initialize NFC functionality
        nfcSetup();
    }

    //Initialize all views in activity
    void initializeViews() {

        //Initialize the root group of image views to control multiple views in a single command
        mRootView = (ViewGroup) findViewById(R.id.activity_logged_in_read_home);
        //Initialize drawer button for hamburger menu
        hamburgerButton = findViewById(R.id.drawer_button);

        //Initialize the four different colours used to denote background object image views.
        paintColourArray = new Integer[4];
        paintColourArray[0] = android.graphics.Color.rgb(255, 157, 0);
        paintColourArray[1] = android.graphics.Color.rgb(253, 195, 204);
        paintColourArray[2] = android.graphics.Color.rgb(0, 235, 205);
        paintColourArray[3] = Color.WHITE;

        //Initialize the image views themselves.
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
        trove = (ImageView) findViewById(R.id.trove);
        back = (ImageView) findViewById(R.id.back);
        backRetrace = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_back_anim_retrace);

        //Hashmap which we can use to reference the image resource of each imageview.
        IvDrawable = new HashMap<ImageView, Integer>();
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
        allViews[9] = key;
        allViews[10] = shell;
        allViews[11] = teddy;
        allViews[12] = tear;
        allViews[13] = umbrella;
        allViews[14] = heart;
        allViews[15] = back;
        allViews[16] = trove;

        //Initialize animations
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_infinite);
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

    //Setup nfc components
    void nfcSetup() {

        //Initialize NFCInteraction object which handles the logic for any NFC interactions.
        nfcInteraction = new NFCInteraction(this, this);
        //Initialize adapter to gather NFC tag data.
        adapter = NfcAdapter.getDefaultAdapter(this);
        //Enable external NFC software to launch an intent within this activity.
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        readTagFilters = new IntentFilter[] {tagDetected };
    }

    //When the hamburger menu is clicked, run this method.
    public void Hamburger(View view){

        //Ensure no other views can interrupt process by removing their clickability.
        disableViewClickability();
        //Stop the trove logo from animating.
        trove.clearAnimation();
        //Stop any current commentary.
        commentaryInstruction.stopPlaying();
        //Launch Hamburger activity using slide animation.
        Intent intent = new Intent(HomeScreen.this, AboutAndLogout.class);
        intent.putExtra("Authenticated", authenticated);
        intent.putExtra("PreviousActivity", "HomeScreen");
        HomeScreen.this.startActivity(intent);
        overridePendingTransition(R.anim.left_to_right_slide_in_activity, R.anim.left_to_right_slide_out_activity);
    }

    //Check what the previous activity was and take appropriate action. This method could be extended to provide additional functionality.
    void checkPreviousActivity(){

        //Get previous activity String value passed in last activity change intent.
        previousActivity = (String) getIntent().getExtras().get("PreviousActivity");

        //If previous activity is Login, play relevant trove instruction.
        if(previousActivity.equals("Login")) {

            //Uri of audio source file.
            Uri audioFileUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.welcomehome);
            //Play audio file using CommentaryInstruction object.
            commentaryInstruction.onPlay(audioFileUri, false, null, "HomeScreen");
        }

        //If previous activity is RecordStory, check whether there is a newly recorded story to review and, if so, play it.
        else if(previousActivity.equals("RecordStory")) {

            //Check value of the newStory variable passed from the previous change activity intent.
            boolean newStory = (Boolean) getIntent().getExtras().get("NewStory");

            //If there is a new story, get the story reference number for locally stored tag files (an image and an audio file), retrieve the files at the location, and then play the files.
            if(newStory) {


                /* The following two lines of code can be used to return tag story content from external rather than internal storage.
                // String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/validStoryFolders/Tag/" + storyRef;
                // File directory = new File(path);
                */

                //Get storyRef reference number.
                String storyRef = (String) getIntent().getExtras().get("storyRef");
                //Find the file directory with the corresponding name.
                File directory = new File (getFilesDir() + File.separator + "Tag" + File.separator + storyRef);
                //List the files at the directory and save them in an array.
                File[] filesOnTag = directory.listFiles();
                //Play the files listed.
                playStory(filesOnTag);
            }

            else {

                //Source of audio instruction advising user how to record a new story or to play back an existing story using an NFC tag.
                Uri audioFileUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.scanpage);
                commentaryInstruction.onPlay(audioFileUri, false, null, "HomeScreen");
            }
        }

        else {

            //Source of audio instruction advising user how to record a new story or to play back an existing story using an NFC tag.
            Uri audioFileUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.scanpage);
            commentaryInstruction.onPlay(audioFileUri, false, null, "HomeScreen");
        }
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

        //Cycle through each image view, finding their image resource and creating a new, painted version.
        for(int i=0; i<allViews.length; i++) {

            Drawable d = VectorDrawableCompat.create(getResources(), IvDrawable.get(allViews[i]), null);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, paintColourArray[paintColourArrayInt]);
            allViews[i].setImageDrawable(d);


            //For the final image view in the array - the trove logo - paint this white.
            if(i==16) {

                DrawableCompat.setTint(d, Color.WHITE);
                allViews[16].setImageDrawable(d);
            }


            //Increment the counter to change the type of paint. Return to the first colour after three.
            if(paintColourArrayInt<3) {

                paintColourArrayInt += 1;
            }
            else {

                paintColourArrayInt = 0;
            }
        }
    }

    //Handling the look and feel of the image views - their colour and then animation.
    void animateViews() {

        paintViews();
        startupTroveLogoAnimation();
    }

    //Animate the trove logo.
    void startupTroveLogoAnimation() {

        trove.startAnimation(bounce);
    }

    //Advance to the recording stories screen upon touching the trove logo.
    public void progressToRecordStory(View view) {

        //After the trove logo button is pressed, disable the clickability of image views.
        disableViewClickability();
        //Stop the trove logo from animating.
        trove.clearAnimation();
        //Stop any commentary instructions.
        commentaryInstruction.stopPlaying();
        //Launch RecordStory activity.
        Intent intent = new Intent(HomeScreen.this, RecordStory.class);
        intent.putExtra("Authenticated", authenticated);
        HomeScreen.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //Toggle the visibility of submitted image views on and off.
    private static void toggleVisibility(ImageView... views) {
        for (ImageView view : views) {
            boolean isVisible = view.getVisibility() == View.VISIBLE;
            view.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
        }
    }

    //Currently the back button on this screen is set as unclickable via its xml layout to encourage logout through the hamburger menu.
    //When the back button image view is pressed, forward to onBackPressed().
    public void Back(View view) {

        onBackPressed();
    }

    //When the back button is pressed, logout and return to login page.
    @Override
    public void onBackPressed() {

        //After the back button is pressed, disable the clickability of image views.
        disableViewClickability();
        //Disable the clickability of the back image view.
        back.setClickable(false);
        //Begin the retrace animation for the back image view.
        back.setImageDrawable(backRetrace);
        backRetrace.start();
        //Play the logout trove dialog.
        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.goodbye), false, HomeScreen.class, "HomeScreen");
        //Delay the change of activities for a second, to let the animations etc complete.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                Intent intent = new Intent(HomeScreen.this, WelcomeScreen.class);
                HomeScreen.this.startActivity(intent);
            }
        }, 1000);
    }

    //Make all views in this activity unclickable.
    void disableViewClickability() {

        back.setClickable(false);
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.activity_logged_in_read_home);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setClickable(false);
        }
    }

    //Execute upon stopping the activity.
    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
//        nfcInteraction.ReadModeOff(adapter);
    }

    //Execute upon pausing the activity.
    @Override
    public void onPause(){
        super.onPause();
        //Stop the activity from executing NFC interaction intents.
        nfcInteraction.ReadModeOff(adapter);
    }

    //Execute upon resuming the activity.
    @Override
    public void onResume(){
        super.onResume();
        //Start the activity from executing NFC interaction intents.
        nfcInteraction.ReadModeOn(adapter, pendingIntent, readTagFilters);
    }

    // Restore UI state from the savedInstanceState.
    // This bundle has also been passed to onCreate.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Get values of authenticated and previousActivity passed by the previous activity.
        authenticated = savedInstanceState.getBoolean("Authenticated");
        previousActivity = savedInstanceState.getString("PreviousActivity");
    }

    // Save the current state of these bundled variables.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Get values of authenticated and previousActivity passed by the previous activity and remember them until required.
        savedInstanceState.putBoolean("Authenticated", authenticated);
        savedInstanceState.putString("PreviousActivity", previousActivity);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}


/*
Some potentially useful code.
 */

//    void interrupt() {
//
//        commentaryInstruction.stopPlaying();
//    }

//    void delay() {
//
//        startupLargeObjectsHandler = new Handler();
//        startupLargeObjectsHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
////            progressToRecordStory();
//            }
//        }, 5000);
//
//    }

/*
        Transition explode = new Explode();


        explode.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {

                Intent intent = new Intent(HomeScreen.this, RecordStory.class);
                HomeScreen.this.startActivity(intent);
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
                leaf, umbrella, tear, teddy, halfcircle, heart, trove, back);

                */