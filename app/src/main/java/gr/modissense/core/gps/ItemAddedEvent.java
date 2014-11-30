package gr.modissense.core.gps;


public class ItemAddedEvent {
    private GPSLogItem item;

    public ItemAddedEvent() {
    }

    public ItemAddedEvent(GPSLogItem item) {
        this.item = item;
    }

    public GPSLogItem getItem() {
        return item;
    }

    public void setItem(GPSLogItem item) {
        this.item = item;
    }
}
