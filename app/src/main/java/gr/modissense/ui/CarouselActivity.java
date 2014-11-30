

package gr.modissense.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;


import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.Blogs;
import gr.modissense.core.Constants;
import gr.modissense.core.ModisSenseService;
import gr.modissense.core.PoiSearchParams;
import gr.modissense.core.gps.GPSLoggingQueueService;
import gr.modissense.core.gps.GPSLoggingService;
import gr.modissense.ui.blog.BlogDetailsActivity;
import gr.modissense.ui.view.NonSwipeableViewPager;
import gr.modissense.util.SafeAsyncTask;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.Views;




/**
 * Activity to view the carousel and view pager indicator with fragments.
 */
public class CarouselActivity extends ModisSenseFragmentActivity {

    @Inject
    protected Bus BUS;

    @InjectView(R.id.vp_pages)
    NonSwipeableViewPager pager;
    @Inject
    ModisSenseServiceProvider serviceProvider;


    private boolean userHasAuthenticated = false;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.carousel_view);

        // VIew injection with Butterknife
        Views.inject(this);

        // Set up navigation drawer
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                    /* Host activity */
                mDrawerLayout,           /* DrawerLayout object */
                R.drawable.ic_drawer,    /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,    /* "open drawer" description */
                R.string.drawer_close) { /* "close drawer" description */

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        //getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);



        // Add 3 tabs, specifying the tab's text and TabListener

        checkAuth();

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BUS.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BUS.unregister(this);
    }

    @Subscribe
    public void updateSearchParams(PoiSearchParams poiSearchParams) {
        // this.searchParams = poiSearchParams;
        //System.out.println("********************************FORCING REFRESH IN ACTIVITY");
        //forceRefresh();

    }

    private void initScreen() {
        if (userHasAuthenticated) {

            pager.setAdapter(new ModisSensePagerAdapter(getResources(), getSupportFragmentManager()));
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    pager.setCurrentItem(tab.getPosition());
                    mDrawerLayout.closeDrawers();
                }

                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // hide the given tab
                }

                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // probably ignore this event
                }
            };
            for (int i = 0; i < pager.getAdapter().getCount(); i++) {
                getSupportActionBar().addTab(
                        getSupportActionBar().newTab()
                                .setText(pager.getAdapter().getPageTitle(i))
                                .setIcon(((ModisSensePagerAdapter)pager.getAdapter()).getPageIcon(i))
                                .setTabListener(tabListener));
            }
            //pager.setCurrentItem(1);
            startService(new Intent(this, GPSLoggingService.class));
        }

        setNavListeners();
    }



    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
//                final Account[] accounts = AccountManager.get(CarouselActivity.this).getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
//                if(accounts.length > 0) {
//                    AccountManagerFuture<Boolean> removeAccountFuture = AccountManager.get(CarouselActivity.this).removeAccount
//                            (accounts[0], null, null);
//                    removeAccountFuture.getResult();
//                }
                final ModisSenseService svc = serviceProvider.getService(CarouselActivity.this);
                return svc != null;

            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);
                if (e instanceof OperationCanceledException) {
                    // User cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                    finish();
                }
            }

            @Override
            protected void onSuccess(Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                userHasAuthenticated = true;
                initScreen();
            }
        }.execute();
    }

    private void setNavListeners() {
        findViewById(R.id.account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(3);
                getSupportActionBar().selectTab(getSupportActionBar().getTabAt(3));
                mDrawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.gpstraces).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(4);
                getSupportActionBar().selectTab(getSupportActionBar().getTabAt(4));
                mDrawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.menu_item_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(1);
                getSupportActionBar().selectTab(getSupportActionBar().getTabAt(1));
                mDrawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.blog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(2);
                getSupportActionBar().selectTab(getSupportActionBar().getTabAt(2));
                mDrawerLayout.closeDrawers();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:

                return true;
            case R.id.blog:
                getSupportActionBar().selectTab(getSupportActionBar().getTabAt(2));
                pager.setCurrentItem(2);
                return true;
            case R.id.gpstraces:
                getSupportActionBar().selectTab(getSupportActionBar().getTabAt(2));
                pager.setCurrentItem(4);
                return true;
            case R.id.account :
                getSupportActionBar().selectTab(getSupportActionBar().getTabAt(3));

                pager.setCurrentItem(3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
