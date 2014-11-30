

package gr.modissense;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.FROYO;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;

import com.github.kevinsawicki.http.HttpRequest;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import dagger.ObjectGraph;
import gr.modissense.core.MapEvent;
import gr.modissense.core.MapNearestEvent;
import gr.modissense.core.ModiUserInfo;
import gr.modissense.core.PoiSearchParams;

import javax.inject.Inject;

/**
 * modissense application
 */
public class ModisSenseApplication extends Application {


    private static ModisSenseApplication instance;
    ObjectGraph objectGraph;
    private MapEvent mapEvent = new MapEvent();
    private MapNearestEvent mapEventNearest = new MapNearestEvent();

    @Inject
    protected Bus BUS;
    public ModiUserInfo friends;
    private boolean nearest = true;

    /**
     * Create main application
     */
    public ModisSenseApplication() {

        // Disable http.keepAlive on Froyo and below
        if (SDK_INT <= FROYO)
            HttpRequest.keepAlive(false);
    }

    /**
     * Create main application
     *
     * @param context
     */
    public ModisSenseApplication(final Context context) {
        this();
        attachBaseContext(context);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        // Perform Injection
        objectGraph = ObjectGraph.create(getRootModule());
        objectGraph.inject(this);
        objectGraph.injectStatics();
        BUS.register(this);

    }

    private Object getRootModule() {
        return new RootModule();
    }



    /**
     * Create main application
     *
     * @param instrumentation
     */
    public ModisSenseApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public void inject(Object object)
    {
        objectGraph.inject(object);
    }



    public static ModisSenseApplication getInstance() {
        return instance;
    }

    public MapEvent getMapEvent() {
        return mapEvent;
    }

    public void setMapEvent(MapEvent mapEvent) {
        this.mapEvent = mapEvent;
    }

    public MapNearestEvent getMapEventNearest() {
        return mapEventNearest;
    }

    public void setMapEventNearest(MapNearestEvent mapEventNearest) {
        this.mapEventNearest = mapEventNearest;
    }

    @Produce
    public MapEvent postMapEvent() {
        return mapEvent;
    }

    @Produce
    public MapNearestEvent postMapNearestEvent() {
        return mapEventNearest;
    }

    public boolean isNearest() {
        return nearest;
    }

    public void setNearest(boolean nearest) {
        this.nearest = nearest;
    }
}
