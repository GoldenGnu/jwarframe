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

package net.nikr.warframe.io.shared;

import java.util.Date;
import java.util.List;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;


public class StringProcessor {
	protected String getString(String[] arr, int index) {
		if (arr.length > index) {
			return arr[index];
		} else {
			return null;
		}
	}

	protected Date getDate(String[] arr, int index) {
		try {
			String d = getString(arr, index);
			return new Date(Integer.valueOf(d) * 1000L);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	protected Float getFloat(String[] arr, int index) {
		try {
			String d = getString(arr, index);
			return Float.valueOf(d);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	protected RewardID getRewardID(String loot) {
		if (loot == null) {
			return null;
		}
		String reward = loot.replace("Blueprint", "")
					.replace("Skin", "")
					.replace(",", "")
					.replaceAll("\\d", "")
					.trim();
		if (reward.equals("cr")) {
			return null;
		} else {
			return new RewardID(reward);
		}
	}

	protected Category getCategory(RewardID rewardID, List<Category> categories) {
		if (rewardID == null) {
			return null;
		}
		for (Category category : categories) {
			for (RewardID reward : category.getRewards()) {
				if (rewardID.equals(reward)) {
					return category;
				}
			}
		}
		return null;
	}
}
