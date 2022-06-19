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

import androidx.fragment.app.Fragment;

import api.ApiService;
import communications.CommunicationController;


public class InputFragmentController extends Fragment {

    private EditText ipEditText;
    private Button connectButton;
    private ControllerActivity controllerActivity;

    //ship selection
    private ImageView[] shipSelections;
    private String selectedShipId = ApiService.PLAYER_ID.HR75.getId();
    private final int selectedColor = Color.parseColor("#27c2b6");
    private final int unselectedColor = Color.parseColor("#00000000");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_input, container, false);

        this.ipEditText=(EditText)view.findViewById(R.id.editTextName);
        this.connectButton=(Button)view.findViewById(R.id.connectButton);
        this.controllerActivity =(ControllerActivity)getActivity();

        new Thread(new Runnable() {
            @Override
            public void run() {
                controllerActivity.setController(new CommunicationController(controllerActivity.getApplicationContext()));
            }
        }).start();

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

        this.connectButton.setOnClickListener(new View.OnClickListener() {
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

}