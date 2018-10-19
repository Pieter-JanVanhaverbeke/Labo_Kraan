package be.kul.gantry.domain;

/**
 * Created by Wim on 12/05/2015.
 */
public class Item {

    //TODO priority
    //TODO Slot referentie

    private final int id;

    public Item(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
