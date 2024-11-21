package mat.szu.ulicepiotrkowa;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.events.MapEventsReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapGuessActivity extends AppCompatActivity {
    
    private Map<String, Street> streetsMap;
    private MapView mapView;
    private MaterialTextView textStreetName, textResult;
    private Street currentStreet;
    private Random random = new Random();
    private Handler handler = new Handler();
    private IMapController mapController;
    private List<Polyline> currentPolylines = new ArrayList<>();
    LinearProgressIndicator progressIndicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // OSMDroid initialization
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_guess);
        
        // Load UI components
        mapView = findViewById(R.id.mapView);
        textStreetName = findViewById(R.id.textStreetName);
        textResult = findViewById(R.id.textResult);
        progressIndicator = findViewById(R.id.progressIndicator);
        
        // Initialize map
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapController = mapView.getController();
        mapController.setZoom(15.0);
        mapController.setCenter(new GeoPoint(51.406696, 19.693181)); // Center on Warsaw
        
        // Load streets data
        streetsMap = JSONHelper.jsonToMap(this);
        if (streetsMap.isEmpty()) {
            Toast.makeText(this, "Brak danych o ulicach!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (currentStreet == null || currentStreet.getPoints().isEmpty()) {
                    return false;
                }
                
                GeoPoint nearestPoint = findClosestPoint(p);
                int minDistance = (int) p.distanceToAsDouble(nearestPoint);
                
                // Wyświetlenie odległości i współrzędnych najbliższego punktu (opcjonalnie)
                if (nearestPoint != null) {
                    drawStreetOnMap();
                    animateToClosestPoint(nearestPoint);
                    textResult.setText(minDistance + " m");
                    textResult.setVisibility(View.VISIBLE);
                    animateProgressIndicator();
                }
                return true; // Zatrzymanie dalszego przetwarzania kliknięcia
            }
            
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false; // Nie obsługujemy długiego kliknięcia.
            }
        };
        
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(mapEventsOverlay);
        
        pickRandomStreet();
    }
    
    private GeoPoint findClosestPoint(GeoPoint p) {
        GeoPoint closestPointOnSegment = null;
        double minDistance = Double.MAX_VALUE;
        
        // Iterowanie po segmentach ulicy
        for (List<GeoPoint> segment : currentStreet.getPoints()) {
            if (segment.size() < 2) {
                continue; // Segment musi mieć przynajmniej dwa punkty
            }
            
            // Szukanie najbliższego punktu na segmentach
            for (int i = 0; i < segment.size() - 1; i++) {
                GeoPoint pointA = segment.get(i);
                GeoPoint pointB = segment.get(i + 1);
                
                // Obliczenie rzutu punktu p na linię AB
                GeoPoint closestPoint = getPointOnLineSegment(pointA, pointB, p);
                double distance = closestPoint.distanceToAsDouble(p);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPointOnSegment = closestPoint;
                }
            }
        }
        
        return closestPointOnSegment;
    }
    
    private GeoPoint getPointOnLineSegment(GeoPoint pointA, GeoPoint pointB, GeoPoint p) {
        // Konwersja współrzędnych na przestrzeń 2D
        double ax = pointA.getLongitude();
        double ay = pointA.getLatitude();
        double bx = pointB.getLongitude();
        double by = pointB.getLatitude();
        double px = p.getLongitude();
        double py = p.getLatitude();
        
        // Wektor AB i AP
        double abx = bx - ax;
        double aby = by - ay;
        double apx = px - ax;
        double apy = py - ay;
        
        // Projekcja wektora AP na AB, ograniczona do odcinka [0, 1]
        double abSquared = abx * abx + aby * aby; // Długość AB do kwadratu
        double t = (apx * abx + apy * aby) / abSquared;
        t = Math.max(0, Math.min(1, t)); // Ograniczenie do zakresu [0, 1]
        
        // Obliczenie rzutu punktu na linię
        double closestX = ax + t * abx;
        double closestY = ay + t * aby;
        
        return new GeoPoint(closestY, closestX); // Tworzenie punktu GeoPoint
    }
    
    
    
    private void animateProgressIndicator() {
        ValueAnimator animator = ValueAnimator.ofInt(100, 0);
        animator.setDuration(3000); // 3 sekundy
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressIndicator.setProgress(progress);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                progressIndicator.setProgressCompat(100,false);
                pickRandomStreet(); // Wywołanie nowej rundy
            }
        });
        animator.start();
    }
    
    private void pickRandomStreet() {
        textResult.setVisibility(View.GONE);
        clearPolylines();
        // Losowanie ulicy
        Random random = new Random();
        List<String> keys = new ArrayList<>(streetsMap.keySet());
        String randomKey = keys.get(random.nextInt(keys.size()));
        currentStreet = streetsMap.get(randomKey);
        
        // Wyświetlenie nazwy ulicy
        textStreetName.setText(currentStreet.getFullName());
        
        // Wyczyszczenie mapy z wcześniejszych polilinii
    }
    
    private void drawStreetOnMap() {
        if (currentStreet == null) return;
        
        // Rysowanie polilinii dla aktualnej ulicy
        for (List<GeoPoint> segment : currentStreet.getPoints()) {
            Polyline polyline = new Polyline();
            polyline.setPoints(segment);
            polyline.setColor(Color.BLUE);
            mapView.getOverlayManager().add(polyline);
            currentPolylines.add(polyline);
        }
        
        mapView.invalidate();
    }
    
    private void clearPolylines()
    {
        // Usuwanie wcześniejszych polilinii
        for (Polyline polyline : currentPolylines)
        {
            mapView.getOverlayManager( ).remove(polyline);
        }
        currentPolylines.clear( );
        mapView.invalidate( );
    }
    
    private void animateToClosestPoint(GeoPoint closestPoint) {
        mapController.animateTo(closestPoint, 16.0, 1000L); // Zoom to level 18 over 1 second
    }
    
    
}
