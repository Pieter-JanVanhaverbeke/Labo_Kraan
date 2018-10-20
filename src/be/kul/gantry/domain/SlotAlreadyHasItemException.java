package be.kul.gantry.domain;

public class SlotAlreadyHasItemException extends Exception {

    public SlotAlreadyHasItemException(){
        super();
    }

    public SlotAlreadyHasItemException(String message){
        super(message);
    }
}
