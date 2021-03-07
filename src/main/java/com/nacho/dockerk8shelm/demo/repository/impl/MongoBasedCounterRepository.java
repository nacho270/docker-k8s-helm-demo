package com.nacho.dockerk8shelm.demo.repository.impl;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.nacho.dockerk8shelm.demo.repository.CounterRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import static java.util.Objects.requireNonNull;

@Slf4j
public class MongoBasedCounterRepository implements CounterRepository {

    private static final String DB_NAME = "docker-k8s-helm-demo";
    private static final String COUNTER_COLLECTION = "counter";

    private final MongoCollection<Document> collection;

    public MongoBasedCounterRepository(String user, String password, String host, Integer port) {
        log.info("Establishing mongo connection to host: {}, port: {}, user: {}", host, port, user);
        var credential = MongoCredential.createCredential(user, "admin", password.toCharArray());
        var mongoClient = new MongoClient(new ServerAddress(host, port), credential, MongoClientOptions.builder().build());
        var database = mongoClient.getDatabase(DB_NAME);
        collection = database.getCollection(COUNTER_COLLECTION);
    }

    @Override
    public Integer increment() {
        var increases = new Document().append("counter", 1);
        var document = new Document().append("$inc", increases);
        return requireNonNull(collection.findOneAndUpdate(new Document(), document,
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)))
                .getInteger("counter");
    }

    @Override
    public Integer getCurrent() {
        MongoCursor<Document> cursor = collection.find().cursor();
        if (cursor.hasNext()) {
            return cursor.next().getInteger("counter");
        }
        return 0;
    }
}
