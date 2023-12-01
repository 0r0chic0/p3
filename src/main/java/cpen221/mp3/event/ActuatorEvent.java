package cpen221.mp3.event;

public class ActuatorEvent implements Event {
    // TODO: Implement this class
    // you can add private fields and methods to this class
    private double timeStamp;
    private int clientId;
    private int entityId;
    private String entityType;
    private boolean value;
    public ActuatorEvent(double TimeStamp,
                         int ClientId,
                         int EntityId,
                         String EntityType,
                         boolean Value) {
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

    public boolean getValueBoolean() {
        // Implement this method
        return value;
    }

    @Override
    public void setValueDouble(double value) {

    }

    @Override
    public void setValueBoolean(boolean value) {
        this.value = value;
    }

    // Actuator events do not have a double value
    // no need to implement this method
    public double getValueDouble() {
        return -1;
    }

    @Override
    public String toString() {
        return "ActuatorEvent{" +
                "TimeStamp=" + getTimeStamp() +
                ",ClientId=" + getClientId() +
                ",EntityId=" + getEntityId() +
                ",EntityType=" + getEntityType() +
                ",Value=" + getValueBoolean() +
                '}';
    }
}
