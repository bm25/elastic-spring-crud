package com.jcpenny.userapi.elastic;

import com.jcpenny.userapi.elastic.user.dao.UserDao;
import com.jcpenny.userapi.elastic.user.model.User;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class SampleDataSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleDataSet.class);
    private static int COUNTER = 0;

    private RestHighLevelClient highLevelClient;

    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("userApiEsClient")
    public void setHighLevelClient(RestHighLevelClient highLevelClient) {
        this.highLevelClient = highLevelClient;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException {
        if (!highLevelClient.indices().exists(new GetIndexRequest(UserDao.INDEX), RequestOptions.DEFAULT)) {
            initIndex();
            LOGGER.info("New index created: {}", UserDao.INDEX);
            bulkIndexCreate();
        }
    }

    private void initIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(UserDao.INDEX);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 2)
        );
        Map<String, Object> longType = new HashMap<>();
        longType.put("type", "long");
        Map<String, Object> textType = new HashMap<>();
        textType.put("type", "text");
        Map<String, Object> keywordType = new HashMap<>();
        keywordType.put("type", "keyword");
        keywordType.put("doc_values", "false");//we are not going to sort, calculate facets or execute agrigate func
        Map<String, Object> integerType = new HashMap<>();
        integerType.put("type", "integer");


        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", textType);
        properties.put("name", textType);
        properties.put("age", textType);
        properties.put("address", textType);
        properties.put("email", textType);
        properties.put("phone", textType);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        request.mapping(mapping);
        CreateIndexResponse indexResponse = highLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println("response id: "+indexResponse.index());
    }

    public void bulkIndexCreate() {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            List<User> users = users();

            users.forEach(user -> {
                IndexRequest indexRequest = new IndexRequest(UserDao.INDEX).
                        source(objectMapper.convertValue(user, Map.class));//TODO: why Map.class???
                bulkRequest.add(indexRequest);
            });

            highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            LOGGER.info("BulkIndex completed: {}", ++COUNTER);
        } catch (Exception e) {
            LOGGER.error("Error bulk index", e);
        }
    }

    private List<User> users() {
        String phoneBaseNpaNxx = "+7921666";
        String addressBase = "Russia, St-Petersburg, 196105, Moyka river enb.";
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Random r = new Random();
            User user = new User();
            user.setName("JohnSmith" + r.nextInt(1000000));
            user.setAge(r.nextInt(100));
            user.setAddress(new StringBuilder(addressBase).append(r.nextInt(100)).append("-").append(r.nextInt(100)).toString());
            user.setEmail(new StringBuilder(user.getName()).append("@mail.ru").toString());
            user.setPhone(new StringBuilder(phoneBaseNpaNxx).append(r.nextInt(8999)+1000).toString());
            users.add(user);
        }
        return users;
    }
}
