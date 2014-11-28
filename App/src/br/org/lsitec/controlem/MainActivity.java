package br.org.lsitec.controlem;

import br.org.lsitec.controlem.*;

import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;

public class MainActivity extends Activity {

	JoystickView joystick;
	
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
	
	Switch mode_switch;
	double speed_x = .5;
	double speed_y = .5;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
		
		// teste de camera
		startActivity(new Intent(getApplicationContext(), FaceController.class));
		
		
		mode_switch = (Switch)findViewById(R.id.mode_switch);
		joystick = (JoystickView)findViewById(R.id.joystick);
		joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
			@Override
			public void onValueChanged(int angle, int power, int direction) {
				double y = power*Math.cos(Math.toRadians(angle));
				double x = power*Math.sin(Math.toRadians(angle));
				y+=100;
				x+=100;
				y/=200;
				x/=200;
				speed_x = x;
				speed_y = y;
			}
		}, JoystickView.DEFAULT_LOOP_INTERVAL);
		
		updateStatus("Desconectado");
		mode_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				alternarModo();
			}
		});
				
		btnUp = (Button)findViewById(R.id.btnUp);
		btnUp.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					speed_y+=.5;
					break;
				case MotionEvent.ACTION_UP:
					speed_y-=.5;
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
					speed_y-=.5;
					break;
				case MotionEvent.ACTION_UP:
					speed_y+=.5;
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
					speed_x+=.5;
					break;
				case MotionEvent.ACTION_UP:
					speed_x-=.5;
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
					speed_x-=.5;
					break;
				case MotionEvent.ACTION_UP:
					speed_x+=.5;
					break;
				}
				return false;
			}
		});		
		alternarModo();
		
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
					connect = new ConnectThread(this, arduino);
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
	
	public void alternarModo(){
/*		MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis()+10, MotionEvent.ACTION_UP,0,0,0);
		joystick.onTouchEvent(e);*/
		
		RelativeLayout layout_analogico = (RelativeLayout)findViewById(R.id.controle_analogico);
		RelativeLayout layout_digital = (RelativeLayout)findViewById(R.id.controle_digital);
		if(mode_switch.isChecked()){
			layout_analogico.setVisibility(View.GONE);
			layout_digital.setVisibility(View.VISIBLE);
		}else{
			layout_analogico.setVisibility(View.VISIBLE);
			layout_digital.setVisibility(View.GONE);
		}
	}

}
