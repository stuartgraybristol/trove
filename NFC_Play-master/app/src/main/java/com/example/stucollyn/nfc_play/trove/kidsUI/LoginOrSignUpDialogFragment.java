package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.stucollyn.nfc_play.*;

/*
This fragment class launches a dialog fragment allowing the user to enter sign up credentials.
 */

public class LoginOrSignUpDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface LoginOrSignUpDialogListener {

        public void onLoginButton();
        public void onSignUpButton();
    }

    // Use this instance of the interface to deliver action events
    LoginOrSignUpDialogFragment.LoginOrSignUpDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (LoginOrSignUpDialogFragment.LoginOrSignUpDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View prompt;
        AlertDialog.Builder builder;
        LayoutInflater inflater;
        builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
//        builder.setView(inflater.inflate(R.layout.dialog_signin, null));
        prompt = inflater.inflate(R.layout.dialog_login_or_signup_kids_ui, null);
        builder.setView(prompt);
        ImageView trove = (ImageView) prompt.findViewById(R.id.trove);
        ImageButton login = (ImageButton) prompt.findViewById(R.id.login);
        ImageButton signup = (ImageButton) prompt.findViewById(R.id.signup);


        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.i("HELP HELP",  "HEY");
                mListener.onLoginButton();
                dismiss();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.i("HELP HELP",  "HEY");
                mListener.onSignUpButton();
                dismiss();
            }
        });

        return builder.create();
    }
}