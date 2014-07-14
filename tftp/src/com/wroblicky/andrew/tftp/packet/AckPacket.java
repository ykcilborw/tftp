package com.wroblicky.andrew.tftp.packet;

/**
 * Represents an ack packet
 * 
 *    2 bytes       2 bytes
 *   -------------------------
 *   | Opcode    |   Block #  |
 *   -------------------------
 * 
 * @author Andrew Wroblicky
 *
 */
public class AckPacket extends TFTPPacket {
	
	public static int ACK_OPCODE = 4;
	
	private int blockNumber;
	
	public AckPacket(int blockNumber) {
		this.blockNumber = blockNumber;
	}
	
	public AckPacket(byte[] data) {
		this.data = data;
		blockNumber = readChar(2);
	}
	
	public int getOpcode() {
		return ACK_OPCODE;
	}
	
	public int getBlockNumber() {
		return blockNumber;
	}
	
	public byte[] generatePacket() {
		addChar((char) getOpcode());
		addChar((char) blockNumber);
		return data;
	}
}