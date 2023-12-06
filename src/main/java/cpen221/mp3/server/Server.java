package cpen221.mp3.server;

import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.client.Client;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.client.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private Client client;
    private List<Event> eventList;
    private List<Event> logList;
    private double maxWaitTime = 2; // in seconds
    private int port;
    private String ip;
    private ServerSocket serverSocket;
    private Filter logFilter;

    // you may need to add additional private fields

    public Server(Client client) {
        // implement the Server constructor
        this.client = client;
        this.eventList = new ArrayList<>();
        this.logList = new ArrayList<>();
        this.port = client.getServerPort();
        init();

    }

    public void init() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() throws IOException {
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            // create a new thread to handle that client
            Thread handler = new Thread(new Runnable() {
                public void run() {
                    try {
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                        }
                    } catch (IOException ioe) {
                        // this exception wouldn't terminate serve(),
                        // since we're now on a different thread, but
                        // we still need to handle it
                        ioe.printStackTrace();
                    }
                }
            });
            // start the thread
            handler.start();
        }
    }

    public void handle(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        Request inRequest = requestDecode(in.read());
        processIncomingRequest(inRequest);
    }

    private Request requestDecode(int bytes) {
        //TODO: implement .toString() decoder
        return null;
    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param maxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double maxWaitTime) {
        // implement this method

        // Important note: updating maxWaitTime may not be as simple as
        // just updating the field. You may need to do some additional
        // work to ensure that events currently being processed are not
        // dropped or ignored by the change in maxWaitTime.
        this.maxWaitTime = maxWaitTime; //TODO: update this with note
    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     *
     * If the actuator is not registered for the client, then this method should do nothing.
     * 
     * @param filter the filter to check
     * @param actuator the actuator to set the state of as true
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {
        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        if (actuator.getClientId() == client.getClientId()) {
            if (filter.satisfies(eventList.get(eventList.size()-1))) {
                Request setState = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE,
                        "true");
                processIncomingEvent(new ActuatorEvent(setState.getTimeStamp(), client.getClientId(),
                        actuator.getId(), actuator.getType(), true));
            }
        }
    }
    
    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     * 
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter the filter to check
     * @param actuator the actuator to toggle the state of (true -> false, false -> true)
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {
        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        boolean checkNoEvent = true;
        for (Event currentEvent : eventList) {
            if (currentEvent.getEntityId() == actuator.getId()) {
                checkNoEvent = false;
            }
        }
        if (checkNoEvent) { return; }
        if (actuator.getClientId() == client.getClientId()) {
            if (filter.satisfies(eventList.get(eventList.size()-1))) {
                Request setState = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE,
                        "toggle"); //TODO: Change sent data
                eventList.add(new ActuatorEvent(setState.getTimeStamp(), client.getClientId(),
                        actuator.getId(), actuator.getType(), actuator.getState()));
            }
        }
    }

    /**
     * Log the event ID for which a given filter was satisfied.
     * This method is checked for every event received by the server.
     *
     * @param filter the filter to check
     */
    public void logIf(Filter filter) {
        // implement this method
        this.logFilter = filter;
        Event latest = eventList.get(eventList.size()-1);
        if (filter.satisfies(latest)) {
            logList.add(latest);
        }
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method should return an empty list.
     * The list should be sorted in the order of event timestamps.
     * After the logs are read, they should be cleared from the server.
     *
     * @return list of events' entity IDs
     */
    public List<Integer> readLogs() {
        // implement this method
        logList = sortList(logList);
        return logList.stream()
                .map(Event::getEntityId)
                .toList();
    }

    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not 
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method should return an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        List<Event> eventsInTime = new ArrayList<>();
        for (Event currentEvent : eventList) {
            if (timeWindow.startTime <= currentEvent.getTimeStamp() && timeWindow.endTime >= currentEvent.getTimeStamp()) {
                eventsInTime.add(currentEvent);
            }
        }
        return eventsInTime;
    }

     /**
     * Returns a set of IDs for all the entities of the client for which 
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     * 
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        // implement this method
        List<Integer> entityList = new ArrayList<>();
        for (Event currentEvent : eventList) {
            if (!entityList.contains(currentEvent.getEntityId())) {
                entityList.add(currentEvent.getEntityId());
            }
        }
        return entityList;
    }

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method should return all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        // implement this method
        List<Event> sortedEvents = sortList(eventList);
        return sortedEvents.subList(Math.max(sortedEvents.size() - n, 0), sortedEvents.size());
    }

    public List<Event> sortList(List<Event> sorting) {
        List<Event> sortedEvents = sorting.stream()
                .sorted(Comparator.comparingDouble(Event::getTimeStamp))
                .collect(Collectors.toList());

        return sortedEvents;
    }

    /**
     * returns the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated.
     *
     * If there was a tie, then this method should return the largest ID.
     * 
     * @return the most active entity ID of the client
     */
    public int mostActiveEntity() {
        // implement this method
        HashMap<Integer, Integer> eventCounts = new HashMap<>();
        int maxEvents = 0;
        int activeEntity = 0;
        for (Event currentEvent : eventList) {
            int currentId = currentEvent.getEntityId();
            if (!eventCounts.containsKey(currentId)) {
                eventCounts.put(currentId, 1);
            } else {
                eventCounts.put(currentId, eventCounts.get(currentId)+1);
            }
            if (eventCounts.get(currentId) > maxEvents) {
                activeEntity = currentId;
                maxEvents = eventCounts.get(currentId);
            } else if (eventCounts.get(currentId) == maxEvents && currentId > activeEntity) {
                activeEntity = currentId;
                maxEvents = eventCounts.get(currentId);
            }
        }
        return activeEntity;
    }

    /**
     * the client can ask the server to predict what will be 
     * the next n timestamps for the next n events 
     * of the given entity of the client (the entity is identified by its ID).
     * 
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of timestamps to predict
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        // implement this method
        return null;
    }

    /**
     * the client can ask the server to predict what will be 
     * the next n values of the timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean() 
     * based on the type of the entity. That is why the return type is List<Object>.
     * 
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of double value to predict
     * @return list of the predicted timestamps
     */
    public List<Object> predictNextNValues(int entityId, int n) {
        // implement this method
        return null;
    }

    void processIncomingEvent(Event event) {
        eventList.add(event);
        eventList = sortList(eventList);

    }

    void processIncomingRequest(Request request) {
        // implement this method
       switch (request.getRequestType()) {
           case CONFIG: switch (request.getRequestCommand()) {
               case CONFIG_UPDATE_MAX_WAIT_TIME: updateMaxWaitTime(Integer.parseInt(request.getRequestData()));
           }
       }

    }
}
