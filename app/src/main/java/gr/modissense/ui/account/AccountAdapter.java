package gr.modissense.ui.account;

import android.view.LayoutInflater;
import gr.modissense.R;
import gr.modissense.core.ModiAccount;
import gr.modissense.ui.view.AlternatingColorListAdapter;


import java.util.List;

public class AccountAdapter extends AlternatingColorListAdapter<ModiAccount> {


    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public AccountAdapter(LayoutInflater inflater, List<ModiAccount> items,
                          boolean selectable) {
        super(R.layout.providers_list, inflater, items, selectable);

    }

    /**
     * @param inflater
     * @param items
     */
    public AccountAdapter(LayoutInflater inflater, List<ModiAccount> items) {
        super(R.layout.providers_list, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.provider, R.id.providerText,
                R.id.signstatus};
    }

    @Override
    protected void update(int position, ModiAccount item) {
        super.update(position, item);
        String textCase = item.getProvider().toString();
        textCase = String.valueOf(textCase.charAt(0)).toUpperCase() + textCase.substring(1, textCase.length());
        imageView(0).setImageResource(getImageResource(item));
        setText(1, textCase);
        setText(2, item.isConnected() ? "(Connected) Click To Sign Out" : "Click To Sign In");

        //setNumber(R.id.tv_date, item.getCreatedAt());
    }

    private int getImageResource(ModiAccount item) {
        switch (item.getProvider()) {
            case TWITTER:
                return R.drawable.twitter;
            case FACEBOOK:
                return R.drawable.facebook;
            case FOURSQUARE:
                return R.drawable.foursquare;
            default:
                return R.drawable.facebook;
        }
    }
}
