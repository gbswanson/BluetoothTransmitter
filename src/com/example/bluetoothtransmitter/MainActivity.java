package com.example.bluetoothtransmitter;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
	private Button callButton;
	public BluetoothAdapter mBluetoothAdapter;
	private ListView myListView;
	private ArrayAdapter<String> BTArrayAdapter;
	public static UUID MY_UUID;
	private final static String TAG = "MainActivity";

	private BluetoothDevice serverDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// take an instance of BluetoothAdapter - Bluetooth radio
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(),
					"Your device does not support Bluetooth", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		// Turn device into receiver
		// This device will listen to and act on data sent by server
		connectButton = (Button) findViewById(R.id.connect);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Once our desired device has been found, connect to it
				ConnectThread clientThread = new ConnectThread(serverDevice);
				clientThread.run(mBluetoothAdapter);
			}
		});

		serverButton = (Button) findViewById(R.id.server);
		serverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// start server and accept connection when requested
				AcceptThread serverThread = new AcceptThread(mBluetoothAdapter,
						getResources());
				serverThread.run();
			}
		});

		callButton = (Button) findViewById(R.id.call);
		callButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:9097817470"));
					startActivity(callIntent);
				} catch (ActivityNotFoundException activityException) {
					Log.e("Calling a Phone Number", "Call failed",
							activityException);
				}
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

		// search for other devices in area
		search();
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

	// Search for devices
	public void search() {
		// Discover new bluetooth devices in the area
		if (mBluetoothAdapter.isDiscovering()) {
			Toast.makeText(this, "Already discovering", Toast.LENGTH_SHORT)
					.show();
			// Cancel any ongoing discovery
			mBluetoothAdapter.cancelDiscovery();
		} else {
			Toast.makeText(this, "Attempting to discover", Toast.LENGTH_SHORT)
					.show();
			BTArrayAdapter.clear();
			mBluetoothAdapter.startDiscovery();

			registerReceiver(bReceiver, new IntentFilter(
					BluetoothDevice.ACTION_FOUND));
		}
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
		super.onDestroy();
		unregisterReceiver(bReceiver);
	}

}