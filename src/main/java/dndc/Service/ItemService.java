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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


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

    //TEST
    public Item findById(String id) throws Exception{
        GetRequest getRequest = new GetRequest(INDEX, TYPE, id);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> resultMap = getResponse.getSource();
        return convertMapToItem(resultMap);
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
}
