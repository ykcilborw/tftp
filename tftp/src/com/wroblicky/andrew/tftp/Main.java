package com.wroblicky.andrew.tftp;

import java.io.IOException;

import javax.print.attribute.standard.Severity;

import com.wroblicky.andrew.tftp.packet.ReadWritePacket;

/**
 * Utility class to launch the TFTP sender and receiver locally
 * 
 * @author Andrew Wroblicky
 *
 */
public class Main {
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			Util.log(Severity.ERROR, "Invalid number of arguments provided");
		}
		startSender(args[0]);
		startReceiver();
    }
	
	public static void startSender(String filename) {
        new Thread(new TFTPSender(60010, filename, ReadWritePacket.Mode.OCTET)).start();
    }

    public static void startReceiver() {
       new Thread(new TFTPReceiver("localhost", 60010)).start();
    }
}