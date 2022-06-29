package rasteroidmvl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

import communications.CommunicationController;
import communications.ConnectionInterface;
import communications.ProtocolDataPacket;


public class ControllerActivity extends AppCompatActivity implements ConnectionInterface {

    private CommunicationController controller;
    private String ip = "192.168.0.18";
    private String Name;
    private String modelId;
    private FragmentManager fragmentManager;
    private final String DEBUG_TAG="CONTROLLER ACTIVITY MESSAGE";
    private String mac;
    private ActiveFragment activeFragment;
    private int screenNumber = 0;

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
                            ConnectionFragment.class
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

    public int getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(int screenNumber) {
        this.screenNumber = screenNumber;
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

    public ActiveFragment getActiveFragment() {
        return activeFragment;
    }

    public void setActiveFragment(ActiveFragment activeFragment) {
        this.activeFragment = activeFragment;
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

    public String getMac() {
        return mac;
    }

    public synchronized void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public void onMessageReceived(ProtocolDataPacket packet) {
        switch (packet.getId()){
            case 180:
                this.mac = (String)packet.getObject();
                System.out.println("Mac recibida!");
                break;
            case 155:
                String[] macRecivedAndPosition = (String[])packet.getObject();
                System.out.println("mac recieved " + macRecivedAndPosition[0]);
                if (!macRecivedAndPosition[0].equals("this")){
                    this.mac = macRecivedAndPosition[0];
                }
                this.screenNumber = Integer.parseInt(macRecivedAndPosition[1]);
                List<Fragment> fragments = fragmentManager.getFragments();
                Fragment currentFragment = fragments.get(fragments.size() - 1);
                if (currentFragment instanceof InputFragmentController){
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((InputFragmentController)currentFragment).getScreenNumber().setText("PC "+screenNumber);
                        }
                    });
                }
                System.out.println("Mac = " + mac);
                break;
            case 550:
                if (this.activeFragment != ActiveFragment.CONTROLLER){

                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, ControllerFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            case 620:
                controller.sendBroadcastMessage(621,
                        (Name==null || Name.isEmpty()) ? "No name" : Name );
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(ControllerActivity.this, "CONRGRATULATIONS FOR THE WIN", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                break;
            case 621:
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(ControllerActivity.this, "GAME OVER LOOSER", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                break;
            case 630:
                if (this.activeFragment != ActiveFragment.INPUT){

                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, InputFragmentController.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack(null)
                            .commit();
                }
                break;
        }
    }

    @Override
    public void onConnectionAccept(String mac) {
        if (this.mac == null) {
            this.setMac(mac);
            System.out.println("accepted" + this.mac);
        }
    }

    @Override
    public void onConnectionClosed(String mac) {
        this.finishAndRemoveTask();
    }

    public enum ActiveFragment {
        CONNECTION,
        INPUT,
        CONTROLLER
    }
}
