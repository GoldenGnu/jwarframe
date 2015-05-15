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

import java.util.Set;
import java.util.TreeSet;
import net.nikr.warframe.gui.shared.FilterTool.MissionTypes;


public class AlertMissionTypes  implements MissionTypes {

	private final Set<String> missionTypes;

	public AlertMissionTypes() {
		missionTypes = new TreeSet<String>();
		missionTypes.add("Assassination");
		missionTypes.add("Capture");
		missionTypes.add("Deception");
		missionTypes.add("Defense");
		missionTypes.add("Excavation");
		missionTypes.add("Extermination");
		missionTypes.add("Hive Sabotage");
		missionTypes.add("Interception");
		missionTypes.add("Mobile Defense");
		missionTypes.add("Rescue");
		missionTypes.add("Retrieval");
		missionTypes.add("Sabotage");
		missionTypes.add("Spy");
		missionTypes.add("Survival");
		missionTypes.add("Rescue");
		missionTypes.add("Excavation");
	}

	@Override
	public Set<String> getMissionTypes() {
		return missionTypes;
	}
}
