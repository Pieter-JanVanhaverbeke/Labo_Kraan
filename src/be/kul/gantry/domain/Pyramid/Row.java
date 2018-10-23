package be.kul.gantry.domain.Pyramid;

import be.kul.gantry.domain.Slot;

import java.util.HashMap;
import java.util.List;

public class Row {

    // key: x coord
    private HashMap<Integer, Location> locations;

    public Row(List<Slot> slots) {
        this.locations = new HashMap<>();

        //TODO vullen
    }
}
