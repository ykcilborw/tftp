package com.wroblicky.andrew.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.print.attribute.standard.Severity;

import com.wroblicky.andrew.tftp.packet.AckPacket;
import com.wroblicky.andrew.tftp.packet.DataPacket;
import com.wroblicky.andrew.tftp.packet.ErrorPacket;
import com.wroblicky.andrew.tftp.packet.ReadWritePacket;
import com.wroblicky.andrew.tftp.packet.TFTPPacket;
import com.wroblicky.andrew.tftp.packet.WRQPacket;

/**
 * Represents TFTP file sending logic
 * 
 * @author Andrew Wroblicky
 * 
 */
public class TFTPSender extends TFTPPeer implements Runnable {

	private String filename;
	private ReadWritePacket.Mode mode;
	private ServerSocket serverSocket;
	
	private DataInputStream din;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	
	public TFTPSender(int port, String filename, ReadWritePacket.Mode mode) {
		this.port = port;
		this.filename = filename;
		this.mode = mode;
	}

	public void run() {
		initializeSocket();
		initializeInputStream();

		int bytesRead = 0;
		int packetNumber = 0;
		byte[] data = new byte[TFTPPacket.PACKET_SIZE];		
		WRQPacket wrqPacket = new WRQPacket(filename, mode);

		try {
			out = new BufferedOutputStream(
					socket.getOutputStream());
			in = new BufferedInputStream(
					socket.getInputStream());

			byte[] packet = wrqPacket.generatePacket();
			Util.log(Severity.REPORT, "SENDER: Sending write request");
			sendPacket(packet, out);

			while (true) {
				Util.log(Severity.REPORT, "SENDER: waiting for ACK");
				
				byte[] receivedData = new byte[512];
				int bytesConsumed = getPacket(receivedData, in);
				if (bytesConsumed == -1) {
					Util.log(Severity.REPORT, "SENDER: End of file reached");
					break;
				}

				// check for  ack
				if (getOpcode(receivedData) == AckPacket.ACK_OPCODE) {
					
					AckPacket ackPacket = new AckPacket(receivedData);
					// might be out of order
					if (ackPacket.getBlockNumber() != (char) packetNumber) {
						Util.log(Severity.WARNING,
								"SENDER: Received a packet out of order: "
										+ ackPacket.getBlockNumber());
						continue; // ignore packet and try again
					}

					// send back the next data
					Util.log(Severity.REPORT,
							"SENDER: ACK received, sending next DATA packet");
					data = new byte[TFTPPacket.PACKET_SIZE];	
					bytesRead = din.read(data, 0, TFTPPacket.PACKET_SIZE - 4);
					
					Util.log(Severity.REPORT, "SENDER bytes read from file: " + bytesRead);
					if (bytesRead == -1) {
						Util.log(Severity.WARNING, "SENDER: EOF reached");
					} else { // enough data to send again
						packetNumber++;
						DataPacket dataPacket = new DataPacket(
								packetNumber, data, bytesRead);
						byte[] dataPacketBytes = dataPacket
								.generatePacket();
						Util.log(Severity.REPORT, "SENDER packet number: " + packetNumber);
						Util.log(Severity.REPORT, "SENDER data packet number of bytes: "
								+ dataPacketBytes.length);
						sendPacket(dataPacketBytes, out);

						Util.log(Severity.REPORT, "SENDER: Data packet sent");

					}						
					if (bytesRead + 4 < TFTPPacket.PACKET_SIZE) {
						// - this is our last packet
						Util.log(Severity.REPORT,
								"SENDER: File was successfully sent!");
						break;
					}
				} else if (getOpcode(receivedData) == ErrorPacket.ERROR_OPCODE) {
					ErrorPacket errorPacket = new ErrorPacket(receivedData);
						Util.log(Severity.ERROR,
								"SENDER: Error packet received with message: "
										+ errorPacket.getMessage());
					disconnect(din, in, out);
					break;
				} else {
					Util.log(Severity.WARNING, "SENDER: Unexpected packet: " + getOpcode(receivedData));
					disconnect(din, in, out);
					break;
				}
			}
		} catch (IOException ioException) {
			Util.log(Severity.ERROR,
					"SENDER: IO Exception in thread: " + ioException.getMessage());
		}
	}
	
	private void initializeInputStream() {
		try {
			din = new DataInputStream(new FileInputStream(filename));
		} catch (IOException e) {
			Util.log(Severity.ERROR, "SENDER< Unable to open source file: " + filename);
		}
	}
	
	private void initializeSocket() {
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
		} catch (UnknownHostException e) {
			Util.log(Severity.ERROR, "SENDER: Unknown host " + ipAddress);
			disconnect(din, in, out);
		} catch (IOException e) {
			Util.log(Severity.ERROR, "SENDER: " + e.getMessage());
			disconnect(din, in, out);
		}
	}

	private void disconnect(DataInputStream din, BufferedInputStream in,
			BufferedOutputStream out) {
		Util.log(Severity.REPORT, "SENDER: Disconnecting...");
		try {
			if (socket != null) {
				socket.close();
			}
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
			if (din != null) {
				din.close();
			}
		} catch (Exception e) {
			Util.log(Severity.WARNING,
					"SENDER: Something went wrong trying to disconnect");
		}
	}
}