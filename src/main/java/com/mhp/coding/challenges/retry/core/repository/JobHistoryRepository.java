package com.mhp.coding.challenges.retry.core.repository;

import com.mongodb.MongoClient;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Simplest possible implementation for accessing the MongoDB
 * to persist job history documents.
 * 
 * @author Niko Schmuck
 */
@Repository
public class JobHistoryRepository {

    @Autowired
    private MongoClient mongo;

    public void add(Map<String, Object> keys) {
        mongo.getDatabase("jobs-demo").getCollection("job_history")
                .insertOne(new Document(keys));
    }

}
