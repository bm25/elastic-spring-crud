package com.jcpenny.userapi.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
//import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.Executors;


@SpringBootApplication
//EnableElasticsearchRepositories
public class EsUserApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EsUserApiApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty("initial-import.enabled")
	public SampleDataSet dataSet() {
		return new SampleDataSet();
	}

	@Bean(name = "ConcurrentTaskExecutor")
	public TaskExecutor taskExecutor () {
		return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(3));
	}
}
