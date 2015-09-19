package com.radiancetops.anonymeyes;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String WAITING = "Waiting...";
    private static final String ARG_PARAM2 = "param2";
    private Camera camera;
    private CameraPreview cameraPreview;
    private MarkerView markerView;
    private TextView resistanceTextView;
    private TextView instructionsTextView;
    private LineView lineView;
    private float stripheight;
    private Button singleButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    private void setCamParameters(Camera camera) {
        camera.setDisplayOrientation(90);
        Camera.Parameters p = camera.getParameters();
        p.setFocusMode(p.FOCUS_MODE_CONTINUOUS_PICTURE);
        int w = p.getPreviewSize().width;
        int h = p.getPreviewSize().height;
        Log.d("CameraFragment", "w: " + w + " h: " + h);

        /* set focus areas */
        if(p.getMaxNumFocusAreas() > 0) {
            Log.d("CameraFragment", "max focus areas: " + p.getMaxNumFocusAreas());
            List<Camera.Area> areas = new ArrayList<>();
            Rect r = new Rect(
                    (int) (w / 2 - stripheight / 2), (int) (h / 3), (int) (w / 2 + stripheight / 2),
                    (int) (2 * h / 3));
            Log.d("CameraFragment", "rect: " + r.flattenToString());
            areas.add(new Camera.Area(r, 1));
            p.setFocusAreas(areas);
        }
        camera.setParameters(p);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        TextView instructionsTextView = (TextView)view.findViewById(R.id.instructionsTextView);
        /*
        RelativeLayout.LayoutParams lineViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int)stripheight);
        lineViewParams.
        lineView.setLayoutParams(lineViewParams);
*/
       singleButton = (Button)view.findViewById(R.id.singleButton);
        singleButton.setVisibility(View.GONE);
        singleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cameraPreview.writeCSV();
                Log.v("data", "printing");
            }
        });
        resistanceTextView = (TextView)view.findViewById(R.id.resistanceTextView);
        resistanceTextView.setTypeface(MainActivity.typefaceHeader);
        resistanceTextView.setText("\n" + WAITING + "\n");

        instructionsTextView = (TextView)view.findViewById(R.id.instructionsTextView);
        instructionsTextView.setTypeface(MainActivity.typefaceHeader);

        markerView = (MarkerView)view.findViewById(R.id.markerView);

        cameraPreview = new CameraPreview(getActivity());

        FrameLayout frameLayout = (FrameLayout)view.findViewById(R.id.camera_preview);
        FrameLayout.LayoutParams surfaceViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        cameraPreview.setLayoutParams(surfaceViewParams);
        frameLayout.addView(cameraPreview);
        //Log.v("line dims", "line view height:" + lineView.getHeight());
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().finish();
        System.exit(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = getCameraInstance();

        Resources r = getResources();
        stripheight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());

        Log.v("CameraFragment", "stripheight: " + stripheight);
        setCamParameters(camera);
        cameraPreview.initVals(camera, (int) stripheight, resistanceTextView, markerView);
    }

    @Override
    public void onDestroy() {
        releaseCamera();
        super.onDestroy();
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
