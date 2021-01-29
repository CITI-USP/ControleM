package br.org.lsitec.controlem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.accounts.NetworkErrorException;
import android.bluetooth.BluetoothSocket;

class SendCommandThread extends Thread {
	/**
	 *
	 */
	private MainActivity mainActivity;
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	private static final double NEUTRAL = .5;
	private double local_x = NEUTRAL;
	private double local_y = NEUTRAL;
	byte count = 0;

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
			sendCommand();
			try {
				sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count ++;
		}
	}
	//inputSpeed    0(min neg) - 2047(center) - 4095(max pos)
	//inputDir      0(min neg) - 2047(center) - 4095(max pos)
	public void sendCommand(){
		//System.out.println("SpeedsSendCommand x="+d+" y="+y);
		int new_x = (int) Math.floor(local_x*4095);
		int new_y = (int) Math.floor(local_y*4095);
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
		buffer[7] = count;
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

	public void changeX(double x){
		local_x = x;
		System.out.println("SpeedsNew x="+local_x+" y="+local_y);
	}
	public void changeY(double y){
		local_y = y;
		System.out.println("SpeedsNew x="+local_x+" y="+local_y);
	}
}




/*
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.erichlotto.bthelper.AbstractCommandThread;

import java.io.IOException;

import static java.lang.Thread.sleep;

// *
// * Created by erich on 1/12/18.
// *

public class SendCommandThread extends AbstractCommandThread {

	byte count = 0;

	private int function=0, y=0;

	public SendCommandThread(BluetoothSocket socket) {
		super(socket);
	}


	public void run() {
		while(!isInterrupted()){
			byte[] buffer = new byte[9];  // buffer store for the stream

			buffer[0] = (byte)'*';
			buffer[1] = (byte)0; // x nao eh usado no contav
			buffer[2] = (byte)0; // x nao eh usado no contav
			buffer[3] = (byte)((0xff00 & y)>>8); //0=neutro, 255=MAX UP, -255=MAX DOWN
			buffer[4] = (byte)(0x00ff & y);
			buffer[5] = (byte)0; // nao mexer por hora
			buffer[6] = (byte)(0x00ff & function); // 0=neutro, 1=functionUP, 2=functionDOWN
			buffer[7] = count;

			byte b = 0;
			for(int i=1; i<buffer.length-1; i++)
				b+=buffer[i];
			buffer[8] = b;

			try {
				String debug = "";
				for(int a=0; a<buffer.length; a++){
					debug += "["+a+"]="+buffer[a]+", ";
				}
				Log.d("buffer", debug);
				mmOutStream.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			count ++;
			try {
				sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void changeY(int y){
		this.y = y;
	}

	public void changeFunction(int function){
		this.function = function;
	}


}*/