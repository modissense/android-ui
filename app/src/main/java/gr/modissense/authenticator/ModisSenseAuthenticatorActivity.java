
package gr.modissense.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import butterknife.Views;
import com.github.kevinsawicki.wishlist.Toaster;
import com.github.kevinsawicki.wishlist.ViewUtils;
import gr.modissense.ModisSenseApplication;
import gr.modissense.R.id;
import gr.modissense.R.layout;
import gr.modissense.R.string;
import gr.modissense.core.Constants;
import gr.modissense.core.ModiAccount;
import gr.modissense.ui.account.AccountAdapter;
import gr.modissense.util.DialogListener;
import gr.modissense.util.Ln;
import gr.modissense.util.ModiAuthError;
import gr.modissense.util.SafeAsyncTask;

import java.util.Arrays;
import java.util.List;

import static android.accounts.AccountManager.*;

/**
 * Activity to authenticate the user against an API (example API on Parse.com)
 */
public class ModisSenseAuthenticatorActivity extends SherlockAccountAuthenticatorActivity {

    /**
     * PARAM_CONFIRMCREDENTIALS
     */
    public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
    /**
     * PARAM_PASSWORD
     */
    public static final String PARAM_PASSWORD = "password";
    /**
     * PARAM_USERNAME
     */
    public static final String PARAM_USERNAME = "username";
    /**
     * PARAM_AUTHTOKEN_TYPE
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    private final Handler handler = new Handler();
    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;
    /**
     * *************NEW*************
     */

    protected List<ModiAccount> items = Arrays.asList(new ModiAccount(ModisSenseAuthDialog.Provider.FACEBOOK),
            new ModiAccount(ModisSenseAuthDialog.Provider.TWITTER),
            new ModiAccount(ModisSenseAuthDialog.Provider.FOURSQUARE));
    /**
     * List view
     */
    protected ListView listView;
    /**
     * Empty view
     */
    protected TextView emptyView;
    /**
     * Progress bar
     */
    protected ProgressBar progressBar;
    /**
     * Is the list currently shown?
     */
    protected boolean listShown;

    private AccountManager accountManager;
    private SafeAsyncTask<Boolean> authenticationTask;
    private String authToken;
    private String authTokenType;
    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean confirmCredentials = false;
    private String email;
    private String password;
    /**
     * In this instance the token is simply the sessionId returned from Parse.com. This could be a
     * oauth token or some other type of timed token that expires/etc. We're just using the parse.com
     * sessionId to prove the example of how to utilize a token.
     */
    private String token;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ModisSenseApplication.getInstance().inject(this);
        accountManager = AccountManager.get(this);
        final Intent intent = getIntent();
        email = intent.getStringExtra(PARAM_USERNAME);
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        requestNewAccount = email == null;
        confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS,
                false);

        setContentView(layout.login_activity);

        Views.inject(this);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onListItemClick((ListView) parent, view, position, id);
            }
        });
        progressBar = (ProgressBar) findViewById(id.pb_loading);

        emptyView = (TextView) findViewById(android.R.id.empty);
        listView.setAdapter(new AccountAdapter(getLayoutInflater(), items));
        ViewUtils.setGone(progressBar, true);
        ViewUtils.setGone(listView, false);


    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        final ModiAccount news = ((ModiAccount) l.getItemAtPosition(position));
        ModisSenseAuthDialog.startDialogAuth(ModisSenseAuthenticatorActivity.this,
                news.getProvider(), new Handler(),  new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
                try {
                    setEmail(values.getString("uid"));
                    setToken(values.getString("uid"));
                    finishLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ModiAuthError e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onCancel() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onBack() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithValidation();
    }

    private void updateUIWithValidation() {
        //boolean populated = populated(emailText) && populated(passwordText);
        //signinButton.setEnabled(populated);
    }

    private boolean populated(EditText editText) {
        return editText.length() > 0;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(string.message_signing_in));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (authenticationTask != null)
                    authenticationTask.cancel(true);
            }
        });
        return dialog;
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * <p/>
     * Specified by android:onClick="handleLogin" in the layout xml
     *
     * @param view
     */
//    public void handleLogin(View view) {
//        if (authenticationTask != null)
//            return;
//
//        if (requestNewAccount)
//            email = emailText.getText().toString();
//        password = passwordText.getText().toString();
//        showProgress();
//
//        authenticationTask = new SafeAsyncTask<Boolean>() {
//            public Boolean call() throws Exception {
//
//                final String query = String.format("%s=%s&%s=%s", PARAM_USERNAME, email, PARAM_PASSWORD, password);
//
//                HttpRequest request = get(URL_AUTH + "?" + query)
//                        .header(HEADER_PARSE_APP_ID, PARSE_APP_ID)
//                        .header(HEADER_PARSE_REST_API_KEY, PARSE_REST_API_KEY);
//
//
//                Ln.d("Authentication response=%s", request.code());
//
//                if (request.ok()) {
//                    final User model = new Gson().fromJson(Strings.toString(request.buffer()), User.class);
//                    token = model.getSessionToken();
//                }
//
//                return request.ok();
//            }
//
//            @Override
//            protected void onException(Exception e) throws RuntimeException {
//                Throwable cause = e.getCause() != null ? e.getCause() : e;
//
//                String message;
//                // A 404 is returned as an Exception with this message
//                if ("Received authentication challenge is null".equals(cause
//                        .getMessage()))
//                    message = getResources().getString(
//                            string.message_bad_credentials);
//                else
//                    message = cause.getMessage();
//
//                Toaster.showLong(ModisSenseAuthenticatorActivity.this, message);
//            }
//
//            @Override
//            public void onSuccess(Boolean authSuccess) {
//                onAuthenticationResult(authSuccess);
//            }
//
//            @Override
//            protected void onFinally() throws RuntimeException {
//                hideProgress();
//                authenticationTask = null;
//            }
//        };
//        authenticationTask.execute();
//    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result
     */
    protected void finishConfirmCredentials(boolean result) {
        final Account account = new Account(email, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        accountManager.setPassword(account, password);

        final Intent intent = new Intent();
        intent.putExtra(KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. Also sets
     * the authToken in AccountManager for this account.
     */

    protected void finishLogin() {
        final Account account = new Account(email, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        if (requestNewAccount)
            accountManager.addAccountExplicitly(account, password, null);
        else
            accountManager.setPassword(account, password);
        final Intent intent = new Intent();
        authToken = token;
        intent.putExtra(KEY_ACCOUNT_NAME, email);
        intent.putExtra(KEY_ACCOUNT_TYPE, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        if (authTokenType != null
                && authTokenType.equals(Constants.Auth.AUTHTOKEN_TYPE)){
            intent.putExtra(KEY_AUTHTOKEN, authToken);
            accountManager.setAuthToken(account, authTokenType, authToken);
        }
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Hide progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void hideProgress() {
        dismissDialog(0);
    }

    /**
     * Show progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void showProgress() {
        showDialog(0);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param result
     */
    public void onAuthenticationResult(boolean result) {
        if (result)
            if (!confirmCredentials)
                finishLogin();
            else
                finishConfirmCredentials(true);
        else {
            Ln.d("onAuthenticationResult: failed to authenticate");

                Toaster.showLong(ModisSenseAuthenticatorActivity.this,
                        string.message_auth_failed);
        }
    }
}
