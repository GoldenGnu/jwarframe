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

package net.nikr.warframe.io.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.io.invasion.Invasion.InvasionPercentage;
import net.nikr.warframe.io.shared.Faction;
import net.nikr.warframe.io.shared.StringProcessor;


public class InvasionProcessor extends StringProcessor {

	public InvasionProcessor() { }

	public List<Invasion> process(List<String> raw, List<Category> categories, Set<String> done) {
		List<Invasion> invasions = new ArrayList<Invasion>();
		for (String line : raw) {
			int count = line.length() - line.replace("|", "").length();
			if (count >= 18) {
				invasions.add(process(line, categories, done));
			}
		}
		return invasions;
	}

	public Invasion process(String raw, List<Category> categories, Set<String> done) {
		Invasion invasion = new Invasion();
		String[] arr = raw.split("\\|");
		int count = 0;
		invasion.setId(getString(arr, count));
		invasion.setNode(getString(arr, ++count));
		invasion.setRegion(getString(arr, ++count));
	//Invading
		invasion.setInvadingFaction(getString(arr, ++count));
		invasion.setInvadingMissionType(getString(arr, ++count));
		String invadingReward = getString(arr, ++count);
		invasion.setInvadingReward(invadingReward);
		RewardID invadingRewardID = getRewardID(invadingReward);
		invasion.setInvadingRewardID(invadingRewardID);
		invasion.setInvadingCategory(getCategory(invadingRewardID, categories));
		invasion.setInvadingLevelRange(getString(arr, ++count));
		count++; //Skip AI
	//Defending
		invasion.setDefendingFaction(getString(arr, ++count));
		invasion.setDefendingMissionType(getString(arr, ++count));
		String defendingReward = getString(arr, ++count);
		invasion.setDefendingReward(defendingReward);
		RewardID defendingRewardID = getRewardID(defendingReward);
		invasion.setDefendingRewardID(defendingRewardID);
		invasion.setDefendingCategory(getCategory(defendingRewardID, categories));
		invasion.setDefendingLevelRange(getString(arr, ++count));
		count++; //Skip AI
		invasion.setActivationTime(getDate(arr, ++count));
		invasion.setCount(getString(arr, ++count));
		invasion.setGoal(getString(arr, ++count));
		invasion.setPercentage(new InvasionPercentage(getFloat(arr, ++count), Faction.getFaction(invasion.getInvadingFaction()), Faction.getFaction(invasion.getDefendingFaction())));
		invasion.setEta(getString(arr, ++count));
		invasion.setDescription(getString(arr, ++count));
		
		invasion.setInvadingCredits(getCredits(invasion.getInvadingReward()));
		invasion.setDefendingCredits(getCredits(invasion.getDefendingReward()));

		invasion.setDone(done.contains(invasion.getId()));
		return invasion;
	}

	private Integer getCredits(String s) {
		try {
			Integer i = Integer.valueOf(s.replace("cr", "").replace(",", "").trim());
			if (i > 0) {
				return i;
			} else {
				return null;
			}
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
}
