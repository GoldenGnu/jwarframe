/*
 * Copyright 2014-2015 Niklas Kyster Rasmussen
 *
 * This file is part of jWarframe.
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


package net.nikr.warframe.io.shared;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImageGetter {

	private static final Logger LOG = LoggerFactory.getLogger(ImageGetter.class);

	private static final Map<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();
	private static final Set<String> failed = new HashSet<String>();

	public static void download(final String s) {
		getFile(s);
	}

	public static Icon getIcon(final String s) {
		BufferedImage bufferedImage = getBufferedImage(s);
		if (bufferedImage != null) {
			return new ImageIcon(bufferedImage);
		} else {
			return null;
		}
	}
	
	public static BufferedImage getBufferedImage(final String s) {
		File file = getFile(s);
		if (file == null || !file.exists()) {
			return null;
		}
		BufferedImage image = imageCache.get(s);
		if (image != null) { //Use cache
			return image;
		} else { //Load from file
			try {
				image = ImageIO.read(file); //Load
				imageCache.put(s, image); //Store
				return image; //Return
			} catch (IOException ex) {
				LOG.error("Failed to load file...", ex);
				return null;
			}
		}
	}

	public static File getFile(final String s) {
		if (s == null) {
			return null;
		}
		String filename = s.toLowerCase().replace(" ", "_") + ".png";
		File file = FileConstants.getImageLocal(filename);
		if (!file.exists()) { //Download from web
			download(filename, file);
		}
		return file;
	}

	private static void download(String filename, File file) {
		if (failed.contains(filename)) { //Only try to download once...
			return;
		}
		LOG.info("Downloading: " + filename);
		InputStream in = null;
		FileOutputStream out = null;
		failed.add(filename);
		try {
			URL url = new URL(FileConstants.getImageOnline(filename));
			in = url.openStream();
			out = new FileOutputStream(file);
			final byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
			}
			failed.remove(filename);
			LOG.info("\t" + filename + " downloaded");
		} catch (MalformedURLException ex) {
			LOG.error("Bad url", ex);
		} catch (IOException ex) {
			LOG.warn("Failed to download: " + filename + " - no problem...");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					//I give up...
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					//I give up...
				}
			}
		}
	}
}
