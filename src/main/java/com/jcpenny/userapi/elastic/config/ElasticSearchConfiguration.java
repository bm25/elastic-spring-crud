package com.jcpenny.userapi.elastic.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class ElasticSearchConfiguration {

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private int esPort;

    @Value("${elasticsearch.cluster-name}")
    private String esClusterName;

    @Bean(name="userApiEsClient", destroyMethod = "close")
    public RestHighLevelClient client() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort, "http")));
    }
}
