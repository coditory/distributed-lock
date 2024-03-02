package com.coditory.sherlock

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import groovy.transform.CompileStatic

@CompileStatic
class KtMongoClientHolder {
    private static MongoClient mongoClient

    synchronized static MongoClient getClient() {
        if (mongoClient != null) return mongoClient
        MongoHolder.startDb()
        String url = MongoHolder.getConnectionString()
        mongoClient = MongoClients.create(url)
        return mongoClient
    }
}
