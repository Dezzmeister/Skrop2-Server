package com.dezzy.skrop2_server.net;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class NetUtils {
	
	public static final String encrypt(final String in, final String key) {		
		byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
		byte[] inBytes = in.getBytes(StandardCharsets.UTF_8);
		List<Byte> output = new ArrayList<Byte>();
		
		byte[] randomizer = getRandomBytes(8);
		for (byte b : randomizer) {
			output.add(b);
		}
		
		if (inBytes.length > keyBytes.length || inBytes.length == 0 || keyBytes.length == 0) {
			return null;
		} else {
			int count = 0;
			for (int i = 0; i < inBytes.length; i++) {
				
				if (count % 3 == 0) {
					output.add((byte)((Math.random() * 255) - 128));
					count++;
				}
				
				output.add(rotateLeft((byte)(inBytes[i] ^ keyBytes[i]), randomizer[count % randomizer.length] & 0x07));
				count++;
			}
		}
		
		byte[] outBytes = new byte[output.size()];
		for (int i = 0; i < output.size(); i++) {
			outBytes[i] = output.get(i);
		}
		
		return Base64.getEncoder().encodeToString(outBytes);
	}
	
	public static final String decrypt(final String in, final String key) {
		byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
		byte[] allInBytes = Base64.getDecoder().decode(in.getBytes(StandardCharsets.UTF_8));
		byte[] inBytes = new byte[allInBytes.length - 8];
		
		List<Byte> output = new ArrayList<Byte>();
		
		byte[] randomizer = new byte[8];
		
		for (int i = 0; i < randomizer.length; i++) {
			randomizer[i] = allInBytes[i];
		}
		
		System.arraycopy(allInBytes, 8, inBytes, 0, inBytes.length);
		
		int garbageCount = 0;
		for (int i = 0; i < inBytes.length; i++) {
			if (i % 3 != 0) {
				byte inByte = rotateRight(inBytes[i], randomizer[i % randomizer.length] & 0x07);
				
				output.add((byte)(keyBytes[i - garbageCount] ^ inByte));
			} else {
				garbageCount++;
			}
		}
		
		byte[] outBytes = new byte[output.size()];
		for (int i = 0; i < output.size(); i++) {
			outBytes[i] = output.get(i);
		}
		
		return new String(outBytes, StandardCharsets.UTF_8);
	}
	
	public static final byte rotateLeft(byte b, int times) {
		if (times == 0) {
			return b;
		}
		
		byte rotated = (byte) (b << 1);
		if ((b & 0x80) >>> 7 == 1) {
			rotated |= 1;
		}
		
		return rotateLeft(rotated, times - 1);
	}
	
	public static final byte rotateRight(byte b, int times) {
		if (times == 0) {
			return b;
		}
		
		byte rotated = (byte) ((b >>> 1) & 0x7F);
		if ((b & 0x01) == 1) {
			rotated |= 0x80;
		}
		
		return rotateRight(rotated, times - 1);
	}
	
	public static final String getRandomStringInRange(int length, byte min, byte max) {
		byte[] out = new byte[length];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte)((Math.random() * (max - min)) + min);
		}
		
		return new String(out, StandardCharsets.UTF_8);
	}
	
	public static final String getRandomString(int length) {
		byte[] out = new byte[length];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte)((Math.random() * 255) - 128);
		}
		
		return new String(out, StandardCharsets.UTF_8);
	}
	
	private static final byte[] getRandomBytes(int len) {
	        byte[] out = new byte[len];
		    for (int i = 0; i < out.length; i++) {
			      out[i] = (byte)((Math.random() * 255) - 128);
		    }
		    
		    return out;
      }
}
