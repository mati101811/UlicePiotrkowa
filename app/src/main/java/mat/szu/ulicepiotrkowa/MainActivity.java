package mat.szu.ulicepiotrkowa;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    
    private MapView map;
    private Button reset;
    private EditText street;
    private Button counter;
    Map<String, List<GeoPoint>> streetsMap;
    Map<String, Polyline> streetPolylines = new HashMap<>( );
    int guesses = 0;
    int maxGuesses;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        streetsMap = Street.loadStreetsMapFromJSON(this);
        
        Configuration.getInstance( ).setUserAgentValue(getPackageName( ));
        
        reset   = findViewById(R.id.reset);
        street  = findViewById(R.id.street);
        counter = findViewById(R.id.counter);
        reset.setOnClickListener(v -> reset( ));
        
        map = findViewById(R.id.map_view);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        
        street.addTextChangedListener(new TextWatcher( )
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // Ten kod zostanie wywołany przed zmianą tekstu
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Ten kod zostanie wywołany w momencie, gdy tekst się zmienia
                String newText = s.toString( ); // Odczytanie nowego tekstu
                // Możesz tutaj wykonać operacje na nowym tekście
            }
            
            @Override
            public void afterTextChanged(Editable s)
            {
                check( );
                // Ten kod zostanie wywołany po zmianie tekstu
            }
        });
        
        map.getController( ).setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(51.406696, 19.693181);
        map.getController( ).setCenter(startPoint);
        
        streetsMap.forEach((streetName, points) -> {
            Polyline polyline = new Polyline(map);
            polyline.setPoints(points);
            polyline.setColor(Color.GRAY);
            streetPolylines.put(streetName, polyline);
            map.getOverlays( ).add(polyline);
        });
        
        
        maxGuesses = (int) streetsMap.keySet( ).stream( ).filter(key -> !key.endsWith("'")).count( );
        counter.setText("0/" + maxGuesses);
        counter.setOnClickListener(v -> summary( ));
        
    }
    
    private void summary( )
    {
        LayoutInflater inflater = getLayoutInflater( );
        View           layout   = inflater.inflate(R.layout.summary, null);
        
        Button       ok              = layout.findViewById(R.id.ok);
        LinearLayout summary_content = layout.findViewById(R.id.summary_content);
        streetPolylines.forEach((streetName, polyline) -> {
            if (polyline.getColor( ) == Color.GRAY && !streetName.endsWith("'"))
            {
                TextView textView = new TextView(this);
                textView.setText(streetName);
                textView.setTextColor(Color.RED);
                summary_content.addView(textView);
            }
        });
        streetPolylines.forEach((streetName, polyline) -> {
            if (polyline.getColor( ) == Color.GREEN && !streetName.endsWith("'"))
            {
                TextView textView = new TextView(this);
                textView.setText(streetName);
                textView.setTextColor(Color.GREEN);
                summary_content.addView(textView);
            }
        });
        //alert dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(layout);
        AlertDialog builder = dialogBuilder.create( );
        builder.show( );
        
        ok.setOnClickListener(v -> builder.dismiss( ));
    }
    
    private void check( )
    {
        String streetName = street.getText( ).toString( ).replaceAll("[żź]", "z")
                .replaceAll("[ć]", "c")
                .replaceAll("[ę]", "e")
                .replaceAll("[ó]", "o")
                .replaceAll("[ł]", "l")
                .replaceAll("[ń]", "n")
                .replaceAll("[ś]", "s")
                .replaceAll("[ą]", "a")
                .replaceAll("[ŻŹ]", "Z")
                .replaceAll("[Ć]", "C")
                .replaceAll("[Ę]", "E")
                .replaceAll("[Ó]", "O")
                .replaceAll("[Ł]", "L")
                .replaceAll("[Ń]", "N")
                .replaceAll("[Ś]", "S")
                .replaceAll("[Ą]", "A").toLowerCase().replace("\\s", "");
        
        if (!streetName.isEmpty( ))
        {
            if (streetPolylines.containsKey(streetName))
            {
                streetPolylines.get(streetName).setColor(Color.GREEN);
                guesses++;
                String counterText = guesses + "/" + maxGuesses;
                counter.setText(counterText);
                street.setText("");
                map.invalidate( );
            }
            if (streetPolylines.containsKey(streetName + "'"))
            {
                streetPolylines.get(streetName + "'").setColor(Color.GREEN);
            }
        }
        
    }
    
    private void reset( )
    {
        streetPolylines.forEach((streetName, polyline) -> polyline.setColor(Color.GRAY));
        map.invalidate();
        String counterText = "0/" + maxGuesses;
        counter.setText(counterText);
        street.setText("");
    }
}