

package gr.modissense.authenticator;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.app.Activity;
import android.os.Bundle;

import gr.modissense.core.Constants;
import javax.inject.Inject;

import java.io.IOException;

/**
 * Bridge class that obtains a API key for the currently configured account
 */
public class ApiKeyProvider {

    @Inject AccountManager accountManager;

    /**
     * This call blocks, so shouldn't be called on the UI thread
     *
     * @return API key to be used for authorization with a {@link gr.modissense.core.ModisSenseService} instance
     * @throws android.accounts.AccountsException
     * @throws java.io.IOException
     */
    public String getAuthKey(Activity activity) throws AccountsException, IOException {
        AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthTokenByFeatures(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE,
                Constants.Auth.AUTHTOKEN_TYPE, new String[0], activity, null, null, null, null);

        return accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
    }

    public String getAuthKeyFresh(Activity activity) throws AccountsException, IOException {
        AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthTokenByFeatures(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE,
                Constants.Auth.AUTHTOKEN_TYPE, new String[0], activity, null, null, null, null);

        String token = accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
        if(token!=null){
            accountManager.invalidateAuthToken(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE,token);
        }
        return accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
    }
}
