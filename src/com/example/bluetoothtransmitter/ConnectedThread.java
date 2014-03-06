package com.example.bluetoothtransmitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectedThread extends Thread {
	private BluetoothSocket mmSocket;
	private InputStream mmInStream;
	private OutputStream mmOutStream;
	private final String TAG = "ConnectedThread";

	public ConnectedThread(BluetoothSocket socket) {
		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		// Get the input and output streams, using temp objects because
		// member streams are final
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	public void run() {
		byte[] buffer = new byte[512]; // buffer store for the stream
		int byteOffset = 0; // bytes returned from read()

		// Keep listening to the InputStream until an exception occurs
		while (true) {
			try {
				// Read from the InputStream
				mmInStream.read(buffer, byteOffset, 1);
				// Send the obtained bytes to the UI activity
				/*
				 * mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
				 * .sendToTarget();
				 */
				String dataPoint = new String(buffer, 0, byteOffset++);
				if (dataPoint.length() > 0) {
					if (dataPoint.substring(dataPoint.length() - 1) == " ") {
						byteOffset = 0;
						Log.d(TAG, dataPoint);
					}
				}
				// Log.d(TAG, dataPoint);

				// Loop string
				// byteOffset = (byteOffset > 511) ? 0 : byteOffset;
			} catch (IOException e) {
				break;
			}
		}
	}

	/* Call this to send data to the remote device */
	public void write(byte[] bytes) {
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
		}
	}

}
