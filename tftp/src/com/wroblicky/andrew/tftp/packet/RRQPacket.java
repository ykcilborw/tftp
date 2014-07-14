package com.wroblicky.andrew.tftp.packet;

/**
 * Represents an RRQ Packet
 * 
 * @author Andrew Wroblicky
 *
 */
public class RRQPacket extends ReadWritePacket {

	public static int RRQ_OPCODE = 1;
	
	public RRQPacket(String filename, Mode mode) {
		super(filename, mode);
	}
	
	public int getOpcode() {
		return RRQ_OPCODE;
	}
}