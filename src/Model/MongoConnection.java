package Model;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import java.util.ArrayList;

public class MongoConnection {

    private MongoClient mongoClient = MongoClients.create();
    private MongoDatabase db = mongoClient.getDatabase("Progra3");
    public MongoCollection<Document> collection;
    private String collectionName = "";


    public String buscar(String query, String projection){
        if(projection.equals("") || projection.equals("{}"))
            projection = "{_id:0, NEWID:1, TITLE:1}";
        StringBuilder results = new StringBuilder();
        ArrayList<Document> aggregation = new ArrayList<>();
        aggregation.add(Document.parse("{$match:" + query + "}"));
        aggregation.add(Document.parse("{$project:" + projection + "}"));
        for (Object o : collection.aggregate(aggregation)) {
            Document d = (Document) o;
            results.append(d.toJson()).append("\n");
        }
        return results.toString();
    }

    public void crearCollection(String name){
        this.collection = db.getCollection(name);
        this.collectionName = name;
    }

    void cargarDatos(String json){
        Document document = Document.parse(json);
        try {
            collection.insertOne(document);
        }
        catch (Exception e){}
    }

    public void createTextIndex(){
        collection.createIndex(Document.parse("{\"TEXT.TITLE\":\"text\", \"TEXT.BODY\":\"text\"}"));
    }

    public void crearIndex(String field){
        try {
            collection.createIndex(Indexes.ascending(field));
        }
        catch (Exception e){}
    }

    public String mapReduce(){
        com.mongodb.MongoClient client = new com.mongodb.MongoClient(new ServerAddress("localhost", 27017));
        DB databaseMR = client.getDB("Progra3");
        DBCollection dbCollection = databaseMR.getCollection(this.collectionName);
        String map = "function() {\n" +
                    "try{\n"+
                        "for(var i = 0; i < this.PLACES.length; i++ ){\n" +
                            "var key = this.PLACES[i];\n" +
                            "emit(key, 1);\n" +
                        "}\n" +
                    "}" +
                    "catch(err){\n"+
                        "emit(\"UNKOWN\", 1)" +
                    "}"+
                "}";
        String reduce = "function(key, values){\n" +
                    "return Array.sum(values);\n" +
                "}";
        MapReduceCommand cmd = new MapReduceCommand(dbCollection, map, reduce,null, MapReduceCommand.OutputType.INLINE, null);
        MapReduceOutput out = dbCollection.mapReduce(cmd);
        StringBuilder results = new StringBuilder("Map Reduce results\n");
        try{
            for(DBObject o : out.results()){
                results.append(o.toString()).append("\n");
            }
            return results.toString();
        }
        catch (Exception e){
            return "Error";
        }
    }

}
