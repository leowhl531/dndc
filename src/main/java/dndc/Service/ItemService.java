package dndc.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dndc.Entity.Item;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


import static dndc.Util.Constant.INDEX;
import static dndc.Util.Constant.TYPE;


@Service
@Slf4j
public class ItemService {

    private RestHighLevelClient client;
    private ObjectMapper objectMapper;

    @Autowired
    public ItemService(RestHighLevelClient client, ObjectMapper objectMapper){
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public String createItem(Item item) throws Exception{
        Map<String, Object> itemMapper = objectMapper.convertValue(item, Map.class);

        IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, item.getItemID())
                .source(itemMapper,  XContentType.JSON);
        System.out.println("Successfully built request.");
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        return indexResponse
                .getResult()
                .name();
    }


    public Item findById(String id) throws Exception{
        GetRequest getRequest = new GetRequest(INDEX, TYPE, id);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> resultMap = getResponse.getSource();
        return convertMapToItem(resultMap);
    }


    private Item convertMapToItem(Map<String, Object> map){
        return objectMapper.convertValue(map, Item.class);
    }
}
