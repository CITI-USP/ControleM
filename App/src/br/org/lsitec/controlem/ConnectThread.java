package br.org.lsitec.controlem;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

class ConnectThread extends Thread {
	    /**
		 * 
		 */
		private final MainActivity mainActivity;
		private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	    
	    public ConnectThread(MainActivity mainActivity, BluetoothDevice device) {
	        this.mainActivity = mainActivity;
			// Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device; 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	        	System.out.println(device.getUuids()[0].getUuid().toString());
	            tmp = device.createRfcommSocketToServiceRecord(this.mainActivity.MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	    
	    public void run() {
	        // Cancel discovery because it will slow down the connection
			this.mainActivity.updateStatus("Conectando...");
	    	System.out.println("CONECTANDO");
	        this.mainActivity.mBluetoothAdapter.cancelDiscovery();
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	    		this.mainActivity.updateStatus("Erro!");
	        	System.out.println(connectException.toString());
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	        // Do work to manage the connection (in a separate thread)
	        this.mainActivity.command = new SendCommandThread(this.mainActivity, mmSocket);
	        this.mainActivity.command.start();
			this.mainActivity.updateStatus("Conectado");
//	        btn.setEnabled(true);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}