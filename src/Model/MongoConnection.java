package Model;

import com.mongodb.DBObject;
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

    public MongoClient mongoClient = MongoClients.create();
    public MongoDatabase db = mongoClient.getDatabase("Progra3");
    public MongoCollection collection;
    public String collectionName = "";
    public static int agregados = 0;

    public String buscar(String query){
        String results = "";
        ArrayList<Document> aggregation = new ArrayList<>();
        aggregation.add(Document.parse("{$match:" + query + "}"));
        aggregation.add(Document.parse("{$project:{_id:1, TITLE:1}}"));
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

    public String crearIndex(String field){
        try {
            collection.createIndex(Indexes.ascending(field));
            return "Creado indice en el campo: " + field;
        }
        catch (Exception e){
            return "Error al crear indice en el campo: " + field;
        }
    }
}
