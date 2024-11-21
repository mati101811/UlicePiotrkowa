package mat.szu.ulicepiotrkowa;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    
    private boolean clickingGameMode = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        MaterialButton startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, clickingGameMode ? MapGuessActivity.class : TypeActivity.class);
            startActivity(intent);
        });
        
        TextView firstText = findViewById(R.id.firstText);
        TextView secondText = findViewById(R.id.secondText);
        MaterialButton   leftButton = findViewById(R.id.leftButton);
        MaterialButton rightButton = findViewById(R.id.rightButton);
        
        leftButton.setOnClickListener(v -> switchMode(firstText, secondText, leftButton, rightButton, true));
        rightButton.setOnClickListener(v -> switchMode(firstText, secondText, leftButton, rightButton, false));
        
        updateButtons(leftButton, rightButton);
    }
    
    private void switchMode(TextView firstText, TextView secondText, MaterialButton leftButton, MaterialButton rightButton, boolean toMode1) {
        int direction = toMode1 ? -1 : 1;
        
        TextView visibleText = clickingGameMode ? firstText : secondText;
        TextView hiddenText = clickingGameMode ? secondText : firstText;
        
        hiddenText.setVisibility(View.VISIBLE);
        
        ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(visibleText, "translationX", 0, direction * -1000);
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(hiddenText, "translationX", direction * 1000, 0);
        
        hideAnimator.setDuration(300);
        showAnimator.setDuration(300);
        
        hideAnimator.start();
        showAnimator.start();
        
        hideAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                visibleText.setVisibility(View.GONE);
                clickingGameMode = toMode1;
                updateButtons(leftButton, rightButton);
            }
        });
    }
    
    private void updateButtons(MaterialButton leftButton, MaterialButton rightButton) {
        leftButton.setEnabled(!clickingGameMode);
        rightButton.setEnabled(clickingGameMode);
    }
}