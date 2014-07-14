package com.wroblicky.andrew.tftp.packet;

/**
 * Parent class abstraction of both a RRQ or WRQ packet
 * 
 * @author Andrew Wroblicky
 *
 */
public abstract class ReadWritePacket extends TFTPPacket {
	
	private String filename;
	private Mode mode;
	
	public ReadWritePacket(String filename, Mode mode) {
		this.filename = filename;
		this.mode = mode;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
		
	public Mode getMode() {
		return mode;
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	public byte[] generatePacket() {
		byte[] opcode = new byte[2];
		opcode[1] = new Integer(getOpcode()).byteValue();
		opcode[0] = 0;
		addByte(opcode[0]);
		addByte(opcode[1]);
		addString(getFilename());
		addByte((byte) 0);
		addString(getMode().getMode());
		addByte((byte)0);
		return data;
	}
	
	public enum Mode {
		
		NETASCII("netascii"),
		OCTET("octet"),
		MAIL("mail"); // obsolete
		
		private String mode;
		
		private Mode(String mode) {
			this.mode = mode;
		}
		
		public String getMode() {
			return mode;
		}
	}
}