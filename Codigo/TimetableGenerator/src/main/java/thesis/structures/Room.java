package thesis.structures;

import java.util.ArrayList;
import java.util.HashMap;

public class Room {
    private final String roomId;

    private HashMap<String, Integer> travelPenalization = new HashMap<>();  // RoomId: Penalization
    private ArrayList<Time> unavails = new ArrayList<>();

    public Room(String roomId){
        this.roomId = roomId;
    }

    public void addTravel(String travelRoomId, int penalization){
        travelPenalization.put(travelRoomId, penalization);
    }

    public int getTravelPenalization(String travelRoomId){
        return travelPenalization.get(travelRoomId);
    }

    public void addUnavailability(Time unavail){
        unavails.add(unavail);
    }

    public String getId(){
        return roomId;
    }
}
