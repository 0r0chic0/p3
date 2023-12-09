package cpen221.mp3.client;

public class Request implements Cloneable {
    private final double timeStamp;
    private final RequestType requestType;
    private final RequestCommand requestCommand;
    private final String requestData;

    private int clientId;

    public Request(RequestType requestType, RequestCommand requestCommand, String requestData,int clientId) {
        this.timeStamp = System.currentTimeMillis();
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
        this.clientId = clientId;
    }

    public Request clone(){
        try {
            return (Request) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId){
        this.clientId = clientId;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    public String getRequestData() {
        return requestData;
    }

    @Override
    public String toString() {
        return "Request{" +
                "timeStamp=" + timeStamp +
                ", requestType=" + requestType +
                ", requestCommand=" + requestCommand +
                ", requestData='" + requestData + '\'' +
                ", clientId=" + clientId +
                '}';
    }
}