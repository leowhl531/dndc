package dndc;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;

@SpringBootApplication
public class DndcApplication {

    public static void main(String[] args) {
        SpringApplication.run(DndcApplication.class, args);
    }

    @Autowired
    public void esSetUp(RestHighLevelClient client) throws IOException {
        XContentBuilder itemBuilder = XContentFactory.jsonBuilder();
        itemBuilder.startObject();
        {
            itemBuilder.field("ItemID", "keyword").field("index", "true");
            itemBuilder.field("name", "string");
            itemBuilder.field("residentID", "keyword");
            itemBuilder.field("description", "string");
            itemBuilder.field("imageUrl", "string");
            itemBuilder.field("address", "string");
            itemBuilder.field("location", new GeoPoint());
            itemBuilder.field("NGOID", "keyword");
            itemBuilder.field("scheduleID", "keyword");
            itemBuilder.field("scheduleTime", "string");
            itemBuilder.field("status", 0);
        }
        itemBuilder.endObject();

        // Form the indexing request, send it, and print the response
        IndexRequest request = new IndexRequest("item", "_doc", "1").source(itemBuilder);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println("Created Index Item!");
        System.out.println(response.toString());

        // Creates Schedule Index
        XContentBuilder scheduleBuilder = XContentFactory.jsonBuilder();
        scheduleBuilder.startObject();
        {
            scheduleBuilder.field("scheduleID", "keyword");
            scheduleBuilder.field("NGOID", "keyword");
            scheduleBuilder.field("ITEM_ID[]", new ArrayList<String>());
            scheduleBuilder.field("scheduleTime", "string");
            scheduleBuilder.field("status", 0);
        }
        scheduleBuilder.endObject();

        // Form the indexing request, send it, and print the response
        request = new IndexRequest("schedule", "_doc", "1").source(scheduleBuilder);
        response = client.index(request, RequestOptions.DEFAULT);

        System.out.println(response.toString());
    }

}
