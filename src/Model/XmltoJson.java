package Model;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                ArrayList<String> datos =new ArrayList<>();

                JSONObject obj = new JSONObject(s).getJSONObject("REUTERS");

                datos.add("\"_id\":\"" + obj.getInt("NEWID") + "\", "); //String newidContent
                datos.add("\"DATE\":\"" + parseTag(obj, "DATE", false) + "\", "); //String dateContent
                datos.add("\"TOPICS\":" + parseTag(obj, "TOPICS", true) + ", "); //String topicsContent
                datos.add("\"PLACES\":" + parseTag(obj, "PLACES", true) + ", "); //String placesContent
                datos.add("\"PEOPLE\":" + parseTag(obj, "PEOPLE",true) + ", "); //String peopleContent
                datos.add("\"ORGS\":" + parseTag(obj, "ORGS", true) + ", "); //String orgsContent
                datos.add("\"EXCHANGES\":" + parseTag(obj, "EXCHANGES", true) + ", "); //String exchangesContent
                datos.add("\"TITLE\":\"" + parseTag(obj.getJSONObject("TEXT"), "TITLE", false) + "\", "); //String titleContent
                datos.add("\"AUTHOR\":\"" + parseTag(obj.getJSONObject("TEXT"), "AUTHOR", false) + "\", "); //String authorContent
                datos.add("\"DATELINE\":\"" + parseTag(obj.getJSONObject("TEXT"), "DATELINE", false) + "\", "); //String datelineContent
                datos.add("\"BODY\":\"" + parseTag(obj.getJSONObject("TEXT"), "BODY", false) + "\","); //String bodyContent
                String pattern = "\"\\w+\":\\s*(\"*)(NOCONTENT)(\"*).*";
                Pattern p = Pattern.compile(pattern);
                json += "{";
                for (String dato : datos) {
                    Matcher matcher = p.matcher(dato);
                    if(!matcher.find()){
                        json += dato;
                    }
                }
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
                            return ((JSONArray) o1).toString();
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
}
