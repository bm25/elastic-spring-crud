package com.jcpenny.userapi.elastic.user.dao;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcpenny.userapi.elastic.user.model.User;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class UserDao {
    public final static String INDEX = "user";

    private RestHighLevelClient restHighLevelClient;
    private ObjectMapper objectMapper;

    public UserDao( ObjectMapper objectMapper, RestHighLevelClient restHighLevelClient) {
        this.objectMapper = objectMapper;
        this.restHighLevelClient = restHighLevelClient;
    }


    public String insertUser(User user){
        user.setId(UUID.randomUUID().getLeastSignificantBits());
        Map dataMap = objectMapper.convertValue(user, Map.class);
        IndexRequest indexRequest = new IndexRequest(INDEX).
                source(dataMap);
        IndexResponse response = null;
        try {
            response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch(ElasticsearchException e) {
            e.getDetailedMessage();
        } catch (java.io.IOException ex){
            ex.getLocalizedMessage();
        }
        return response.toString();
    }

    public Map<String, Object> getUserById(String id){
        GetRequest getRequest = new GetRequest(INDEX, id);
        GetResponse getResponse = null;
        try {
            getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        } catch (java.io.IOException e){
            e.getLocalizedMessage();
        }
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        return sourceAsMap;
    }

    public String matchAllQuery(){

        SearchRequest searchRequest = new SearchRequest(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (java.io.IOException e){
            e.getLocalizedMessage();
        }
        return searchResponse.toString();
    }

    public Map<String, Object> updateUserById(String id, User user){
        UpdateRequest updateRequest = new UpdateRequest(INDEX, id)
                .fetchSource(true);    // Fetch Object after its update
        Map<String, Object> error = new HashMap<>();
        error.put("Error", "Unable to update user");
        try {
            String userJson = objectMapper.writeValueAsString(user);
            updateRequest.doc(userJson, XContentType.JSON);
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            Map<String, Object> sourceAsMap = updateResponse.getGetResult().sourceAsMap();
            return sourceAsMap;
        }catch (JsonMappingException e){
            e.getMessage();
        } catch (java.io.IOException e){
            e.getLocalizedMessage();
        }
        return error;
    }


    public String deleteUserById(String id) {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX, id);
        DeleteResponse response = null;
        try {
            response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (java.io.IOException e){
            e.getLocalizedMessage();
        }
        return response.status().toString();
    }
}
