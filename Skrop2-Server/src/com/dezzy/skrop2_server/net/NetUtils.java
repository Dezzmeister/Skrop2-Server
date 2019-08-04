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
		
		if (inBytes.length > keyBytes.length || inBytes.length == 0 || keyBytes.length == 0) {
			return null;
		} else {
			int count = 0;
			for (int i = 0; i < inBytes.length; i++) {
				
				if (count % 3 == 0) {
					output.add((byte)((Math.random() * 255) - 128));
					count++;
				}
				
				output.add((byte) (inBytes[i] ^ keyBytes[i]));
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
		byte[] inBytes = Base64.getDecoder().decode(in.getBytes(StandardCharsets.UTF_8));
		
		List<Byte> output = new ArrayList<Byte>();
		
		int garbageCount = 0;
		for (int i = 0; i < inBytes.length; i++) {
			if (i % 3 != 0) {
				output.add((byte) (keyBytes[i - garbageCount] ^ inBytes[i]));
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
	
	public static final String getRandomKey(int length) {
		byte[] out = new byte[length];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte)((Math.random() * 255) - 128);
		}
		
		return new String(out, StandardCharsets.UTF_8);
	}
}
