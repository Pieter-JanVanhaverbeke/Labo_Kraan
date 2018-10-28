package be.kul.gantry.domain;

public class SlotUnreachableException extends Exception{
    public SlotUnreachableException() {
        super("Slot can't be reached.");
    }

    public SlotUnreachableException(String message) {
        super("Can't reach slot:\n" + message);
    }
}
