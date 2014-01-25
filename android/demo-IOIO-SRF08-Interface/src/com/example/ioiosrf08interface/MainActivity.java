package com.example.ioiosrf08interface;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
	Spinner srf08Address;
	ToggleButton btnPingingEnabled;
	Spinner newSrf08Address;
	Button btnChangeSrf08Address;

	/** TWI (Two Wire Interface, otherwise known as I2C */
	private TwiMaster twi;
	
	int sonarValue = 0;
	int lightSensorValue = 0;
	
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

    	srf08Address = (Spinner) findViewById(R.id.srf08Address);
    	btnPingingEnabled = (ToggleButton) findViewById(R.id.btnPingingEnabled);
    	newSrf08Address = (Spinner) findViewById(R.id.newSrf08Address);
    	btnChangeSrf08Address = (Button) findViewById(R.id.btnChangeSrf08Address);
    
    	populateSonarAddressSelectList();
    	setupChangeSrf08AddressButtonClickListener();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Add the possible SRF08 addresses into the select list
     * 
     */
    protected void populateSonarAddressSelectList() {
    	List<String> list = new ArrayList<String>();
    	list.add("0xE0");
    	list.add("0xE2");
    	list.add("0xE4");
    	list.add("0xE6");
    	list.add("0xE8");
    	list.add("0xEA");
    	list.add("0xEC");
    	list.add("0xEE");
    	//These four addresses below cause the IOIO I2C connection to be lost when writing to them.
    	//Dont change the sonar address to any of these! Not sure if its an IOIO bug or something else.
    	//list.add("0xF0");
    	//list.add("0xF2");
    	//list.add("0xF4");
    	//list.add("0xF6");
    	list.add("0xF8");
    	list.add("0xFA");
    	list.add("0xFC");
    	list.add("0xFE");
    	list.add("0x00 (Broadcast, Pings Everything)");
    	
    	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	srf08Address.setAdapter(dataAdapter);

    	newSrf08Address.setAdapter(dataAdapter);
    	
    }
    
    /**
     * Setup the button click listener on the changeSRF08Address button
     */
    protected void setupChangeSrf08AddressButtonClickListener () {
    	btnChangeSrf08Address.setOnClickListener(new View.OnClickListener() {
    	    @Override
    	    public void onClick(View v) {
    	    	confirmChangeSrf08Address();
    	    }
    	});    	
    }
    
    /**
     * Routine called when the button to change the SRF08 address is clicked.
     * Will first prompt the user if they are sure.
     * 
     */
    protected void confirmChangeSrf08Address() {
    	btnPingingEnabled.setChecked(false);
    	
    	new AlertDialog.Builder(this)
        .setTitle("Confirm SRF08 Address Change")
        .setMessage("Are you sure you want to change the address of your SRF08 sonar from address (" + srf08Address.getSelectedItem().toString() + ") to the new address of (" + newSrf08Address.getSelectedItem().toString() + ")? If so, make sure your sonar is the ONLY item connected to your I2C bus and then press Yes.")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                // continue with address change
				byte currentSonarAddress = (byte) (getSelectedSonarAddress(srf08Address.getSelectedItem().toString())/2);
				int newSonarAddress = getSelectedSonarAddress(newSrf08Address.getSelectedItem().toString());
            	changeSrf08Address(currentSonarAddress, newSonarAddress);
            }
         })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                // do nothing
            }
         })
        //.setIcon(R.drawable.ic_dialog_alert)
        .show(); 	
    }
    
	/**
	 * Translate a string sonar address value into an int sonar address value.
	 * Used for translating the values in the drop down lists.
	 * 
	 */
	protected int getSelectedSonarAddress (String sonarAddressTextValue) {
		//
		//Default address of the SRF08 is 0xE0
		//It can be changed by the user to any of 16 addresses E0, E2, E4, E6, E8, EA, EC, EE, F0, F2, F4, F6, F8, FA, FC or FE, therefore up to 16 sonar's can be used
		//You'll need to reprogram the SRF08 address to use anything other than 0xE0
		//All addresses must be divided by 2  when sending as an address on the bus since the bus
		//is only 7 bit. It requires addresses to be right
		//shifted by 1. So address 0xE0 is 112, which is the Decimal of
		//Hex address 0xE0 shifted right by 1 (or divided by 2 - same thing)
		//
		//You can also write to address 0x00 (the broadcast address) to trigger all sonars to ping at once
		//

		if (sonarAddressTextValue.equalsIgnoreCase("0xE0")) {
			return (0xE0);
		}
		else if (sonarAddressTextValue.equalsIgnoreCase("0xE2")) {
			return (0xE2);
		}
		else if (sonarAddressTextValue.equalsIgnoreCase("0xE4")) {
			return (0xE4);
		}
		else if (sonarAddressTextValue.equalsIgnoreCase("0xE6")) {
			return (0xE6);
		}
		else if (sonarAddressTextValue.equalsIgnoreCase("0xE8")) {
			return (0xE8);
		}
		else if (sonarAddressTextValue.equalsIgnoreCase("0xEA")) {
			return (0xEA);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xEC")) {
			return (0xEC);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xEE")) {
			return (0xEE);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xF0")) {
			return (0xF0);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xF2")) {
			return (0xF2);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xF4")) {
			return (0xF4);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xF6")) {
			return (0xF6);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xF8")) {
			return (0xF8);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xFA")) {
			return (0xFA);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xFC")) {
			return (0xFC);
		}			
		else if (sonarAddressTextValue.equalsIgnoreCase("0xFE")) {
			return (0xFE);
		}					
		else if (sonarAddressTextValue.contains("0x00")) {
			return (0x00);
		}			
		else {
			return (0xE0);
		}
	}
	
	/**
	 * Triggers the sonar ping and then reads the distance and light sensor values
	 * and displays on the UI
	 */
	protected void pingSonar(int sonarAddress) {

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
			//TwiMaster.Result result = twi.writeReadAsync(sonarAddress, false, sonarRequest, sonarRequest.length, null, 0);
			twi.writeRead(sonarAddress, false, sonarRequest, sonarRequest.length, null, 0);
			//...optionally do some stuff while the transaction is taking place...
			//blocks until response is available
			//result.waitReady();

		} catch (Exception e) {
			//do something here on exception
			Log.e("app", "Error in twi.writeRead", e);
		}

		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
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
			int sonarWaitCount = 0;
			boolean isSonarResultReady = false;
			do {
				sonarWaitCount ++;
				Log.d("app",  "Sonar waiting for results. Wait Count: "+sonarWaitCount);
				twi.writeRead(sonarAddress, false, sonarRequest, sonarRequest.length, sonarResponse, sonarResponse.length);
				
				Byte sonarVersionByte = new Byte(sonarResponse[0]);
				sonarVersion = sonarVersionByte.intValue();
				Log.d("app",  "Sonar response " + sonarVersion);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}					
				
				if (sonarVersion > 0 && sonarVersion < 255) {
					isSonarResultReady = true;
				}
				
			} while ((sonarVersion <= 0 || sonarVersion == 255) && sonarWaitCount < 14);				

			if (!isSonarResultReady) {
				sonarValue = 0;
				lightSensorValue = 0;
				return;
			}
			
			//
			//Read the light sensor value from the srf08
			//
			srf08CommandRegisterAddress = 0x01;
			sonarRequest = new byte[] { srf08CommandRegisterAddress };
			sonarResponse = new byte[1];
			twi.writeRead(sonarAddress, false, sonarRequest, sonarRequest.length, sonarResponse, sonarResponse.length);
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
			twi.writeRead(sonarAddress, false, sonarRequest, sonarRequest.length, sonarResponse, sonarResponse.length);
			
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
								
	}
	
	/**
	 * Change the address of the SRF08. It defaults to 0xE0.
	 * To change the address, make sure it is the only device on the i2c bus
	 * and send the commands to its current address and command register 0 as follows:
	 * 0xA0, 0xAA, 0xA5, newAddress
	 * 
	 * For newAddress send in something like 0xE0, 0xE2, etc. Do not divide the address
	 * by two in this case for sending in the new address.
	 * The old address should be divided by two and turned into a byte, the new address should not be divided by 2 and 
	 * instead left as an int.
	 * 
	 * PER DEVANTECH DOCS:
	 * Changing the I2C Bus Address
	 * To change the I2C address of the SRF08 you must have only one sonar on the bus. Write 
	 * the 3 sequence commands in the correct order followed by the address. Example; 
	 * to change the address of a sonar currently at 0xE0 (the default shipped address) to 0xF2, 
	 * write the following to address 0xE0; (0xA0, 0xAA, 0xA5, 0xF2 ). These commands must be 
	 * sent in the correct sequence to change the I2C address, additionally, No other command 
	 * may be issued in the middle of the sequence. The sequence must be sent to the command 
	 * register at location 0, which means 4 separate write transactions on the I2C bus. When done, you should 
	 * label the sonar with its address, however if you do forget, just power it up without sending any commands. 
	 * The SRF08 will flash its address out on the LED. One long flash followed by a number of shorter flashes 
	 * indicating its address. The flashing is terminated immediately on sending a command the SRF08.
	 * 
	 * In a pinch, you can change the address of the SRF08 using this method if you dont know its current address or if its not responding:
	 * This changes the address of the only sonar connected to the I2C bus to 0xE0
	 * 		changeSrf08Address((byte) 0x00, 0xE0);
	 */
	public void changeSrf08Address (byte oldSonarAddress, int newSonarAddress) {
		Log.d("app",  "About to change SRF08 address from " + oldSonarAddress + " to " + newSonarAddress);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}			
		
		//
		//Change srf08 address
		//
		//First three writes are the command sequence, then send the new address in the 4th write (divide the address by two as well, so 0xE0/2)
		try {
			byte srf08CommandRegisterAddress = 0x00;
			//byte commandToSend = 0xA0;
			//byte[] changeAddressBytes = new byte[] { srf08CommandRegisterAddress, commandToSend };
			byte[] changeAddressBytes = intToCommandByteArray(srf08CommandRegisterAddress, 0xA0);
			twi.writeRead(oldSonarAddress, false, changeAddressBytes, changeAddressBytes.length, null, 0);				
		
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}			
			
			//commandToSend = (0xAA/2);
			//changeAddressBytes = new byte[] { srf08CommandRegisterAddress, commandToSend };
			changeAddressBytes = intToCommandByteArray(srf08CommandRegisterAddress, 0xAA);
			twi.writeRead(oldSonarAddress, false, changeAddressBytes, changeAddressBytes.length, null, 0);				
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			
			//commandToSend = (0xA5/2);
			//changeAddressBytes = new byte[] { srf08CommandRegisterAddress, commandToSend };
			changeAddressBytes = intToCommandByteArray(srf08CommandRegisterAddress, 0xA5);
			twi.writeRead(oldSonarAddress, false, changeAddressBytes, changeAddressBytes.length, null, 0);				

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			
			//commandToSend = newSonarAddress;
			//changeAddressBytes = new byte[] { srf08CommandRegisterAddress, commandToSend };
			changeAddressBytes = intToCommandByteArray(srf08CommandRegisterAddress, newSonarAddress);
			twi.writeRead(oldSonarAddress, false, changeAddressBytes, changeAddressBytes.length, null, 0);				
			
		} catch (Exception e) {
			//do something here on exception
			Log.e("app", "Error in twi.writeRead", e);
		}			
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}
		
		String logMessage = "SRF08 address changed from " + oldSonarAddress + " to " + newSonarAddress;
		Toast.makeText(getApplicationContext(), logMessage, Toast.LENGTH_LONG).show();		
		Log.d("app", logMessage);
	}
	
	/**
	 * In order to send int commands on the I2C bus, we need to turn an int into 2 bytes
	 * This is because java bytes are signed, so the mas value we can go to is 128
	 * However, a command like 0xA0 needs to send decimal value 160.
	 * This will return a byte array with three bytes. byte 0 is the command byte passed into
	 * this function, usually 0x00 for the SRF08 command register.
	 * byte 1 is the low byte
	 * byte 2 is the high byte
	 * 
	 * @param value
	 * @return
	 */
	protected byte[] intToCommandByteArray (byte commandByte, int value) {
		byte[] data = new byte[3]; // <- assuming "in" value in 0..65535 range and we can use 2 bytes only
		data[0] = 0x00;
		data[1] = (byte)(value & 0xFF);
		data[2] = (byte)((value >> 8) & 0xFF);
		return data;
		
		//
		//To convert back to a int
		//
		//int high = data[2] >= 0 ? data[2] : 256 + data[2];
		//int low = data[1] >= 0 ? data[1] : 256 + data[1];

		//int res = low | (high << 8);		
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
		
		/** The on-board LED. */
		//private DigitalOutput led_;				
		
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
			//led_ = ioio_.openDigitalOutput(0, true);

			//This opens a TWI module number twiNum in master mode, using its dedicated SDA and SCL pins (specify 0, 1 or 2 for param 1)
			//The TWI module will run at 100KHz (400KHz and 1MHz also supported) 
			//and will use I²C voltage levels (pass true as third argument for SMBus levels).
			//SDA1
			twi = ioio_.openTwiMaster(0, TwiMaster.Rate.RATE_100KHz, false);
			//SDA2
			//twi = ioio_.openTwiMaster(1, TwiMaster.Rate.RATE_100KHz, false);
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
			//led_.write(false);	//Gotta right a 0 to the onboad led to get it to turn on.

			//
			//Ping sonar if the toggle button is enabled
			//
			if (btnPingingEnabled.isChecked()) {
				int sonarAddress = getSelectedSonarAddress(srf08Address.getSelectedItem().toString())/2;
				pingSonar(sonarAddress);
			}
			else {
				sonarValue = 0;
				lightSensorValue = 0;
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
		
	}
	
}
