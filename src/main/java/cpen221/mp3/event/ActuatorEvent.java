package cpen221.mp3.event;

public class ActuatorEvent implements Event,Cloneable {
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

        this.timeStamp = TimeStamp;
        this.clientId = ClientId;
        this.entityId = EntityId;
        this.entityType = EntityType;
        this.value = Value;
    }

    public ActuatorEvent clone(){
        try {
            return (ActuatorEvent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
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
