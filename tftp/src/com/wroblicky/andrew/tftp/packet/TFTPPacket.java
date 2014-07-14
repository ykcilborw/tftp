package com.wroblicky.andrew.tftp.packet;
import java.nio.ByteBuffer;


public abstract class TFTPPacket {
	
	public static int MAXIMUM_PACKET_SIZE = 1468; // http://www.compuphase.com/tftp.htm
	public static int PACKET_SIZE = 512;  // from rfc-1350 (in bytes)
	
	private int currentPacketSize = 0;
	int currentOffset = 0;
	
	byte[] data;
	
	public abstract int getOpcode();
	
	public TFTPPacket() {
		this.data = new byte[PACKET_SIZE];
	}
	
	public boolean addByte(byte b) {
		if (currentPacketSize >= PACKET_SIZE) {
			return false;
		}
		data[currentPacketSize] = b;
		currentPacketSize++;
		return true;
	}
	
	public boolean addChar(char c) {
		byte [] twoBytes = { (byte)(c & 0xff), (byte)(c >> 8 & 0xff) };
	    addByte(twoBytes[1]);
	    addByte(twoBytes[0]);
	    return true;
	}
	
	public boolean addString(String s) {
		byte[] b = s.getBytes();
		for (int i = 0; i < b.length; i++) {
			if (!addByte(b[i])) {
				return false;
			}
		}
		return true;
	}
	
	public char readChar(int offset) {
		byte[] result = new byte[2];
		result[0] = data[offset];
		result[1] = data[offset + 1]; 
		ByteBuffer wrapped = ByteBuffer.wrap(result); // big-endian by default
		return (char) wrapped.getShort();
	}
	
	public String readString(int offset) {
		StringBuilder output = new StringBuilder();
		currentOffset = offset;
		for (int i = offset; i < PACKET_SIZE; i++) {
			if (data[i] == 0) {
				break; //zero-terminated
			}
			output.append((char)data[i]);
			currentOffset += 1;
		}
		currentOffset += 1;
		return output.toString();
	}
	
	public String readString() {
		StringBuilder output = new StringBuilder();
		for (int i = currentOffset; i < PACKET_SIZE; i++) {
			if (data[i] == 0) {
				break; //zero-terminated
			}
			output.append((char)data[i]);
			currentOffset += 1;
		}
		return output.toString();
	}
	
	public abstract byte[] generatePacket();
}