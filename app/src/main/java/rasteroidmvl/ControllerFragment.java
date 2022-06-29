package rasteroidmvl;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import communications.ConnectionInterface;
import communications.ProtocolDataPacket;


public class ControllerFragment extends Fragment {

    private Joystick joystick;
    private ImageView fire;
    private ControllerActivity controllerActivity;
    private int lastAngle;
    private int lastStrength;
    private boolean connected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        this.controllerActivity = ((ControllerActivity)this.getActivity());
        this.controllerActivity.setActiveFragment(ControllerActivity.ActiveFragment.CONTROLLER);

        new Thread(() -> {
            while (this.controllerActivity.getMac() == null){
                try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
                System.out.println("Esperando a recibir mac");
            }
            System.out.println(this.controllerActivity.getMac());
            ProtocolDataPacket modelo = controllerActivity.getController().createPacket(
                    this.controllerActivity.getMac(), 156, controllerActivity.getModelId());
            controllerActivity.getController().sendMessage(modelo);
            System.out.println("Modelo enviado");
        }).start();

        joystick = view.findViewById(R.id.joystickView);
        fire = view.findViewById(R.id.fire);
        this.connected = false;
        joystick.setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if (controllerActivity.getMac() != null) {
                    if (lastAngle!=angle || lastStrength!=strength) {
                        ProtocolDataPacket datos = controllerActivity.getController().createPacket(
                                controllerActivity.getMac(), 152, new int[] {strength, angle});

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                controllerActivity.getController().sendMessage(datos);
                            }
                        }).start();

                        lastStrength=strength;
                        lastAngle=angle;
                    }
                }
            }
        });

        fire.setOnTouchListener((view1, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                fire.setBackgroundResource(R.drawable.disparo_selected);

                ProtocolDataPacket datos = controllerActivity.getController().createPacket(
                        this.controllerActivity.getMac(), 151, null);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        controllerActivity.getController().sendMessage(datos);
                    }
                }).start();

            } else {
                fire.setBackgroundResource(R.drawable.disparo);

            }
            return true;
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //hide ui to improve immersion
        UiManager.setUiVisibility(getActivity(), false);
    }
}