package com.wroblicky.andrew.tftp.packet;

import java.util.Arrays;


/**
 * Represents a data packet
 * 
 *  2 bytes     2 bytes      n bytes
 *  ----------------------------------
 * | Opcode |   Block #  |   Data     |
 *  ----------------------------------
 *
 * @author Andrew Wroblicky
 */
public class DataPacket extends TFTPPacket {
	
	public static int DATA_OPCODE = 3;
	
	private int blockNumber;
	private byte[] content;
	private int bytesRead;
	
	public DataPacket(int blockNumber, byte[] content, int bytesRead) {
		this.blockNumber = blockNumber;
		this.content = content;
		this.bytesRead = bytesRead;
		if (blockNumber == 2) {
			System.out.println("DataPacket content: " + new String(content));
		}
	}
	
	public DataPacket(byte[] data, int bytesRead) {
		this.data = data;
		this.blockNumber = readChar(2);
		this.content = Arrays.copyOfRange(data, 4, bytesRead);
	}
	
	public int getOpcode() {
		return DATA_OPCODE;
	}
	
	public int getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}
	
	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
	
	public byte[] generatePacket() {
		data = new byte[bytesRead + 4];
		addChar((char) getOpcode());
		System.out.println("dataPacket blockNumber: " + blockNumber);
		addChar((char) blockNumber);
		for (int i = 0; i < bytesRead; i++) {
			addByte(content[i]);
		}
		return data;
	}
}