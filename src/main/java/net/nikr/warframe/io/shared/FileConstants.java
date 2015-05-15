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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.nikr.warframe.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConstants {

	private static final Logger LOG = LoggerFactory.getLogger(FileConstants.class);

	private static final String AUDIO = "audio" + File.separator;
	private static final String IMAGES = "images" + File.separator;
	private static final String FILTERS = "settings" + File.separator + "filters" + File.separator;
	private static final String DATA_LOCAL = "data" + File.separator + "data_";
	private static final String SETTINGS_LOCAL = "settings" + File.separator;
	private static final String DATA_ONLINE = "http://warframe.nikr.net/jwarframe/data_";
	private static final String IMAGES_ONLINE = "http://warframe.nikr.net/jwarframe/images/";

	public static File getImageLocal(String filename) {
		return new File(getLocalFile(IMAGES + filename, Main.isPortable()));
	}

	public static File getAudioLocal(String filename) {
		return new File(getLocalFile(AUDIO + filename, Main.isPortable()));
	}

	public static String getImageOnline(String filename) {
		return IMAGES_ONLINE + filename;
	}

	public static File getDataDirectory() {
		return new File(FileConstants.getLocalFile(FileConstants.DATA_LOCAL, Main.isPortable()));
	}

	public static File getImageDirectory() {
		return new File(FileConstants.getLocalFile(FileConstants.IMAGES, Main.isPortable()));
	}

	public static File getCategoryLocal(String filename) {
		return new File(getLocalFile(DATA_LOCAL + filename, Main.isPortable()));
	}

	public static String getCategoryOnline(String filename) {
		return FileConstants.DATA_ONLINE + filename;
	}

	public static File getCategoriesLocal() {
		return new File(getLocalFile(DATA_LOCAL + "categories.dat", Main.isPortable()));
	}

	public static String getCategoriesOnline() {
		return DATA_ONLINE + "categories.dat";
	}

	public static String getFiltersOnline() {
		return DATA_ONLINE + "filters.dat";
	}

	public static File getFilterSet(String filename) {
		return new File(getLocalFile(FILTERS + filename, Main.isPortable()));
	}

	public static File getFilterSets() {
		return new File(getLocalFile(FILTERS, Main.isPortable()));
	}

	public static File getFilters() {
		return new File(getLocalFile(SETTINGS_LOCAL + "filters.dat", Main.isPortable()));
	}

	public static File getSettings() {
		return new File(getLocalFile(SETTINGS_LOCAL + "settings.dat", Main.isPortable()));
	}

	public static File getCategoryFilters() {
		return new File(getLocalFile(SETTINGS_LOCAL + "category.dat", Main.isPortable()));
	}

	public static File getDone() {
		return new File(getLocalFile(SETTINGS_LOCAL + "done.dat", Main.isPortable()));
	}

	public static File getZoom() {
		return new File(getLocalFile(SETTINGS_LOCAL + "zoom.dat", Main.isPortable()));
	}

	public static File getMissionType() {
		return new File(getLocalFile(SETTINGS_LOCAL + "missiontypes.dat", Main.isPortable()));
	}

	public static File getKillHelp() {
		return new File(getLocalFile(SETTINGS_LOCAL + "killhelp.dat", Main.isPortable()));
	}

	public static File getVersionLocal() {
		return new File(getLocalFile(DATA_LOCAL + "version.dat", Main.isPortable()));
	}

	public static File getJar() {
		return new File(getLocalFile("jwarframe.jar", true));
	}

	public static String getUpdateJar() {
		return getLocalFile("jupdate.jar", true);
	}

	public static File getBat() {
		return new File(getLocalFile("jwarframe.bat", true));
	}

	public static File getHome() {
		return new File(getLocalFile("", true));
	}

	public static String getVersionOnline() {
		return DATA_ONLINE + "version.dat";
	}

	private static String getLocalFile(final String filename, boolean portable) {
		LOG.debug("Looking for file: {} portable: {}", filename, portable);
		try {
			File file;
			File ret;
			if (!portable) {
				File userDir = new File(System.getProperty("user.home", "."));
				if (Main.isMac()) { // preferences are stored in user.home/Library/Preferences
					file = new File(userDir, "Library" + File.separator + "Preferences" + File.separator + "jWarframe");
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

	private synchronized static void makeDirectory(File file) {
		if (!file.exists()) {
			if (!file.mkdirs()) {
				LOG.error("failed to create directories for " + file.getAbsolutePath());
			}
		}
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

	public static void copyFile(File source, File dest) {
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		int i;
		try {
			input = new BufferedInputStream(new FileInputStream(source));
			output = new BufferedOutputStream(new FileOutputStream(dest));
			while ((i = input.read()) != -1) {
				output.write(i);
			}
			output.flush();
		} catch (MalformedInputException ex) {
			LOG.error("Failed to copy file", ex);
		} catch (IOException ex) {
			LOG.error("Failed to copy file", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					LOG.error("Failed to copy file", ex);
				}
			}
			if (output != null) {
				try {
					output.close();
				} catch (IOException ex) {
					LOG.error("Failed to copy file", ex);
				}
			}
		}
	}

	public static List<String> getFileList(File dir, String ext) {
		List<String> list = new ArrayList<String>();

		String[] files = dir.list(new GenericExtFilter(ext));
		if (files != null) {
			list.addAll(Arrays.asList(files));
		}
		return list;
	}
 
	// inner class, generic extension filter
	private static class GenericExtFilter implements FilenameFilter {
 
		private final String ext;
 
		public GenericExtFilter(String ext) {
			this.ext = ext;
		}
 
		@Override
		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}
}
