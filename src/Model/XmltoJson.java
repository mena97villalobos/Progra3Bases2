
package Model;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.concurrent.Task;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class XmltoJson {

    public void XMLtoJSON(String file, String filename, MongoConnection mongo) {
        try {
            String XMLString;
            XMLString = readFile(file, Charset.forName("UTF-8"));
            Document doc = Jsoup.parse(XMLString, "", Parser.xmlParser());
            int i = 0;
            String pattern = "\"\\w+\":\\s*(\"*)(NOCONTENT)(\"*).*";
            Pattern p = Pattern.compile(pattern);
            for (Element e : doc.select("REUTERS")) {
                StringBuilder json = new StringBuilder();
                ArrayList<String> datos =new ArrayList<>();
                JSONObject obj = XML.toJSONObject(e.toString()).getJSONObject("REUTERS"); //new JSONObject(s).getJSONObject("REUTERS");
                datos.add("\"NEWID\":" + Integer.toString(obj.getInt("NEWID")) + ", "); //String newidContent
                datos.add("\"DATE\":\"" + parseTag(obj, "DATE", false) + "\", "); //String dateContent
                datos.add("\"TOPICS\":" + parseTag(obj, "TOPICS", true) + ", "); //String topicsContent
                datos.add("\"PLACES\":" + parseTag(obj, "PLACES", true) + ", "); //String placesContent
                datos.add("\"PEOPLE\":" + parseTag(obj, "PEOPLE",true) + ", "); //String peopleContent
                datos.add("\"ORGS\":" + parseTag(obj, "ORGS", true) + ", "); //String orgsContent
                datos.add("\"EXCHANGES\":" + parseTag(obj, "EXCHANGES", true) + ", "); //String exchangesContent
                datos.add("\"TEXT\":{"); //Anidar los datos del tag TEXT
                datos.add("\"TITLE\":\"" + parseTag(obj.getJSONObject("TEXT"), "TITLE", false) + "\", "); //String titleContent
                datos.add("\"AUTHOR\":\"" + parseTag(obj.getJSONObject("TEXT"), "AUTHOR", false) + "\", "); //String authorContent
                datos.add("\"DATELINE\":\"" + parseTag(obj.getJSONObject("TEXT"), "DATELINE", false) + "\", "); //String datelineContent
                datos.add("\"BODY\":\"" + parseTag(obj.getJSONObject("TEXT"), "BODY", false) + "\","); //String bodyContent
                datos.add("}"); //Anidar los datos del tag TEXT
                json.append("{");
                for (String dato : datos) {
                    Matcher matcher = p.matcher(dato);
                    if(!matcher.find()){
                        json.append(dato);
                    }
                }
                json.append("}");
                writeJSON(json.toString(), filename + "_" + String.valueOf(i) + ".json");
                mongo.cargarDatos(json.toString());
                i++;
            }
        }
        catch (FileNotFoundException e){}
        catch (IOException e) {}
    }

    private void writeJSON(String json, String filename){
        Task task = new Task() {
            @Override
            protected Void call(){
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filename), "utf-8"))) {
                    writer.write(json);
                }
                catch (FileNotFoundException e){}
                catch (UnsupportedEncodingException e){}
                catch (IOException e) {}
                return null;
            }
        };
        Thread t = new Thread(task);
        t.start();
    }

    private String parseTag(JSONObject json, String tag, boolean isArray){
        String content;
        if(isArray){
            Object o = json.get(tag);
            if(o instanceof JSONObject){
                Object contenidoD = ((JSONObject) o).get("D");
                if(contenidoD instanceof JSONArray){
                    return contenidoD.toString();
                }
                else{
                    return "[\"" + contenidoD + "\"]";
                }
            }
            else if(o instanceof JSONArray){
                if(((JSONArray) o).get(0).equals("NO"))
                    return "NOCONTENT";
                else{
                    if(!((JSONArray) o).get(1).equals("")) {
                        Object o1 = new JSONObject(((JSONArray) o).get(1).toString()).get("D");
                        if (o1 instanceof JSONArray) {
                            return o1.toString();
                        }
                        if (o1.toString().equals(""))
                            return "NOCONTENT";
                        return "[\"" + o1.toString() + "\"]";
                    }
                    return "NOCONTENT";
                }
            }
            else
                return "NOCONTENT";
        }
        else{
            try{
                content = json.getString(tag);
            }
            catch (JSONException e){
                content = "NOCONTENT";
            }
        }
        return content.replace("\"", "");
    }

    private static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}