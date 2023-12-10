package cpen221.mp3.entity;

import cpen221.mp3.client.Request;
import cpen221.mp3.event.Event;
import cpen221.mp3.server.SeverCommandToActuator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Actuator implements Entity{
    private final int id;
    private int clientId;
    private final String type;
    private boolean state;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    // the following specifies the http endpoint that the actuator should send events to
    private String serverIP = null;
    private int serverPort = 0;
    // the following specifies the http endpoint that the actuator should be able to receive commands on from server
    private String host = null;
    private int port = 0;
    private Socket receiveSocket;
    private Socket socket;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    private void initServer() {
        try {
            if(state==false){
                return;
            }
            if(port!=0){
                ServerSocket server = new ServerSocket(port);
                receiveSocket = server.accept();
                processServerMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Actuator(int id, String type, boolean init_state) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        // TODO: need to establish a server socket to listen for commands from server
//        initServer();
    }
    public Actuator(int id, int clientId, String type, boolean init_state) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        // TODO: need to establish a server socket to listen for commands from server
        //initServer();
    }

    public Actuator(int id, int clientId, String type, boolean init_state,String serverIP, int serverPort,int port) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.port = port;
        // TODO: need to establish a server socket to listen for commands from server
        new Thread(()->initServer()).start();

    }


    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        // TODO: need to establish a server socket to listen for commands from server
       // initServer();
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
        return true;
    }

    public boolean getState() {
        return state;
    }

    public String getIP() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void updateState(boolean new_state) {
        this.state = new_state;
    }

    /**
     * Registers the actuator for the given client
     *
     * @return true if the actuator is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {

        if (this.clientId == -1) {
            if (clientId >= 0) {
                this.clientId = clientId;
                return true;
            }
        } else if (clientId == this.clientId) {
            return true;
        }
        return false;
    }

    /**
     * Sets or updates the http endpoint that
     * the actuator should send events to
     *
     * @param serverIP   the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency) {
        if (frequency > 0) {
            this.eventGenerationFrequency = frequency;
        }
    }

    public void sendEvent(Event event) {

        if (serverIP == null || serverIP.equals("") || serverPort == 0) {
            return;
        }
        int times = 0;

        OutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            socket = new Socket(serverIP, serverPort);
            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                if (times == 5) {
                    Thread.sleep(10 * 1000);
                }
                event.setTimeStamp(Math.random());
                event.setValueBoolean(new Random().nextInt(2)==0?true:false);
                oos.writeObject(event.clone());
                Thread.sleep((long) (eventGenerationFrequency * 1000));
                times = 0;
            } catch (IOException e) {
                times++;
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void processServerMessage() {

        while(true){
            try {
                InputStream is = receiveSocket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                SeverCommandToActuator o = (SeverCommandToActuator) ois.readObject();
                if(o==SeverCommandToActuator.SET_STATE){
                    this.state = true;
                    System.out.println(this.getState());
                }else if(o==SeverCommandToActuator.TOGGLE_STATE){
                    this.state = !this.state;
                    System.out.println(this.getState());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Actuator{" +
                "getId=" + getId() +
                ",ClientId=" + getClientId() +
                ",EntityType=" + getType() +
                ",IP=" + getIP() +
                ",Port=" + getPort() +
                '}';
    }


}