package dndc.Controller;

import com.sun.xml.internal.bind.v2.TODO;
import dndc.Entity.Item;
import dndc.Service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ItemController {

    private ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService){
        this.itemService = itemService;
    }

    @PostMapping("/donor/new_item")
    public ResponseEntity createItem(@ModelAttribute Item item, @RequestParam("image") MultipartFile image) throws Exception{
        //test if param is working, TODO delete
        if(image != null){
            item.setImageUrl("https://img.discogs.com/6lJcw-8uxGj7qcg8xzLUWvWLOKE=/578x789/smart/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/A-15885-1586013096-1429.jpeg.jpg");
        }
        return new ResponseEntity(itemService.createItem(item), HttpStatus.CREATED);
    }

    @PostMapping("/donor/delete_item")
    public ResponseEntity deleteItem(){
        //TODO
        return null;
    }

    @GetMapping("/donor/my_item")
    public Item findById() throws Exception{
        //hard code for test, TODO delete
        return itemService.findById("18701797671");
    }

    @GetMapping("/ngo/search_item")
    public List<Item> searchItem(){
        //TODO
        return null;
    }
}
