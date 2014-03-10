package com.example.bluetoothtransmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.util.Log;

public class AcceptThread extends Thread {
	private Resources mResources;
	private final String TAG = "AcceptThread";
	private final BluetoothServerSocket mmServerSocket;
	private UUID MY_UUID;
	private final String NAME = "Rhythm";

	public AcceptThread(BluetoothAdapter myBluetoothAdapter, Resources resources) {
		// Need resources to read from file
		mResources = resources;

		// Hard code the UUID so that server and client have the same UUID
		MY_UUID = UUID.fromString("f59d7af0-9f77-11e3-a5e2-0800200c9a66");

		// Use a temporary object that is later assigned to mmServerSocket,
		// because mmServerSocket is final
		BluetoothServerSocket tmp = null;
		try {
			// MY_UUID is the app's UUID string, also used by the client
			// code
			Log.d(TAG, "Server listening");
			tmp = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME,
					MY_UUID);
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
				Log.d(TAG, "Correct socket returned");

				// Do work to manage the connection (in a separate thread)
				manageConnectedSocket(socket);

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

	private void manageConnectedSocket(BluetoothSocket socket) {
		ConnectedThread mConnectedThread = new ConnectedThread(socket);
		// Open ECG data file
		try {
			// while (true) {
			Log.d(TAG, "Input stream started");
			InputStream mInputStream = mResources.getAssets().open(
					"short_ecg.txt");

			if (mInputStream != null) {
				InputStreamReader mInputStreamReader = new InputStreamReader(
						mInputStream);
				BufferedReader mBufferedReader = new BufferedReader(
						mInputStreamReader);
				String line;

				// Read through ECG data file line by line and write
				// to output stream

				while ((line = mBufferedReader.readLine()) != null) {
					Log.d(TAG, line);
					mConnectedThread.write(line.getBytes());
					line = "  ";
					mConnectedThread.write(line.getBytes());
				}
			}
			mInputStream.close();
			// }
		} catch (IOException e) {
			Log.e(TAG, "Error opening input file");
			e.printStackTrace();
		}
	}
}
