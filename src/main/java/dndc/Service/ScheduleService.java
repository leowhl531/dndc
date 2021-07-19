package dndc.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dndc.Entity.Item;
import dndc.Entity.Schedule;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//TODO
@Service
public class ScheduleService {

    private RestHighLevelClient client;
    private ObjectMapper objectMapper;
    private ItemService itemService;

    @Autowired
    public ScheduleService(RestHighLevelClient client, ObjectMapper objectMapper, ItemService itemService){
        this.client = client;
        this.objectMapper = objectMapper;
        this.itemService = itemService;
    }

    public String createSchedule(Schedule schedule) throws Exception {

        UUID uuid = UUID.randomUUID();
        schedule.setScheduleID(uuid.toString());

        List<String> exist = new ArrayList<>();
        List<Item> itemList = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for(String itemId : schedule.getItemIDs()){
            Item cur = itemService.findById(itemId);
            if(cur != null){
                itemList.add(cur);
                exist.add(itemId);
            }else{
                notFound.add(itemId);
            }
        }
        schedule.setItemList(itemList);
        schedule.setItemIDs(exist);
        itemService.updateItems(exist, schedule.getScheduleID(), schedule.getScheduleTime(), schedule.getNGOID(), 1);
        // Creates Schedule Index
        XContentBuilder scheduleBuilder = XContentFactory.jsonBuilder();
        scheduleBuilder.startObject();
        {
            scheduleBuilder.field("scheduleID", schedule.getScheduleID());
            scheduleBuilder.field("NGOID", schedule.getNGOID());
            scheduleBuilder.field("itemIDs", schedule.getItemIDs());
//            scheduleBuilder.field("itemList", schedule.getItemList());
            scheduleBuilder.field("scheduleTime", schedule.getScheduleTime());
            scheduleBuilder.field("status", schedule.getStatus());
            scheduleBuilder.field("NGOUsername", schedule.getNGOUsername());
        }
        scheduleBuilder.endObject();

        // Form the indexing request, send it, and print the response
        IndexRequest request = new IndexRequest("schedule", "_doc", schedule.getScheduleID()).source(scheduleBuilder);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        return response.getResult().name();

    }
}
