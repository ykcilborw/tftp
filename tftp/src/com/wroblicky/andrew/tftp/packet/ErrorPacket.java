package com.wroblicky.andrew.tftp.packet;

/**
 * Represents an error packet
 * 
 *   2 bytes     2 bytes      string    1 byte
 *   -----------------------------------------
 *   | Opcode |  ErrorCode |   ErrMsg   |   0  |
 *   -----------------------------------------
 * 
 * @author Andrew Wroblicky
 *
 */
public class ErrorPacket extends TFTPPacket {
	
	public static int ERROR_OPCODE = 5;
	private int errorCode;
	private String message;
	
	// error codes
	public static String ERROR_CODE_ZERO = "Not defined, see error message (if any)";
	public static String ERROR_CODE_ONE = "File not found.";
    public static String ERROR_CODE_TWO = "Access violation";
	public static String ERROR_CODE_THREE = "Disk full or allocation exceeded";
	public static String ERROR_CODE_FOUR = "Illegal TFTP operation";
	public static String ERROR_CODE_FIVE = "Unknown transfer ID.";
	public static String ERROR_CODE_SIX = "File already exists";
	public static String ERROR_CODE_SEVEN = "No such user";
	
	public ErrorPacket(int errorCode, String message) {
		System.out.println("making error packet");
		this.errorCode = errorCode;
		this.message = message;
	}
	
	public ErrorPacket(byte[] data) {
		this.errorCode = readChar(2);
		this.message = readString(4);
	}
	
	public int getOpcode() {
		return ERROR_OPCODE;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public String getMessage() {
		return message;
	}
	
	public byte[] generatePacket() {
		addByte((byte) getOpcode());
		addByte((byte) errorCode);
		addString(message);
		addByte((byte)0);
		return data;
	}
}