package com.example.demo;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoDatabase;

@ChangeLog
public class DatabaseChangeLog {
    @ChangeSet(order = "0001", id = "0001", author = "test")
    public void myFirstChangeLog(MongoDatabase db) {
        db.createCollection("helloMongock");
    }
}
