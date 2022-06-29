package rasteroidmvl;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import communications.CommunicationController;


public class ConnectionFragment extends Fragment {

    private Button connectButton;
    private ControllerActivity controllerActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connection, container, false);

        this.connectButton=(Button)view.findViewById(R.id.connectButton);
        this.controllerActivity =(ControllerActivity)getActivity();
        this.controllerActivity.setActiveFragment(ControllerActivity.ActiveFragment.CONNECTION);

        new Thread(new Runnable() {
            @Override
            public void run() {
                controllerActivity.setController(new CommunicationController(controllerActivity.getApplicationContext()));
            }
        }).start();

        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, InputFragmentController.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}