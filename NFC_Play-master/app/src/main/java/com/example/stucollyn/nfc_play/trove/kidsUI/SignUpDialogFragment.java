package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.stucollyn.nfc_play.R;

/**
 * Created by StuCollyn on 20/07/2018.
 */

/*
This DialogFragment is used to display a sign up dialog box, allowing users to enter their details for signing up to trove.
 */

public class SignUpDialogFragment extends DialogFragment {

    String username = "";
    String password = "";
    String firstName = "";
    String lastName = "";

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeSignUpDialogListener {
        public void onDialogSignUpPositiveClick(String username, String password, String firstName, String lastName);
        public void onDialogSignUpNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    SignUpDialogFragment.NoticeSignUpDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
//            mListener = (SignUpDialogFragment.NoticeSignUpDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    //Run when the dialog box is created
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View prompt = inflater.inflate(R.layout.dialog_signup_kids_ui, null);
        builder.setView(prompt);
        final EditText user = (EditText) prompt.findViewById(R.id.username);
        final EditText pass = (EditText) prompt.findViewById(R.id.password);
//        final EditText fn = (EditText) prompt.findViewById(R.id.firstname);
//        final EditText ln = (EditText) prompt.findViewById(R.id.lastname);



        // Add action buttons
        builder.setPositiveButton("SignUp", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // sign in the user ...
//                firstName = fn.getText().toString();
//                lastName = ln.getText().toString();
                password = pass.getText().toString();
                username = user.getText().toString();
                mListener.onDialogSignUpPositiveClick(username, password, firstName, lastName);
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SignUpDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
