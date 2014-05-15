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

package net.nikr.warframe.io.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.io.alert.Alert.Mission;
import net.nikr.warframe.io.shared.StringProcessor;


public class AlertProcessor extends StringProcessor {

	public AlertProcessor() { }

	public List<Alert> process(List<String> raw, List<Category> categories, Set<String> done) {
		List<Alert> alerts = new ArrayList<Alert>();
		for (String line : raw) {
			int count = line.length() - line.replace("|", "").length();
			if (count >= 10) {
				alerts.add(process(line, categories, done));
			}
		}
		return alerts;
	}

	public Alert process(String raw, List<Category> categories, Set<String> done) {
		Alert alert = new Alert();
		String[] arr = raw.split("\\|");
		int count = 0;
		alert.setId(getString(arr, count));
		alert.setNode(getString(arr, ++count));
		alert.setRegion(getString(arr, ++count));
		String mission = getString(arr, ++count);
		String faction = getString(arr, ++count);
		alert.setMission(new Mission(mission, faction));
		alert.setMinLevel(getString(arr, ++count));
		alert.setMaxLevel(getString(arr, ++count));
		alert.setActivation(getDate(arr, ++count));
		alert.setExpiry(getDate(arr, ++count));
		String reward = getString(arr, ++count);
		alert.setCredits(getCredits(reward));
		String loot = getLoot(reward);
		alert.setLoot(loot);
		RewardID rewardID = getRewardID(loot);
		alert.setCategory(getCategory(rewardID, categories));
		alert.setRewardID(rewardID);
		alert.setDescription(getString(arr, ++count));

		alert.setDone(done.contains(alert.getId()));
		return alert;
	}

	private Integer getCredits(String s) {
		int start = s.indexOf(" - ");
		if (start >= 0) {
			s = s.substring(0, start);
		}
		String number = s.replaceAll("\\D", "");
		return Integer.valueOf(number);
	}

	private String getLoot(String s) {
		int start = s.indexOf(" - ") + 3;
		if (start >= 3 && start < s.length()) {
			s = s.substring(start);
			return s;
		} else {
			return null;
		}
	}
}
