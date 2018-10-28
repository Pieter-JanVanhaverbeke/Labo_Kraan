package be.kul.gantry.domain;

public class NoSlotAvailableException extends Exception{

    public NoSlotAvailableException() {
        super("No open slot available.");
    }

    public NoSlotAvailableException(String message) {
        super(message);
    }
}
