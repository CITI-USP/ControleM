package br.org.lsitec.controlem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

class SendCommandThread extends Thread {
	    /**
		 *  Envia dados para a placa de acordo com o protocolo
		 */
		private MainActivity mainActivity;
		private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    byte count = 0;
	 
	    public SendCommandThread(MainActivity mainActivity, BluetoothSocket socket) {
	    	this.mainActivity = mainActivity;
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
	    		sendCommand(this.mainActivity.speed_x, this.mainActivity.speed_y);
	    		try {
					sleep(20);
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

	        byte[] buffer = new byte[9];  // buffer store for the stream
	        
	        // início da sequência
            buffer[0] = (byte)'*';
            // valor do eixo X (2 bytes)
            buffer[1] = (byte)((0xff00 & new_x)>>8); 
            buffer[2] = (byte)(0x00ff & new_x);
            // valor do eixo Y (2 bytes)
            buffer[3] = (byte)((0xff00 & new_y)>>8);
            buffer[4] = (byte)(0x00ff & new_y);
            // flags (2 bytes)
            buffer[5] = (byte)0;
            buffer[6] = (byte)0;
            // número sequencial
            buffer[7] = count;
            // checksum
            byte b = 0;
            for(int i=1; i<buffer.length-1; i++)
            	b+=buffer[i];
            buffer[8] = b;
            
            write(buffer);
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