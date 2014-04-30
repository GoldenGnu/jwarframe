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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import net.nikr.warframe.gui.reward.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RewardsGetter {

	private static final Logger LOG = LoggerFactory.getLogger(RewardsGetter.class);

	public RewardsGetter() { }

	public void checkUpdates() {
		String local = getLocalVersion();
		String online = getOnlineVersion();
		if (online != null && (local == null || !local.equals(online))) {
			update(online);
		}
	}

	private String getLocalVersion() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(FileConstants.getVersionLocal()));
			String str;
			while ((str = in.readLine()) != null) {
				return str;
			}
		} catch (IOException e) {
			
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					//I give up
				}
			}
		}
		return null;
	}

	private String getOnlineVersion() {
		InputStream is = null;
		try {
			URL url = new URL(FileConstants.getVersionOnline());
			is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				return line;
			}
		} catch (MalformedURLException ex) {
			 LOG.error("Bad url", ex);
		} catch (IOException ex) {
			 LOG.error("Error connecting", ex);
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException ioe) {
				// nothing to see here
			}
		}
		return null;
	}

	private void update(String version) {
		//Delete old data files...
		File data = FileConstants.getDataDirectory();
		FileConstants.deleteDirectory(data);
		//Get new categories online
		boolean updated = updateRewards(FileConstants.getCategoriesLocal(), FileConstants.getCategoriesOnline());
		if (updated) {
			//Get data files online
			ListReader reader = new ListReader();
			List<String> alertData = reader.load(FileConstants.getCategoriesLocal());
			for (String s : alertData) {
				Category category = new Category(s);
				updated = updated && updateRewards(FileConstants.getCategoryLocal(category.getFilename()), FileConstants.getCategoryOnline(category.getFilename()));
			}
		}
		if (updated) {
			//Update version file
			writeData(version);
			LOG.info("Data updated to " + version);
			//Delete old images
			File images = FileConstants.getImageDirectory();
			FileConstants.deleteDirectory(images);
		}
	}

	private void writeData(String online) {
		ListWriter writer = new ListWriter();
		writer.save(Collections.singletonList(online), FileConstants.getVersionLocal());
	}

	private boolean updateRewards(File local, String online) {
		ListGetter getter = new ListGetter();
		List<String> onlineList = getter.get(online);
		if (onlineList.isEmpty()) {
			return false;
		}
		ListWriter writer = new ListWriter();
		return writer.save(onlineList, local);
	}
}
