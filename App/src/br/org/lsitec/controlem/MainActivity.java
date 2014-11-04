package br.org.lsitec.controlem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice arduino;
	
	String[] macs = {"88:53:2E:A4:CD:64", //Ubuntu-0
					 "00:11:12:06:03:59"}; //Arduino
	int REQUEST_ENABLE_BT = 1;
	String ARDUINO_MAC_ADDRESS = macs[1];
	UUID MY_UUID;
	
	SendCommandThread command;
	Button btnUp, btnDown, btnLeft, btnRight;
	ConnectThread connect;
	boolean btnUpPressed, btnDownPressed, btnLeftPressed, btnRightPressed, btnStopPressed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
		updateStatus("Desconectado");
		btnUp = (Button)findViewById(R.id.btnUp);
		btnUp.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					btnUpPressed = true;
					break;
				case MotionEvent.ACTION_UP:
					btnUpPressed = false;
					break;
				}
				return false;
			}
		});
		btnDown = (Button)findViewById(R.id.btnDown);
		btnDown.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					btnDownPressed = true;
					break;
				case MotionEvent.ACTION_UP:
					btnDownPressed = false;
					break;
				}
				return false;
			}
		});
		btnRight = (Button)findViewById(R.id.btnRight);
		btnRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					btnRightPressed = true;
					break;
				case MotionEvent.ACTION_UP:
					btnRightPressed = false;
					break;
				}
				return false;
			}
		});
		btnLeft = (Button)findViewById(R.id.btnLeft);
		btnLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					btnLeftPressed = true;
					break;
				case MotionEvent.ACTION_UP:
					btnLeftPressed = false;
					break;
				}
				return false;
			}
		});		
	}
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device; 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	        	System.out.println(device.getUuids()[0].getUuid().toString());
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//	            tmp = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	    
	    public void run() {
	        // Cancel discovery because it will slow down the connection
			updateStatus("Conectando...");
	    	System.out.println("CONECTANDO");
	        mBluetoothAdapter.cancelDiscovery();
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	    		updateStatus("Erro!");
	        	System.out.println(connectException.toString());
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	        // Do work to manage the connection (in a separate thread)
	        command = new SendCommandThread(mmSocket);
	        command.start();
			updateStatus("Conectado");
//	        btn.setEnabled(true);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class SendCommandThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    int count = 0;
	 
	    public SendCommandThread(BluetoothSocket socket) {
	    	System.out.println("CONECTOU");
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	    	while(true){
	    		double x=.5, y=.5;
	    		if(btnUpPressed)y+=.5;
	    		if(btnDownPressed)y-=.5;
	    		if(btnRightPressed)x+=.5;
	    		if(btnLeftPressed)x-=.5;
//	    		System.out.println(x+","+y+"\nUp:"+btnUpPressed+"\nDown:"+btnDownPressed+"\nLeft:"+btnLeftPressed+"\nRight:"+btnRightPressed);
	    		sendCommand(x, y);
	    		try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		count ++;
	    	}
	    }
	    
	    public void sendCommand(double d, double y){
	    	int new_x = (int) Math.floor(d*4095);
	    	int new_y = (int) Math.floor(y*4095);
	    	new_x = Math.min(4095, Math.max(new_x, 0));
	    	new_y = Math.min(4095, Math.max(new_y, 0));
//	    	System.out.println("X:"+new_x+" Y:"+new_y);
	        byte[] buffer = new byte[9];  // buffer store for the stream
	        
            buffer[0] = (byte)'*';
            buffer[1] = (byte)((0xff00 & new_x)>>8); 
            buffer[2] = (byte)(0x00ff & new_x);
            buffer[3] = (byte)((0xff00 & new_y)>>8);
            buffer[4] = (byte)(0x00ff & new_y);
            buffer[5] = (byte)0;
            buffer[6] = (byte)0;
            buffer[7] = (byte)(0x00ff & count);
            byte b = 0;
            for(int i=1; i<buffer.length-1; i++)
            	b+=buffer[i];
            buffer[8] = b;
            write(buffer);
            // Send the obtained bytes to the UI activity
//            System.out.println("enviou");
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
//	        	System.out.println("bytes="+bytes[0]+","+bytes[1]+","+bytes[2]+","+bytes[3]+","+bytes[4]+","+bytes[5]+","+bytes[6]);
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	        	mmInStream.close();
	        	mmOutStream.close();
	            mmSocket.close();
		    	System.out.println("CLOSED");
	        } catch (IOException e) { }
	    }
	}

	@Override 
	protected void onStart() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), "SEM BT", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){
			for (BluetoothDevice device : pairedDevices){
				System.out.println(device.getName()+">"+device.getAddress()+">"+device.getUuids().length);
				if(device.getAddress().equals(ARDUINO_MAC_ADDRESS)){
					arduino = device;
					connect = new ConnectThread(arduino);
					connect.start();
					break;
				}
			}
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		updateStatus("Desconectado");
        if(command!=null)command.cancel();
		super.onStop();
	}
	
	public void updateStatus(final String message) {
	    runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	TextView status = (TextView)findViewById(R.id.status);
	    		status.setText(message);
	        }
	    });
	}

}