package dndc.Controller;

import com.sun.xml.internal.bind.v2.TODO;
import dndc.Entity.AddressFormatter;
import dndc.Entity.Item;
import dndc.Service.ItemService;
import org.apache.tomcat.jni.Address;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
public class ItemController {

    private ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService){
        this.itemService = itemService;
    }

    @PostMapping("/donor/new_item")
    public ResponseEntity createItem(@ModelAttribute Item item, @ModelAttribute AddressFormatter addressFormatter, @RequestParam("image") MultipartFile image) throws Exception{
        try {
            item.setImageUrl(itemService.saveImage(image));
        }catch (Exception e){
            throw new IllegalStateException("fail to upload image");
        }

        item.setLocation(addressFormatter.getGeoPoint());
        item.setAddress(addressFormatter.getFormat());
        item.setPostTime(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));

        return new ResponseEntity(itemService.createItem(item), HttpStatus.CREATED);
    }

    @PostMapping("/donor/delete_item/{itemID}")
    public ResponseEntity deleteItem(@PathVariable(value = "itemID") String itemID) throws Exception{
        System.out.println(itemID);
        return new ResponseEntity(itemService.deleteById(itemID), HttpStatus.OK);
    }

    @GetMapping("/donor/my_item")
    public List<Item> findById() throws Exception{
        //hard code for test, TODO delete
        return itemService.findByUserId("554455");
    }

    @GetMapping("/ngo/search_item")
    public List<Item> searchItem() throws IOException {
        return itemService.searchByGeo(new GeoPoint(33.9474144,-117.6889273 ));
    }
}
