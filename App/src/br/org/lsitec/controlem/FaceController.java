package br.org.lsitec.controlem;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class FaceController extends Activity {

	private static final String TAG = FaceController.class.toString();
	private Camera frontCamera;
	private FacePreview mPreview;
	private boolean faceDetectionRunning;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		boolean hasCamera = checkCameraHardware(getApplicationContext());
		if (hasCamera) {
			System.out.println("camera frontal encontrada!");
			
			frontCamera = getCameraInstance();
			
			// Create our Preview view and set it as the content of our activity.
	        mPreview = new FacePreview(getApplicationContext(), frontCamera);
	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	        preview.addView(mPreview);
	        
	        doFaceDetection(frontCamera.getParameters());
		}
		else {
			System.out.println("não foi possível encontrar câmera frontal");
		}
		
	}
	
	private int doFaceDetection(Parameters params) {
		Log.d(TAG, "iniciando detector de faces...");
		if (faceDetectionRunning) {
	        return 0;
	    }
		
	    // check if face detection is supported or not
	    // using Camera.Parameters
	    if (params.getMaxNumDetectedFaces() <= 0) {
	        Log.e(TAG, "Face Detection not supported");
	        TextView text = (TextView) findViewById(R.id.info_text);
	        text.setText("Este dispositivo não detecta faces.");
	        return -1;
	    }

	    MyFaceDetectionListener fDListener = new MyFaceDetectionListener();
	    frontCamera.setFaceDetectionListener(fDListener);
	    frontCamera.startFaceDetection();
	    faceDetectionRunning = true;
	    return 1;
	}
	
	public int stopFaceDetection() {
	    if (faceDetectionRunning) {
	        frontCamera.stopFaceDetection();
	        faceDetectionRunning = false;
	        return 1;
	    }
	    return 0;
	}

	/** Check if this device has a FRONT camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	    	int id = Camera.getNumberOfCameras() - 1;
	    	//Camera.getCameraInfo(id, CameraInfo());
	    	
	    	// busca câmera frontal...
	        c = Camera.open(id); // attempt to get a Camera instance
	    }
	    catch (Exception e) {
	        // Camera is not available (in use or does not exist)
	    	System.out.println("erro ao tentar usar camera");
	    }
	    return c; // returns null if camera is unavailable
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Applications should release the camera immediately in onPause()
		if (frontCamera != null) {
			frontCamera.release();
			frontCamera = null;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//  Applications should re-open() the camera in onResume(). 
		frontCamera = getCameraInstance();
	}
	
	class MyFaceDetectionListener implements Camera.FaceDetectionListener {

		@Override
	    public void onFaceDetection(Face[] faces, Camera camera) {
			// guarda  informações das faces encontradas
			mPreview.setFaces(faces);
			
	        if (faces.length > 0){
	            Log.d(TAG, "face detected: "+ faces.length +
	                    " Face 1 Location X: " + faces[0].rect.centerX() +
	                    " Y: " + faces[0].rect.centerY() );
	            Log.d(TAG, "detalhes:");
	            for (Face face : faces) {
	            	Log.d(TAG, "score: " + face.score);
					Log.d(TAG, "retangulo: " + face.rect.flattenToString());
				}
	        }
	        
	        mPreview.invalidate();
	    }
	}
}
