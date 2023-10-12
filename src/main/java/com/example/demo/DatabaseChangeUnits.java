package com.example.demo;

import com.mongodb.client.MongoDatabase;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;

public class DatabaseChangeUnits {
    @ChangeUnit(id="0002", order = "0002", author = "test")
    public static class MyFirstChangeUnit {
        private final MongoDatabase db;

        public MyFirstChangeUnit(MongoDatabase db) {
            this.db = db;
        }

        @Execution
        public void insertSomething() {
            db.getCollection("helloMongock").insertOne(new Document("myKey", "firstValue"));
        }

        @RollbackExecution
        public void rollback() {
        }
    }
}
