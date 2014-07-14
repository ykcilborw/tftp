package com.wroblicky.andrew.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.print.attribute.standard.Severity;

import com.wroblicky.andrew.tftp.packet.AckPacket;
import com.wroblicky.andrew.tftp.packet.DataPacket;
import com.wroblicky.andrew.tftp.packet.ErrorPacket;
import com.wroblicky.andrew.tftp.packet.TFTPPacket;
import com.wroblicky.andrew.tftp.packet.WRQPacket;

/**
 * Represents TFTP file receiving logic
 * 
 * @author Andrew Wroblicky
 * 
 */
public class TFTPReceiver extends TFTPPeer implements Runnable {
	
	private BufferedInputStream in = null;
	private BufferedOutputStream out = null;
	private DataOutputStream dout = null;
	
	public TFTPReceiver(String ipAddress, int port) {
		this.port = port;
		this.ipAddress = ipAddress;
	}
	
	// thread to receive file
	public void run() {
		
		initializeSocket();
		
		try {
	   		in = new BufferedInputStream(socket.getInputStream());
	   		out = new BufferedOutputStream(socket.getOutputStream());
	   		byte[] receivedData = new byte[TFTPPacket.PACKET_SIZE];
	   		
	   		getPacket(receivedData, in);

	   		if (isWRQ(receivedData)) {
	   			Util.log(Severity.REPORT, "RECEIVER: Received write request: " 
	   					+ getOpcode(receivedData));
	   			initializeOutputStream(new WRQPacket(receivedData));				

				Util.log(Severity.REPORT, "RECEIVER: Sending write request ack");
				// in this special case the ack number is 0
				AckPacket ackPacket = new AckPacket(0);
				sendPacket(ackPacket.generatePacket(), out);

				while (true) {
					
					Util.log(Severity.REPORT, "RECEIVER: Waiting for next data packet");
					
					receivedData = new byte[TFTPPacket.PACKET_SIZE];
					int bytesRead = getPacket(receivedData, in);
					Util.log(Severity.REPORT, "RECEIVER: Received " + bytesRead + " bytes");
					
					if (bytesRead != -1) {
						if (!isDataPacket(receivedData)) {

							Util.log(Severity.ERROR, "RECEIVER: Unexpected packet arrived." +
									" Expecting DATA packet, but instead got: " 
									+ getOpcode(receivedData));
							disconnect(in, out, dout);
						}
						
						DataPacket dataPacket = new DataPacket(receivedData, bytesRead);
						Util.log(Severity.REPORT, "RECEIVER packet content length: "
								+ dataPacket.getContent().length);
						Util.log(Severity.REPORT, "RECEIVER: "
								+ new String(dataPacket.getContent()));
						Util.log(Severity.REPORT, "RECEIVER data packet number: "
								+ dataPacket.getBlockNumber());
						dout.write(dataPacket.getContent());

						ackPacket = new AckPacket(dataPacket.getBlockNumber());
						sendPacket(ackPacket.generatePacket(), out);

						if (dataPacket.getContent().length + 4 < TFTPPacket.PACKET_SIZE) {
							Util.log(Severity.REPORT, "RECEIVER: File transferred");
							break;
						}
					} else {
						Util.log(Severity.REPORT, "RECEIVER: No bytes read." +
								" File must be transferred");
						break;
					}
				}
			} else {
				Util.log(Severity.ERROR, "RECEIVER: expected write but got: " 
						+ getOpcode(receivedData));
			}
		} catch (EOFException e ) {
			Util.log(Severity.ERROR, "RECEIVER: " + e.getMessage());
		} catch (IOException e ) {
			Util.log(Severity.ERROR, "RECEIVER: " + e.getMessage());
		} finally {
			disconnect(in, out, dout);
		}
   }
	
	private boolean isWRQ(byte[] receivedData) {
		return getOpcode(receivedData) == WRQPacket.WRQ_OPCODE;
	}
	
	private boolean isDataPacket(byte[] receivedData) {
		return getOpcode(receivedData) == DataPacket.DATA_OPCODE;
	}
	
	private void initializeSocket() {
		while (socket == null) {
			try {
				socket = new Socket(ipAddress, port);
			} catch (UnknownHostException e) {
				Util.log(Severity.ERROR, "RECEIVER: Unknown host " + ipAddress);
			} catch (IOException e) {
				Util.log(Severity.ERROR, "RECEIVER: " + e.getMessage());
			}
		}
	}
	
	private void initializeOutputStream(WRQPacket wrqPacket) {
		try {
			File file = new File("Received\\" + wrqPacket.getFilename());
			file.getParentFile().mkdir();
			file.createNewFile();
			dout = new DataOutputStream(new FileOutputStream(file));
		} catch (IOException e) {
			Util.log(Severity.ERROR, "RECEIVER: Unable to open file for writing");

			//- inform client about the error
			ErrorPacket errorPacket = new ErrorPacket(0, 
					"Internal file access error");
    		sendPacket(errorPacket.generatePacket(), out);
    		disconnect(in, out, dout);
		}
	}

	private void disconnect(BufferedInputStream in,
			BufferedOutputStream out, DataOutputStream dout) {
	
		Util.log(Severity.REPORT, "RECEIVER: Disconnecting...");
	
		try {
			socket.close();
			out.close();
			in.close();
			if (dout != null) {
				dout.close();
			}
		} catch (Exception e) {
			Util.log(Severity.WARNING,
					"RECEIVER: Something went wrong trying to disconnect the socket");
		}
	}
}