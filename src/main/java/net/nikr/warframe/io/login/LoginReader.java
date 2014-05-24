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

package net.nikr.warframe.io.login;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoginReader {

	private static final Logger LOG = LoggerFactory.getLogger(LoginReader.class);

	private static final String WARFRAME_PATH = File.separator + "Local" + File.separator + "Warframe" + File.separator + "EE.log";

	public LoginReader() { }

	public Boolean loginRewardAvailible() {
		String env = System.getenv("APPDATA");
		if (env == null) {
			LOG.error("Login Reward: APPDATA is null");
			return null;
		}
		File dateFile = new File(env);
		String s = dateFile.getParentFile().getAbsolutePath() + WARFRAME_PATH;
		File file = new File(s);
		long time = file.lastModified();
		if (time > 0) {
			Date date = new Date(time);
			return date.before(getResetTime());
		} else {
			LOG.error("Login Reward: time is 0 (zero)");
			return null;
		}
	}

	private Date getResetTime() {
		// today
		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		// reset hour, minutes, seconds and millis
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}
	
}
