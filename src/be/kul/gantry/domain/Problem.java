package be.kul.gantry.domain;



import be.kul.gantry.domain.Pyramid.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.SendingContext.RunTime;

import java.io.*;
import java.util.*;

import static be.kul.gantry.domain.Slot.SlotType.INPUT;
import static be.kul.gantry.domain.Slot.SlotType.OUTPUT;
import static be.kul.gantry.domain.Slot.SlotType.STORAGE;

/**
 * Created by Wim on 27/04/2015.
 */
public class Problem {

    private PrintWriter outputWriter;

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
    private Slot inputSlot;
    private Slot outputSlot;
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
                if(slot.getType() == INPUT) this.inputSlot = slot;
                if(slot.getType() == OUTPUT) this.outputSlot = slot;
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

    public void setOutputWriter(PrintWriter printWriter){
        this.outputWriter = printWriter;
        for(Gantry gantry: gantries) gantry.setOutputWriter(printWriter);
    }

    public void solve(){

        // temporary variables -----------------------------------------------------------------------------------------
        Job inputJob;
        Job outputJob;
        Slot inputFromSlot, inputToSlot;
        Slot outputFromSlot, outputToSlot;
        Slot empty;
        int centerX = (maxX - minX) / 2;
        int centerY = (maxY - minY) / 2;
        double moveStartTime;

        List<Slot> frontIgnoreSlots = new LinkedList<>();
        for(Row row: rows.values()){

            // TODO multiple heights when stacked

            frontIgnoreSlots.addAll(row.getLeftMostSlots());
        }
        List<Slot> backIgnoreSlots = new LinkedList<>();
        for(Row row: rows.values()){
            backIgnoreSlots.addAll(row.getRightMostSlot());
        }

        // print starting positions ------------------------------------------------------------------------------------
        for(Gantry gantry: gantries) gantry.printStart();

        // handle input and output queues until completion -------------------------------------------------------------
        while (!this.inputJobSequence.isEmpty() || !this.outputJobSequence.isEmpty()){

            // reset temporaries ---------------------------------------------------------------------------------------
            inputJob = null;
            outputJob = null;
            inputFromSlot = null;
            inputToSlot = null;
            outputFromSlot = null;
            outputToSlot = null;

            // sync time between gantries ------------------------------------------------------------------------------
            moveStartTime = Math.max(gantries.get(0).getCurrentTime(), gantries.get(1).getCurrentTime());

            // handle potential output ---------------------------------------------------------------------------------
            if(!this.outputJobSequence.isEmpty() &&
                    (outputFromSlot = items.get(outputJobSequence.get(0).getItem().getId()).getSlot()) != null) {
                outputJob = outputJobSequence.remove(0);
                outputToSlot = outputJob.getPlace().getSlot();
                try {
                    if(outputFromSlot.isBuried()) moveStartTime = unBury(outputFromSlot);
                } catch (SlotUnreachableException e) {
                    // should not occur with one gantry
                    e.printStackTrace();
                }
            }

            // handle potential input ----------------------------------------------------------------------------------
            if(!this.inputJobSequence.isEmpty()) {
                inputJob = inputJobSequence.remove(0);
                inputFromSlot = inputJob.getPickup().getSlot();
                try {
                    // look for slot from middle of yard ---------------------------------------------------------------
                    inputToSlot = findEmpty(centerX, centerY, inputJob.getItem(), null);
                } catch (NoSlotAvailableException e){
                    // should not occur, deadlock if occurs
                    e.printStackTrace();
                }
            }

            // throw exception if gantries collide ---------------------------------------------------------------------
            if(outputFromSlot != null &&
                    inputFromSlot != null &&
                    Math.abs(outputFromSlot.getCenterX() - inputFromSlot.getCenterX()) < 20){

                // TODO other side of yard, same issue

                try {
                    gantries.get(0).move(outputFromSlot);
                    gantries.get(0).pickDropItem(outputFromSlot.getItem());
                    empty = findEmpty(outputFromSlot.getCenterX(), outputFromSlot.getCenterY(), outputFromSlot.getItem(), frontIgnoreSlots);
                    gantries.get(0).move(empty);
                    gantries.get(0).pickDropItem(null);
                    moveStartTime = gantries.get(0).getCurrentTime();

                    // synchronise times and wait ----------------------------------------------------------------------
                    gantries.get(1).setCurrentTime(moveStartTime);
                    gantries.get(1).waitForTime();
                } catch (SlotUnreachableException e) {
                    e.printStackTrace();
                } catch (NoSlotAvailableException e) {
                    e.printStackTrace();
                }
            }

            // move to pickup slot and pickup --------------------------------------------------------------------------
            // for input gantry ----------------------------------------------------------------------------------------
            if(inputJob != null) gantryAction(gantries.get(0), inputJob, inputFromSlot, inputJob.getItem(), moveStartTime);
            else {
                try {
                    gantries.get(0).move(inputSlot);
                } catch (SlotUnreachableException e) {
                    // shouldn't appear
                    e.printStackTrace();
                }
            }
            if(outputJob != null) gantryAction(gantries.get(1), outputJob, outputFromSlot, outputJob.getItem(), moveStartTime);
            else {
                try {
                    gantries.get(1).move(outputSlot);
                } catch (SlotUnreachableException e) {
                    // shouldn't appear
                    e.printStackTrace();
                }
            }

            // sync gantry times and wait ------------------------------------------------------------------------------
            moveStartTime = Math.max(gantries.get(0).getCurrentTime(), gantries.get(1).getCurrentTime());

            // move to drop and drop -----------------------------------------------------------------------------------
            if(inputJob != null) gantryAction(gantries.get(0), inputJob, inputToSlot, null, moveStartTime);
            if(outputJob != null) gantryAction(gantries.get(1), outputJob, outputToSlot, null, moveStartTime);
        }
        outputWriter.close();
    }

    private void gantryAction(Gantry gantry, Job job, Slot slot, Item item, double moveStartTime){

        try {
            if(job != null) {
                if(gantry.getCurrentTime() < moveStartTime){
                    gantry.setCurrentTime(moveStartTime);
                    gantry.waitForTime();
                }
                gantry.move(slot);
                if(item == null){
                    gantry.getItem().setSlot(slot);
                }
                gantry.pickDropItem(item);
            }
        } catch (SlotUnreachableException e) {
            throw new RuntimeException(String.format("job %s: unreachable for gantry 0", job.toString()));
        } catch (SlotAlreadyHasItemException e) {
            e.printStackTrace();
        }
    }

    /**
     * move items from slots above the slot containing the needed item.
     *
     * @param slot bottom slot to un-bury.
     * @throws SlotUnreachableException thrown when the slot cannot be reached by the gantry, debug only for one gantry.
     */
    public double unBury(Slot slot) throws SlotUnreachableException{

        // temporary variables -----------------------------------------------------------------------------------------
        List<Slot> toMove = slot.getAbove();
        toMove.sort(Comparator.naturalOrder());
        Slot empty;

        // move item away from all slots above buried slot -------------------------------------------------------------
        for(Slot slotToMove: toMove){
            try {
                if(slotToMove.getItem() != null) {
                    empty = findEmpty(slotToMove.getCenterX(), slotToMove.getCenterY(), slotToMove.getItem(), toMove);

                    gantries.get(1).move(slotToMove);
                    gantries.get(1).pickDropItem(slotToMove.getItem());
                    slotToMove.removeItem();
                    empty.setItem(gantries.get(1).getItem());
                    gantries.get(1).move(empty);
                    gantries.get(1).pickDropItem(null);
                }
            } catch (NoSlotAvailableException e) {
                // should not occur, deadlock if occurs <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                e.printStackTrace();
            } catch (SlotAlreadyHasItemException e) {
                // should never occur
                e.printStackTrace();
            }
        }
        return gantries.get(1).getCurrentTime();
    }

    public Slot findEmpty(int centerX, int centerY, Item toPlace, List<Slot> ignore) throws NoSlotAvailableException{

        // set comparator settings and sort ----------------------------------------------------------------------------
        comparator.setCenterX(centerX);
        comparator.setCenterY(centerY);
        slots.sort(comparator);
        Slot best = null;

        // start looking for slot --------------------------------------------------------------------------------------
        for (Slot slot: slots) {
            /*
             condition to be empty +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                - have no item
                - don't be in the ignore list if it exists
                - be sure to not collapse
                - be a storage slot
                - don't be on the same x and y coordinates
                ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              */
            if(slot.getItem() == null &&
                    (ignore == null || !ignore.contains(slot)) &&
                    slot.willNotCollapse() && slot.getType() == STORAGE &&
                    (toPlace.getSlot() == null ||
                            (slot.getCenterX() != toPlace.getSlot().getCenterX() &&
                            slot.getCenterY() != toPlace.getSlot().getCenterY()))
                    ){
                best = slot;

                // no higher priority item buried -> return ------------------------------------------------------------

                // TODO max distance constraint

                if(best.getPriority() > toPlace.getPriority()) return best;
                // else look for better location -----------------------------------------------------------------------
            }
        }

        // return slot if slot still available -------------------------------------------------------------------------
        if(best != null) return best;
        throw new NoSlotAvailableException();
    }
}
