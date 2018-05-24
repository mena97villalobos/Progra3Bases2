package Model;

import java.io.*;
import java.util.*;
import org.json.*;

/*
  JSON library provided by json.org (http://www.json.org/)
*/

public class XmltoJson {
    public static void main(String[] args) {
        int i=0,size=args.length;

        try {
            while (i<size) {
                String XMLString="";
                XMLString = new Scanner( new File(args[i]),"UTF-8").useDelimiter("\\A").next();
                JSONObject jsonObj = null;
                jsonObj = XML.toJSONObject(XMLString);
                String json = jsonObj.toString();
                if (!json.equals("{}")) {
                    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("C:/Users/mena9/Desktop/json1.json"), "utf-8"))) {
                        writer.write(json);
                    }
                    catch (Exception e){

                    }
                }
                i++;
            }
        } catch (FileNotFoundException e) {
        } catch (JSONException e) {}
    }
}
