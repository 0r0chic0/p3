package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    private final int clientId;
    private String email;
    private String serverIP;
    private int serverPort;

    private Socket socket;



    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public int getClientId() {
        return clientId;
    }

    /**
     * Registers an entity for the client
     *
     * @return true if the entity is new and gets successfully registered, false if the Entity is already registered
     */
    public boolean addEntity(Entity entity) {

        if(entity.getClientId()==-1){
            entity.registerForClient(this.clientId);
            return true;
        }else{
            if(entity.getClientId()==this.clientId){
                return true;
            }else {
                return false;
            }
        }
    }

    // sends a request to the server
    public void sendRequest(Request request) {

        if(socket == null){
            try {
                socket = new Socket(serverIP,serverPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(request.clone());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}