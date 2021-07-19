package dndc.Entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Schedule {
    private String scheduleID;
    private String NGOID;
    private String scheduleTime;
    private List<String> itemIDs;
    private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up
    private List<Item> itemList; // optional for My_schedule
    private String NGOUsername;
}
