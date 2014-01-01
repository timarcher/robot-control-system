package com.timarcher.demo_streamvideo;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * This is the main activity of the Streaming video demo application.
 * 
 * 
 */
public class MainActivity extends Activity {
	/** A tag for logging output. */
	private static final String TAG = "MainActivity";
	/** Whether or not the application is streaming media. */
	private boolean isStreaming = false;
	
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    
	/** The camera to use. */
	Camera camera = null;
	MediaRecorder mediaRecorder = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        surfaceView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
        surfaceHolder = surfaceView.getHolder();
        //surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Handler for when the button to start/stop streaming is clicked.
     * @param view
     */
    public void toggleStreaming(View view) {
    	if (!isStreaming) {
    		startStreaming(view);
    	}
    	else {
    		stopStreaming(view);    		
    	}
    }
    
    /**
     * Start the streaming, capture from camera and stream to the destination.
     * @param view
     */
    private void startStreaming(View view) {    	
        Log.i(TAG, "startStreaming method called.");   

    	try {
    		//
    		//Get a reference to the the appropriate camera to record from
    		//
    		if (camera == null) {
        		camera = Camera.open();
    			camera.unlock();
    		}

    		/*
	    	Camera.Parameters parameters = camera.getParameters();	    	
	    	//camera-id 0 = rear camera
	    	//camera-id 1 = front camera	    	
	    	parameters.set("camera-id", 0);
	    	parameters.setPreviewSize(640, 480); // or (800,480)
	    	camera.setParameters(parameters);
	    	//camera.unlock(); // unlock, to give other process to access it otherwise it can't be used later
    		*/
    		
    		//
    		//Setup the media recorder object and begin capturing the audio/video
    		//    	
	    	mediaRecorder = new MediaRecorder();	
	    	//To avoid error "setcamera called in an invalid state" you must call setCamera immediately
	    	//after instantiating the mediarecord. Attempting to set the audio source first will throw that error.
	    	mediaRecorder.setCamera(camera);
	    	mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface()); 
	    	
	    	//mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    	//mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);	 
	    	mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    	mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
	    	
	    	//mediaRecorder.setOutputFile("/mnt/sdcard/DCIM/Camera/myvideo.mp4");
	    	mediaRecorder.setOutputFile("/dev/null");
//	    	mediaRecorder.setOutputFile("/mnt/sdcard/myvideo.mp4");
	    	mediaRecorder.setMaxDuration(20000); // Set max duration 20 sec.
	    	mediaRecorder.setMaxFileSize(1000000); // Set max file size 1M
	    	mediaRecorder.setVideoFrameRate(15);
	    	mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
	    	      
	    	mediaRecorder.prepare();
	    	mediaRecorder.start();
	    	
	    	//
	    	//Set flags that streaming has started, and update the UI
	    	//
	    	this.isStreaming = true;
	    	
	    	Button streamingButton = (Button)findViewById(R.id.streamingControlButton);
	    	streamingButton.setText("Stop Streaming");
	    
	        Toast.makeText(getApplicationContext(), 
	        			   "Streaming started.", 
	        			   Toast.LENGTH_SHORT).show();
    	} catch (Exception e) {
	        Toast.makeText(getApplicationContext(), 
     			   "Error: " + e.toString(), 
     			   Toast.LENGTH_LONG).show();

    		Log.e(TAG, "Unable to start streaming. " + e.toString());       		
    	}
    }

    /**
     * Stop the streaming, close down any resources being used.
     * @param view
     */
    private void stopStreaming(View view) {
        Log.i(TAG, "stopStreaming method called.");   
        
        //
        //Release the camera resources
        //
        try {
	        if (camera != null) {
	        	camera.release();
	        }
        } catch (Exception e) {
    		Log.e(TAG, "An error occurred releasing the camera. " + e.toString());       		        	
        }

        
        //
        //Release the media recorder resources
        //
        try {
	        if (mediaRecorder != null) {
	        	mediaRecorder.stop();
	        	mediaRecorder.reset();
	        	mediaRecorder.release();        	
	        }
        } catch (Exception e) {
    		Log.e(TAG, "An error occurred releasing the mediaRecorder. " + e.toString());       		        	
        }

        
    	this.isStreaming = false;

    	Button streamingButton = (Button)findViewById(R.id.streamingControlButton);
    	streamingButton.setText("Start Streaming");

    	Toast.makeText(getApplicationContext(), 
 			   "Streaming stopped.", 
 			   Toast.LENGTH_SHORT).show();    	
    }

}
