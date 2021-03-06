package com.example.demo_ioio_motor_interface;


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
 * This is the main activity of the IOIO Robot Motor Control demo application.
 * 
 * It is used to interface with a Magnevation motor driver board. The motor driver board uses two LMD18200T
 * components to drive the motors.
 * 
 * Magnevation Motor Driver Board Notes:
 * Motor Driver Board Controls and Component Connections
 * 
 * Pinout when looking at the Magnevation board from above.
 * 39                  1
 * --------------------
 * --------------------
 * 40                  2
 * 
 *                       Motor Board
 * Function					Pin				IOIO Pin
 * -----------------------------------------------------
 * Right Direction			36					9
 * Right Brake				40					11
 * Right Speed				29					7
 * Right Thermal Flag		32					13
 * Right Current Sensing	7					32
 * 
 * Left Direction			34					8
 * Left Brake				38					10
 * Left Speed				27					6
 * Left Thermal Flag		30					12
 * Left Current Sensing		9					31
 * 
 * Left Encoder(1)								14
 * Left Encoder(2)								15
 * Right Encoder(1)								16
 * Right Encoder(2)								17
 * 
 * 
 * To enable current sensing, jumper R10 and R11
 * To enable the thermal flags, jumper R2 and R3
 * To enable the motor driver board to power the OOPIC, jumper R13
 * PWM Can be enabled on IOIO Pins 3-7, 10-14, 18-26, and 47-48
 * 
 * 
 * '''''''''''''''''''' LMD18200 Notes ''''''''''''''''''''''''' 
 * The LMD18200 is a 3A H-Bridge designed for motion control applications. 
 * The device is built using a multi-technology process which combines 
 * bipolar and CMOS control circuitry with DMOS power devices on the same 
 * monolithic structure. Ideal for driving DC and stepper motors; the 
 * LMD18200 accommodates peak output currents up to 6A. An innovative 
 * circuit which facilitates low-loss sensing of the output current has 
 * been implemented.
 * 
 * Delivers up to 3A continuous output 
 * Operates at supply voltages up to 55V 
 * Low RDS(ON) typically 0.3Ohm per switch 
 * TTL and CMOS compatible inputs 
 * No "shoot-through" current 
 * Thermal warning flag output at 145�C 
 * Thermal shutdown (outputs off) at 170�C 
 * Internal clamp diodes 
 * Shorted load protection 
 * Internal charge pump with external bootstrap capability   
 * 
 */
public class MainActivity extends IOIOActivity {
	/** Pin constants / declarations */
	//See pin capabilities on https://github.com/ytai/ioio/wiki/Getting-To-Know-The-Board
	private static int PIN_I2C_SDA0						= 4;
	private static int PIN_I2C_SCL0						= 5;
	private static int PIN_LEFT_MOTOR_PWM 				= 6;
	private static int PIN_RIGHT_MOTOR_PWM 				= 7;
	private static int PIN_LEFT_MOTOR_DIRECTION 		= 8;
	private static int PIN_RIGHT_MOTOR_DIRECTION 		= 9;
	private static int PIN_LEFT_MOTOR_BRAKE 			= 10;
	private static int PIN_RIGHT_MOTOR_BRAKE 			= 11;
	private static int PIN_LEFT_MOTOR_THERMAL_FLAG 		= 12;
	private static int PIN_RIGHT_MOTOR_THERMAL_FLAG 	= 13;
	
	private static int PIN_LEFT_MOTOR_ENCODER_1 		= 14;
	private static int PIN_LEFT_MOTOR_ENCODER_2 		= 15;
	private static int PIN_RIGHT_MOTOR_ENCODER_1 		= 16;
	private static int PIN_RIGHT_MOTOR_ENCODER_2 		= 17;
		
	private static int PIN_LEFT_MOTOR_CURRENT 			= 31;
	private static int PIN_RIGHT_MOTOR_CURRENT 			= 32;
	
	//i2c details can be found here
	//https://github.com/ytai/ioio/wiki/TWI
		
		
	TextView txtIoioStatus;
	private ToggleButton btnMove;
	
	/** Left motor hardware information */
	ToggleButton btnLeftDirection;
	SeekBar sbLeftSpeed;
    TextView txtLeftMotorDirection;
    TextView txtLeftMotorBrake;
    TextView txtLeftMotorSpeed;
    TextView txtLeftMotorThermalFlag;
    TextView txtLeftMotorCurrentSensing;
    TextView txtLeftMotorEncoder1;
    TextView txtLeftMotorEncoder2;
    
	/** Right motor hardware information */
	ToggleButton btnRightDirection;
	SeekBar sbRightSpeed;
    TextView txtRightMotorDirection;
    TextView txtRightMotorBrake;
    TextView txtRightMotorSpeed;
    TextView txtRightMotorThermalFlag;
    TextView txtRightMotorCurrentSensing;
    TextView txtRightMotorEncoder1;
    TextView txtRightMotorEncoder2;
	
    /** Encoder Values */
    long leftMotorEncoder1Value = 0;
    long leftMotorEncoder2Value = 0;
    long rightMotorEncoder1Value = 0;
    long rightMotorEncoder2Value = 0;

    
    /**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   
        setContentView(R.layout.activity_main);
        
        txtIoioStatus = (TextView) findViewById(R.id.txtIoioStatus);
        btnMove = (ToggleButton) findViewById(R.id.btnMove);
        
		//
        //Left motor hardware information
        //
        btnLeftDirection = (ToggleButton) findViewById(R.id.btnLeftDirection);
        btnLeftDirection.setChecked(true);
        sbLeftSpeed = (SeekBar) findViewById(R.id.sbLeftSpeed);        
	    txtLeftMotorDirection = (TextView) findViewById(R.id.txtLeftMotorDirection);
	    txtLeftMotorBrake = (TextView) findViewById(R.id.txtLeftMotorBrake);
	    txtLeftMotorSpeed = (TextView) findViewById(R.id.txtLeftMotorSpeed);
	    txtLeftMotorThermalFlag = (TextView) findViewById(R.id.txtLeftMotorThermalFlag);
	    txtLeftMotorCurrentSensing = (TextView) findViewById(R.id.txtLeftMotorCurrentSensing);
	    txtLeftMotorEncoder1 = (TextView) findViewById(R.id.txtLeftMotorEncoder1);
	    txtLeftMotorEncoder2 = (TextView) findViewById(R.id.txtLeftMotorEncoder2);

		//
	    //Right motor hardware information
	    //
	    btnRightDirection = (ToggleButton) findViewById(R.id.btnRightDirection);
        btnRightDirection.setChecked(true);
        sbRightSpeed = (SeekBar) findViewById(R.id.sbRightSpeed);        
	    txtRightMotorDirection = (TextView) findViewById(R.id.txtRightMotorDirection);
	    txtRightMotorBrake = (TextView) findViewById(R.id.txtRightMotorBrake);
	    txtRightMotorSpeed = (TextView) findViewById(R.id.txtRightMotorSpeed);
	    txtRightMotorThermalFlag = (TextView) findViewById(R.id.txtRightMotorThermalFlag);
	    txtRightMotorCurrentSensing = (TextView) findViewById(R.id.txtRightMotorCurrentSensing);
	    txtRightMotorEncoder1 = (TextView) findViewById(R.id.txtRightMotorEncoder1);
	    txtRightMotorEncoder2 = (TextView) findViewById(R.id.txtRightMotorEncoder2);
	    
	    txtIoioStatus.setText("Disconnected.");
	    txtIoioStatus.setBackgroundColor(Color.RED);
		//txtLeftMotorDirection.setText("Connecting...");
		//txtRightMotorDirection.setText("Connecting...");

        
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
		
		/** The on-board LED. */
		private DigitalOutput led_;
		
		/** Left motor control pins. */
		private PwmOutput leftMotorPwm;
		private DigitalOutput leftMotorDirection;
		private DigitalOutput leftMotorBrake;
		private DigitalInput leftMotorThermalFlag;
		private AnalogInput leftMotorCurrent;
		
		/** Right motor control pins. */
		private PwmOutput rightMotorPwm;
		private DigitalOutput rightMotorDirection;
		private DigitalOutput rightMotorBrake;
		private DigitalInput rightMotorThermalFlag;
		private AnalogInput rightMotorCurrent;

		/** Left encoder */
		private DigitalInput leftMotorEncoder1;		
		private Thread leftMotorEncoder1MonitorThread;
		private DigitalInput leftMotorEncoder2;		
		private Thread leftMotorEncoder2MonitorThread;
		
		/** Right encoder */
		private DigitalInput rightMotorEncoder1;		
		private Thread rightMotorEncoder1MonitorThread;
		private DigitalInput rightMotorEncoder2;		
		private Thread rightMotorEncoder2MonitorThread;
		
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
			
			//PWM Can be enabled on Pins 3-7, 10-14, 18-26, and 47-48
			//Borrowed some example code from here: http://robotfreak.googlecode.com/svn/trunk/ioio/IOIORobotController/src/ioio/examples/robot_controller/IOIORobotControllerActivity.java
			leftMotorPwm = ioio_.openPwmOutput(PIN_LEFT_MOTOR_PWM, 1000);  //pin, freq
			leftMotorDirection = ioio_.openDigitalOutput(PIN_LEFT_MOTOR_DIRECTION);
			leftMotorBrake = ioio_.openDigitalOutput(PIN_LEFT_MOTOR_BRAKE);
			leftMotorThermalFlag = ioio_.openDigitalInput(PIN_LEFT_MOTOR_THERMAL_FLAG);
			leftMotorCurrent = ioio_.openAnalogInput(PIN_LEFT_MOTOR_CURRENT);
			
			rightMotorPwm = ioio_.openPwmOutput(PIN_RIGHT_MOTOR_PWM, 1000);  //pin, freq
			rightMotorDirection = ioio_.openDigitalOutput(PIN_RIGHT_MOTOR_DIRECTION);
			rightMotorBrake = ioio_.openDigitalOutput(PIN_RIGHT_MOTOR_BRAKE);
			rightMotorThermalFlag = ioio_.openDigitalInput(PIN_RIGHT_MOTOR_THERMAL_FLAG);
			rightMotorCurrent = ioio_.openAnalogInput(PIN_RIGHT_MOTOR_CURRENT);
			
			//Setup a thread to monitor the left motor encoder 1
			leftMotorEncoder1 = ioio_.openDigitalInput(PIN_LEFT_MOTOR_ENCODER_1);
			leftMotorEncoder1MonitorThread = new Thread(new Runnable() {
		        public void run() {
		        	try {
		        		for (;;) {
			        		leftMotorEncoder1.waitForValue(true);
			        		leftMotorEncoder1Value ++;
			        		leftMotorEncoder1.waitForValue(false);
		        		}
		        	} catch (Exception e) {
		        		//Need to figure out how to handle
		        		Log.e("app", "Error reading left encoder #1 value", e);		        		
		        	}
		        	
		        }
		    });
			leftMotorEncoder1MonitorThread.start();
			
			//Setup a thread to monitor the left motor encoder 2
			leftMotorEncoder2 = ioio_.openDigitalInput(PIN_LEFT_MOTOR_ENCODER_2);
			leftMotorEncoder2MonitorThread = new Thread(new Runnable() {
		        public void run() {
		        	try {
		        		for (;;) {
			        		leftMotorEncoder2.waitForValue(true);
			        		leftMotorEncoder2Value ++;
			        		leftMotorEncoder2.waitForValue(false);
		        		}
		        	} catch (Exception e) {
		        		//Need to figure out how to handle
		        		Log.e("app", "Error reading left encoder #2 value", e);		        		
		        	}
		        	
		        }
		    });
			leftMotorEncoder2MonitorThread.start();		
			
			//Setup a thread to monitor the right motor encoder 1
			rightMotorEncoder1 = ioio_.openDigitalInput(PIN_RIGHT_MOTOR_ENCODER_1);
			rightMotorEncoder1MonitorThread = new Thread(new Runnable() {
		        public void run() {
		        	try {
		        		for (;;) {
		        			rightMotorEncoder1.waitForValue(true);
			        		rightMotorEncoder1Value ++;
			        		rightMotorEncoder1.waitForValue(false);
		        		}
		        	} catch (Exception e) {
		        		//Need to figure out how to handle
		        		Log.e("app", "Error reading right encoder #1 value", e);		        		
		        	}
		        	
		        }
		    });
			rightMotorEncoder1MonitorThread.start();
			
			//Setup a thread to monitor the right motor encoder 2
			rightMotorEncoder2 = ioio_.openDigitalInput(PIN_RIGHT_MOTOR_ENCODER_2);
			rightMotorEncoder2MonitorThread = new Thread(new Runnable() {
		        public void run() {
		        	try {
		        		for (;;) {
		        			rightMotorEncoder2.waitForValue(true);
			        		rightMotorEncoder2Value ++;
			        		rightMotorEncoder2.waitForValue(false);
		        		}
		        	} catch (Exception e) {
		        		//Need to figure out how to handle
		        		Log.e("app", "Error reading right encoder #2 value", e);
		        	}
		        	
		        }
		    });
			rightMotorEncoder2MonitorThread.start();			
			
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

			try {
				stopMotors();
			} catch (ConnectionLostException e) {				
			}
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
			led_.write(!btnMove.isChecked());

			
			if (btnMove.isChecked()) {
				leftMotorDirection.write(btnLeftDirection.isChecked());			
				rightMotorDirection.write(btnRightDirection.isChecked());			
				
				leftMotorBrake.write(false);
				rightMotorBrake.write(false);

				//1000 seems to be the fastest speed
				//leftMotorPwm.setDutyCycle(10);
				//leftMotorPwm.setPulseWidth(sbLeftSpeed.getProgress());
				//rightMotorPwm.setDutyCycle(10);
				//rightMotorPwm.setPulseWidth(sbRightSpeed.getProgress());
				
				//The progress bar allows for the user to set speed between 0 and 1000
				//That'll translate into a 0 to 1 for the duty cycle on the IOIO 
				float leftDutyCycle = sbLeftSpeed.getProgress() / 1000.00f;
				float rightDutyCycle = sbRightSpeed.getProgress() / 1000.00f;
				leftMotorPwm.setDutyCycle(leftDutyCycle);
				rightMotorPwm.setDutyCycle(rightDutyCycle);
				
			}
			else {
				stopMotors();				
			}
			
			//
			//Since the IOIO runs in its own thread, need to update the view from the UI thread
			//
			runOnUiThread(new Runnable() {
				public void run() {
					//Just some dummy status code
					txtIoioStatus.setText("Looping: " + String.valueOf(loopCount));

					//
					//Display the motor directions text
					//
					if (btnLeftDirection.isChecked()) {
						txtLeftMotorDirection.setText("Forward");
					}
					else {
						txtLeftMotorDirection.setText("Reverse");
					}

					if (btnRightDirection.isChecked()) {
						txtRightMotorDirection.setText("Forward");						
					}
					else {
						txtRightMotorDirection.setText("Reverse");
					}
					
					
					//
					//Display the left motor speed, current sense and thermal flag
					//
					txtLeftMotorSpeed.setText(String.valueOf(sbLeftSpeed.getProgress()));
					try {
						float leftMotorCurrentValue = leftMotorCurrent.read();
						//Just to stabilize numbers when motors are not moving
						if (leftMotorCurrentValue <= .001f) {
							leftMotorCurrentValue = 0;
						}
					    txtLeftMotorThermalFlag.setText(String.valueOf(leftMotorThermalFlag.read()));
					    txtLeftMotorCurrentSensing.setText(String.valueOf(leftMotorCurrentValue));
					    
					} catch (Exception e) {
						txtLeftMotorThermalFlag.setText("Read Error");
					}		
					
					//
					//Display the left motor speed, current sense and thermal flag
					//
					txtRightMotorSpeed.setText(String.valueOf(sbRightSpeed.getProgress()));
					try {
						float rightMotorCurrentValue = rightMotorCurrent.read();
						//Just to stabilize numbers when motors are not moving
						if (rightMotorCurrentValue <= .001f) {
							rightMotorCurrentValue = 0;
						}
					    txtRightMotorThermalFlag.setText(String.valueOf(rightMotorThermalFlag.read()));
					    txtRightMotorCurrentSensing.setText(String.valueOf(rightMotorCurrentValue));
					} catch (Exception e) {
						txtRightMotorThermalFlag.setText("Read Error");						
					}					

					//Update Encoders
					txtLeftMotorEncoder1.setText(String.valueOf(leftMotorEncoder1Value));
					txtLeftMotorEncoder2.setText(String.valueOf(leftMotorEncoder2Value));
					txtRightMotorEncoder1.setText(String.valueOf(rightMotorEncoder1Value));
					txtRightMotorEncoder2.setText(String.valueOf(rightMotorEncoder2Value));
			    }
			});			

			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		
		/**
		 * Method to stop the motors.
		 * @throws ConnectionLostException
		 */
		public void stopMotors () throws ConnectionLostException {
			setBrakesEnabled(true);			
		}

		/**
		 * Either enable or disable the motor brakes based on the parameter sent in. If brakes
		 * are applied, the motors are also stopped.
		 * 
		 * @param areBrakesEnabled boolean true if brakes should be turned on, false if not.
		 * @throws ConnectionLostException
		 */
		public void setBrakesEnabled (boolean areBrakesEnabled) throws ConnectionLostException {
			if (leftMotorPwm != null && rightMotorPwm != null && leftMotorBrake != null && rightMotorBrake != null) {
				//If we enable brakes, also stop the motors.
				if (areBrakesEnabled) {
					leftMotorPwm.setDutyCycle(0);
					rightMotorPwm.setDutyCycle(0);		
				}
				
				leftMotorBrake.write(areBrakesEnabled);
				rightMotorBrake.write(areBrakesEnabled);
			}
		}
	}
	
}
