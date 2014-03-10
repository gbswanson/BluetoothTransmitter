package com.example.bluetoothtransmitter;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectThread extends Thread {
	private final BluetoothSocket mmSocket;
	private final String TAG = "ConnectThread";
	private UUID MY_UUID;

	public ConnectThread(BluetoothDevice device) {

		// Hard code the UUID so that server and client have the same UUID
		MY_UUID = UUID.fromString("f59d7af0-9f77-11e3-a5e2-0800200c9a66");

		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp = null;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server
			// code
			Log.d(TAG, "Create socket");
			tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
		}
		mmSocket = tmp;
	}

	public void run(BluetoothAdapter mBluetoothAdapter) {
		// Cancel discovery because it will slow down the connection
		mBluetoothAdapter.cancelDiscovery();
		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			Log.d(TAG, "Connecting through sockt");
			mmSocket.connect();
			Log.d(TAG, "Connection made?");
		} catch (IOException connectException) {
			// Unable to connect; close the socket and get out
			Log.e(TAG, "Unable to connect");

			try {
				Log.e(TAG, "Socket closed because of exception");
				mmSocket.close();
			} catch (IOException closeException) {
			}
			return;
		}
		Log.d(TAG, "Connection made");
		// Do work to manage the connection (in a separate thread)
		manageConnectedSocket(mmSocket);
	}

	private void manageConnectedSocket(BluetoothSocket socket) {
		ConnectedThread mConnectedThread = new ConnectedThread(socket);
		// Open ECG data file

		mConnectedThread.run();
	}

	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
		}
	}
}
