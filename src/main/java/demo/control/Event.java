package demo.control;

/**
 * Created by Snart Lu on 2018/2/5.
 */
public class Event {
    private EventType type;

    public Event(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public static enum EventType {
        SAVE
    }
}
