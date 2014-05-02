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

package net.nikr.warframe.io.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import net.nikr.warframe.Program;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ListGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Updater {
	private static final Logger LOG = LoggerFactory.getLogger(Updater.class);

	public void update() {
		LOG.info("Checking online version");
		VersionGetter getter = new VersionGetter();
		String version = getter.get();
		LOG.info("Online: " + version + " Local: " + Program.PROGRAM_VERSION);
		if (version != null && !version.equals(Program.PROGRAM_VERSION)) {
			int value = JOptionPane.showConfirmDialog(null, "Update jWarframe now?", "Update Available", JOptionPane.OK_CANCEL_OPTION);
			if (value == JOptionPane.OK_OPTION) {
				LOG.info("Updating program");
				runUpdate();
			}
		}
	}

	private void runUpdate() {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(getJavaHome());
		processBuilder.command(getArgsString());
		try {
			Process process = processBuilder.start();
			System.exit(0);
			/*
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ( (line = br.readLine()) != null) {
			   builder.append(line);
			   builder.append(System.getProperty("line.separator"));
			}
			System.out.println(builder.toString());
			*/
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private File getJavaHome() {
		return new File(System.getProperty("java.home") + File.separator + "bin");
	}

	private List<String> getArgsString() {
		List<String> list = new ArrayList<String>();
		list.add("java");
		list.add("-jar");
		list.add(FileConstants.getUpdateJar());
		list.add("http://warframe.nikr.net/jwarframe/download/update/");
		list.add(FileConstants.getJar().getAbsolutePath());
		return list;
	}

	private static class VersionGetter extends ListGetter {

		protected String get() {
			List<String> list = get("http://warframe.nikr.net/jwarframe/download/update/update_version.dat");
			if (list.size() == 1) {
				return list.get(0);
			} else {
				return null;
			}
		}
		
	}
}
