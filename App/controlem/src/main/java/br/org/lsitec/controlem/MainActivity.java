package br.org.lsitec.controlem;

import java.util.Set;
import java.util.UUID;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;

import com.erichlotto.bthelper.BTHelper;
import com.erichlotto.bthelper.ConnectionThread;

public class MainActivity<main_Activity> extends AppCompatActivity{
	private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 96;
	JoystickView joystick;
	
	//BluetoothAdapter mBluetoothAdapter;
	//BluetoothDevice arduino;
	//String[] macs = {"88:53:2E:A4:CD:64",  //Ubuntu-0
	//		         "00:11:12:06:03:59",  //Arduino-0 (HC05 - preto)
	//		         "98:D3:31:40:18:C2"}; //Arduino-1 (HC05 - Controle-M usado na cadeira de rodas)
	//"00:21:13:03:CD:E1"  //HC05 - Magicbot
	//String[] macs = {"88:53:2E:A4:CD:64","00:11:12:06:03:59","00:21:13:03:CD:E1","98:D3:31:40:18:C2"};  a ordem afeta o search de paired devices
	//int REQUEST_ENABLE_BT = 1;
	//String ARDUINO_MAC_ADDRESS_1 = macs[1];
	//String ARDUINO_MAC_ADDRESS_2 = macs[2];
	UUID MY_UUID;

	TextView tvVersion, tvFeedback;
	Button btConnect;
	BTHelper btHelper;
	SendCommandThread command;
	Button btnUp, btnDown, btnLeft, btnRight;

	public Switch mode_switch;
	private static final double GO = 1, GO_BACK = 0, NEUTRAL = .5;
	//double speed_x = NEUTRAL;
	//double speed_y = NEUTRAL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		btHelper = new BTHelper(this);
		tvFeedback = findViewById(R.id.tvFeedback);
		btConnect = findViewById(R.id.btConnect);
		btnUp = (Button)findViewById(R.id.btnUp);
		btnDown = (Button)findViewById(R.id.btnDown);
		btnRight = (Button)findViewById(R.id.btnRight);
		btnLeft = (Button)findViewById(R.id.btnLeft);
		mode_switch = (Switch)findViewById(R.id.mode_switch);
		mode_switch.setVisibility(View.GONE);

		MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

		joystick = (JoystickView)findViewById(R.id.joystick);

		// futuras melhorias: centralizar joystick e ajustar cores ao tema
		joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
			@Override
			public void onValueChanged(int angle, int power, int direction) {
				double y = power*Math.cos(Math.toRadians(angle));
				double x = power*Math.sin(Math.toRadians(angle));
				y+=100;
				x+=100;
				y/=200;
				x/=200;
				command.changeX(x);
				command.changeY(y);
				//speed_x = x;
				//speed_y = y;
			}
		}, JoystickView.DEFAULT_LOOP_INTERVAL);

		//joystick.setVisibility(View.GONE);
		//joystick.setVisibility(View.VISIBLE);

		//teste de mudança de cor
		//joystick.setBackgroundColor(16);
		//joystick.setBackground(3232321);


		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
					MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
		} else {
			// We have the permission!
			onPermissionsGranted();
		}
		//command.changeX(NEUTRAL);
		//command.changeY(NEUTRAL);

		alternarModo();//para manter a coerencia entre o mode_switch
	}
	
	/*@Override
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
				System.out.println("Paired: "+device.getName()+">"+device.getAddress()+">"+device.getUuids().length);
				if(device.getAddress().equals(ARDUINO_MAC_ADDRESS_1) || device.getAddress().equals(ARDUINO_MAC_ADDRESS_2)){
					arduino = device;
					connect = new ConnectThread(this, arduino);
					connect.start();
					break;
				}
			}
		}
		super.onStart();
	}*/


	/*@Override
	protected void onStop() {
		updateStatus("Desconectado");
        if(command!=null)command.cancel();
		super.onStop();
	}*/
	
	public void updateStatus(final String message) {
	    runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	TextView status = (TextView)findViewById(R.id.status);
	    		status.setText(message);
	        }
	    });
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public void alternarModo(){
/*		MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis()+10, MotionEvent.ACTION_UP,0,0,0);
		joystick.onTouchEvent(e);*/
		RelativeLayout layout_analogico = (RelativeLayout)findViewById(R.id.controle_analogico);
		RelativeLayout layout_digital = (RelativeLayout)findViewById(R.id.controle_digital);
		if(mode_switch.isChecked()){
			layout_analogico.setVisibility(View.VISIBLE);
			layout_digital.setVisibility(View.GONE);
			mode_switch.setText("Modo analógico");
		}else{
			layout_analogico.setVisibility(View.GONE);
			layout_digital.setVisibility(View.VISIBLE);
			mode_switch.setText("Modo digital");


			//ver se o togle digital / analógico começa com analógico e só pode mudar depois de conectar o BT
			//nada dá certo joystick.setPivotX(100);
			//joystick.setPivotY(220);

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_COARSE_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					onPermissionsGranted();

				} else {
					finish();
				}
			}
		}
	}

	void onPermissionsGranted() {
		btConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				btHelper.showConnectionDialog(listener);
			}
		});
		btHelper.connectToLastDevice(listener);
	}

	ConnectionThread.OnConnectionListener listener = new ConnectionThread.OnConnectionListener() {
		@Override
		public void onConnected(BluetoothDevice bluetoothDevice, BluetoothSocket bluetoothSocket) {
			tvFeedback.setText(getString(R.string.label_connected));
			btConnect.setVisibility(View.GONE);
			mode_switch.setVisibility(View.VISIBLE);//se muda o modo antes da conexão a tela não fica legal, melhor só deixar mudar depois de conectado

			// este bloco é para que o joystick apareça centralizado
			/*RelativeLayout layout_analogico = (RelativeLayout)findViewById(R.id.controle_analogico);
			RelativeLayout layout_digital = (RelativeLayout)findViewById(R.id.controle_digital);
			layout_analogico.setVisibility(View.GONE);
			layout_digital.setVisibility(View.VISIBLE);
			layout_analogico.setVisibility(View.VISIBLE);
			layout_digital.setVisibility(View.GONE);*/
			// fim do bloco

			command = new SendCommandThread(bluetoothSocket);
			command.start();

			mode_switch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					alternarModo();
				}
			});



			btnUp.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							command.changeY(GO);
							//speed_y += .5;
							break;
						case MotionEvent.ACTION_UP:
							command.changeY(NEUTRAL);
							//speed_y -= .5;
							break;
					}
					return true;
				}
			});

			btnDown.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							command.changeY(GO_BACK);
							//speed_y -= .5;
							break;
						case MotionEvent.ACTION_UP:
							command.changeY(NEUTRAL);
							//speed_y += .5;
							break;
					}
					return true;
				}
			});

			btnRight.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							command.changeX(GO);
							//speed_x += .5;
							break;
						case MotionEvent.ACTION_UP:
							command.changeX(NEUTRAL);
							//speed_x -= .5;
							break;
					}
					return true;
				}
			});

			btnLeft.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							command.changeX(GO_BACK);
							//speed_x -= .5;
							break;
						case MotionEvent.ACTION_UP:
							command.changeX(NEUTRAL);
							//speed_x += .5;
							break;
					}
					return true;
				}
			});


		}

		@Override
		public void onError(Exception e) {
			tvFeedback.setText(e.toString());
		}
	};

	@Override
	protected void onStop() {
		System.out.println("Stop");
		command.changeX(NEUTRAL);
		command.changeY(NEUTRAL);
		super.onStop();
	}
	//o critico da APP é: com o joystick numa direção (carrinho em movimento) apertar o home do celular, o carrinho continua andando
	@Override
	protected void onPause() {
		System.out.println("Pause");
		command.changeX(NEUTRAL);
		command.changeY(NEUTRAL);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		System.out.println("Destroy");
		command.changeX(NEUTRAL);
		command.changeY(NEUTRAL);
		btHelper.onDestroy();
		super.onDestroy();
	}

}
