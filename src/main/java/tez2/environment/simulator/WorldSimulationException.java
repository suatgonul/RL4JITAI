package tez2.environment.simulator;

/**
 * Created by suatgonul on 12/20/2016.
 */
public class WorldSimulationException extends RuntimeException {
    public WorldSimulationException(String message) {
        super(message);
    }

    public WorldSimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
