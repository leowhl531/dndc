package dndc.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//TODO
@Service
public class ScheduleService {

    private RestHighLevelClient client;
    private ObjectMapper objectMapper;

    @Autowired
    public ScheduleService(RestHighLevelClient client, ObjectMapper objectMapper){
        this.client = client;
        this.objectMapper = objectMapper;
    }
}
