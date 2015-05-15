/*
 * Copyright 2014-2015 Niklas Kyster Rasmussen
 *
 * This file is part of jWarframe.
 *
 * Original code by Oleg Ryaboy, based on work by Miguel Enriquez (http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java)
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
package net.nikr.warframe.io.run;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ListReader;
import net.nikr.warframe.io.shared.ListWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oleg Ryaboy, based on work by Miguel Enriquez 
 */
public class AutoRun {

	private static final Logger LOG = LoggerFactory.getLogger(AutoRun.class);

	private static final File STARTUP_FILE = getStartupFile("jWarframe.bat");
	private static final File STARTUP_DIR = getStartupDirectory();

	public static boolean uninstall() {
		LOG.info("Uninstalling AutoRun");
		if (!canEdit()) {
			return false;
		}
		if (STARTUP_FILE.exists()) {
			STARTUP_FILE.delete();
		}
		return !isInstalled();
	}

	public static boolean install() {
		LOG.info("Installing AutoRun");
		if (!canEdit()) {
			writeBat(FileConstants.getBat());
			return false;
		}
		writeBat(STARTUP_FILE);
		if (isInstalled()) { //Everything is okay
			return true;
		} else { //Write file to jWarframe directory for manually move
			writeBat(FileConstants.getBat());
			return false;
		}
	}

	public static boolean update() {
		if (!canEdit()) {
			return false;
		}
		if (!isInstalled()) { //Not installed
			return false;
		}
		if (!valid()) {
			LOG.info("Updating AutoRun");
			install();
		}
		return valid();
	}

	public static boolean isInstalled() {
		if (!canEdit()) {
			return false;
		}
		return STARTUP_FILE.exists();
	}

	public static File getStartup() {
		return STARTUP_DIR;
	}

	private static void writeBat(File file) {
		ListWriter writer = new ListWriter();
		writer.save(getBatContent(), file);
	}

	private static boolean valid() {
		if (!isInstalled()) {
			return false;
		}
		ListReader reader = new ListReader();
		List<String> list = reader.load(STARTUP_FILE);
		if (list.isEmpty()) {
			return false;
		}
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String s : list) {
			if (first) {
				first = false;
			} else {
				builder.append("\r\n");
			}
			builder.append(s);
		}
		return builder.toString().equals(getBatContent());
		
	}

	private static String getBatContent() {
		return getJavaString() + "\"" + FileConstants.getJar().getAbsolutePath() + "\" -startup";
	}

	private static boolean canEdit() {
		return STARTUP_DIR != null && STARTUP_FILE != null && STARTUP_DIR.exists();
	}

	private static File getStartupDirectory() {
		String filename = getStartupString();
		if (filename != null) {
			return new File(filename);
		} else {
			return null;
		}
	}
	private static File getStartupFile(String filename) {
		String filedir = getStartupString();
		if (filedir != null) {
			return new File(filedir + File.separator + filename);
		} else {
			return null;
		}
	}

	private static String getJavaString() {
		return "cd "
				+ System.getProperty("java.home")
				+ File.separator
				+ "bin"
				+ File.separator
				+ "\r\n"
				+ "start javaw -jar ";
	}
	private static String getStartupString() {
		return readRegistry("HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Startup");
		//return readRegistry("HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Common Startup");
		//return null;
	}
    /**
     * 
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     */
    private static String readRegistry(String location, String key){
        try {
            // Run reg query, then read output with StreamReader (internal class)
            Process process = Runtime.getRuntime().exec("reg query " + '"'+ location + "\" /v \"" + key + "\"");

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            // Output has the following format:
            // \n<Version information>\n\n<key>\t<registry type>\t<value>
            if( !output.contains("\t") && !output.contains(" ")){
                  return null;
            }

            // Parse out the value
            String[] parsed = output.split("\\t|\\s{4}");
            return parsed[parsed.length-1];
        }
        catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
			return null;
		}

    }
    private static class StreamReader extends Thread {
        private final InputStream is;
        private final StringWriter sw= new StringWriter();

        public StreamReader(InputStream is) {
            this.is = is;
        }

		@Override
        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            }
            catch (IOException e) { 
        }
        }

        public String getResult() {
            return sw.toString();
        }
    }
}