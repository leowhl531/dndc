package dndc.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dndc.Entity.Item;
import dndc.Entity.Schedule;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

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
    //post schedule
    public String createSchedule(Schedule schedule) throws Exception {

        UUID uuid = UUID.randomUUID();
        schedule.setScheduleID(uuid.toString());

        List<String> exist = new ArrayList<>();

        //check item's eligibility
        for(String itemId : schedule.getItemIDs()){
            Item cur = itemService.findById(itemId);
            if(cur == null){
                System.out.println("item " + cur.getName() + " not found");
            }else if(cur.getStatus() == 1){
                System.out.println("item " + cur.getName() + " already been schedule");
            }else if(cur.getStatus() == 2){
                System.out.println("item " + cur.getName() + " already been picked up");
            }else{
                exist.add(itemId);
            }
        }
        if(exist.isEmpty()){
            return "No item has been selected.";
        }
        schedule.setItemIDs(exist);
        schedule.setNgoID("89757");
        schedule.setNgoUsername("GOOD WILL");

        //update item status & schedule info
        itemService.updateItems(exist, schedule.getScheduleID(), schedule.getScheduleTime(), schedule.getNgoID(), 1);
        // Creates Schedule Index
        XContentBuilder scheduleBuilder = XContentFactory.jsonBuilder();
        scheduleBuilder.startObject();
        {
            scheduleBuilder.field("scheduleID", schedule.getScheduleID());
            scheduleBuilder.field("ngoID", schedule.getNgoID());
            scheduleBuilder.field("itemIDs", schedule.getItemIDs());
            scheduleBuilder.field("scheduleTime", schedule.getScheduleTime());
            scheduleBuilder.field("status", schedule.getStatus());
            scheduleBuilder.field("ngoUsername", schedule.getNgoUsername());
        }
        scheduleBuilder.endObject();

        // Form the indexing request, send it, and print the response
        IndexRequest request = new IndexRequest("schedule", "_doc", schedule.getScheduleID()).source(scheduleBuilder);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        return response.getResult().name();

    }

    //mark schedule complete
    public boolean markComplete(String scheduleID) throws IOException {
        return markCompleteAction(scheduleID, "item", 2) && markCompleteAction(scheduleID, "schedule", 1);

    }

    private boolean markCompleteAction(String id, String index, int status) throws IOException {
        // update request
        UpdateByQueryRequest updateRequest = new UpdateByQueryRequest(index);
        // search query
        String queryString = "if (ctx._source.scheduleID == '" + id + "') {ctx._source.status=" + status + "}";
        updateRequest.setScript(new Script(ScriptType.INLINE, "painless", queryString, Collections.emptyMap()));
        // execution
        BulkByScrollResponse bulkResponse = client.updateByQuery(updateRequest, RequestOptions.DEFAULT);
        long updatedDocs = bulkResponse.getUpdated();
        if (updatedDocs > 0) {
            return true;
        } else {
            return false;
        }
    }

    //Get my schedule
    public List<Schedule> getMySchedule(String NgoID) throws Exception {
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("NGOID", NgoID);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchQueryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("schedule");
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // get access to the returned documents
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<Schedule> scheduleList = new ArrayList<>();
        for(SearchHit hit : searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Schedule curSchedule = convertMapToItem(sourceAsMap);
            List<Item> itemList = new ArrayList<>();
            for(String itemId : curSchedule.getItemIDs()){
                itemList.add(itemService.findById(itemId));
            }
            curSchedule.setItemList(itemList);
            scheduleList.add(curSchedule);
        }
        return scheduleList;
    }


    private Schedule convertMapToItem(Map<String, Object> map){
        return objectMapper.convertValue(map, Schedule.class);
    }
}
