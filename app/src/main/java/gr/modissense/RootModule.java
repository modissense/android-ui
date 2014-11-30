package gr.modissense;

import dagger.Module;

/**
 * Add all the other modules to this one.
 */
@Module
        (
                includes = {
                        AndroidModule.class,
                        GPSModule.class,
                        ModisSenseModule.class
                }
        )
public class RootModule {
}
