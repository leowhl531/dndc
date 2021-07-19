package dndc.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import dndc.Entity.Item;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


import static dndc.Util.Constant.INDEX;
import static dndc.Util.Constant.TYPE;
import static org.apache.http.entity.ContentType.*;


@Service
@Slf4j
public class ItemService {

    private RestHighLevelClient client;
    private ObjectMapper objectMapper;
    private AmazonS3 amazonS3;

    @Value("${s3.endpointUrl}")
    private String endpointUrl;

    @Autowired
    public ItemService(RestHighLevelClient client, ObjectMapper objectMapper, AmazonS3 amazonS3){
        this.client = client;
        this.objectMapper = objectMapper;
        this.amazonS3 = amazonS3;
    }
    //POST ITEM
    public String createItem(Item item) throws Exception{

        UUID uuid = UUID.randomUUID();
        item.setItemID(uuid.toString());

        Map<String, Object> itemMapper = objectMapper.convertValue(item, Map.class);

        IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, item.getItemID())
                .source(itemMapper,  XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        return indexResponse
                .getResult()
                .name();
    }

    //GET ITEMS
    public List<Item> findById(String residentID) throws Exception{

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("residentID", residentID));
        sourceBuilder.from(0);
        sourceBuilder.size(25);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("item");
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<Item> itemList = new ArrayList<>();

        for(SearchHit hit : searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            itemList.add(convertMapToItem(sourceAsMap));
        }

        return itemList;
    }

    //DELETE ITEM
    public boolean deleteById(String itemID) throws IOException{


        DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(INDEX);
        // query condition
        deleteRequest.setQuery(QueryBuilders.matchQuery("itemID", itemID));
        // execution
        BulkByScrollResponse bulkResponse = client.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse);
        long deletedDocs = bulkResponse.getDeleted();
        if (deletedDocs > 0) {
            return true;
        } else {
            return false;
        }
    }




    public String saveImage(MultipartFile image) throws Exception{
        //check if the file is empty
        if (image.isEmpty()) {
            throw new IllegalStateException("please upload an image");
        }
        //Check if the file is an image
        if (!Arrays.asList(IMAGE_PNG.getMimeType(),
                IMAGE_BMP.getMimeType(),
                IMAGE_GIF.getMimeType(),
                IMAGE_JPEG.getMimeType()).contains(image.getContentType())) {
            throw new IllegalStateException("uploaded is not an image");
        }
        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(image);
            String fileName = generateFileName(image);
            fileUrl = endpointUrl + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;


    }

    private Item convertMapToItem(Map<String, Object> map){
        return objectMapper.convertValue(map, Item.class);
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }


    private void uploadFileTos3bucket(String fileName, File file) {
        amazonS3.putObject(new PutObjectRequest("dndcimage", fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public List<Item> searchByGeo(GeoPoint geoPoint) throws IOException {
        GeoDistanceQueryBuilder qb = QueryBuilders.geoDistanceQuery("location")
                .point(geoPoint.getLat(), geoPoint.getLon())
                .distance(10, DistanceUnit.KILOMETERS);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("item");
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // Get access to the returned documents
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // System.out.println(searchHits.length);
        List<Item> itemList = new ArrayList<>();

        for(SearchHit hit : searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            itemList.add(convertMapToItem(sourceAsMap));
        }

        return itemList;

    }
}
