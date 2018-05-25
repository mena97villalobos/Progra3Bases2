package Model;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    public MongoClient mongoClient = MongoClients.create();
    public MongoDatabase db = mongoClient.getDatabase("Progra3");
    public MongoCollection collection;
    public void buscar(String query){

    }

    public void crearCollection(String name){
        this.collection = db.getCollection(name);
    }
}
