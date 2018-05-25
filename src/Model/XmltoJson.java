package Model;
import java.io.*;
import java.util.*;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class XmltoJson {

    public String jsonInicial = "";

    public ArrayList XMLtoJSON(String filePath, String filename) {
        ArrayList<File> files = new ArrayList<>();
        try {
            String XMLString = "";
            XMLString = new Scanner(new File(filePath), "UTF-8").useDelimiter("\\A").next();
            Document doc = Jsoup.parse(XMLString, "", Parser.xmlParser());
            String jsonFinal = "";
            for (Element e : doc.select("REUTERS")) {
                JSONObject jsonObj = null;
                jsonObj = XML.toJSONObject(e.toString());
                jsonFinal += jsonObj.toString() + "\n";
            }
            String json = "";
            String[] docs = jsonFinal.split("\n");
            int i = 0;
            for (String s : docs) {
                json += "{";
                JSONObject obj = new JSONObject(s).getJSONObject("REUTERS");
                json += "\"_id\":\"" + obj.getInt("NEWID") + "\", "; //String newidContent
                json += "\"DATE\":\"" + parseTag(obj, "DATE", false) + "\", "; //String dateContent
                json += "\"TOPICS\":" + parseTag(obj, "TOPICS", true) + ", "; //String topicsContent
                json += "\"PLACES\":" + parseTag(obj, "PLACES", true) + ", "; //String placesContent
                json += "\"PEOPLE\":" + parseTag(obj, "PEOPLE",true) + ", "; //String peopleContent
                json += "\"ORGS\":" + parseTag(obj, "ORGS", true) + ", "; //String orgsContent
                json += "\"EXCHANGES\":" + parseTag(obj, "EXCHANGES", true) + ", "; //String exchangesContent
                json += "\"TITLE\":\"" + parseTag(obj.getJSONObject("TEXT"), "TITLE", false) + "\", "; //String titleContent
                json += "\"AUTHOR\":\"" + parseTag(obj.getJSONObject("TEXT"), "AUTHOR", false) + "\", "; //String authorContent
                json += "\"DATELINE\":\"" + parseTag(obj.getJSONObject("TEXT"), "DATELINE", false) + "\", "; //String datelineContent
                json += "\"BODY\":\"" + parseTag(obj.getJSONObject("TEXT"), "BODY", false) + "\","; //String bodyContent
                json += "}";
                writeJSON(json, filename + "_" + String.valueOf(i) + ".json");
                files.add(new File(filename + "_" + String.valueOf(i) + ".json"));
                json = "";
                i++;
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return files;
    }


    private void writeJSON(String json, String filename){
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"))) {
            writer.write(json);
        }
        catch (FileNotFoundException e){}
        catch (UnsupportedEncodingException e){}
        catch (IOException e) {}
    }

    private String parseTag(JSONObject json, String tag, boolean isArray){
        String content = "";
        if(isArray){
            Object o = json.get(tag);
            if(o instanceof JSONObject){
                Object contenidoD = ((JSONObject) o).get("D");
                if(contenidoD instanceof JSONArray){
                    return ((JSONArray) contenidoD).toString();
                }
                else{
                    return "[\"" + contenidoD + "\"]";
                }
            }
            else if(o instanceof JSONArray){
                if(((JSONArray) o).get(0).equals("NO"))
                    return "[]";
                else{
                    if(!((JSONArray) o).get(1).equals("")) {
                        Object o1 = new JSONObject(((JSONArray) o).get(1).toString()).get("D");
                        if (o1 instanceof JSONArray) {
                            return ((JSONArray) o1).toString();
                        }
                        if (o1.toString().equals(""))
                            return "[]";
                        return "[\"" + o1.toString() + "\"]";
                    }
                    return "[]";
                }
            }
            else
                return "[]";
        }
        else{
            try{
                content = json.getString(tag);
            }
            catch (JSONException e){
                content = "";
            }
        }
        return content.replace("\"", "");
    }
}
