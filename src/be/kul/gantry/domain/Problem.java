package be.kul.gantry.domain;



import be.kul.gantry.domain.Pyramid.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static be.kul.gantry.domain.Slot.SlotType.INPUT;
import static be.kul.gantry.domain.Slot.SlotType.STORAGE;

/**
 * Created by Wim on 27/04/2015.
 */
public class Problem {

    //TODO Lokaal zoeknen voor slot om Item te plaatsen.
    //TODO Distance function gewogen met snelheid in x en y richting.

    private final int minX, maxX, minY, maxY;
    private final int maxLevels;
    private final List<Item> items;
    private final List<Job> inputJobSequence;
    private final List<Job> outputJobSequence;

    private final List<Gantry> gantries;
    private final List<Slot> slots;
    private final int safetyDistance;
    private final int pickupPlaceDuration;

    private HashMap<Integer, Row> rows;
    private EmptySlotComparator comparator;

    public Problem(int minX, int maxX, int minY, int maxY, int storageMinX, int storageMaxX, int maxLevels,
                   List<Item> items, List<Gantry> gantries, List<Slot> slots,
                   List<Job> inputJobSequence, List<Job> outputJobSequence, int gantrySafetyDist, int pickupPlaceDuration) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.maxLevels = maxLevels;
        this.items = new ArrayList<>(items);
        this.gantries = new ArrayList<>(gantries);
        this.slots = new ArrayList<>(slots);
        this.inputJobSequence = new ArrayList<>(inputJobSequence);
        this.outputJobSequence = new ArrayList<>(outputJobSequence);
        this.safetyDistance = gantrySafetyDist;
        this.pickupPlaceDuration = pickupPlaceDuration;

        this.rows = new HashMap<>();
        LinkedList<Slot> sortedSlots, temp = new LinkedList<>(slots);
        while(temp.size() > 2){
            sortedSlots = new LinkedList<>();
            int y = temp.getFirst().getCenterY();
            for(Slot slot: temp){
                if(slot.getCenterY() == y && slot.getType() != Slot.SlotType.INPUT && slot.getType() != Slot.SlotType.OUTPUT) sortedSlots.add(slot);
            }
            temp.removeAll(sortedSlots);
            this.rows.put(y, new Row(sortedSlots, maxLevels, storageMinX, storageMaxX));
        }
        comparator = new EmptySlotComparator();
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Job> getInputJobSequence() {
        return inputJobSequence;
    }

    public List<Job> getOutputJobSequence() {
        return outputJobSequence;
    }

    public List<Gantry> getGantries() {
        return gantries;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public int getSafetyDistance() {
        return safetyDistance;
    }

    public int getPickupPlaceDuration() {
        return pickupPlaceDuration;
    }

    public void writeJsonFile(File file) throws IOException {
        JSONObject root = new JSONObject();

        JSONObject parameters = new JSONObject();
        root.put("parameters",parameters);

        parameters.put("gantrySafetyDistance",safetyDistance);
        parameters.put("maxLevels",maxLevels);
        parameters.put("pickupPlaceDuration",pickupPlaceDuration);

        JSONArray items = new JSONArray();
        root.put("items",items);

        for(Item item : this.items) {
            JSONObject jo = new JSONObject();
            jo.put("id",item.getId());

            items.add(jo);
        }


        JSONArray slots = new JSONArray();
        root.put("slots",slots);
        for(Slot slot : this.slots) {
            JSONObject jo = new JSONObject();
            jo.put("id",slot.getId());
            jo.put("cx",slot.getCenterX());
            jo.put("cy",slot.getCenterY());
            jo.put("minX",slot.getXMin());
            jo.put("maxX",slot.getXMax());
            jo.put("minY",slot.getYMin());
            jo.put("maxY",slot.getYMax());
            jo.put("z",slot.getZ());
            jo.put("type",slot.getType().name());
            jo.put("itemId",slot.getItem() == null ? null : slot.getItem().getId());

            slots.add(jo);
        }

        JSONArray gantries = new JSONArray();
        root.put("gantries",gantries);
        for(Gantry gantry : this.gantries) {
            JSONObject jo = new JSONObject();

            jo.put("id",gantry.getId());
            jo.put("xMin",gantry.getXMin());
            jo.put("xMax",gantry.getXMax());
            jo.put("startX",gantry.getStartX());
            jo.put("startY",gantry.getStartY());
            jo.put("xSpeed",gantry.getXSpeed());
            jo.put("ySpeed",gantry.getYSpeed());

            gantries.add(jo);
        }

        JSONArray inputSequence = new JSONArray();
        root.put("inputSequence",inputSequence);

        for(Job inputJ : this.inputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",inputJ.getItem().getId());
            jo.put("fromId",inputJ.getPickup().getSlot().getId());

            inputSequence.add(jo);
        }

        JSONArray outputSequence = new JSONArray();
        root.put("outputSequence",outputSequence);

        for(Job outputJ : this.outputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",outputJ.getItem().getId());
            jo.put("toId",outputJ.getPlace().getSlot().getId());

            outputSequence.add(jo);
        }

        try(FileWriter fw = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            fw.write(gson.toJson(root));
        }

    }

    public static Problem fromJson(File file) throws IOException, ParseException {


        JSONParser parser = new JSONParser();

        try(FileReader reader = new FileReader(file)) {
            JSONObject root = (JSONObject) parser.parse(reader);

            List<Item> itemList = new ArrayList<>();
            List<Slot> slotList = new ArrayList<>();
            List<Gantry> gantryList = new ArrayList<>();
            List<Job> inputJobList = new ArrayList<>();
            List<Job> outputJobList = new ArrayList<>();

            JSONObject parameters = (JSONObject) root.get("parameters");
            int safetyDist = ((Long)parameters.get("gantrySafetyDistance")).intValue();
            int maxLevels = ((Long)parameters.get("maxLevels")).intValue();
            int pickupPlaceDuration = ((Long)parameters.get("pickupPlaceDuration")).intValue();

            JSONArray items = (JSONArray) root.get("items");
            for(Object o : items) {
                int id = ((Long)((JSONObject)o).get("id")).intValue();

                Item c = new Item(id);
                itemList.add(c);
            }


            int overallMinX = Integer.MAX_VALUE, overallMaxX = Integer.MIN_VALUE;
            int storageMinX = Integer.MAX_VALUE, storageMaxX = Integer.MIN_VALUE;
            int overallMinY = Integer.MAX_VALUE, overallMaxY = Integer.MIN_VALUE;

            JSONArray slots = (JSONArray) root.get("slots");
            for(Object o : slots) {
                JSONObject slot = (JSONObject) o;

                int id = ((Long)slot.get("id")).intValue();
                int cx = ((Long)slot.get("cx")).intValue();
                int cy = ((Long)slot.get("cy")).intValue();
                int minX = ((Long)slot.get("minX")).intValue();
                int minY = ((Long)slot.get("minY")).intValue();
                int maxX = ((Long)slot.get("maxX")).intValue();
                int maxY = ((Long)slot.get("maxY")).intValue();
                int z = ((Long)slot.get("z")).intValue();

                overallMinX = Math.min(overallMinX,minX);
                overallMaxX = Math.max(overallMaxX,maxX);
                overallMinY = Math.min(overallMinY,minY);
                overallMaxY = Math.max(overallMaxY,maxY);

                Slot.SlotType type = Slot.SlotType.valueOf((String)slot.get("type"));
                Integer itemId = slot.get("itemId") == null ? null : ((Long)slot.get("itemId")).intValue();
                Item c = itemId == null ? null : itemList.get(itemId);

                Slot s = new Slot(id,cx,cy,minX,maxX,minY,maxY,z,type,c);
                // added -----
                if(s.getType() == STORAGE){
                    storageMaxX = Math.max(storageMaxX, maxX);
                    storageMinX = Math.min(storageMinX, minX);
                }

                if(c!= null) try {
                    c.setSlot(s);
                } catch (SlotAlreadyHasItemException e) {
                    //e.printStackTrace();
                    //ignore bij opstellen.
                }
                // added -----
                slotList.add(s);
            }


            JSONArray gantries = (JSONArray) root.get("gantries");
            for(Object o : gantries) {
                JSONObject gantry = (JSONObject) o;


                int id = ((Long)gantry.get("id")).intValue();
                int xMin = ((Long)gantry.get("xMin")).intValue();
                int xMax = ((Long)gantry.get("xMax")).intValue();
                int startX = ((Long)gantry.get("startX")).intValue();
                int startY = ((Long)gantry.get("startY")).intValue();
                double xSpeed = ((Double)gantry.get("xSpeed")).doubleValue();
                double ySpeed = ((Double)gantry.get("ySpeed")).doubleValue();

                Gantry g = new Gantry(id, xMin, xMax, startX, startY, xSpeed, ySpeed, pickupPlaceDuration);
                gantryList.add(g);
            }

            JSONArray inputJobs = (JSONArray) root.get("inputSequence");
            int jid = 0;
            for(Object o : inputJobs) {
                JSONObject inputJob = (JSONObject) o;

                int iid = ((Long) inputJob.get("itemId")).intValue();
                int sid = ((Long) inputJob.get("fromId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),slotList.get(sid),null);
                inputJobList.add(job);
            }

            JSONArray outputJobs = (JSONArray) root.get("outputSequence");
            for(Object o : outputJobs) {
                JSONObject outputJob = (JSONObject) o;

                int iid = ((Long) outputJob.get("itemId")).intValue();
                int sid = ((Long) outputJob.get("toId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),null, slotList.get(sid));

                outputJobList.add(job);
                //adding priority to item
                itemList.get(iid).setPriority(outputJobList.size());
            }


            return new Problem(
                    overallMinX,
                    overallMaxX,
                    overallMinY,
                    overallMaxY,
                    storageMinX,
                    storageMaxX,
                    maxLevels,
                    itemList,
                    gantryList,
                    slotList,
                    inputJobList,
                    outputJobList,
                    safetyDist,
                    pickupPlaceDuration);

        }

    }

    public void solve(){

        Slot fromSlot, toSlot;
        Job job;
        while (!this.inputJobSequence.isEmpty() || !this.outputJobSequence.isEmpty()){

            if(!this.outputJobSequence.isEmpty() &&
                    (fromSlot = items.get(outputJobSequence.get(0).getItem().getId()).getSlot()) != null) {
                job = outputJobSequence.remove(0);
                toSlot = job.getPlace().getSlot();
                try {
                    if(fromSlot.isBuried()) unBury(fromSlot);
                    gantries.get(0).move(job.getItem(), fromSlot, toSlot);
                } catch (SlotAlreadyHasItemException e) {
                    e.printStackTrace();
                } catch (SlotUnreachableException e) {
                    e.printStackTrace();
                }
            }
            else if(!this.inputJobSequence.isEmpty()) {
                job = inputJobSequence.remove(0);
                fromSlot = job.getPickup().getSlot();
                try {
                    toSlot = findEmpty(fromSlot.getCenterX(), fromSlot.getCenterY(), job.getItem());
                    try {
                        gantries.get(0).move(job.getItem(), fromSlot, toSlot);
                    } catch (SlotAlreadyHasItemException e){
                        e.printStackTrace();
                        return;
                    } catch (SlotUnreachableException e){
                        e.printStackTrace();
                    }
                } catch (NoSlotAvailableException e){
                    e.printStackTrace();
                }
            }

        }
    }

    public void unBury(Slot slot) throws SlotUnreachableException{
        List<Slot> toMove = slot.getAbove();
        toMove.sort(Comparator.naturalOrder());
        Slot empty;
        for(Slot slotToMove: toMove){
            try {
                empty = findEmpty(slotToMove.getCenterX(), slotToMove.getCenterY(), slotToMove.getItem());
                gantries.get(0).move(
                        slotToMove.getItem(),
                        slotToMove,
                        empty
                );
            } catch (SlotAlreadyHasItemException e) {
                e.printStackTrace();
            } catch (NoSlotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    public Slot findEmpty(int centerX, int centerY, Item toPlace) throws NoSlotAvailableException{

        if(!outputJobSequence.isEmpty() && outputJobSequence.get(0).getItem() == toPlace){
            return outputJobSequence.remove(0).getPlace().getSlot();
        }

        comparator.setCenterX(centerX);
        comparator.setCenterY(centerY);
        slots.sort(comparator);
        Slot best = null;

        for (Slot slot: slots) {
            if(slot.getItem() == null && slot.willNotCollapse() && slot.getType() == STORAGE &&
                    (toPlace.getSlot() == null ||
                            (slot.getCenterX() != toPlace.getSlot().getCenterX() && slot.getCenterY() != toPlace.getSlot().getCenterY()))){
                best = slot;
                if(best.getPriority() > toPlace.getPriority()) return best;
            }
        }
        if(best != null) return best;
        throw new NoSlotAvailableException();
    }
}
