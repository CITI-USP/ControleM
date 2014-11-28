package br.org.lsitec.controlem;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.widget.FrameLayout;

public class FaceController extends Activity {

	private Camera frontCamera;
	private FacePreview mPreview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);
		
		boolean hasCamera = checkCameraHardware(getApplicationContext());
		if (hasCamera) {
			System.out.println("camera frontal encontrada!");
			
			frontCamera = getCameraInstance();
			
			// Create our Preview view and set it as the content of our activity.
	        mPreview = new FacePreview(getApplicationContext(), frontCamera);
	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	        preview.addView(mPreview);
		}
		else {
			System.out.println("não foi possível encontrar câmera frontal");
		}
		
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
		frontCamera.release();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//  Applications should re-open() the camera in onResume(). 
		frontCamera = getCameraInstance();
	}
}
