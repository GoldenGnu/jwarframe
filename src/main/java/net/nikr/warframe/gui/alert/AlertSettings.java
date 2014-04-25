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

package net.nikr.warframe.gui.alert;

import java.util.EnumSet;
import java.util.Set;
import net.nikr.warframe.gui.settings.SettingsConstants;


public class AlertSettings {
	private final int credits;
	private final boolean blueprints;
	private final boolean mods;
	private final boolean auras;
	private final boolean resources;
	private final boolean filters;

	public AlertSettings(int credits, boolean blueprints, boolean mods, boolean auras, boolean resources, boolean filters) {
		this.credits = credits;
		this.blueprints = blueprints;
		this.mods = mods;
		this.auras = auras;
		this.resources = resources;
		this.filters = filters;
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
			case 5: settings.add(SettingsConstants.ALERT_CREDIT_NONE); break;
		}
		//Blueprints
		if (blueprints) {
			settings.add(SettingsConstants.ALERT_BLUEPRINT);
		}
		//Mods
		if (mods) {
			settings.add(SettingsConstants.ALERT_MOD);
		}
		//Aura
		if (auras) {
			settings.add(SettingsConstants.ALERT_AURA);
		}
		//Resources
		if (resources) {
			settings.add(SettingsConstants.ALERT_RESOURCE);
		}
		//Filters
		if (filters) {
			settings.add(SettingsConstants.ALERT_FILTERS);
		}
		return settings;
	}
}
