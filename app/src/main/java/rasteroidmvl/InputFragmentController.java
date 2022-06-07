package rasteroidmvl;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import communications.CommunicationController;


public class InputFragmentController extends Fragment {

    private EditText ipEditText;
    private Button connectButton;
    private ControllerActivity controllerActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_input, container, false);

        this.ipEditText=(EditText)view.findViewById(R.id.editTextIP);
        this.connectButton=(Button)view.findViewById(R.id.connectButton);
        this.controllerActivity =(ControllerActivity)getActivity();

        new Thread(new Runnable() {
            @Override
            public void run() {
                controllerActivity.setController(new CommunicationController(controllerActivity.getApplicationContext()));
            }
        }).start();

        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerActivity.setIp(ipEditText.getText().toString());
                controllerActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, ControllerFragment.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }
}