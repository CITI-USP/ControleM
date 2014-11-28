package br.org.lsitec.controlem;

import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** A basic Camera preview class */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class FacePreview extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = FacePreview.class.toString();
	private SurfaceHolder mHolder;
    private Camera mCamera;

    public FacePreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	mCamera.stopPreview();
    	mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        adjustOrientation(w, h);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

	/**
	 * @param w
	 * @param h
	 */
	private void adjustOrientation(int w, int h) {
		// força a orientação em 0 grau
		Parameters parameters = mCamera.getParameters();
        Log.d(TAG, "tamanho da superfície: " + w + " X " + h);
		
		parameters.setPreviewSize(h, w);                           
		mCamera.setDisplayOrientation(90);
		
		Log.d(TAG, "face detection? " + parameters.getMaxNumDetectedFaces());
		
/*		// set preview size and make any resize, rotate or
        // reformatting changes here
		
        Display display = getDisplay();
        Parameters parameters = mCamera.getParameters();
        
        Log.d(TAG, "tamanho da superfície: " + w + " X " + h);
        Log.d(TAG, "orientação da tela: " + display.getRotation());
        
        switch (display.getOrientation()) {
		case Surface.ROTATION_0:
			parameters.setPreviewSize(h, w);                           
            mCamera.setDisplayOrientation(90);
			break;
			
		case Surface.ROTATION_90:
			parameters.setPreviewSize(w, h);
			break;
			
		case Surface.ROTATION_180:
			parameters.setPreviewSize(h, w);
			break;
			
		case Surface.ROTATION_270:
			parameters.setPreviewSize(w, h);
            mCamera.setDisplayOrientation(180);
			break;
		}*/
	}
}
