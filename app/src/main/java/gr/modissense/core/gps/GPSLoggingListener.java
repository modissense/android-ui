package gr.modissense.core.gps;


import android.content.Context;
import com.squareup.tape.ObjectQueue;

public class GPSLoggingListener implements ObjectQueue.Listener<GPSLogItem> {
    private final Context context;

    public GPSLoggingListener(Context context) {
        this.context = context;
    }

    @Override
    public void onAdd(ObjectQueue<GPSLogItem> queue, GPSLogItem entry) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRemove(ObjectQueue<GPSLogItem> queue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

//    @Override
//    public void onAdd(ObjectQueue<ImageUploadTask>, ImageUploadTask task) {
//        context.startService(new Intent(context, ImageQueueService.class));
//    }
//
//    @Override
//    public void onRemove(ObjectQueue<ImageUploadTask>) {
//    }
}


