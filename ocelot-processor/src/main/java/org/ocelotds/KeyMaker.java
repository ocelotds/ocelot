/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public class KeyMaker {
	
	private static final Logger logger = LoggerFactory.getLogger(KeyMaker.class);
	/**
	 * Create a md5 from string
	 *
	 * @param msg
	 * @return
	 */
	public String getMd5(String msg) {
		MessageDigest md;
		try {
			md = getMessageDigest();
			byte[] hash = md.digest(msg.getBytes(StandardCharsets.UTF_8));
			//converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			logger.error("Fail to get MD5 of String "+msg, ex);
		}
		return null;
	}
	
	MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance(Constants.ALGORITHM);
	}
	
}
