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

package net.nikr.warframe.gui.settings;


public enum SettingsConstants {
	SETTINGS_SET(0, true),
	ALERT_CREDIT_ALL(0, true),
	ALERT_CREDIT_3K(0, false),
	ALERT_CREDIT_5K(0, false),
	ALERT_CREDIT_7K(0, false),
	ALERT_CREDIT_10K(0, false),
	ALERT_CREDIT_20K(0, false),
	ALERT_CREDIT_NONE(0, false),
	INVASION_CREDITS_ALL(0, true),
	INVASION_CREDITS_NONE(0, false),
	INVASION_CREDITS_25K(0, false),
	INVASION_CREDITS_35K(0, false),
	INVASION_CORPUS(0, true),
	INVASION_GRINEER(0, true),
	INVASION_INFESTED(0, true),
	INVASION_HELP_CORPUS(2, true),
	INVASION_HELP_GRINEER(2, true),
	LOGIN_REWARD(1, true),
	NOTIFY_AUDIO(1, true),
	NOTIFY_AUDIO_REPEAT(3, true),
	ALERT_IGNORE_ASSASSINATION(4, false),
	ALERT_IGNORE_DEFENSE(4, false),
	ALERT_IGNORE_EXTERMINATION(4, false),
	ALERT_IGNORE_MOBILE_DEFENSE(4, false),
	ALERT_IGNORE_SABOTAGE(4, false),
	ALERT_IGNORE_SURVIVAL(4, false),
	ALERT_IGNORE_RESCUE(4, false),
	ALERT_IGNORE_EXCAVATION(4, false),
	INVASION_IGNORE_ASSASSINATION(4, false),
	INVASION_IGNORE_DEFENSE(4, false),
	INVASION_IGNORE_EXTERMINATION(4, false),
	INVASION_IGNORE_MOBILE_DEFENSE(4, false),
	INVASION_IGNORE_SURVIVAL(4, false),
	SHOW_POPUP(5, true),
	TRAY_ON_CLOSE(5, false),
	;

	private final int version;
	private final boolean value ;

	private SettingsConstants(int version, boolean value) {
		this.version = version;
		this.value = value;
	}

	public static int getSettingsVersion() {
		return 5;
	}

	public int getVersion() {
		return version;
	}

	public boolean getValue() {
		return value;
	}
}
