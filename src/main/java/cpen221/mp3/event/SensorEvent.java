package cpen221.mp3.event;

public class SensorEvent implements Event {
    // TODO: Implement this class
    // you can add private fields and methods to this class
    private double timeStamp;
    private int clientId;
    private int entityId;
    private String entityType;
    private double value;

    public SensorEvent(double TimeStamp,
                       int ClientId,
                       int EntityId,
                       String EntityType,
                       double Value) {
        // Implement this constructor
        this.timeStamp = TimeStamp;
        this.clientId = ClientId;
        this.entityId = EntityId;
        this.entityType = EntityType;
        this.value = Value;
    }

    public double getTimeStamp() {
        // Implement this method
        return timeStamp;
    }

    @Override
    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getClientId() {
        // Implement this method
        return clientId;
    }

    public int getEntityId() {
        // Implement this method
        return entityId;
    }

    public String getEntityType() {
        // Implement this method
        return entityType;
    }

    public double getValueDouble() {
        // Implement this method
        return value;
    }

    // Sensor events do not have a boolean value
    // no need to implement this method
    public boolean getValueBoolean() {
        return false;
    }

    @Override
    public void setValueDouble(double value) {
        this.value = value;
    }

    @Override
    public void setValueBoolean(boolean value) { }

    @Override
    public String toString() {
        return "SensorEvent{" +
                "TimeStamp=" + getTimeStamp() +
                ",ClientId=" + getClientId() +
                ",EntityId=" + getEntityId() +
                ",EntityType=" + getEntityType() +
                ",Value=" + getValueDouble() +
                '}';
    }
}
