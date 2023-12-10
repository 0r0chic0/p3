package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.CSVEventReader;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleServerTests {

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();
    Client client = new Client(0, "test@test.com", "1.1.1.1", 1);
    Actuator actuator1 = new Actuator(97, 0, "Switch", true,"127.0.0.1",10000,16000);

    @Test
    public void testSetActuatorStateIf() throws InterruptedException {
        System.out.println(eventList.get(9));
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        server.setActuatorStateIf(sensorValueFilter, actuator1);
        Thread.sleep(1000);
        assertEquals(true, actuator1.getState());
    }

    @Test
    public void testToggleActuatorStateIf() throws InterruptedException {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        server.toggleActuatorStateIf(sensorValueFilter, actuator1);
        Thread.sleep(1000);
        assertEquals(false, actuator1.getState());
    }

    @Test
    public void testEventsInTimeWindow() {
        Server server = new Server(client);
        TimeWindow tw = new TimeWindow(0.2, 1);
        for (int i = 0; i < 100; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        List<Event> result = server.eventsInTimeWindow(tw);
        assertEquals(9, result.size());
    }

    @Test
    public void testLastNEvents() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        List<Event> result = server.lastNEvents(2);
        assertEquals(2, result.size());
        assertEquals("PressureSensor", result.get(1).getEntityType());
        assertEquals(144, result.get(1).getEntityId());
    }

    @Test
    public void testMostActiveEntity() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        int mostActiveEntity = server.mostActiveEntity();
        assertEquals(11, mostActiveEntity);
    }

    @Test
    public void testMostActiveEntity1() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        Event event7 = new SensorEvent(0.00024, 0, 1,"TempSensor", 12.0);
        Event event8 = new SensorEvent(0.00028, 0, 1,"TempSensor", 13.0);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        simulatedEvents.add(event7);
        simulatedEvents.add(event8);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        System.out.println(simulatedEvents);
        int mostActiveEntity = server.mostActiveEntity();
        assertEquals(1, mostActiveEntity);
    }

    @Test
    public void testMostActiveEntity2() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        Event event7 = new SensorEvent(0.00024, 0, 1,"TempSensor", 12.0);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        simulatedEvents.add(event7);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        System.out.println(simulatedEvents);
        int mostActiveEntity = server.mostActiveEntity();
        assertEquals(11, mostActiveEntity);
    }
    @Test
    public void testGetAllEntities() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        Set<Integer> expected = new HashSet<>();
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
            int entityId = (simulatedEvents.get(i)).getEntityId();
            expected.add(entityId);
        }
        Set<Integer> result = new HashSet<>(server.getAllEntities());
        assertEquals(expected,result);
    }


    @Test
    public void testnextNpredictedValuesDouble() {
        Server server = new Server(client);
        Event e1 = new SensorEvent(1.0001,0,20,"Sensor",1.1);
        Event e2 = new SensorEvent(1.0002,0,20,"Sensor",2.1);
        Event e3 = new SensorEvent(1.0003,0,20,"Sensor",1.1);
        List<Event> events= new ArrayList<>();
        events.add(e1);
        events.add(e2);
        events.add(e3);
        List<Double> predictedval = new ArrayList<>();
        predictedval.add(2.1);
        predictedval.add(1.1);
        predictedval.add(2.1);
        predictedval.add(1.1);
        predictedval.add(2.1);
        for(int i = 0 ; i< events.size(); i++)
        {
            server.processIncomingEvent(events.get(i));
        }
        List<Object> result = server.predictNextNValues(20,5);
        assertEquals(predictedval, result);

    }
    @Test
    public void testnextNpredictedValuesBoolean() {
        Server server = new Server(client);
        Event e4 = new ActuatorEvent(1.0004,0,20,"Switch",false);
        Event e5 = new ActuatorEvent(1.0005,0,20,"Switch",false);
        Event e6 = new ActuatorEvent(1.0006,0,20,"Switch",false);
        Event e7 = new ActuatorEvent(1.0007,0,20,"Switch",true);
        List<Event> events= new ArrayList<>();
        events.add(e4);
        events.add(e5);
        events.add(e6);
        events.add(e7);
        List<Boolean> predictedval = new ArrayList<>();
        predictedval.add(false);
        predictedval.add(true);
        predictedval.add(false);
        predictedval.add(true);
        predictedval.add(false);
        predictedval.add(true);
        for(int i = 0 ; i< events.size(); i++)
        {
            server.processIncomingEvent(events.get(i));
        }
        List<Object> result = server.predictNextNValues(20,6);
        assertEquals(predictedval, result);

    }
    @Test
    public void testnextNpredictedTimestamps() {
        Server server = new Server(client);
        Event e4 = new ActuatorEvent(1.0004,0,20,"Switch",false);
        Event e5 = new ActuatorEvent(1.0005,0,20,"Switch",false);
        Event e6 = new ActuatorEvent(1.0006,0,20,"Switch",false);
        Event e7 = new ActuatorEvent(1.0007,0,20,"Switch",true);
        List<Event> events= new ArrayList<>();
        events.add(e4);
        events.add(e5);
        events.add(e6);
        events.add(e7);
        List<Double> predictedval = new ArrayList<>();
        predictedval.add(1.0008);
        predictedval.add(1.0009);
        predictedval.add(1.001);
        predictedval.add(1.00106);
        predictedval.add(1.00113);
        predictedval.add(1.0022);
        for(int i = 0 ; i< events.size(); i++)
        {
            server.processIncomingEvent(events.get(i));
        }
        List<Double> result = server.predictNextNtimestamps(20,6);
        assertEquals(predictedval, result);

    }
}