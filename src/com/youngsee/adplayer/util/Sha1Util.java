package com.youngsee.adplayer.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1Util {

	private static final Logger sLogger = new Logger();

	private static final String ALGORITHM = "SHA-1";
	private static final int BLOCKNUM = 3;
	private static final int BLOCKLENGTH = 1024;

	private static String convertToHexString(byte data[]) {
		if (data == null) {
			sLogger.i("Convert data is null.");
			return null;
		}

		StringBuffer sb = new StringBuffer();

		String byteHex;
		for (int i = 0; i < data.length; i++) {
			byteHex = Integer.toHexString(data[i] & 0xFF);
			if (byteHex.length() < 2) {
				sb.append(0);
			}
			sb.append(byteHex);
		}

		return sb.toString();
	}

	public static String getSignature(String datastr) {
		if (datastr == null) {
			sLogger.i("Data string is null.");
			return null;
		}

		String signature = null;

		MessageDigest md;
		try {
			md = MessageDigest.getInstance(ALGORITHM);
			md.update(datastr.getBytes());
			signature = convertToHexString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return signature;
	}
	
	private static String computeSignatureWithBlock(String file, int blocknum, int blocklength) {
		if (!FileUtils.isFileValid(file)) {
			sLogger.i("File isn't a valid file, file = " + file + ".");
			return null;
		}
		if (blocknum <= 0) {
			sLogger.i("Invalid block number, blocknum = " + blocknum + ".");
			return null;
		}
		if (blocklength <= 0) {
			sLogger.i("Invalid block length, blocklength = " + blocklength + ".");
			return null;
		}
		
		long filelength = FileUtils.getFileLength(file);
		
		long partlength = filelength / blocknum;
		if (partlength < blocklength) {
			sLogger.i("Invalid part length, partlength = " + partlength + " blocklength = "
					+ blocklength + ".");
			return null;
		}
		
		String signature = null;
		
		RandomAccessFile in = null;
		try {
			in = new RandomAccessFile(file, "r");
			
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);

            byte[] buf = new byte[blocklength];
            
            // First block
            int len = in.read(buf, 0, blocklength);
            md.update(buf, 0, len);
            
            if (blocknum > 1) {
            	if (blocknum > 2) {
            		long partblockoffset = (partlength - blocklength) / 2;

            		for (int i = 0; i < blocknum - 2; i++) {
            			in.seek((partlength * (i + 1)) + partblockoffset);
            			len = in.read(buf, 0, blocklength);
                        md.update(buf, 0, len);
            		}
            	}

            	// Last block
            	in.seek(filelength - blocklength);
            	len = in.read(buf, 0, blocklength);
                md.update(buf, 0, len);
            }
            
            signature = convertToHexString(md.digest());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return signature;
	}
	
	private static String computeSignature(String file) {
		if (!FileUtils.isFileValid(file)) {
			sLogger.i("File isn't a valid file, file = " + file + ".");
			return null;
		}
		
		String signature = null;
		
        FileInputStream in = null;
        try {
			in = new FileInputStream(file);
			
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);

			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) >0) {
				md.update(buf, 0, len);
			}

			signature = convertToHexString(md.digest());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        
        return signature;
	}

	private static String computeFileSignature(String file, int blocknum, int blocklength) {
		long filelength = FileUtils.getFileLength(file);
		if (filelength == -1) {
			sLogger.i("Invalid file length.");
			return null;
		}

		if (filelength > (blocknum * blocklength)) {
			return computeSignatureWithBlock(file, blocknum, blocklength);
		} else {
			return computeSignature(file);
		}
	}

	public static String getFileSignature(String file) {
		if (!FileUtils.isFileValid(file)) {
			sLogger.i("File isn't a valid file, file = " + file + ".");
			return null;
		}

		return computeFileSignature(file, BLOCKNUM, BLOCKLENGTH);
	}

}
