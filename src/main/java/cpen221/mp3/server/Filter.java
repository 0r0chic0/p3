package cpen221.mp3.server;

import cpen221.mp3.event.Event;

import java.util.ArrayList;
import java.util.List;

enum DoubleOperator {
    EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN_OR_EQUALS
}

enum BooleanOperator {
    EQUALS,
    NOT_EQUALS
}

public class Filter {
    // you can add private fields and methods to this class
    private BooleanOperator booleanOperator;
    private DoubleOperator doubleOperator;
    private boolean boolValue;
    private double doubleValue;
    private String matchField;
    private List<Filter> filterList;

    /**
     * Constructs a filter that compares the boolean (actuator) event value
     * to the given boolean value using the given BooleanOperator.
     * (X (BooleanOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A BooleanOperator can be one of the following:
     * 
     * BooleanOperator.EQUALS
     * BooleanOperator.NOT_EQUALS
     *
     * @param operator the BooleanOperator to use to compare the event value with the given value
     * @param value the boolean value to match
     */
    public Filter(BooleanOperator operator, boolean value) {
        this.booleanOperator = operator;
        this.boolValue = value;
        this.doubleValue = Double.MAX_VALUE;

    }

    /**
     * Constructs a filter that compares a double field in events
     * with the given double value using the given DoubleOperator.
     * (X (DoubleOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A DoubleOperator can be one of the following:
     * 
     * DoubleOperator.EQUALS
     * DoubleOperator.GREATER_THAN
     * DoubleOperator.LESS_THAN
     * DoubleOperator.GREATER_THAN_OR_EQUALS
     * DoubleOperator.LESS_THAN_OR_EQUALS
     * 
     * For non-double (boolean) value events, the satisfies method should return false.
     *
     * @param field the field to match (event "value" or event "timestamp")
     * @param operator the DoubleOperator to use to compare the event value with the given value
     * @param value the double value to match
     *
     * @throws IllegalArgumentException if the given field is not "value" or "timestamp"
     */
    public Filter(String field, DoubleOperator operator, double value) {
        this.doubleOperator = operator;
        this.doubleValue = value;
        this.matchField = field;

    }
    
    /**
     * A filter can be composed of other filters.
     * in this case, the filter should satisfy all the filters in the list.
     * Constructs a complex filter composed of other filters.
     *
     * @param filters the list of filters to use in the composition
     */
    public Filter(List<Filter> filters) {
        this.filterList = filters;
    }

    /**
     * Returns true if the given event satisfies the filter criteria.
     *
     * @param event the event to check
     * @return true if the event satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(Event event) {
        if (filterList != null) {
            for (Filter currentFilter : filterList) {
                if (!currentFilter.satisfies(event)) {
                    return false;
                }
            }
            return true;
        }
        if (event.getValueDouble() == -1) { //Event is from actuator
            if (booleanOperator == BooleanOperator.EQUALS) {
                return event.getValueBoolean() == boolValue;
            }
            return event.getValueBoolean() != boolValue;
        } else {
            if (matchField == "value") {
                return switch (doubleOperator) {
                    case EQUALS -> event.getValueDouble() == doubleValue;
                    case LESS_THAN -> event.getValueDouble() < doubleValue;
                    case GREATER_THAN -> event.getValueDouble() > doubleValue;
                    case LESS_THAN_OR_EQUALS -> event.getValueDouble() <= doubleValue;
                    case GREATER_THAN_OR_EQUALS -> event.getValueDouble() >= doubleValue;
                };
            }
            return switch (doubleOperator) {
                case EQUALS -> event.getTimeStamp() == doubleValue;
                case LESS_THAN -> event.getTimeStamp() < doubleValue;
                case GREATER_THAN -> event.getTimeStamp() > doubleValue;
                case LESS_THAN_OR_EQUALS -> event.getTimeStamp() <= doubleValue;
                case GREATER_THAN_OR_EQUALS -> event.getTimeStamp() >= doubleValue;
            };
        }
    }

    /**
     * Returns true if the given list of events satisfies the filter criteria.
     *
     * @param events the list of events to check
     * @return true if every event in the list satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(List<Event> events) {
        for (Event currentEvent : events) {
            if (!satisfies(currentEvent)) { return false; }
        }
        return true;
    }

    /**
     * Returns a new event if it satisfies the filter criteria.
     * If the given event does not satisfy the filter criteria, then this method should return null.
     *
     * @param event the event to sift
     * @return a new event if it satisfies the filter criteria, null otherwise
     */
    public Event sift(Event event) {
        // TODO: maybe fix? seems weird
        if (satisfies(event)) { return event; }
        return null;
    }

    /**
     * Returns a list of events that contains only the events in the given list that satisfy the filter criteria.
     * If no events in the given list satisfy the filter criteria, then this method should return an empty list.
     *
     * @param events the list of events to sift
     * @return a list of events that contains only the events in the given list that satisfy the filter criteria
     *        or an empty list if no events in the given list satisfy the filter criteria
     */
    public List<Event> sift(List<Event> events) {
        List<Event> siftedEvents = new ArrayList<>();
        for (Event currentEvent : events) {
            siftedEvents.add(sift(currentEvent));
        }
        return siftedEvents;
    }

    @Override
    public String toString() {
        // TODO: implement this method
        String operator;
        String value;
        String field = matchField;
        if (booleanOperator == null) {
            operator = doubleOperator.toString();
        } else { operator = booleanOperator.toString(); }
        if (doubleValue == Double.MAX_VALUE) {
            value = String.valueOf(boolValue);
        } else { value = String.valueOf(doubleValue); }
        return "Filter{" +
                "Operator=" + operator +
                ", value=" + value +
                ", field" + field +
                '}';
    }
}
