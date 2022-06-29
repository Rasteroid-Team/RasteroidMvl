package rasteroidmvl;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "LOTTIE MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        LottieAnimationView lottieAnimationView =
                (LottieAnimationView) findViewById(R.id.animationView);
        lottieAnimationView.setRepeatCount(0);
        lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(new Intent(getApplicationContext(),
                        ControllerActivity.class));
                Log.d(DEBUG_TAG, "ANIMACIÓN FINALIZADA.");
                finish();
            }
        });
        lottieAnimationView.addAnimatorUpdateListener((animation) -> {
            if (lottieAnimationView.getProgress() == 0.5f) {
                Log.d(DEBUG_TAG,"animacion alcanza 50%");
                Toast.makeText(this, "Alcanzadp 50 %", Toast.LENGTH_SHORT).show();
            }
        });
        lottieAnimationView.playAnimation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UiManager.setUiVisibility(this, false);
    }
}