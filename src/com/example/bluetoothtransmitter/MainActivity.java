package com.example.bluetoothtransmitter;

import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int REQUEST_ENABLE_BT = 1;
	private Button connectButton;
	private Button serverButton;
	private Button findBtn;
	public BluetoothAdapter myBluetoothAdapter;
	private ListView myListView;
	private ArrayAdapter<String> BTArrayAdapter;
	public final static String NAME = "Rhythm";
	public static UUID MY_UUID;
	private final static String TAG = "MainActivity";

	private BluetoothDevice serverDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// take an instance of BluetoothAdapter - Bluetooth radio
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (myBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(),
					"Your device does not support Bluetooth", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		// Hard code the UUID so that server and client have the same UUID
		MY_UUID = UUID.fromString("f59d7af0-9f77-11e3-a5e2-0800200c9a66");

		// Turn device into reciever
		// This device will listen to and act on data sent by server
		connectButton = (Button) findViewById(R.id.connect);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				connect(v);
			}
		});

		// Turn device into the server
		// this device will transmit ECG data to client
		serverButton = (Button) findViewById(R.id.server);
		serverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				serverCode(v);
			}
		});

		myListView = (ListView) findViewById(R.id.listView1);

		// create the arrayAdapter that contains the BTDevices, and set it
		// to the ListView
		BTArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		myListView.setAdapter(BTArrayAdapter);

		Log.d(TAG, "INITIAL");

		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivity(discoverableIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			Log.e(TAG, "Bluetooth not enabled");
			finish();
			return;
		}
	}

	public void serverCode(View view) {
		// start server and accept connection when requested
		AcceptThread serverThread = new AcceptThread();
		serverThread.run();

	}

	// Client side attempts to make connection to server
	public void connect(View view) {
		// Discover new bluetooth devices in the area
		if (myBluetoothAdapter.isDiscovering()) {
			Toast.makeText(this, "Already discovering", Toast.LENGTH_SHORT)
					.show();
			// Cancel any ongoing discovery
			myBluetoothAdapter.cancelDiscovery();
		} else {
			Toast.makeText(this, "Attempting to discover", Toast.LENGTH_SHORT)
					.show();
			BTArrayAdapter.clear();
			myBluetoothAdapter.startDiscovery();

			registerReceiver(bReceiver, new IntentFilter(
					BluetoothDevice.ACTION_FOUND));
		}

		// Once our desired device has been found, connect to it
		ConnectThread clientThread = new ConnectThread(serverDevice);
		clientThread.run();
	}

	final BroadcastReceiver bReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Look for our device to connect to
				// Currently the device names are hardcoded
				if (device.getName().equals("LG G2")
						|| device.getName().equals("Droid")) {
					Log.d(TAG, "Correct device found");
					/*
					 * FIX ME
					 */
					serverDevice = device;
				}
				// add the name and the MAC address of the object to the
				// arrayAdapter
				BTArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
				BTArrayAdapter.notifyDataSetChanged();

			}
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(bReceiver);
	}

	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				Log.d(TAG, "Server listening");
				tmp = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						NAME, MY_UUID);
			} catch (IOException e) {
			}
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					Log.d(TAG, "Server waiting on socket to be returned");
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					break;
				}
				Log.d(TAG, "Socket returned");
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					// manageConnectedSocket(socket);
					Log.d(TAG, "Correct socket returned");
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
			}
		}
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
				// MY_UUID is the app's UUID string, also used by the server
				// code
				Log.d(TAG, "Create socket");
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			myBluetoothAdapter.cancelDiscovery();

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
			// manageConnectedSocket(mmSocket);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
