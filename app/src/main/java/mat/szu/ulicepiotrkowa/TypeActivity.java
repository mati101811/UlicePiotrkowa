package mat.szu.ulicepiotrkowa;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeActivity extends AppCompatActivity
{
   
    boolean topArrowDown = true;
    boolean bottomArrowDown = true;
    private MapView mapView;
    private Button resetButton;
    private EditText streetInputEditText;
    private Button counter;
    Map<String, Street> fullStreetsMap = new HashMap<>( );
    Map<String, List<Polyline>> streetPolylinesMap = new HashMap<>( );
    //    Map<String, List<GeoPoint>> streetsMap;
    //    Map<String, Polyline> streetPolylines = new HashMap<>( );
    int guessesTake = 0;
    int maxGuessesCount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);
        fullStreetsMap = JSONHelper.jsonToMap(this);
        
        Configuration.getInstance( ).setUserAgentValue(getPackageName( ));
        
        resetButton         = findViewById(R.id.reset);
        streetInputEditText = findViewById(R.id.street);
        counter             = findViewById(R.id.counter);
        resetButton.setOnClickListener(v -> reset( ));
        
        mapView = findViewById(R.id.map_view);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        
        streetInputEditText.addTextChangedListener(new TextWatcher( )
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
        
        mapView.getController( ).setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(51.406696, 19.693181);
        mapView.getController( ).setCenter(startPoint);
        fullStreetsMap.forEach((streetName, street) -> {
            List<Polyline> polylines = new ArrayList<>( );
            street.getPoints( ).forEach(points -> {
                Polyline polyline = new Polyline(mapView);
                polyline.setPoints(points);
                polyline.setColor(Color.GRAY);
                mapView.getOverlays( ).add(polyline);
                polylines.add(polyline);
            });
            streetPolylinesMap.put(streetName, polylines);
        });
        
        maxGuessesCount = fullStreetsMap.size( );
        String counterText = "0/" + maxGuessesCount;
        counter.setText(counterText);
        
        counter.setOnClickListener(v -> summary( ));
        
    }
    
    private void loadDialog( )
    {
        FrameLayout dialog = findViewById(R.id.dialog);
        MaterialCardView    cardView             = findViewById(R.id.dialog_content);
        ImageView           close                = findViewById(R.id.close_button);
        LinearLayout        topSectionHeader           = findViewById(R.id.topSectionHeader);
        LinearLayout        bottomSectionHeader        = findViewById(R.id.bottomSectionHeader);
        ScrollView           topSectionContentScroll    = findViewById(R.id.topSectionContentScroll);
        ScrollView           bottomSectionContentScroll = findViewById(R.id.bottomSectionContentScroll);
        LinearLayout        topSectionContentLayout    = findViewById(R.id.topSectionContentLayout);
        LinearLayout        bottomSectionContentLayout = findViewById(R.id.bottomSectionContentLayout);
        ImageView topArrow = findViewById(R.id.topSectionArrow);
        ImageView bottomArrow = findViewById(R.id.bottomSectionArrow);
        topSectionContentLayout.removeAllViews( );
        bottomSectionContentLayout.removeAllViews( );
        
        streetPolylinesMap.forEach((streetName, polyline) -> {
            if (polyline.get(0).getColor( ) == Color.GREEN)
            {
                TextView textView = new TextView(this);
                textView.setText(fullStreetsMap.get(streetName).getFullName( ));
                topSectionContentLayout.addView(textView);
                textView.setOnClickListener(v -> {
                    mapView.getController( ).setZoom(18.0);
                    mapView.getController( ).animateTo(polyline.get(0).getPoints( ).get(0), 18d, 1000L);
                });
            }
            else
            {
                TextView textView = new TextView(this);
                textView.setText(fullStreetsMap.get(streetName).getFullName( ));
                bottomSectionContentLayout.addView(textView);
                textView.setOnClickListener(v -> {
                    mapView.getController( ).setZoom(18.0);
                    mapView.getController( ).animateTo(polyline.get(0).getPoints( ).get(0), 18d, 1000L);
                });
            }
        });
        
        topSectionHeader.setOnClickListener(v -> {
            //oby dwie do rozwinięcia
            if(topArrowDown && bottomArrowDown)
            {
                topArrowDown = false;
                animateViews(topArrow, null, topSectionContentScroll, null, 242, 0);
            }
            //dolne jest rozwinięte
            else if (!bottomArrowDown)
            {
                topArrowDown = false;
                bottomArrowDown = true;
                animateViews(topArrow, bottomArrow, topSectionContentScroll, bottomSectionContentScroll, 242, 0);
            }
            //gorne jest rozwinięte
            else
            {
                topArrowDown = true;
                animateViews(topArrow, null, topSectionContentScroll, null, 0, 0);
            }
        });
        
        bottomSectionHeader.setOnClickListener(v -> {
            //oby dwie do rozwinięcia
            if(topArrowDown && bottomArrowDown)
            {
                bottomArrowDown = false;
                animateViews(null, bottomArrow, null, bottomSectionContentScroll, 0, 242);
            }
            //górne jest rozwinięte
            else if (!topArrowDown)
            {
                bottomArrowDown = false;
                topArrowDown = true;
                animateViews(topArrow, bottomArrow, topSectionContentScroll, bottomSectionContentScroll, 0, 242);
            }
            //dolne jest rozwinięte
            else
            {
                bottomArrowDown = true;
                animateViews(null, bottomArrow, null, bottomSectionContentScroll, 0, 0);
            }
        });
        
        cardView.setOnClickListener(v -> {
        });
       
        dialog.setOnClickListener(v -> {
            dialog.setVisibility(View.GONE);
        });
        
        close.setOnClickListener(v -> {
            dialog.setVisibility(View.GONE);
        });
        
        dialog.setVisibility(View.VISIBLE);
    }
    
    public void animateViews(ImageView image1, ImageView image2, ScrollView layout1, ScrollView layout2, float targetHeightDp1, float targetHeightDp2) {
        // Przekształcenie DP na piksele
        float density = getResources().getDisplayMetrics().density;
        int targetHeightPx1 = (int) (targetHeightDp1 * density);
        int targetHeightPx2 = (int) (targetHeightDp2 * density);
        
        List<Animator> animators = new ArrayList<>();
        
        // Sprawdzenie, czy image1 nie jest null i animowanie obrotu
        if (image1 != null) {
            ObjectAnimator rotateImage1 = ObjectAnimator.ofFloat(image1, "rotation", image1.getRotation(), image1.getRotation()==90f ? -90f : 90f);
            rotateImage1.setDuration(200);
            animators.add(rotateImage1);  // Dodanie animacji obrotu do listy
        }
        
        // Sprawdzenie, czy image2 nie jest null i animowanie obrotu
        if (image2 != null) {
            ObjectAnimator rotateImage2 = ObjectAnimator.ofFloat(image2, "rotation", image2.getRotation(), image2.getRotation()==90f ? -90f : 90f);
            rotateImage2.setDuration(200);
            animators.add(rotateImage2);  // Dodanie animacji obrotu do listy
        }
        
        // Sprawdzenie, czy layout1 nie jest null i animowanie zmiany wysokości
        if (layout1 != null) {
            ValueAnimator heightAnimator1 = ValueAnimator.ofInt(layout1.getHeight(), targetHeightPx1);
            heightAnimator1.setDuration(200);
            heightAnimator1.addUpdateListener(animation -> {
                layout1.getLayoutParams().height = (int) animation.getAnimatedValue();
                layout1.requestLayout();
            });
            Log.d("TAG", "animateViews: dodawanie top section do animacji z "+layout1.getHeight()+ " do "+targetHeightDp1);
            animators.add(heightAnimator1);  // Dodanie animacji wysokości do listy
        }
        
        // Sprawdzenie, czy layout2 nie jest null i animowanie zmiany wysokości
        if (layout2 != null) {
            ValueAnimator heightAnimator2 = ValueAnimator.ofInt(layout2.getHeight(), targetHeightPx2);
            heightAnimator2.setDuration(200);
            heightAnimator2.addUpdateListener(animation -> {
                layout2.getLayoutParams().height = (int) animation.getAnimatedValue();
                layout2.requestLayout();
            });
            Log.d("TAG", "animateViews: dodawanie bottom section do animacji z "+layout2.getHeight()+ " do "+targetHeightDp2);
            animators.add(heightAnimator2);  // Dodanie animacji wysokości do listy
        }
        
        // Połącz wszystkie animacje w AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);  // Wszystkie animacje będą odtwarzane jednocześnie
        animatorSet.setInterpolator(new DecelerateInterpolator());  // Łagodniejsze przejścia
        animatorSet.start();  // Uruchom animacje
    }
    
    
    
    private void summary( )
    {
        //zoom map
        loadDialog( );
    }
    
    private void check( )
    {
        String streetName =
                streetInputEditText.getText( ).toString( ).replaceAll("[żź]", "z").replaceAll("[ć]", "c").replaceAll("[ę]", "e").replaceAll("[ó]",
                                                                                                                                            "o").replaceAll(
                        "[ł]", "l").replaceAll("[ń]", "n").replaceAll("[ś]", "s").replaceAll("[ą]", "a").replaceAll("[ŻŹ]", "Z").replaceAll("[Ć]",
                                                                                                                                            "C").replaceAll(
                        "[Ę]", "E").replaceAll("[Ó]", "O").replaceAll("[Ł]", "L").replaceAll("[Ń]", "N").replaceAll("[Ś]", "S").replaceAll("[Ą]",
                                                                                                                                           "A").toLowerCase( ).replace(
                        "\\s", "");
        Log.d("TAG", "check: "+streetName);
        if (!streetName.isEmpty( ))
        {
            if (streetPolylinesMap.containsKey(streetName))
            {
                List<Polyline> polylines = streetPolylinesMap.get(streetName);
                polylines.forEach(polyline -> polyline.setColor(Color.GREEN));
                guessesTake++;
                String counterText = guessesTake + "/" + maxGuessesCount;
                counter.setText(counterText);
                streetInputEditText.setText("");
                mapView.invalidate( );
            }
        }
    }
    
    private void reset( )
    {
        streetPolylinesMap.forEach((streetName, polyline) -> polyline.forEach(polyline1 -> polyline1.setColor(Color.GRAY)));
        mapView.invalidate( );
        String counterText = "0/" + maxGuessesCount;
        counter.setText(counterText);
        streetInputEditText.setText("");
    }
}