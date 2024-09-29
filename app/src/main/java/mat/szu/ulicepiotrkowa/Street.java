package mat.szu.ulicepiotrkowa;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;

public class Street {
    
    // Metoda do odczytu danych z pliku JSON i zapisania ich w mapie
    public static Map<String, List<GeoPoint>> loadStreetsMapFromJSON(Context context) {
        Map<String, List<GeoPoint>> streetsMap = new HashMap<>();
        try {
            // Otwórz plik JSON z zasobów raw
            InputStream inputStream = context.getResources().openRawResource(R.raw.streets);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            
            // Czytaj plik linia po linii
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            // Parsowanie JSON do obiektów
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject streetObject = jsonArray.getJSONObject(i);
                String name = streetObject.getString("name");
                
                JSONArray pointsArray = streetObject.getJSONArray("points");
                List<GeoPoint> points = new ArrayList<>();
                for (int j = 0; j < pointsArray.length(); j++) {
                    JSONObject pointObject = pointsArray.getJSONObject(j);
                    double lat = pointObject.getDouble("lat");
                    double lon = pointObject.getDouble("lon");
                    points.add(new GeoPoint(lat, lon));
                }
                
                // Dodaj do mapy nazwę ulicy jako klucz i listę punktów jako wartość
                streetsMap.put(name, points);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return streetsMap;
    }
}
