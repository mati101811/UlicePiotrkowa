package mat.szu.ulicepiotrkowa;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONHelper
{
    public static Map<String, Street> jsonToMap(Context context)
    {
        String json = loadJSONFromAsset(context);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(GeoPoint.class, new GeoPointDeserializer());
        Gson gson = gsonBuilder.create();
        // Deserializacja listy obiektów Street
        Type         listType = new TypeToken<List<Street>>( ) {}.getType( );
        List<Street> streets  = gson.fromJson(json, listType);
        
        // Tworzenie mapy
        Map<String, Street> streetMap = new HashMap<>( );
        for (Street street : streets)
        {
            Log.d("TAG", "jsonToMap: dodawanie do mapy: "+street.getSimpleName( )+" "+street.getFullName( )+" "+street.getPoints( ));
            streetMap.put(street.getSimpleName( ), street);
        }
        return streetMap;
    }
    
    // Deserializer dla GeoPoint
    static class GeoPointDeserializer implements JsonDeserializer<GeoPoint>
    {
        @Override
        public GeoPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            double     lat        = jsonObject.get("lat").getAsDouble();
            double lon = jsonObject.get("lon").getAsDouble();
            return new GeoPoint(lat, lon);
        }
    }
    
    private static String loadJSONFromAsset(Context context) {
        try (InputStreamReader reader = new InputStreamReader(context.getResources().openRawResource(R.raw.streets))) {
            char[] buffer = new char[1024];
            StringBuilder stringBuilder = new StringBuilder();
            int length;
            while ((length = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, length);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
