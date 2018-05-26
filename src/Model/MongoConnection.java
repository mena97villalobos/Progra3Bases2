package Model;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.util.JSON;
import org.bson.Document;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MongoConnection {

    private MongoClient mongoClient = MongoClients.create();
    private MongoDatabase db = mongoClient.getDatabase("Progra3");
    public MongoCollection collection;
    private String collectionName = "";
    private static int agregados = 0;


    public String buscar(String query, String projection){
        String results = "";
        ArrayList<Document> aggregation = new ArrayList<>();
        aggregation.add(Document.parse("{$match:" + query + "}"));
        aggregation.add(Document.parse("{$project:" + projection + "}"));
        for (Object o : collection.aggregate(aggregation)) {
            Document d = (Document) o;
            results += d.toJson() + "\n";
        }
        return results;
    }

    public void crearCollection(String name){
        this.collection = db.getCollection(name);
        this.collectionName = name;
    }

    public String cargarDatos(ArrayList<File> files){
        String estado = "";
        int i = 0;
        try {
            for (File file : files) {
                Scanner scanner = new Scanner(file);
                String text = scanner.useDelimiter("\\A").next();
                scanner.close();
                Document document = Document.parse(text);
                try {
                    collection.insertOne(document);
                }
                catch (Exception e){}
                i++;
            }
            agregados += i;
            estado = String.valueOf(agregados) + " archivos cargados a la colección " + this.collectionName;
        } catch (IOException e) {
            estado = "Error al cargar datos";
            e.printStackTrace();
        }
        return estado;
    }

    public String createTextIndex(){
        collection.createIndex(Document.parse("{TEXT:\"text\", BODY:\"text\"}"));
        return "Indices de texto sobre TITLE Y BODY creados";
    }

    public String crearIndex(String field){
        try {
            collection.createIndex(Indexes.ascending(field));
            return "Creado indice en el campo: " + field;
        }
        catch (Exception e){
            return "Error al crear indice en el campo: " + field;
        }
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
        String results = "Map Reduce results\n";
        try{
            for(DBObject o : out.results()){
                results += o.toString() + "\n";
            }
            return results;
        }
        catch (Exception e){
            return "Error";
        }
    }

}
