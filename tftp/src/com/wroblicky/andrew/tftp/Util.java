package com.wroblicky.andrew.tftp;
import javax.print.attribute.standard.Severity;

/**
 * Utility class for logging
 * 
 * @author Andrew Wroblicky
 *
 */
public class Util {
	
	public static void log(Severity severity, String message) {
		System.out.println(severity.toString().toUpperCase() + ": " + message);
	}
}
