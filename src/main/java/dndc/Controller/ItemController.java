package dndc.Controller;

import dndc.Entity.Item;
import dndc.Service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ItemController {

    private ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService){
        this.itemService = itemService;
    }

    @PostMapping("/item")
    public ResponseEntity createItem(@RequestBody Item item) throws Exception{
        return new ResponseEntity(itemService.createItem(item), HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public Item findById(@PathVariable String id) throws Exception{
        return itemService.findById(id);
    }
}
