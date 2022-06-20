package rasteroidmvl;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import communications.ConnectionInterface;
import communications.ProtocolDataPacket;


public class ControllerFragment extends Fragment implements ConnectionInterface {

    private Joystick joystick;
    private ImageView fire;
    private ControllerActivity controllerActivity;
    private String mac;
    private int lastAngle;
    private int lastStrength;
    private boolean connected;
    MediaPlayer disparo;
    MediaPlayer acelerar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disparo = MediaPlayer.create(getActivity(), R.raw.disparo);
        acelerar = MediaPlayer.create(getActivity(), R.raw.acelera);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        this.controllerActivity = ((ControllerActivity)this.getActivity());
        this.controllerActivity.getController().addAllListeners(this);

        if (controllerActivity.getIp()!=null && !controllerActivity.getIp().isEmpty()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    controllerActivity.getController().connectToIp(controllerActivity.getIp());
                    //send selected ship
                    //ProtocolDataPacket selectedShip = controllerActivity.getController().createPacket(mac, 155, "pl:id:phoenix");
                }
            }).start();
        }

        joystick = view.findViewById(R.id.joystickView);
        fire = view.findViewById(R.id.fire);
        this.connected = false;
        joystick.setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                acelerar.start();
                if (mac != null) {
                    if (lastAngle!=angle || lastStrength!=strength) {
                        ProtocolDataPacket datos = controllerActivity.getController().createPacket(mac, 152, new int[] {strength, angle});

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
            disparo.start();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                fire.setBackgroundResource(R.drawable.disparo_selected);

                ProtocolDataPacket datos = controllerActivity.getController().createPacket(mac, 151, null);

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

    @Override
    public void onMessageReceived(ProtocolDataPacket packet) {
        switch (packet.getId()){
            case 180:
                this.mac = (String)packet.getObject();
                System.out.println("Mac recibida!");
                break;
            case 155:
                System.out.println("Mac = "+mac);
                new Thread(() -> {
                    while (mac == null){
                        try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
                        System.out.println("Esperando a recibir mac");
                    }
                    ProtocolDataPacket modelo = controllerActivity.getController().createPacket(mac, 156, controllerActivity.getModelId());
                    controllerActivity.getController().sendMessage(modelo);
                    System.out.println("Modelo enviado");
                }).start();
        }
    }

    @Override
    public void onConnectionAccept(String mac) {
        this.mac=mac;
    }

    @Override
    public void onConnectionClosed(String mac) {

    }
}