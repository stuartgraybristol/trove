package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.example.stucollyn.nfc_play.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


/*LoginScreen Activity follows after the SplashScreen introduction and allows existing users to
login to the trove app or new users sign up*/
public class LoginScreen extends FragmentActivity implements LoginDialogFragment.NoticeLoginDialogListener, SignUpDialogFragment.NoticeSignUpDialogListener {

    //Declare global variables
    private static final String TAG = "miine App: ";
    Animation welcome_fade_in, welcome_fade_out, welcome2_fade_in, welcome2_fade_out, login_fade_in, login_fade_out, signup_fade_in, signup_fade_out, logout_fade_in,
            logout_fade_out, miine_fade_in, miine_fade_out, miine_shrink, miine_shake,
            miine_open_fade_in, miine_open_fade_out, instruction_fade_in, instruction_fade_out,
            balloon_move_normal, balloon_move_slower, balloon_move_faster, passcode_box_fade_out;
    Button loginButton, signupButton;
    Button logoutButton;
    TextView welcome, welcome2, instruction, passCodeBox1, passCodeBox2, passCodeBox3, passCodeBox4;
    ImageView keypad_background, miine, miine_open, balloon1, balloon2, balloon3;
    TableLayout keypad, passCodeBoxTable;
    StringBuilder passcodeAppend;
    String passcodeAttempt, passcodeTarget;
    int passcodeCounter;
    int mode;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    boolean isNetworkConnected;

    //onCreate method called on Activity start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Access a Cloud Firestore instance from your Activity

//        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Initialize String builder to accept passcode keypad input
        passcodeAppend = new StringBuilder("");
        passcodeAttempt = "";
        passcodeTarget = "1234";
        passcodeCounter = 0;

        //Initialize Activity views and animations, and display
        InitAnimation();
        InitView();
        FadeInLogin();
        isNetworkConnected = isNetworkConnected();

        if(!isNetworkConnected) {

            logoutButton.setVisibility(View.INVISIBLE);
            signupButton.setVisibility(View.INVISIBLE);
            loginButton.setText("progressToRecordStory");
        }

        else {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {

                logoutButton.setVisibility(View.VISIBLE);
                signupButton.setVisibility(View.INVISIBLE);
                loginButton.setText("progressToRecordStory");
                welcome2.setText(user.getEmail());
                welcome2.setGravity(Gravity.CENTER);

                welcome2.startAnimation(welcome_fade_in);
                logoutButton.startAnimation(logout_fade_in);
            } else {
                signupButton.startAnimation(logout_fade_in);
                signupButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isNetworkConnected() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void showLoginNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LoginDialogFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onLoginDialogPositiveClick(String username, String password) {
        // User touched the dialog's positive button
        Log.i("username: ", username);
        Log.i("password: ", password);

        FirebaseLogin(username, password);
    }

    @Override
    public void onLoginDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    public void showSignUpNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new SignUpDialogFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogSignUpPositiveClick(String username, String password, String firstName, String lastName) {
        // User touched the dialog's positive button
        Log.i("good", "success");

        FirebaseSignUp(username, password, firstName, lastName);
    }

    @Override
    public void onDialogSignUpNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    //Initialize the single view animations used in the class
    private void InitAnimation() {

        welcome_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        welcome_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        welcome2_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        welcome2_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        login_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        login_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        signup_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        signup_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        logout_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        logout_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        passcode_box_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        miine_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        miine_shrink = AnimationUtils.loadAnimation(this, R.anim.trove_shrink);
        miine_shake = AnimationUtils.loadAnimation(this, R.anim.trove_shake);
        miine_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        miine_open_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        miine_open_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        instruction_fade_in = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_in);
        instruction_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        balloon_move_normal = AnimationUtils.loadAnimation(this, R.anim.balloon_move_normal);
        balloon_move_slower = AnimationUtils.loadAnimation(this, R.anim.balloon_move_slower);
        balloon_move_faster = AnimationUtils.loadAnimation(this, R.anim.balloon_move_faster);
    }

    //Initialize the single views used in the Activity
    private void InitView() {

        welcome = (TextView) findViewById(R.id.welcome_text);
        welcome2 = (TextView) findViewById(R.id.welcome_text2);
        loginButton = (Button) findViewById(R.id.login_button);
        logoutButton = (Button) findViewById(R.id.logout_button);
        signupButton = (Button) findViewById(R.id.signup_button);
        miine = (ImageView) findViewById(R.id.miine);
        balloon1 = (ImageView) findViewById(R.id.balloon1);
        balloon2 = (ImageView) findViewById(R.id.balloon2);
        balloon3 = (ImageView) findViewById(R.id.balloon3);
        miine_open = (ImageView) findViewById(R.id.miine_open);
        instruction = (TextView) findViewById(R.id.instruction_text);
    }

    //Initial fade in animations for trove logo, login and sign up buttons
    private void FadeInLogin() {

        miine.startAnimation(miine_fade_in);
        welcome.startAnimation(welcome_fade_in);
        loginButton.startAnimation(login_fade_in);
//        welcome2.startAnimation(welcome_fade_in);
//        signupButton.startAnimation(logout_fade_in);
    }

    public void SignUp(View signUp) {

        showSignUpNoticeDialog();
    }

    //When login button is pressed execute the following
    public void Login(View login) {

        if(!isNetworkConnected) {

            AdvanceOffline();
        }

        else {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Name, email address, and profile photo Url
                String name = user.getDisplayName();
                String email = user.getEmail();
//            Uri photoUrl = user.getPhotoUrl();

                // Check if user's email is verified
                boolean emailVerified = user.isEmailVerified();

                Toast.makeText(LoginScreen.this, "Already logged in as: " + user.getEmail(),
                        Toast.LENGTH_SHORT).show();
                Advance();
            } else {

                showLoginNoticeDialog();

            }

        }
    }

    void FirebaseDatabaseNewUser(String username, String password, String firstName, String lastName) {

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("Username", username);
        newUser.put("Password", password);
        newUser.put("First Name", firstName);
        newUser.put("Last Name", lastName);

        db.collection("User").document(username)
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    void FirebaseSignUp(final String username, final String password, final String firstName, final String lastName) {

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(LoginScreen.this, "Account Created.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseDatabaseNewUser(username, password, firstName, lastName);
                            FirebaseLogin(username, password);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    void FirebaseLogin(String username, String password) {

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginScreen.this, "Logging in as: " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            welcome2.setText(user.getEmail());
                            welcome2.startAnimation(welcome2_fade_in);
                            welcome2.setVisibility(View.VISIBLE);
                            signupButton.startAnimation(signup_fade_out);
                            signupButton.setVisibility(View.INVISIBLE);
                            Advance();
                        }

                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    //Balloon animation scheduler
    private void AnimateBalloons() {

        miine_open.setVisibility(View.VISIBLE);

        //If no balloons have started, start the slow, fast, and normal paced balloon animations
        if (!balloon_move_slower.hasStarted()) {

            balloon1.startAnimation(balloon_move_normal);
            balloon2.startAnimation(balloon_move_slower);
            balloon3.startAnimation(balloon_move_faster);
        }

        //If the slowest and final balloon has finished, start next wave of balloons
        if (balloon_move_slower.hasEnded()) {

            balloon1.startAnimation(balloon_move_normal);
            balloon2.startAnimation(balloon_move_slower);
            balloon3.startAnimation(balloon_move_faster);
        }

        //Listen for the success balloon animation to finish, then fade out trove logo
        balloon_move_faster.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                miine_open.startAnimation(miine_open_fade_out);
            }
        });

        //Listen for the trove logo to finish fading out, then start MainMenu Activity
        miine_open_fade_out.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                miine_open.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(LoginScreen.this, MainMenu.class);
                intent.putExtra("Orientation", mode);
                LoginScreen.this.startActivity(intent);

                //Activity fade transition
                overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        });
    }

    private void AdvanceOffline() {

        welcome.startAnimation(welcome_fade_out);
        welcome2.startAnimation(welcome_fade_out);
        loginButton.startAnimation(login_fade_out);
        miine_open.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.INVISIBLE);
        welcome.setVisibility(View.INVISIBLE);
        welcome2.setVisibility(View.INVISIBLE);
        miine.setClickable(false);
        welcome.setVisibility(View.INVISIBLE);

        login_fade_out.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                AnimateBalloons();
            }
        });

    }

    //When correct pass code is matched, start advancement animations, before opening new MainMenu Activity
    private void Advance() {


//        keypad_background.startAnimation(keypad_background_fade_out);
//        keypad.startAnimation(keypad_fade_out);
//        passCodeBoxTable.startAnimation(passcode_box_fade_out);
//        instruction.startAnimation(instruction_fade_out);
//        welcome.startAnimation(welcome_fade_out);
//        passCodeBoxTable.setVisibility(View.INVISIBLE);
//        instruction.setVisibility(View.INVISIBLE);

        welcome.startAnimation(welcome_fade_out);
        welcome2.startAnimation(welcome_fade_out);
        loginButton.startAnimation(login_fade_out);
        logoutButton.startAnimation(login_fade_out);
        signupButton.startAnimation(logout_fade_out);
        miine_open.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.INVISIBLE);
        logoutButton.setVisibility(View.INVISIBLE);
        signupButton.setVisibility(View.INVISIBLE);
        welcome.setVisibility(View.INVISIBLE);
        welcome2.setVisibility(View.INVISIBLE);
        miine.setClickable(false);
        welcome.setVisibility(View.INVISIBLE);

        login_fade_out.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                AnimateBalloons();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    public void Logout(View view) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(LoginScreen.this, "Logging out: " + user.getEmail(),
                Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();

        welcome2.startAnimation(welcome_fade_out);
        loginButton.setText("Login");
        signupButton.setVisibility(View.VISIBLE);
        welcome2.setVisibility(View.INVISIBLE);
        logout_fade_out = AnimationUtils.loadAnimation(this, R.anim.splash_screen_fade_out);
        logoutButton.setVisibility(View.VISIBLE);

    }


    //On back button pressed, restart Activity
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(LoginScreen.this, SelectMode.class);
        LoginScreen.this.startActivity(intent);
    }
}
