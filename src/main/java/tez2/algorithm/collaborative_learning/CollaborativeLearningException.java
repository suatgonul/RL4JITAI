package tez2.algorithm.collaborative_learning;

/**
 * Created by suat on 14-May-17.
 */
public class CollaborativeLearningException extends RuntimeException{
    public CollaborativeLearningException(String message) {
        super(message);
    }

    public CollaborativeLearningException(String message, Throwable cause) {
        super(message, cause);
    }
}
