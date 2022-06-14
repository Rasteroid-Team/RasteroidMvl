package rasteroidmvl;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public class UiManager {

        public static void setUiVisibility(Activity activity, boolean visible){
                if (visible){
                        show_ui(activity);
                } else {
                       hide_ui(activity);
                }
        }

        private static void hide_ui(Activity activity){
                View decorView = activity.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
        }

        private static void show_ui(Activity activity){
                View decorView = activity.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);
        }

}
