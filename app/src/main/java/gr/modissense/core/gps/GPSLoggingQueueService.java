package gr.modissense.core.gps;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.squareup.tape.TaskQueue;
import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.core.ModisSenseService;
import gr.modissense.util.Ln;

import javax.inject.Inject;

public class GPSLoggingQueueService extends Service implements GPSLogItem.Callback{
    @Inject
    GPSLoggingTaskQueue queue;

    private boolean running;

    public GPSLoggingQueueService() {
        ModisSenseApplication.getInstance().inject(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Obtain TaskQueue here (e.g., through injection)
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void executeNext() {
        if (running) return; // Only one task at a time.
        GPSLogItem task = queue.peek();
        if (task != null) {
            running = true;
            task.execute(this);
        } else {
            Ln.i("Service stopping!");
            stopSelf(); // No more tasks are present. Stop.
        }
    }

    public void imageUploadComplete() {
        running = false;
        queue.remove();
        executeNext();
    }

    @Override
    public void onSuccess(String url) {
        running = false;
        queue.remove();
        executeNext();

    }

    @Override
    public void onFailure() {
 //       running=false;
 //       executeNext();
    }
}
