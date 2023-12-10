package cpen221.mp3.server;

import cpen221.mp3.entity.Actuator;
import cpen221.mp3.client.Client;
import cpen221.mp3.event.Event;
import cpen221.mp3.client.Request;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class Server {
    private Client client;
    private int clientId;
    private double maxWaitTime = 2; // in seconds


    private ConcurrentSkipListSet<Event> events = new ConcurrentSkipListSet<>(new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            return o1.getTimeStamp()-o2.getTimeStamp()>0?1:-1;
        }
    });

    private ConcurrentSkipListSet<Request> requests = new ConcurrentSkipListSet<>(new Comparator<Request>() {
        @Override
        public int compare(Request r1, Request r2) {
            return r1.getTimeStamp()-r2.getTimeStamp()>0?1:-1;
        }
    });
    private Filter logFilter;

    private ConcurrentSkipListSet<Event> logList = new ConcurrentSkipListSet<>(new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            return e1.getTimeStamp()-e2.getTimeStamp()>0?1:-1;
        }
    });


    public Server(Client client) {
        // implement the Server constructor
        this.client = client;
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
        this.maxWaitTime = maxWaitTime;

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

        if(actuator.getClientId()!=this.clientId){
            return;
        }
        Event event = events.last();
        if(!filter.satisfies(event)){
            return;
        }
        try {
            Socket socket = new Socket(actuator.getHost(),actuator.getPort());
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(SeverCommandToActuator.SET_STATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        if(actuator.getClientId()!=this.clientId){
            return;
        }
        Event event = events.last();
        if(!filter.satisfies(event)){
            return;
        }
        try {
            Socket socket = new Socket(actuator.getHost(),actuator.getPort());
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(SeverCommandToActuator.TOGGLE_STATE);
        } catch (IOException e) {
            e.printStackTrace();
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
        Event latest = events.last();
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
     * @return list of event IDs
     */
    public List<Integer> readLogs() {
        return logList.stream()
                .map(Event::getEntityId)
                .collect(Collectors.toList());
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
        // implement this method
        List<Event> eventList = new ArrayList<>();
        for (Event event : events) {
            if(event.getTimeStamp()>=timeWindow.getStartTime()&&event.getTimeStamp()<=timeWindow.getEndTime()){
                eventList.add(event);
            }
        }
        return eventList;
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
        List<Integer> list = new ArrayList<>();
        for (Event event : events) {
            list.add(event.getEntityId());
        }
        return list;
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

        ConcurrentSkipListSet<Event> clone = events.clone();
        List<Event> eventList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            eventList.add(clone.pollLast());
        }
        Collections.reverse(eventList);
        return eventList;
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
        HashMap<Integer,Integer> activeMap = new HashMap<>();
        for (Event event : events) {
            int entityId = event.getEntityId();
            if(activeMap.containsKey(entityId)){
                activeMap.put(entityId,activeMap.get(entityId)+1);
            }else{
                activeMap.put(entityId,1);
            }
        }
        System.out.println(activeMap);
        int max = -1;
        int entityId = -1;
        for (Integer eid : activeMap.keySet()) {
            if(activeMap.get(eid)>max){
                max = activeMap.get(eid);
                entityId = eid;
            }else if(activeMap.get(eid)==max){
                if(eid>entityId){
                    entityId = eid;
                }
            }
        }
        return entityId;
    }
    /**
     * the client can ask the server to predict what will be
     * the next n values of the values for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean()
     * based on the type of the entity. That is why the return type is List<Object>.
     *
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n the number of double value to predict
     * @return list of the predicted values
     */

    public List<Object> predictNextNValues(int entityId, int n) {
        List<Object> predictedValues = new ArrayList<>();

        // Filter events associated with the specified entity ID
        List<Event> eventsForEntity = events.stream()
                .filter(event -> event.getEntityId() == entityId)
                .collect(Collectors.toList());


        // Determine the type of values associated with the entity
        if (!eventsForEntity.isEmpty()) {
            Event sampleEvent = eventsForEntity.get(0); // Sample event to determine the type

            if ((!sampleEvent.getValueBoolean() || sampleEvent.getValueBoolean())&& sampleEvent.getValueDouble()==-1 ) {
                // Predict the next N boolean values using logistic regression
                List<Boolean> historicalValues = eventsForEntity.stream()
                        .map(Event::getValueBoolean)
                        .collect(Collectors.toList());

                predictedValues.addAll(predictNextNBooleanValues(historicalValues, n));
            } else {
                // Predict the next N double values using Exponential Moving Average (EMA)
                List<Double> historicalValues = eventsForEntity.stream()
                        .map(Event::getValueDouble)
                        .collect(Collectors.toList());

                predictedValues.addAll(predictNextNDoubleValues(historicalValues, n));
            }
        }

        return predictedValues;
    }
    /**
     * Predict the next N double values using Exponential Moving Average (EMA).
     *
     * @param historicalValues list of historical double values
     * @param n the number of values to predict
     * @return list of the predicted double values
     */
    private static List<Double> predictNextNDoubleValues(List<Double> historicalValues, int n)
    {
        List<Double> predictedValues = new ArrayList<>();
        if (!historicalValues.isEmpty())
        {
            double size = historicalValues.size();
            double ema = historicalValues.get(historicalValues.size()- 1);
            double alpha = 2.0 /(size + 1.0);
            int counter = 0;

            for(int i = 0 ; i< historicalValues.size()-1; i++)
            {
                if(historicalValues.get(i).equals(historicalValues.get(i+1)))
                {
                    counter++;
                }
            }
            if(counter == historicalValues.size()-1)
            {
                for(int j = 0 ; j<n ; j++)
                {
                    predictedValues.add(historicalValues.get(0));
                }
            }
            else if( historicalValues.size() % 2 == 0)
            {
                int count1 = 0;
                int count2 = 0;
              for(int i = 0 ; i < historicalValues.size(); i=i+2)
              {
                  if (historicalValues.get(i).equals(historicalValues.get(0))) {
                      count1++;
                  }
                  if (historicalValues.get(i + 1).equals(historicalValues.get(1))) {
                      count2++;
                  }
              }
                  if(count1 == count2)
                  {
                      for(int j = 0; j < n ; j=j+2)
                      {
                          predictedValues.add(historicalValues.get(0));
                          predictedValues.add(historicalValues.get(1));
                      }
                      if(n%2 !=0)
                      {
                          predictedValues.remove(n);
                      }
                  }
                  else
                  {
                      for (int i = 0; i < n; i++)
                      {
                          // Calculate the next EMA
                          ema = alpha * historicalValues.get(historicalValues.size()-1-i) + (1 - alpha) * ema;

                          // Add the predicted value to the result
                          predictedValues.add(ema);
                      }
                  }
            }


            else if( historicalValues.size() % 2 !=0)
            {
                int count1=0;
                int count2=0;
             for(int i = 0 ; i < historicalValues.size() ; i=i+2) {
                 if (historicalValues.get(i).equals(historicalValues.get(0))) {
                     count1++;
                 }
                 if (i + 1 < historicalValues.size()) {
                     if (historicalValues.get(i + 1).equals(historicalValues.get(1))) {
                         count2++;
                     }
                 }
             }
                 if(count1 - count2 == 1)
                 {
                     for(int j = 0; j < n ; j=j+2)
                     {
                         predictedValues.add(historicalValues.get(1));
                         predictedValues.add(historicalValues.get(0));
                     }
                     if(n%2 !=0)
                     {
                         predictedValues.remove(n);
                     }
                 }
                 else {
                     for (int i = 0; i < n; i++)
                         {
                             // Calculate the next EMA
                             ema = alpha * historicalValues.get(Math.abs(historicalValues.size()-1-i)% n) + (1 - alpha) * ema;
                             // Add the predicted value to the result
                             predictedValues.add(ema);
                         }
                     }

            }

        }

        return predictedValues;
    }

    /**
     * Predict the next N boolean values using a simplified version of logistic regression.
     *
     * @param historicalValues list of historical boolean values
     * @param n the number of values to predict
     * @return list of the predicted boolean values
     */
    private static List<Boolean> predictNextNBooleanValues(List<Boolean> historicalValues, int n) {
        List<Boolean> predictedValues = new ArrayList<>();

        if (!historicalValues.isEmpty()) {
            // Logistic regression parameters
            double weight = 2.0 / (historicalValues.size() + 1.0);
            int counter = 0;

            for (int i = 0; i < historicalValues.size() - 1; i++) {
                if (historicalValues.get(i).equals(historicalValues.get(i + 1))) {
                    counter++;
                }
            }
            if (counter == historicalValues.size()-1) {
                for(int j = 0 ; j<n ; j++)
                {
                    predictedValues.add(historicalValues.get(0));
                }
            } else if (historicalValues.size() % 2 == 0) {
                int count1 = 0;
                int count2 = 0;
                for (int i = 0; i < historicalValues.size(); i = i + 2)
                {
                    if (historicalValues.get(i).equals(historicalValues.get(0)))
                    {
                        count1++;
                    }
                    if (historicalValues.get(i + 1).equals(historicalValues.get(1))) {
                        count2++;
                    }
                }
                if (count1 == count2) {
                    for (int j = 0; j < n; j = j + 2) {
                        predictedValues.add(historicalValues.get(0));
                        predictedValues.add(historicalValues.get(1));
                    }
                    if (n % 2 != 0) {
                        predictedValues.remove(n);
                    }
                }
                else
                {
                    List<Double> copy = new ArrayList<>();
                    for (int j = 0; j < n; j++) {
                        copy.add(historicalValues.get(Math.abs(historicalValues.size() -1 - j)%n) ? 1.0 : 0.0);
                        double logit = 0.0;

                        for (int k = j; k < historicalValues.size(); k++)
                        {
                            // decay factor for older values
                            double decayFactor = Math.pow(0.9, k);
                            logit += decayFactor * weight * copy.get(k);
                        }

                        double prediction = sigmoid(logit);
                        boolean predictedBoolean = prediction > 0.5;

                        // Update weight based on prediction error
                        double learningRate = 0.1;
                        double error = predictedBoolean ? 1.0 - prediction : 0.0 - prediction;
                        weight += learningRate * error;
                        predictedValues.add(predictedBoolean);
                    }

                    return predictedValues;
                }


            } else if (historicalValues.size() % 2 != 0) {
                int count1 = 0;
                int count2 = 0;
                for (int i = 0; i < historicalValues.size() ; i=i+2) {
                    if (historicalValues.get(i).equals(historicalValues.get(0))) {
                        count1++;
                    }
                    if (i + 1 < historicalValues.size()) {
                        if (historicalValues.get(i + 1).equals(historicalValues.get(1))) {
                            count2++;
                        }
                    }
                }
                if (count1 - count2 == 1) {
                    for (int j = 0; j < n; j = j + 2) {
                        predictedValues.add(historicalValues.get(1));
                        predictedValues.add(historicalValues.get(0));
                    }
                    if (n % 2 != 0) {
                        predictedValues.remove(n);
                    }
                }
                else
                {
                    List<Double> copy = new ArrayList<>();
                    for (int j = 0; j < n; j++) {
                        copy.add(historicalValues.get(Math.abs(historicalValues.size()-1 - j)%n) ? 1.0 : 0.0);
                        double logit = 0.0;

                        for (int k = 0; k < copy.size(); k++) {
                            // decay factor for older values
                            double decayFactor = Math.pow(0.9, k);
                            logit += decayFactor * weight * copy.get(k);
                        }

                        double prediction = sigmoid(logit);
                        boolean predictedBoolean = prediction > 0.5;

                        // Update weight based on prediction error
                        double learningRate = 0.1;
                        double error = predictedBoolean ? 1.0 - prediction : 0.0 - prediction;
                        weight += learningRate * error;
                        predictedValues.add(predictedBoolean);
                    }

                    return predictedValues;
                }

            }
        }return predictedValues;
    }
    /**
     * Applies the sigmoid function to the given input.
     *
     * @param z The input value to the sigmoid function.
     * @return The result of applying the sigmoid function to the input.
     */
    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
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
    public List<Double> predictNextNtimestamps(int entityId, int n)
    {
        List<Double> predictedValues2 = new ArrayList<>();

        // Filter events associated with the specified entity ID
        List<Event> eventsForEntity2 = events.stream()
                .filter(event -> event.getEntityId() == entityId)
                .collect(Collectors.toList());

        List<Double> historicalValues2 = eventsForEntity2.stream()
                .map(Event::getTimeStamp)
                .collect(Collectors.toList());

        double alpha =  2.0 / (historicalValues2.size() + 1.0);

        if (!historicalValues2.isEmpty())
        {
            List<Double> difference = new ArrayList<>();
            difference.add(0.0);
            for(int j = 0 ; j< historicalValues2.size()-1 ; j++)
            {
                double diff = historicalValues2.get(j+1) - historicalValues2.get(j);
                difference.add(diff);
            }
            double ema = difference.get(difference.size() - 1);
            double addition = historicalValues2.get(historicalValues2.size()-1);

            for (int i = 0; i < n; i++) {
                // Calculate the next EMA
                ema = alpha * difference.get(Math.abs(difference.size()-1-i)%n) + (1 - alpha) * ema;
                addition = addition+ema;
                // Add the predicted value to the result
                predictedValues2.add(addition);
            }

        } return predictedValues2;
    }


    public void processIncomingEvent(Event event) {

        events.add(event);
    }

    public void processIncomingRequest(Request request) {

        requests.add(request);
    }

}