package codegeneratorjava;

import java.util.Hashtable;
import java.util.Map;

import utilities.Log;

/**
 * @author Ben
 */
public class StateTable {
    
    public enum State {
        FOR_EXPR        ("for-expr"),
        PAR_FOR_EXPR    ("par-expr"),
        WHILE_EXPR      ("while-expr"),
        ;
        
        private final String name;
        
        private State(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    // The actual entries in the table
    private Map<State, Boolean> states = new Hashtable<>();
    
    public StateTable() {
        states.put(State.FOR_EXPR, false);
        states.put(State.PAR_FOR_EXPR, false);
        states.put(State.WHILE_EXPR, false);
    }
    
    public Boolean put(State state, boolean value) {
        return states.put(state, value);
    }
    
    public Boolean get(State state) {
        return states.get(state);
    }
    
    public void printState(String indent) {
        Log.log(indent + "Content ");
        for (State s : states.keySet())
            Log.log(indent + s + ".........:" + states.get(s));
    }
}
