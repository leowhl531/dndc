package dndc.Entity;

import lombok.Data;
import org.elasticsearch.common.geo.GeoPoint;

@Data
public class Item {

    private String name;
    private String itemID;
    private String address;

}
