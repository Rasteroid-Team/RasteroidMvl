package rasteroidmvl;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import api.ApiService;
import communications.CommunicationController;


public class InputFragmentController extends Fragment {

    private EditText ipEditText;
    private Button startButton;
    private ControllerActivity controllerActivity;
    private TextView screenNumber;

    //ship selection
    private ImageView[] shipSelections;
    private String selectedShipId = ApiService.PLAYER_ID.HR75.getId();
    private final int selectedColor = Color.parseColor("#27c2b6");
    private final int unselectedColor = Color.parseColor("#00000000");


    public EditText getIpEditText() {
        return ipEditText;
    }

    public String getSelectedShipId() {
        return selectedShipId;
    }

    public TextView getScreenNumber() {
        return screenNumber;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_input, container, false);

        this.ipEditText=(EditText)view.findViewById(R.id.editTextName);
        this.startButton=(Button)view.findViewById(R.id.startButton);
        this.controllerActivity =(ControllerActivity)getActivity();
        this.controllerActivity.setActiveFragment(ControllerActivity.ActiveFragment.INPUT);
        this.screenNumber = (TextView)view.findViewById(R.id.textScreenNumber);
        if (this.controllerActivity.getScreenNumber() != 0) {
            this.screenNumber.setText("PC "+this.controllerActivity.getScreenNumber());
        }

        this.controllerActivity.getController().addAllListeners(this.controllerActivity);

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

        //set ship selection handlers
        shipSelections = new ImageView[2];
        //models
        {
            shipSelections[0] = view.findViewById(R.id.hr75);
            shipSelections[1] = view.findViewById(R.id.phoenix);
        }
        //handler
        for (ImageView ship:shipSelections) {
            ship.setOnClickListener(shipSelectorListener());
        }

        controllerActivity.setName(ipEditText.getText().toString());
        controllerActivity.setModelId(selectedShipId);

        this.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerActivity.setName(ipEditText.getText().toString());
                controllerActivity.setModelId(selectedShipId);
                controllerActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, ControllerFragment.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private View.OnClickListener shipSelectorListener()
    {
        return view -> {
            //interface
            for (ImageView shipSelection:shipSelections) {
                if (shipSelection == view){
                    shipSelection.setBackgroundColor(selectedColor);
                } else {
                    shipSelection.setBackgroundColor(unselectedColor);
                }
            }
            //logic
            if (view == shipSelections[0]){
                selectedShipId = ApiService.PLAYER_ID.HR75.getId();
            } else if (view == shipSelections[1]){
                selectedShipId = ApiService.PLAYER_ID.PHOENIX.getId();
            }
        };
    }

    @Override
    public void onPause() {
        controllerActivity.setName(ipEditText.getText().toString());
        controllerActivity.setModelId(selectedShipId);
        super.onPause();
    }

    @Override
    public void onStop() {
        controllerActivity.setName(ipEditText.getText().toString());
        controllerActivity.setModelId(selectedShipId);
        super.onStop();
    }
}