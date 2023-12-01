package cpen221.mp3.entity;

import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Sensor implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private String serverIP = null;
    private int serverPort = 0;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)

    private Socket socket;

    public Sensor(int id, String type) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
    }

    public Sensor(int id, int clientId, String type) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
    }

    public Sensor(int id, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;   // remains unregistered
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public Sensor(int id, int clientId, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public String getType() {
        return type;
    }

    public boolean isActuator() {
        return false;
    }

    /**
     * Registers the sensor for the given client
     *
     * @return true if the sensor is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        // implement this method
        if(this.clientId==-1){
            if(clientId>=0){
                this.clientId = clientId;
                return true;
            }
        }else if(clientId==this.clientId){
            return true;
        }
        return false;
    }

    /**
     * Sets or updates the http endpoint that
     * the sensor should send events to
     *
     * @param serverIP the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency){
        // implement this method
        if(frequency>0){
            this.eventGenerationFrequency = frequency;
        }
    }

    public void sendEvent(Event event) {

        int times = 0;
        // implement this method
        // note that Event is a complex object that you need to serialize before sending
        OutputStream os = null;
        try {
            if(serverIP==null||serverIP.equals("")||serverPort==0){
                return;
            }
            socket = new Socket(serverIP,serverPort);
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            try {
                if(times==5){
                    Thread.sleep(10*1000);
                }
                event.setTimeStamp(Math.random());
                event.setValueDouble(Math.random());
                os.write(event.toString().getBytes());
                Thread.sleep((long) (eventGenerationFrequency*1000));
                times = 0;
            } catch (IOException e) {
                times++;
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}