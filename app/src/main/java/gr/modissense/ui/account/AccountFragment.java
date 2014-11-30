package gr.modissense.ui.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.*;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.authenticator.ApiKeyProvider;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.authenticator.ModisSenseAuthDialog;
import gr.modissense.core.ModiAccount;
import gr.modissense.core.ModiResult;
import gr.modissense.core.ModiUserInfo;
import gr.modissense.core.gps.GpsStartEvent;
import gr.modissense.core.gps.GpsStopEvent;
import gr.modissense.ui.view.ItemListFragment;
import gr.modissense.ui.view.ThrowableLoader;
import gr.modissense.util.DialogListener;
import gr.modissense.util.ModiAuthError;
import gr.modissense.util.SafeAsyncTask;


import javax.inject.Inject;

import java.util.Collections;
import java.util.List;

import static android.app.AlertDialog.*;


public class AccountFragment extends ItemListFragment<ModiAccount> {

    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;
    @Inject
    ApiKeyProvider keyProvider;
    @Inject
    Bus bus;
    View headerView;
    Button deleteAccountButton;
    ToggleButton logGpsTraces;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_accounts);


    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
        headerView = activity.getLayoutInflater()
                .inflate(R.layout.accounts_list_item_labels, null);
        deleteAccountButton = (Button) headerView.findViewById(R.id.deleteaccount);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Builder builder = new Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this account?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new SafeAsyncTask<ModiResult>() {

                                    @Override
                                    public ModiResult call() throws Exception {
                                        return serviceProvider.getService(getActivity()).deleteUser(null);
                                    }

                                    @Override
                                    protected void onSuccess(ModiResult s) throws Exception {
                                        System.out.println(s);
                                        getLogoutService().logout(new Runnable() {
                                            @Override
                                            public void run() {
                                                getActivity().finish();
                                            }
                                        });
                                    }
                                }.execute();

                            }
                        })
                        .setNegativeButton("No", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
        logGpsTraces = (ToggleButton) headerView.findViewById(R.id.gpsbutton);
        final SharedPreferences prefs = activity.getSharedPreferences("gr.modissense", Context.MODE_PRIVATE);
        boolean isc = prefs.getBoolean("gpsoff",false);
        logGpsTraces.setChecked(!isc);
        logGpsTraces.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    prefs.edit().putBoolean("gpsoff",false).apply();
                    bus.post(new GpsStartEvent());
                } else {
                    prefs.edit().putBoolean("gpsoff",true).apply();
                    bus.post(new GpsStopEvent());
                }
            }
        });
        getListAdapter()
                .addHeader(headerView);
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    public Loader<List<ModiAccount>> onCreateLoader(int id, Bundle args) {
        final List<ModiAccount> initialItems = items;
        return new ThrowableLoader<List<ModiAccount>>(getActivity(), items) {

            @Override
            public List<ModiAccount> loadData() throws Exception {
                if (getActivity() != null) {
                    ModiUserInfo info = serviceProvider.getService(getActivity()).getUserInfo();
                    ModisSenseApplication.getInstance().friends = info;
                    System.out.println(ModisSenseApplication.getInstance().friends);
                    return serviceProvider.getService(getActivity()).getConnectedSocialAccounts();
                } else {
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<ModiAccount>> loader, List<ModiAccount> items) {
        super.onLoadFinished(loader, items);
        if(ModisSenseApplication.getInstance().friends !=null){
            ((TextView)headerView.findViewById(R.id.username_field)).setText(ModisSenseApplication.getInstance().friends.getUsername());
            if(ModisSenseApplication.getInstance().friends.getImage() != null && !"null".equals(ModisSenseApplication.getInstance().friends.getImage())) {
                Picasso.with(ModisSenseApplication.getInstance())
                        .load(ModisSenseApplication.getInstance().friends.getImage())
                        .placeholder(R.drawable.gravatar_icon)
                        .into((android.widget.ImageView) headerView.findViewById(R.id.userImage));

            }
        }
    }

    @Override
    protected SingleTypeAdapter<ModiAccount> createAdapter(List<ModiAccount> items) {
        return new AccountAdapter(getActivity().getLayoutInflater(), items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        final ModiAccount news = ((ModiAccount) l.getItemAtPosition(position));
        if (!news.isConnected()) {
            new SafeAsyncTask<String>() {

                @Override
                public String call() throws Exception {
                    return keyProvider.getAuthKey(getActivity());
                }

                @Override
                protected void onSuccess(String s) throws Exception {
                    ModisSenseAuthDialog.startDialogAuth(getActivity(),
                            news.getProvider(), new Handler(), new DialogListener() {
                        @Override
                        public void onComplete(Bundle values) {
                            forceRefresh();
                        }

                        @Override
                        public void onError(ModiAuthError e) {

                        }

                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onBack() {

                        }
                    }, s);
                }
            }.execute();
        } else {
            Builder builder = new Builder(getActivity());
            builder.setMessage("Are you sure you want to disconnect this account?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new SafeAsyncTask<ModiResult>() {

                                @Override
                                public ModiResult call() throws Exception {
                                    return serviceProvider.getService(getActivity()).deleteUser(news.getProvider().name().toLowerCase());
                                }

                                @Override
                                protected void onSuccess(ModiResult s) throws Exception {
                                    System.out.println(s);
                                    forceRefresh();
                                }
                            }.execute();

                        }
                    })
                    .setNegativeButton("No", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_accounts;
    }



}
