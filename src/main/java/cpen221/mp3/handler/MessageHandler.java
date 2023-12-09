package cpen221.mp3.handler;

import cpen221.mp3.server.Server;

import java.net.ServerSocket;
import java.net.Socket;

public class MessageHandler {
    private ServerSocket serverSocket;
    private int port;

    private Server server;//一个客户端对应一台服务器

    // you may need to add additional private fields and methods to this class

    public MessageHandler(int port) {
        this.port = port;
    }

    public void start() {
        // the following is just to get you started
        // you may need to change it to fit your implementation
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            while (true) {
                Socket incomingSocket = serverSocket.accept();
                System.out.println("Client/Entity connected: " + incomingSocket.getInetAddress().getHostAddress());
                // create a new thread to handle the client request or entity event
                Thread handlerThread = new Thread(new MessageHandlerThread(incomingSocket));
                handlerThread.start();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // you would need to initialize the RequestHandler with the port number
        // and then start it here
        MessageHandler handler = new MessageHandler(8888);
        handler.start();
    }
}
