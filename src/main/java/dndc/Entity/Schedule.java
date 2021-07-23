package dndc.Entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Schedule {
    private String scheduleID;
    private String ngoID;
    private String scheduleTime;
    private List<String> itemIDs;
    private int status;  // 0 for scheduled, 1 for picked up
    private List<Item> itemList; // optional for My_schedule
    private String ngoUsername;
}
