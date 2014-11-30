package gr.modissense.core.gps;


import android.content.Context;
import android.content.Intent;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskInjector;
import com.squareup.tape.TaskQueue;
import gr.modissense.ModisSenseApplication;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GPSLoggingTaskQueue extends TaskQueue<GPSLogItem> {
    private static final String FILENAME = "gps_log_task_queue";
    private final Context context;
    private final Bus bus;
    private List<GPSLogItem> itemsLogged = new LinkedList<GPSLogItem>();

    public GPSLoggingTaskQueue(ObjectQueue<GPSLogItem> delegate, Context context, Bus bus) {
        super(delegate,new TaskInjector<GPSLogItem>() {
            @Override
            public void injectMembers(GPSLogItem task) {
                ModisSenseApplication.getInstance().inject(task);
            }
        });
        this.context = context;
        this.bus = bus;
        bus.register(this);

        if (size() > 0) {
            startService();
        }
    }

    public static GPSLoggingTaskQueue create(Context context, Gson gson, Bus bus) {
        FileObjectQueue.Converter<GPSLogItem> converter = new GsonConverter<GPSLogItem>(gson, GPSLogItem.class);
        File queueFile = new File(context.getFilesDir(), FILENAME);
        FileObjectQueue<GPSLogItem> delegate;
        try {
            delegate = new FileObjectQueue<GPSLogItem>(queueFile, converter);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file queue.", e);
        }
        return new GPSLoggingTaskQueue(delegate, context, bus);
    }

    private void startService() {
        context.startService(new Intent(context, GPSLoggingQueueService.class));
    }

    @Override
    public void add(GPSLogItem entry) {
        super.add(entry);
        bus.post(produceSizeChanged(entry));
        itemsLogged.add(entry);
        startService();
    }

    @Override
    public void remove() {
        super.remove();
        //bus.post(produceSizeChanged());
    }

//    @SuppressWarnings("UnusedDeclaration") // Used by event bus.
    //@Produce
    public ItemAddedEvent produceSizeChanged(GPSLogItem logItem) {
        return new ItemAddedEvent(logItem);
    }

    public List<GPSLogItem> getItemsLogged() {
        return itemsLogged;
    }
}
