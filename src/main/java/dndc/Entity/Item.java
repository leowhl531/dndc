package dndc.Entity;

import com.sun.javafx.beans.IDProperty;
import lombok.Data;

import org.elasticsearch.common.geo.GeoPoint;

import javax.annotation.Generated;

@Data
public class Item {

    private String name;
    private String itemID;
    private String residentID;
    private String description;
    private String imageUrl;
    private String address;
    private GeoPoint location;
    private String postTime;

    private String NGOID;
    private String scheduleID;
    private String scheduleTime;
    private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up

}
