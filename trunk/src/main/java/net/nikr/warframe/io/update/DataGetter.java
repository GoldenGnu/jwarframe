/*
 * Copyright 2014 Niklas Kyster Rasmussen
 *
 * This file is part of jWarframe.
 *
 * Original code from jEveAssets (https://code.google.com/p/jeveassets/)
 *
 * jWarframe is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jWarframe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jWarframe; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package net.nikr.warframe.io.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.MalformedInputException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataGetter {

	private static final Logger LOG = LoggerFactory.getLogger(DataGetter.class);

	public boolean get(String link, File out, String checksum) {
		return get(link, out, checksum, 0);
	}

	private boolean get(String link, File out, String checksum, int tries) {
		System.out.println("Downloading: " + link + " to: " + out.getAbsolutePath());
		DigestInputStream input = null;
		FileOutputStream output = null;
		int n;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			URL url = new URL(link);
			URLConnection con = url.openConnection();
			
			byte[] buffer = new byte[4096];
			input = new DigestInputStream(con.getInputStream(), md);
			output = new FileOutputStream(out);
			while ((n = input.read(buffer)) != -1) {
				output.write(buffer, 0, n);
			}
			output.flush();
			String sum = getToHex(md.digest());
			if (sum.equals(checksum)) {
				return true; //OK
			}
		} catch (MalformedInputException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			LOG.error(ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			}
			if (output != null) {
				try {
					output.close();
				} catch (IOException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			}
		}
		if (tries < 10){ //Retry 10 times
			out.delete();
			tries++;
			return get(link, out, checksum, tries);
		} else { //Failed 10 times, I give up...
			return false;
		}
	}

	private String getToHex(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
}
