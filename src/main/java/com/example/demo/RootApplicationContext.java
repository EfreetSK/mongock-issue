package com.example.demo;

import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import io.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver;
import io.mongock.runner.standalone.MongockStandalone;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, KafkaAutoConfiguration.class})
public class RootApplicationContext {

    /**
     * Temporary solution as database name isn't set properly in Spring Boot 3.1 autoconfiguration for MongoDB (MongoReactiveDataAutoConfiguration class).
     * Should be fixed in Spring Boot 3.2, once fixed this bean can be removed
     * <a href="https://github.com/spring-projects/spring-boot/issues/35566"/>
     */
    @Bean
    SimpleReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory(MongoClient mongoClient, MongoProperties properties) {
        return new SimpleReactiveMongoDatabaseFactory(mongoClient, properties.getDatabase());
    }

    @Bean("mongock")
    public Object mongockMigration(MongoClientSettings settings, MongoProperties mongoProperties, StandardMongoClientSettingsBuilderCustomizer customizer) {
        final com.mongodb.client.MongoClient mongoClient = new MongoClientFactory(List.of(customizer)).createMongoClient(settings);
        MongoSync4Driver driver = MongoSync4Driver.withDefaultLock(mongoClient, mongoProperties.getMongoClientDatabase());
        driver.setMigrationRepositoryName("_databaseChangeLog");
        driver.setLockRepositoryName("_databaseChangeLogLock");

        MongockStandalone.builder()
                .setDriver(driver)
                .addMigrationScanPackage(RootApplicationContext.class.getPackageName())
                .buildRunner()
                .execute();

        mongoClient.close();
        return new Object();
    }

    @Bean
    @DependsOn("mongock")
    public MongoClient reactiveStreamsMongoClient(
            ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers, MongoClientSettings settings) {
        ReactiveMongoClientFactory factory = new ReactiveMongoClientFactory(
                builderCustomizers.orderedStream().collect(Collectors.toList()));
        return factory.createMongoClient(settings);
    }

    @Bean("txManager")
    public ReactiveMongoTransactionManager txManager(ReactiveMongoDatabaseFactory factory) {
        return new ReactiveMongoTransactionManager(factory);
    }

    @Bean
    @DependsOn("txManager")
    public ReactiveMongoRepositoryFactory mongoRepositoryFactory(ReactiveMongoOperations mongoOperations) {
        return new ReactiveMongoRepositoryFactory(mongoOperations);
    }
}
