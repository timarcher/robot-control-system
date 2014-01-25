package com.timarcher.robotface;

import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * An example of a robot face which also uses TTS (text to speech) to speak.
 * To change the voice on your phone, go to:
 * Settings->Language & Input->Text to Speech Output
 * Select a voice and then press the Listen to an Example option to hear it.
 * 
 * I found the stock android voices dont sound as good as 3rd party ones.
 * As of this demo, I have installed the Ivona voices. Go to the play store, search for Ivona and 
 * install their voices. They are currently free. You need to instal the engine, and then it will take you to
 * their website to choose voices to install. I installed Nicole (Australian English) and Gwyneth (Welsh English) 
 * 
 * To change voices to Ivona after install, go to:
 * Settings->Language & Input->Text to Speech Output
 * Select the Ivona engine, and then in its settings choose the voice that you would like to use.
 * 
 * 
 * @author tarcher
 * @date 1/31/13
 */
public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final String TAG = "SpeechActivity";
    private TextToSpeech tts;
    private FaceView faceView;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//
		//Make the app full screen, turn off the title bar
		//
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setIcon(0);
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//
		//Remove the icon and title from the action bar
		//
		getActionBar().setDisplayShowHomeEnabled(false);	//icon
		getActionBar().setDisplayShowTitleEnabled(false);	//title text
		
		//
		//Prevent the screen from going to sleep
		//
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.activity_main);
		
		//
		//Set the background color to black and add the face
		//
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.main_view);
		mainLayout.setBackgroundColor(Color.BLACK);
		
		faceView = new FaceView(this);
		mainLayout.addView(faceView);
	    	    
	    //
	    //Initialize text to speech
	    //
		//Check for the TTS library
        //Intent checkIntent = new Intent();
        //checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //startActivityForResult(checkIntent, 0);
        
		Log.i(TAG, "onCreateCalled.");
		tts = new TextToSpeech(this, this);
		
		
		//
		//Add a button to blink the eyes
		//
		Button blinkButton = new Button(this);
		blinkButton.setText("Blink");
		blinkButton.setBackgroundColor(Color.CYAN);
		blinkButton.setOnClickListener(new OnClickListener()
		{
		     @Override
		     public void onClick(View v) {
		         faceView.blink();
		     }
		});
		
		//
		//Add a button to talk
		//
		Button talkButton = new Button(this);
		talkButton.setText("Talk");
		talkButton.setBackgroundColor(Color.CYAN);
		
		//mainLayout.add(blinkButton);
		talkButton.setOnClickListener(new OnClickListener()
		{
		     @Override
		     public void onClick(View v) {
		    	 //sayIt("Hey Adddie, don't press my button!");
		    	 faceView.talk();
		     }
		});		

		//
		//Add buttons to the layout
		LinearLayout linearLayout = new LinearLayout (this);
		linearLayout.setGravity(Gravity.BOTTOM);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(130, 75);
        layoutParams.setMargins(20, 0, 20, 0);		
		linearLayout.addView(blinkButton, layoutParams);
		linearLayout.addView(talkButton, layoutParams);
		mainLayout.addView(linearLayout);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Overridden method to initialize the text to speech engine
	 * 
	 * @param status
	 */
    @Override
    public void onInit(int status) {
    	Log.i(TAG, "onInit Called. Status:" + status);

		// Now that the TTS engine is ready, we enable the button
        if (status == TextToSpeech.SUCCESS) {
        	int result = tts.setLanguage(Locale.US);
        	//int result = tts.setLanguage(Locale.UK);
        	
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This Language is not supported");
            } else {
        		//
        		//By default the value is 1.0 You can set lower values than 1.0 to decrease pitch 
        		//level or greater values for increase pitch level.
        		//
        		//tts.setPitch(0.6f);
        		
        		//
        		//The speed rate can be set using setSpeechRate(). This also will take default 
        		//of 1.0 value. You can double the speed rate by setting 2.0 or make half the speed level by setting 0.5
        		//
        		//tts.setSpeechRate(2f);        	

        		//sayIt("Hello from Android! How are you today? ");
            }
            
        }
        else {
        	Log.e(TAG, "TTS Initilization Failed");
        }
        
        Toast.makeText(getBaseContext(), 
                "TTS onInit Called. Status:"+status, 
                Toast.LENGTH_SHORT).show();         
    }
    
    /**
     * Shutdown TTS
     * 
     */
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    
    /**
     * Method to say the text provided.
     * @param text
     */
    protected void sayIt (String text) {
    	tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);    	
    }
    
        
}
