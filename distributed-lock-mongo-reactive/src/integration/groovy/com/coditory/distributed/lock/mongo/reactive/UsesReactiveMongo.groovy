package com.coditory.distributed.lock.mongo.reactive

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import groovy.transform.CompileStatic
import org.bson.Document
import org.testcontainers.containers.GenericContainer

@CompileStatic
trait UsesReactiveMongo {
  static final String databaseName = "distributed-lock-mongo"
  static final MongoClient mongoClient = startMongo()

  MongoCollection<Document> getCollection(String collectionName) {
    return mongoClient.getDatabase(databaseName)
        .getCollection(collectionName)
  }

  private static MongoClient startMongo() {
    // using an older version to preserve backward compatibility
    GenericContainer mongo = new GenericContainer<>("mongo:3.4")
        .withExposedPorts(27017)
    mongo.start()
    return MongoClients.create("mongodb://localhost:${mongo.firstMappedPort}/$databaseName")
  }
}