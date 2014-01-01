package com.example.ioiosrf08interface;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.android.IOIOActivity;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

/**
 * This is the main activity of the IOIO SRF08 Sonar Interface demo application.
 * 
 * 
 */
public class MainActivity extends IOIOActivity {
	/** Pin constants / declarations */
	//See pin capabilities on https://github.com/ytai/ioio/wiki/Getting-To-Know-The-Board
	private static int PIN_I2C_SDA0						= 4;
	private static int PIN_I2C_SCL0						= 5;

	//i2c details can be found here
	//https://github.com/ytai/ioio/wiki/TWI
		
		
	TextView txtIoioStatus;
	TextView txtSonarValue;
	TextView txtLightSensorValue;
	
	
    /**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   
        setContentView(R.layout.activity_main);
        
        txtIoioStatus = (TextView) findViewById(R.id.txtIoioStatus);        
        txtSonarValue = (TextView) findViewById(R.id.txtSonarValue);
        txtLightSensorValue = (TextView) findViewById(R.id.txtLightSensorValue);
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}	
	
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		long loopCount = 0;
		int sonarValue = 0;
		int lightSensorValue = 0;
		
		/** The on-board LED. */
		private DigitalOutput led_;
				
		/** TWI (Two Wire Interface, otherwise known as I2C */
		private TwiMaster twi;
		
		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			runOnUiThread(new Runnable() {
				public void run() {
					txtIoioStatus.setText("Setting up.");
					txtIoioStatus.setBackgroundColor(Color.GREEN);
			    }
			});			

			loopCount = 0;
			led_ = ioio_.openDigitalOutput(0, true);

			//This opens a TWI module number twiNum in master mode, using its dedicated SDA and SCL pins (specify 0, 1 or 2 for param 1)
			//The TWI module will run at 100KHz (400KHz and 1MHz also supported) 
			//and will use I�C voltage levels (pass true as third argument for SMBus levels).
			twi = ioio_.openTwiMaster(0, TwiMaster.Rate.RATE_100KHz, false);
		}

		/**
		 * Called when the IOIO has been disconnected.
		 * 
		 */
		@Override
		public void disconnected() {
			runOnUiThread(new Runnable() {
				public void run() {
					txtIoioStatus.setText("Disconnected.");
					txtIoioStatus.setBackgroundColor(Color.RED);
			    }
			});			
		}
		
		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			loopCount ++;
			led_.write(true);

			
			//
			//Default address of the SRF08 is 0xE0
			//It can be changed by the user to any of 16 addresses E0, E2, E4, E6, E8, EA, EC, EE, F0, F2, F4, F6, F8, FA, FC or FE, therefore up to 16 sonar's can be used
			//You'll need to reprogram the SRF08 address to use anything other than 0xE0
			//All addresses must be divided by 2 
			//it requires addresses to be right
			//shifted by 1. So address 0xE0 is 112, which is the Decimal of
			//Hex address 0xE0 shifted right by 1 (or divided by 2 - same thing)
			//
			//You can also write to address 0x00 (the broadcast address) to trigger all sonars to ping at once
			int sonarAddress1 = (0xE0/2); 

			//
			//Trigger a ping from the SRF08 sonar
			//			
			try {				
				byte srf08CommandRegisterAddress = 0x00;
				//0x50 = ranging result in inches
				//0x51 = ranging result in centimeters
				//0x52 = ranging result in micro-seconds
				byte rangingCommand = 0x51;
				byte[] sonarRequest = new byte[] { srf08CommandRegisterAddress, rangingCommand };
				
				Log.d("app",  "About to initiate sonar ranging.");
				TwiMaster.Result result = twi.writeReadAsync(sonarAddress1, false, sonarRequest, sonarRequest.length, null, 0);
				//...optionally do some stuff while the transaction is taking place...
				//blocks until response is available
				//result.waitReady();

			} catch (Exception e) {
				//do something here on exception
				Log.e("app", "Error in twi.writeRead", e);
			}

			//
			//Read the results from the sonar
			//
			try {							
				byte srf08CommandRegisterAddress;
				byte[] sonarRequest;
				byte[] sonarResponse;
				
				//
				//Try to read the version from the srf08
				//We need to loop and wait for the sonar to have a value in its register. The
				//version read will return something other than a 0 or 255 when it has a value that can be read
				//
				srf08CommandRegisterAddress = 0x00;
				sonarRequest = new byte[] { srf08CommandRegisterAddress };
				sonarResponse = new byte[1];
				int sonarVersion = 0;
				do {
					Log.d("app",  "Sonar waiting for results.");
					twi.writeRead(sonarAddress1, false, sonarRequest, sonarRequest.length, sonarResponse, sonarResponse.length);
					Byte sonarVersionByte = new Byte(sonarResponse[0]);
					sonarVersion = sonarVersionByte.intValue();
					Log.d("app",  "Sonar response " + sonarVersion);
				} while (sonarVersion == 0 || sonarVersion == 255);				

				
				//
				//Read the light sensor value from the srf08
				//
				srf08CommandRegisterAddress = 0x01;
				sonarRequest = new byte[] { srf08CommandRegisterAddress };
				sonarResponse = new byte[1];
				twi.writeRead(sonarAddress1, false, sonarRequest, sonarRequest.length, sonarResponse, sonarResponse.length);
				Byte lightSensorValueByte = new Byte(sonarResponse[0]);
				//We do this to remove the sign, otherwise light sensor values greater than 127 show up as negative
				lightSensorValue = lightSensorValueByte.intValue() & 0xFF;
				
				Log.d("app",  "Sonar light sensor value " + lightSensorValue);
				
				
				//
				//Read the sonar ranger/distance value from the srf08
				//
				srf08CommandRegisterAddress = 0x02;
				sonarRequest = new byte[] { srf08CommandRegisterAddress };
				sonarResponse = new byte[2];
				Log.d("app",  "About to read sonar distance results.");
				twi.writeRead(sonarAddress1, false, sonarRequest, sonarRequest.length, sonarResponse, sonarResponse.length);
				
				Byte sonarRangeHighByte = new Byte(sonarResponse[0]);
				Byte sonarRangeLowByte = new Byte(sonarResponse[1]);
				
				lightSensorValue = lightSensorValueByte.intValue() & 0xFF;
				
				sonarValue = ((sonarRangeHighByte & 0xFF) * 256) + (sonarRangeLowByte & 0xFF);
				//sonarValue = (sonarResponse[0]*256) + sonarResponse[1];
				Log.d("app",  "Sonar distance results " + sonarValue);
				
			} catch (Exception e) {
				//do something here on exception
				Log.e("app", "Error in twi.writeRead", e);
			}
			
			
			//
			//Since the IOIO runs in its own thread, need to update the view from the UI thread
			//
			runOnUiThread(new Runnable() {
				public void run() {
					//Just some dummy status code
					txtIoioStatus.setText("Looping: " + String.valueOf(loopCount));
					
					txtSonarValue.setText(String.valueOf(sonarValue));
					txtLightSensorValue.setText(String.valueOf(lightSensorValue));					
			    }
			});			
			
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}
		
		
		/**
		 * Change the address of the SRF08. It defaults to 0xE0.
		 * To change the address, make sure it is the only device on the i2c bus.
		 * 
		 * WARNING: THIS HAS NOT BEEN TESTED YET.
		 * For newAddress send in something like (0xE0/2). Divide the new address by 2 since they are bytes
		 */
		public void changeSrf08Address (byte newAddress) throws Exception {
			//
			//Change srf08 address
			//
			//First three bytes are the command sequence, then send the new address in the 4th byte (divide by two)
			byte[] changeAddressBytes = new byte[] {(byte) 0xA0, (byte) 0xAA, (byte) 0xA5, newAddress };				
			//This will write the bytes in the request array to the device with the requested address 
			//The false passed as second argument means we're using 7-bit addressing mode.
			twi.writeRead(0x00, false, changeAddressBytes, changeAddressBytes.length, null, 0);
		}
	}
	
}
