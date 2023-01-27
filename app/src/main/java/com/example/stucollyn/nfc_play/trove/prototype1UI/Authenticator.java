package com.example.stucollyn.nfc_play.trove.prototype1UI;


/*
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;


public class Authenticator extends AbstractAccountAuthenticator {

    Context mContext;

    // Simple constructor
    public Authenticator(Context context) {

        super(context);
        mContext = context;
    }

    // Editing properties is not supported
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
        throw new UnsupportedOperationException();
    }

    // Don't add additional accounts
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {

        AccountManager am = AccountManager.get(mContext);
        if (ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // Checking to see if you can have a look at accounts present on the device.
            Log.d("Authenticator", "GET_ACCOUNTS not present.");
        }

        if (UserAccountUtil.getAccount(mContext) != null) {
            // This means there's an account present already. If you don't want to support multiple accounts, keep this.
            // This is how you report an error occurred.
            final Bundle result = new Bundle();

            result.putInt(AccountManager.KEY_ERROR_CODE, 400);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, mContext.getResources().getString(R.string.one_account_allowed));

            return result;
        }

        final Intent intent = new Intent(mContext, AccountsActivity.class);

        // This key can be anything. Try to use your domain/package
        intent.putExtra("com.pilanites.streaks", accountType);

        // This key can be anything too. It's just a way of identifying the token's type (used when there are multiple permissions)
        intent.putExtra("full_access", authTokenType);

        // This key can be anything too. Used for your reference. Can skip it too.
        intent.putExtra("is_adding_new_account", true);

        // Copy this exactly from the line below.
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    // Ignore attempts to confirm credentials
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse r, Account account, Bundle bundle) {
        return null;
    }

    // Implement this method if you want to save authToken with the account, great for using the inbuilt sync functionality.
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        
        AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            authToken = HTTPNetwork.login(account.name, am.getPassword(account));
        }


        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If you reach here, person needs to login again. or sign up

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity which is the AccountsActivity in my case.
        final Intent intent = new Intent(mContext, AccountsActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra("com.pilanites.streaks", account.type);
        intent.putExtra("full_access", authTokenType);

        Bundle retBundle = new Bundle();
        retBundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return retBundle;

    }

    // Getting a label for the auth token is not supported
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException();
    }

    // Updating user credentials is not supported
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse r, Account account, String s, Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    // Checking features for the account is not supported
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse r, Account account, String[] strings) {
        throw new UnsupportedOperationException();
    }

    // Handle a user logging out here.
    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) {
        return super.getAccountRemovalAllowed(response, account);
    }
}

*/