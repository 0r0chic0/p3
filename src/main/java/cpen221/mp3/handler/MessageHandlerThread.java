package cpen221.mp3.handler;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Entity;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class MessageHandlerThread implements Runnable {

    ConcurrentHashMap<Integer, Server> serverMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Server, List<Entity>> serverEntityMap = new ConcurrentHashMap<>();
    private Socket incomingSocket;

    public MessageHandlerThread(Socket incomingSocket) {
        this.incomingSocket = incomingSocket;
    }

    @Override
    public void run() {
        // handle the client request or entity event here
        // and deal with exceptions if needed
        try {
            InputStream is = incomingSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            Object o = ois.readObject();
            if(o instanceof Event){
                int clientId = ((Event) o).getClientId();
                Server server = serverMap.get(clientId);//看这个客户端对应的服务器是否已经创建
                Client client = null;
                if(server==null){
                    //创建一个服务器对应客户端
                    client = new Client(clientId,"",incomingSocket.getLocalAddress().getHostAddress(),incomingSocket.getLocalPort());
                    server = new Server(client);
                }
                //如果是事件，则代表是传感器或者执行器发送的，在这里构造一个传感器或执行器
                if(o instanceof SensorEvent){
                    //传感器
                    SensorEvent se = (SensorEvent)o;
                    int entityId = se.getEntityId();
                    //如果这个id已经存在就不管，如果不存在就创建一个
                    if(!isExists(server,entityId)){
                        Sensor sensor = new Sensor(se.getEntityId(),se.getClientId(),se.getEntityType(),incomingSocket.getLocalAddress().getHostAddress(),incomingSocket.getLocalPort());
                        sensor.setSocket(incomingSocket);
                    }
                }else{
                    //执行器
                    ActuatorEvent se = (ActuatorEvent) o;
                    int entityId = se.getEntityId();
                    //如果这个id已经存在就不管，如果不存在就创建一个
                    if(!isExists(server,entityId)){
                        Actuator actuator = new Actuator(se.getEntityId(),se.getClientId(),se.getEntityType(),false,incomingSocket.getLocalAddress().getHostAddress(),incomingSocket.getLocalPort());
                        actuator.setSocket(incomingSocket);
                    }
                }
                server.processIncomingEvent((Event) o);
            }else if(o instanceof Request){
                Request r = (Request) o;
                int clientId = r.getClientId();
                Server server = serverMap.get(clientId);
                if(server==null){
                    Client client = new Client(clientId,"",incomingSocket.getLocalAddress().getHostAddress(),incomingSocket.getLocalPort());
                    server = new Server(client);
                    serverMap.put(clientId,server);
                }
                server.processIncomingRequest(r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean isExists(Server server,int entityId) {
        List<Entity> entities = serverEntityMap.get(server);
        for (Entity entity : entities) {
            if(entity.getClientId()==entityId){
                return true;
            }
        }
        return false;
    }
}