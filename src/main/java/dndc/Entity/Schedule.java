package dndc.Entity;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Schedule {
    private String scheduleID;
    private String NGOID;
    private String scheduleTime;
    private String[] ItemIDList;
    private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up
    private ArrayList<Item> itemList; // optional for My_schedule
    private String NGOUsername;
}
