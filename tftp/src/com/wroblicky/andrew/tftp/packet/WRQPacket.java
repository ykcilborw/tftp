package com.wroblicky.andrew.tftp.packet;

/**
 * Represents a WRQ packet.
 * 
 *  2 bytes     string    1 byte     string   1 byte
 *  ------------------------------------------------
 *  | Opcode |  Filename  |   0  |    Mode    |   0  |
 *  ------------------------------------------------
 * 
 * @author Andrew Wroblicky
 *
 */
public class WRQPacket extends ReadWritePacket {
	
	public static int WRQ_OPCODE = 2;
	
	public WRQPacket(String filename, ReadWritePacket.Mode mode) {
		super(filename, mode);
	}
	
	public WRQPacket(byte[] data) {
		super("", Mode.OCTET);
		this.data = data;
		String filename = readString(2);
		String mode = readString().toUpperCase();
		setFilename(filename);
		Mode m;
		m = Mode.valueOf(mode);
		setMode(m);
	}
	
	public int getOpcode() {
		return WRQ_OPCODE;
	}
}