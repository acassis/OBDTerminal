/*
 * Copyright (C) 2014 Petrolr LLC, a Colorado limited liability company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* 
 * Written by the Petrolr team in 2014. Based on the Android SDK Bluetooth Chat Example... matthew.helm@gmail.com
 */


package com.petrolr.petrolr_obdterminal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity implements Runnable{
	
	private static final String TAG = "OBDII Terminal";
	private ListView mConversationView;
	
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private static StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	static BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private static BluetoothChatService mChatService = null;
	  
	Handler handler;
	  
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	  
	  
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
	
	// Types of OBD data to read
	public static final int OBD_SPEED = 1;
	public static final int OBD_RPM = 2;
	public static final int OBD_TEMPERATURE = 3;
	public static final int OBD_ENGINELOAD = 4;
	public static final int OBD_ACCELERATION = 5;
	public static final int OBD_FUELTANK = 6;
	public static final int MAXCMD = 7;

	// 1 = speed, 2 = rpm, 3 = acceleration, etc
    private int obd_command = 0;
	private int count = 0;
	private boolean showstatus = false;

    EditText text_speed;
    EditText text_rpm;
    EditText text_temp;
    EditText text_load;
    EditText text_acel;
    EditText text_tank;
    
    TextView obdmsg;
    
    Button bRequestSpeed;
    Button bRequestRpm;
    Button bRequestTemp;
    Button bRequestLoad;
    Button bRequestAcel;
    Button bRequestTank;
    
    String command_txt;
    
    @Override
    public void run() {
    		try {
    			while (true) {
    				Thread.sleep(50);
    				handler.post(new Runnable(){
    					@Override
    					public void run(){
    						Log.d(TAG, "Thread running " + count);
    						obd_command = (count % MAXCMD);
    						count++;
    						
    						switch (obd_command){
    						case OBD_SPEED:
    							sendMessage("010D\r");
        						//text_rpm.setText(realspeed + "");
        						break;
    						case OBD_RPM:
    							sendMessage("010C\r");
    							break;
    						case OBD_TEMPERATURE:
    							sendMessage("0105\r");
    							break;
    						case OBD_ENGINELOAD:
    							sendMessage("0104\r");
    							break;
    						case OBD_ACCELERATION:
    							sendMessage("0111\r");
    							break;
    						case OBD_FUELTANK:
    							sendMessage("012F\r");
    							break;
    							
    						}
    					}
    				});
    			}
    		}catch (Exception e) {
    		}finally{
    			
    		}
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terminal_frag);

		//Speed
		text_speed = (EditText) findViewById(R.id.showSpeed);
    	text_speed.setInputType(InputType.TYPE_CLASS_TEXT);
    	text_speed.setSingleLine();

		//RPM
		text_rpm = (EditText) findViewById(R.id.showRpm);
    	text_rpm.setInputType(InputType.TYPE_CLASS_TEXT);
    	text_rpm.setSingleLine();

		//Temperature
		text_temp = (EditText) findViewById(R.id.showTemp);
    	text_temp.setInputType(InputType.TYPE_CLASS_TEXT);
    	text_temp.setSingleLine();

		//Load
		text_load = (EditText) findViewById(R.id.showLoad);
    	text_load.setInputType(InputType.TYPE_CLASS_TEXT);
    	text_load.setSingleLine();
    	
		//Acceleration
		text_acel = (EditText) findViewById(R.id.showAcel);
    	text_acel.setInputType(InputType.TYPE_CLASS_TEXT);
    	text_acel.setSingleLine();
    	
		//Fuel Tank
		text_tank = (EditText) findViewById(R.id.showTank);
    	text_tank.setInputType(InputType.TYPE_CLASS_TEXT);
    	text_tank.setSingleLine();
    	
		//OBD MSG
		obdmsg = (TextView) findViewById(R.id.textView1);
    	obdmsg.setInputType(InputType.TYPE_CLASS_TEXT);
    	obdmsg.setLines(6);
    	obdmsg.setSingleLine(false);
    	
		final ActionBar actionBar = getActionBar();
	  //  actionBar.setDisplayOptions(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(true);
	    
	  // Get local Bluetooth adapter
	  mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	  Log.d(TAG, "Adapter: " + mBluetoothAdapter);
	  
	  // If the adapter is null, then Bluetooth is not supported
	  if (mBluetoothAdapter == null) {
	      Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	      finish();
	      return;
	  }
	  
	  text_speed.setText("0");
	  //msgWindowRpm.setText("0");
	  try{
		  handler = new Handler();
		  new Thread(this).start();
	  }catch(Exception e){
			Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
	  }
		
	}

	private int hextodec(String value) {
		int intval = 0;
		
		if(value.length() < 2)
			return -1;
		
        if (value.charAt(0) >= '0' && value.charAt(0) <= '9')
        	intval = intval + ((value.charAt(0) - '0') * 16);
        else if (value.charAt(0) >= 'A' && value.charAt(0) <= 'F')
        		intval = intval + ((value.charAt(0) - 'A' + 10) * 16);
        	else
        		return -1;
        if (value.charAt(1) >= '0' && value.charAt(1) <= '9')
        	intval = intval + (value.charAt(1) - '0');
        else if (value.charAt(1) >= 'A' && value.charAt(1) <= 'F')
        	intval = intval + (value.charAt(1) - 'A' + 10);
        	else
        		return -1;
        
        return intval;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
		Intent serverIntent = null;
		
	    switch (item.getItemId()) {
	    
	    case R.id.secure_connect_scan:
	        // Launch the DeviceListActivity to see devices and do scan
	        serverIntent = new Intent(this, DeviceListActivity.class);
	        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
	        return true;
	        
	        
	    default:
            return super.onOptionsItemSelected(item);
	    }
	    
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    
    	addListenerOnButtonSpeed();   
    	addListenerOnButtonRpm();
    	addListenerOnButtonTemp();
    	addListenerOnButtonLoad();   
    	addListenerOnButtonAcel();
    	addListenerOnButtonTank();
    	
    	
	    // If BT is not on, request that it be enabled.
	    // setupChat() will then be called during onActivityResult
	    if (!mBluetoothAdapter.isEnabled()) {
	        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	    // Otherwise, setup the chat session
	    } else
	        if (mChatService == null) setupChat();
	}

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }	
	
	
	
	  private void setupChat() {
	        Log.d(TAG, "setupChat()");

	        // Initialize the array adapter for the conversation thread
	        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
	        mConversationView = (ListView) findViewById(R.id.in);
	        mConversationView.setAdapter(mConversationArrayAdapter);


	        // Initialize the BluetoothChatService to perform bluetooth connections
	        mChatService = new BluetoothChatService(this, mHandler);
	        Log.d("BT Handler SETUP ", "" +  mChatService.BTmsgHandler);

	        // Initialize the buffer for outgoing messages
	        mOutStringBuffer = new StringBuffer("");
	   
	       
	    }	
	  
	    public void sendMessage(String message) {
	        // Check that we're actually connected before trying anything
	        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED && !showstatus) {
	            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
	            showstatus = true;
	            return;
	        }

	        // Check that there's actually something to send
	        if (message.length() > 0) {
	            // Get the message bytes and tell the BluetoothChatService to write
	            byte[] send = message.getBytes();
	            mChatService.write(send);
	            LogWriter.write_info("\n" + "Cmd: " + message);
	            // Reset out string buffer to zero and clear the edit text field
	            mOutStringBuffer.setLength(0);
	            //mOutEditText.setText(mOutStringBuffer);
	        }
	    }
	  
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	
	    	Log.d("Terminal", "onActivityResult...");
	        
	        switch (requestCode) {
	        case REQUEST_CONNECT_DEVICE_SECURE:
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                connectDevice(data, true);
	            }
	            break;
	        case REQUEST_CONNECT_DEVICE_INSECURE:
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                connectDevice(data, false);
	            }
	            break;
	        case REQUEST_ENABLE_BT:
	            // When the request to enable Bluetooth returns
	            if (resultCode == Activity.RESULT_OK) {
	                // Bluetooth is now enabled, so set up a chat session
	                setupChat();
	            } else {
	                // User did not enable Bluetooth or an error occurred
	                Log.d(TAG, "BT not enabled");
	                Toast.makeText(this, "BT NOT ENABLED", Toast.LENGTH_SHORT).show();
	                finish();
	            }
	        }
	    }  
	  
	    private void connectDevice(Intent data, boolean secure) {
	        // Get the device MAC address
	        String address = data.getExtras()
	            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	        // Get the BluetoothDevice object
	        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	        // Attempt to connect to the device
	        mChatService.connect(device, secure);
	        showstatus = false;
	    }
	  
	  
	    private final Handler mHandler = new Handler() {
    		

	        @Override
	        public void handleMessage(Message msg) {

	            switch (msg.what) {
	            case MESSAGE_STATE_CHANGE:
	               
	                switch (msg.arg1) {
	                case BluetoothChatService.STATE_CONNECTED:
	                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
	                    mConversationArrayAdapter.clear();
	                    onConnect();
	                    break;
	                case BluetoothChatService.STATE_CONNECTING:
	                    setStatus(R.string.title_connecting);
	                    break;
	                case BluetoothChatService.STATE_LISTEN:
	                case BluetoothChatService.STATE_NONE:
	                    setStatus(R.string.title_not_connected);
	                    break;
	                }
	                break;
	            case MESSAGE_WRITE:
	                break;
	            case MESSAGE_READ:
	            	//StringBuilder res = new StringBuilder();
	                byte[] readBuf = (byte[]) msg.obj;
	                int i=0;

	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String(readBuf, 0, msg.arg1);

	                //show received string
                	obdmsg.append(readMessage);

                	if (obdmsg.getLineCount() > 14)
                		obdmsg.setText("");
	                
	        		LogWriter.write_info(readMessage); 

	        		//make sure we received a valid message
                	//if (!readMessage.contains("7E8") && !readMessage.contains("7E9") && !readMessage.contains("7EA"))
                	//	break;
	                
	        		String[] elements = readMessage.split(" ");

	        		// Search for command recognition string 41
	        		for (i=0; i < elements.length; i++){
	        			if (elements[i].contains("41"))
	        				break;
	        		}
	        		// We need to receive 41 XX YY then at least 2 more strings
	        		if (elements.length > (i+2)){
	        			//Speed
	        			if (elements[i+1].contains("0D"))
	        				text_speed.setText(hextodec(elements[i+2]) + "");
	        			
	        			//Temperature
		        		if (elements[i+1].contains("05"))
		       				text_temp.setText((hextodec(elements[i+2])-40) + "");

		        		//Engine Load
		        		if (elements[i+1].contains("04"))
		        			text_load.setText((hextodec(elements[i+2])*100)/255 + "");
		        		
		        		//Acceleration
		        		if (elements[i+1].contains("11"))
		        			text_acel.setText((hextodec(elements[i+2])*100)/255 + "");
		        		
		        		//Tank level
		        		if (elements[i+1].contains("2F"))
		        			text_tank.setText((hextodec(elements[i+2])*100)/255 + "");

		        		//RPM is 41 XX YY ZZ then at least 3 more strings
	        			if (elements.length > (i+3))
	        				if (elements[i+1].contains("0C")){
	        					int rpm_hi = 0, rpm_lo = 0, realrpm = 0;
	        					rpm_hi = hextodec(elements[i+2]);
	        					rpm_lo = hextodec(elements[i+3]);
	        					//RPM = ((A*256)+b)/4
	    		                realrpm = ((rpm_hi * 256) + rpm_lo)/4;
	        					text_rpm.setText(realrpm + "");
	        				}
	        		}
	        		/*

	                if (obd_command == OBD_RPM) {
	                	
	                	int rpm1, rpm2, realrpm = 0;
	                	
	                	if (msg.arg1 > 15){
		                	msgstart = 14;
		                	msgend = 16;
		                }
		                	
		                String rpm_hi = readMessage.substring(msgstart, msgend) + " ";
		                rpm1 = hextodec(rpm_hi);

	                	if (msg.arg1 > 19){
		                	msgstart = 17;
		                	msgend = 20;
		                }
		                String rpm_low = readMessage.substring(msgstart, msgend) + " ";
		                rpm2 = hextodec(rpm_low);
		                
		                //RPM = ((A*256)+b)/4
		                realrpm = ((rpm1 * 256) + rpm2)/4;

		                text_rpm.setText(realrpm + "");
	                } */	                
	                
	                break;
	                
	            case MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to "
	                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	            case MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	            }
	        }

			
	    };		  
	  

	
		public void addListenerOnButtonSpeed() {
			bRequestSpeed = (Button) findViewById(R.id.bSpeed);
			bRequestSpeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
			    	text_speed.setText("");
			    	obd_command = OBD_SPEED;
			    	sendMessage("010D\r");
				}
			});
		}

		public void addListenerOnButtonRpm() {
			bRequestRpm = (Button) findViewById(R.id.bRpm);
			bRequestRpm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
			    	text_rpm.setText("");
			    	obd_command = OBD_RPM;
			    	sendMessage("010C\r");
				}
			});
		}
		
		public void addListenerOnButtonTemp() {
			bRequestTemp = (Button) findViewById(R.id.bTemp);
			bRequestTemp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
			    	text_temp.setText("");
			    	obd_command = OBD_TEMPERATURE;
			    	sendMessage("0105\r");
				}
			});
		}

		public void addListenerOnButtonLoad() {
			bRequestLoad = (Button) findViewById(R.id.bLoad);
			bRequestLoad.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
			    	text_load.setText("");
			    	obd_command = OBD_ENGINELOAD;
			    	sendMessage("0104\r");
				}
			});
		}
		
		public void addListenerOnButtonAcel() {
			bRequestAcel = (Button) findViewById(R.id.bAcel);
			bRequestAcel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
			    	text_acel.setText("");
			    	obd_command = OBD_ACCELERATION;
			    	sendMessage("0111\r");
				}
			});
		}
		
		public void addListenerOnButtonTank() {
			bRequestTank = (Button) findViewById(R.id.bTank);
			bRequestTank.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
			    	text_tank.setText("");
			    	obd_command = OBD_FUELTANK;
			    	sendMessage("012F\r");
				}
			});
		}
		
		public void onConnect(){
			//sendMessage("ATDP");
			sendMessage("ATZ\r");
			sendMessage("ATE0\r");
			sendMessage("ATH1\r");
		}
	
}
