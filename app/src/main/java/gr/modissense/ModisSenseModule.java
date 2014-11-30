package gr.modissense;

import android.accounts.AccountManager;
import android.content.Context;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.authenticator.ModisSenseAuthenticatorActivity;
import gr.modissense.core.gps.GPSLogItem;
import gr.modissense.core.gps.GPSLoggingQueueService;
import gr.modissense.core.gps.GPSLoggingService;
import gr.modissense.core.gps.GPSLoggingTaskQueue;
import gr.modissense.ui.*;
import gr.modissense.ui.account.AccountFragment;
import gr.modissense.ui.blog.BlogDetailsActivity;
import gr.modissense.ui.blog.BlogFragment;
import gr.modissense.ui.blog.BlogHistoryFragment;
import gr.modissense.ui.blog.BlogMapFragment;
import gr.modissense.ui.blog.BlogSectionFragment;
import gr.modissense.ui.blog.VisitAddDialog;
import gr.modissense.ui.blog.VisitUpdateDialog;
import gr.modissense.ui.gps.GPSFragment;
import gr.modissense.ui.poi.PoiAddDialog;
import gr.modissense.ui.poi.PoiEditDialog;
import gr.modissense.ui.poi.PoiMapFragment;
import gr.modissense.ui.poi.PoiNearestFragment;
import gr.modissense.ui.poi.PoiSearchActivity;
import gr.modissense.ui.poi.PoiSelectDialog;
import gr.modissense.ui.poi.PoiViewActivity;

import javax.inject.Singleton;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
        (
                complete = false,

                injects = {
                        ModisSenseApplication.class,
                        ModisSenseAuthenticatorActivity.class,
                        CarouselActivity.class,
                        AccountFragment.class,
                        PoiMapFragment.class,
                        PoiSearchActivity.class,
                        PoiViewActivity.class,
                        PoiAddDialog.class,
                        PoiEditDialog.class,
                        PoiSelectDialog.class,
                        GPSLoggingTaskQueue.class,
                        GPSLoggingQueueService.class,
                        GPSLoggingService.class,
                        GPSFragment.class,
                        GPSLogItem.class,
                        BlogFragment.class,
                        BlogMapFragment.class,
                        BlogSectionFragment.class,
                        PoiEditDialog.ConfirmDialogFragment.class,
                        PoiNearestFragment.class,
                        BlogHistoryFragment.class,
                        BlogDetailsActivity.class,
                        DialogMapFragment.class,
                        VisitAddDialog.class,
                        VisitUpdateDialog.class


                }

        )
public class ModisSenseModule {

    @Singleton
    @Provides
    Bus provideOttoBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    LogoutService provideLogoutService(final Context context, final AccountManager accountManager) {
        return new LogoutService(context, accountManager);
    }


}
