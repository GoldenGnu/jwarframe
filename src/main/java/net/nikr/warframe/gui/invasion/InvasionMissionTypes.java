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

import java.util.Map;
import java.util.TreeMap;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.FilterTool.MissionTypes;


public class InvasionMissionTypes implements MissionTypes {
	private final Map<String, SettingsConstants> missionTypes;

	public InvasionMissionTypes() {
		missionTypes = new TreeMap<String, SettingsConstants>();
		missionTypes.put("Assassination", SettingsConstants.INVASION_IGNORE_ASSASSINATION);
		missionTypes.put("Defense", SettingsConstants.INVASION_IGNORE_DEFENSE);
		missionTypes.put("Extermination", SettingsConstants.INVASION_IGNORE_EXTERMINATION);
		missionTypes.put("Mobile Defense", SettingsConstants.INVASION_IGNORE_MOBILE_DEFENSE);
		missionTypes.put("Survival", SettingsConstants.INVASION_IGNORE_SURVIVAL);
	}

	@Override
	public Map<String, SettingsConstants> getMissionTypes() {
		return missionTypes;
	}
}
