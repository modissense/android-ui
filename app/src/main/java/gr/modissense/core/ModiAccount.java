package gr.modissense.core;

import gr.modissense.authenticator.ModisSenseAuthDialog;



public class ModiAccount {
    private ModisSenseAuthDialog.Provider provider;
    private boolean connected = false;
    public ModiAccount() {
    }

    public ModiAccount(ModisSenseAuthDialog.Provider provider) {
        this.provider = provider;
    }

    public ModisSenseAuthDialog.Provider getProvider() {
        return provider;
    }

    public void setProvider(ModisSenseAuthDialog.Provider provider) {
        this.provider = provider;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
