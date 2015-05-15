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

package net.nikr.warframe.gui.alert;

import java.util.EnumSet;
import java.util.Set;
import net.nikr.warframe.gui.settings.SettingsConstants;


public class AlertSettings {
	private final int credits;

	public AlertSettings(int credits) {
		this.credits = credits;
	}

	public Set<SettingsConstants> getSettings() {
		Set<SettingsConstants> settings = EnumSet.noneOf(SettingsConstants.class);
		//Credits
		switch (credits) {
			case 0: settings.add(SettingsConstants.ALERT_CREDIT_ALL); break;
			case 1: settings.add(SettingsConstants.ALERT_CREDIT_3K); break;
			case 2: settings.add(SettingsConstants.ALERT_CREDIT_5K); break;
			case 3: settings.add(SettingsConstants.ALERT_CREDIT_7K); break;
			case 4: settings.add(SettingsConstants.ALERT_CREDIT_10K); break;
			case 5: settings.add(SettingsConstants.ALERT_CREDIT_20K); break;
			case 6: settings.add(SettingsConstants.ALERT_CREDIT_NONE); break;
		}
		return settings;
	}
}
