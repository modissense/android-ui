
package gr.modissense;

import android.accounts.AccountsException;
import android.app.Activity;

import com.google.android.gms.internal.ac;

import gr.modissense.authenticator.ApiKeyProvider;
import gr.modissense.core.ModisSenseService;
import gr.modissense.core.UserAgentProvider;
import javax.inject.Inject;

import java.io.IOException;

/**
 * Provider for a {@link gr.modissense.core.ModisSenseService} instance
 */
public class ModisSenseServiceProvider {

    @Inject ApiKeyProvider keyProvider;
    @Inject UserAgentProvider userAgentProvider;

    /**
     * Get service for configured key provider
     * <p>
     * This method gets an auth key and so it blocks and shouldn't be called on the main thread.
     *
     * @return bootstrap service
     * @throws java.io.IOException
     * @throws android.accounts.AccountsException
     */
    public ModisSenseService getService(Activity activity) throws IOException, AccountsException {
        return new ModisSenseService(keyProvider.getAuthKey(activity), userAgentProvider);
    }

    public void invalidateAccount(Activity activity) throws IOException, AccountsException {
        keyProvider.getAuthKeyFresh(activity);
    }
}
