/*
 * Copyright 2014 Niklas Kyster Rasmussen
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

import java.io.File;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileConstants {

	private static final Logger LOG = LoggerFactory.getLogger(FileConstants.class);

	private static boolean portable = false;

	private static final String IMAGES = "images" + File.separator;
	private static final String DATA_LOCAL = "data" + File.separator + "data_";
	private static final String DATA_ONLINE = "http://warframe.nikr.net/jwarframe/data_";
	private static final String IMAGES_ONLINE = "http://warframe.nikr.net/jwarframe/images/";

	public static void setPortable(boolean portable) {
		FileConstants.portable = portable;
	}

	public static File getImageLocal(String filename) {
		return new File(getLocalFile(IMAGES + filename));
	}

	public static String getImageOnline(String filename) {
		return IMAGES_ONLINE + filename;
	}

	public static File getDataDirectory() {
		return new File(FileConstants.getLocalFile(FileConstants.DATA_LOCAL));
	}

	public static File getImageDirectory() {
		return new File(FileConstants.getLocalFile(FileConstants.IMAGES));
	}

	public static File getCategoryLocal(String filename) {
		return new File(getLocalFile(DATA_LOCAL + filename));
	}

	public static String getCategoryOnline(String filename) {
		return FileConstants.DATA_ONLINE + filename;
	}

	public static File getCategoriesLocal() {
		return new File(getLocalFile(DATA_LOCAL + "categories.dat"));
	}

	public static String getCategoriesOnline() {
		return DATA_ONLINE + "categories.dat";
	}

	public static File getFilters() {
		return new File(getLocalFile("filters.dat"));
	}

	public static File getSettings() {
		return new File(getLocalFile("settings.dat"));
	}

	public static File getDone() {
		return new File(getLocalFile("done.dat"));
	}

	public static File getVersionLocal() {
		return new File(getLocalFile(DATA_LOCAL + "version.dat"));
	}

	public static String getVersionOnline() {
		return DATA_ONLINE + "version.dat";
	}

	public static void deleteDirectory(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { //some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDirectory(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	private static String getLocalFile(final String filename) {
		LOG.debug("Looking for file: {} dynamic: {}", filename, portable);
		try {
			File file;
			File ret;
			if (!portable) {
				File userDir = new File(System.getProperty("user.home", "."));
				if (onMac()) { // preferences are stored in user.home/Library/Preferences
					file = new File(userDir, "Library/Preferences/jWarframe");
				} else {
					file = new File(userDir.getAbsolutePath() + File.separator + ".jwarframe");
				}
				ret = new File(file.getAbsolutePath() + File.separator + filename);
				
			} else {
				file = new File(net.nikr.warframe.Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
				ret = new File(file.getAbsolutePath() + File.separator + filename);
			}
			File parent;
			if (ret.isDirectory()) {
				parent = ret;
			} else {
				parent = ret.getParentFile();
			}
			if (!parent.exists()) {
				makeDirectory(parent);
			}
			LOG.debug("Found file at: {}", ret.getAbsolutePath());
			return ret.getAbsolutePath();
		} catch (URISyntaxException ex) {
			LOG.error("Failed to get program directory: Please email the latest error.txt in the logs directory to niklaskr@gmail.com", ex);
		}
		return null;
	}

	private static boolean onMac() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
	}

	private synchronized static void makeDirectory(File file) {
		if (!file.exists()) {
			if (!file.mkdirs()) {
				LOG.error("failed to create directories for " + file.getAbsolutePath());
			}
		}
	}
}