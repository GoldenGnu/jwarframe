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

package net.nikr.warframe.gui.invasion;

import java.util.EnumSet;
import java.util.Set;
import net.nikr.warframe.gui.settings.SettingsConstants;


public class InvasionSettings {
	private final int credits;
	private final boolean killCorpus;
	private final boolean killGrineer;
	private final boolean killInfested;
	private final boolean helpCorpus;
	private final boolean helpGrineer;
	private final Set<SettingsConstants> missionTypes;

	public InvasionSettings(int credits, boolean killCorpus, boolean killGrineer, boolean killInfested, boolean helpCorpus, boolean helpGrineer, Set<SettingsConstants> missionTypes) {
		this.credits = credits;
		this.killCorpus = killCorpus;
		this.killGrineer = killGrineer;
		this.killInfested = killInfested;
		this.helpCorpus = helpCorpus;
		this.helpGrineer = helpGrineer;
		this.missionTypes = missionTypes;
	}

	public Set<SettingsConstants> getSettings() {
		Set<SettingsConstants> settings = EnumSet.noneOf(SettingsConstants.class);
		//Credits
		switch (credits) {
			case 0: settings.add(SettingsConstants.INVASION_CREDITS_ALL); break;
			case 1: settings.add(SettingsConstants.INVASION_CREDITS_25K); break;
			case 2: settings.add(SettingsConstants.INVASION_CREDITS_35K); break;
			case 3: settings.add(SettingsConstants.INVASION_CREDITS_NONE); break;
		}
		//Corpus
		if (killCorpus) {
			settings.add(SettingsConstants.INVASION_CORPUS);
		}
		//Grineer
		if (killGrineer) {
			settings.add(SettingsConstants.INVASION_GRINEER);
		}
		//Infested
		if (killInfested) {
			settings.add(SettingsConstants.INVASION_INFESTED);
		}
		if (helpCorpus) {
			settings.add(SettingsConstants.INVASION_HELP_CORPUS);
		}
		if (helpGrineer) {
			settings.add(SettingsConstants.INVASION_HELP_GRINEER);
		}
		settings.addAll(missionTypes);
		return settings;
	}
}
