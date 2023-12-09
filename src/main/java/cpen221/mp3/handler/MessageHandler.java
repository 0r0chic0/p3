package cpen221.mp3.handler;

import cpen221.mp3.server.Server;

import java.net.ServerSocket;
import java.net.Socket;

public class MessageHandler {
    private ServerSocket serverSocket;
    private int port;

    private Server server;

    public MessageHandler(int port) {
        this.port = port;
    }

    public void start() {
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
        MessageHandler handler = new MessageHandler(8888);
        handler.start();
    }
}
