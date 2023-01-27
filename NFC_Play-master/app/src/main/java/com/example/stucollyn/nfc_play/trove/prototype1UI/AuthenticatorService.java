package com.example.stucollyn.nfc_play.trove.prototype1UI;

/*

public class AuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */

/*
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

//For manifest
<service
        android:name="com.example.AuthenticatorService"
        android:enabled="true"
        android:exported="true" >
        <intent-filter>
            <action android:name="android.accounts.AccountAuthenticator" />
        </intent-filter>

        <meta-data
            android:name="android.accounts.AccountAuthenticator"
            android:resource="@xml/authenticator" />
    </service>

*/