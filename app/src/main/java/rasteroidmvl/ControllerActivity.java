package rasteroidmvl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import communications.CommunicationController;


public class ControllerActivity extends AppCompatActivity {

    private CommunicationController controller;
    private String ip;
    private String Name;
    private String modelId;
    private FragmentManager fragmentManager;
    private final String DEBUG_TAG="CONTROLLER ACTIVITY MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_main);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            Log.d(DEBUG_TAG, "savedinstancestate es null");
            fragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView,
                            InputFragmentController.class
                            , null)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //hide ui to improve immersion
        UiManager.setUiVisibility(this, false);
    }

    public CommunicationController getController() {
        return controller;
    }

    public void setController(CommunicationController controller) {
        this.controller = controller;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setModelId(String selectedShipId) {
        modelId = selectedShipId;
    }

    public String getModelId(){
        return modelId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
