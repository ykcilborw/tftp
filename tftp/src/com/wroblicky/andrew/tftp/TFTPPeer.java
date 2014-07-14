package com.wroblicky.andrew.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.print.attribute.standard.Severity;

import com.wroblicky.andrew.tftp.packet.TFTPPacket;

/**
 * Parent class for common methods in both TFTPReceiver and TFTPSender
 * 
 * @author Andrew Wroblicky
 *
 */
public abstract class TFTPPeer {
	
	String ipAddress;
	int port;
	Socket socket;
	
	boolean sendPacket(byte[] packet, BufferedOutputStream out) {
		try {
			out.write(packet, 0, packet.length);
			out.flush();
			return true;
		} catch (Exception e) {
			Util.log(Severity.ERROR, "Exception in sendPacket()");
			return false;
		}
	}
	
	int getPacket(byte[] receivedData, BufferedInputStream in) {
		int bytesRead = 0;
		
		try {
			bytesRead = in.read(receivedData, 0, TFTPPacket.PACKET_SIZE);
		} catch (IOException e) {
			Util.log(Severity.WARNING, "getPacket failed to read a packet");
			return -1;
		}
			
	   return bytesRead;
	}
	
	int getOpcode(byte[] data) {
		byte[] opcode = new byte[2];
		opcode[0] = data[0];
		opcode[1] = data[1]; 
		ByteBuffer wrapped = ByteBuffer.wrap(opcode); // big-endian by default
		return wrapped.getShort();
	}
}