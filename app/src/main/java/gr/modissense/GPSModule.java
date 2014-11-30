package gr.modissense;


import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;
import gr.modissense.core.gps.GPSLoggingTaskQueue;

import javax.inject.Singleton;

@Module
        (
                complete = false,
                library = true
        )
public class GPSModule {


    @Provides
    @Singleton
    GPSLoggingTaskQueue provideTaskQueue(final Context appContext, Gson gson, Bus bus) {
        return GPSLoggingTaskQueue.create(appContext, gson, bus);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
}
