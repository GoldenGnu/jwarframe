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

import ca.odell.glazedlists.matchers.Matcher;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.nikr.warframe.gui.invasion.InvasionTool.KillHelp;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.shared.CategoryFilter;
import net.nikr.warframe.io.invasion.Invasion;


public class InvasionMatcher implements Matcher<Invasion> {
	private final int credits;
	private final Map<String, CategoryFilter> categoryFilters;
	private final Set<String> filters;
	private final Set<String> filterMissionTypes;
	private final String toolName;
	private final Map<String, Set<KillHelp>> killHelp;

	public InvasionMatcher(int credits, Map<String, CategoryFilter> categoryFilters, Set<String> filters, Set<String> filterMissionTypes, String toolName, Map<String, Set<KillHelp>> killHelp) {
		this.credits = credits;
		this.categoryFilters = categoryFilters;
		this.filters = filters;
		this.filterMissionTypes = filterMissionTypes;
		this.toolName = toolName;
		this.killHelp = killHelp;
	}

	@Override
	public boolean matches(Invasion invasion) {
		boolean invadingCredits = invasion.isInvadingCredits();
		boolean defendinCredits = invasion.isDefendinCredits();
	//CREDITS
		if (credits == 1) { //25K+
			if (invasion.isInvadingCredits() && invasion.getInvadingCredits() < 25000) {
				invadingCredits = false;
			}
			if (invasion.isDefendinCredits() && invasion.getDefendingCredits() < 25000) {
				defendinCredits = false;
			}
		}
		if (credits == 2) { //35K+
			if (invasion.isInvadingCredits() && invasion.getInvadingCredits() < 35000) {
				invadingCredits = false;
			}
			if (invasion.isDefendinCredits() && invasion.getDefendingCredits() < 35000) {
				defendinCredits = false;
			}
		}
		if (credits == 3) { //50K+
			if (invasion.isInvadingCredits() && invasion.getInvadingCredits() < 50000) {
				invadingCredits = false;
			}
			if (invasion.isDefendinCredits() && invasion.getDefendingCredits() < 50000) {
				defendinCredits = false;
			}
		}
	//REWARD
		boolean invadingReward = false;
		boolean defendinReward = false;
		//Ignore invading category
		if (invasion.getInvadingRewardID() != null) {
			Category category = invasion.getInvadingCategory();
			CategoryFilter filter = null;
			if (category != null) { //If category is known
				filter = categoryFilters.get(category.getName());
			}
			if (filter == null) { //Fallback on filters
				filter = CategoryFilter.FILTERS;
			}
			switch (filter) {
				case ALL:
					invadingReward = true;
					break;
				case FILTERS:
					invadingReward = !filters.contains(invasion.getInvadingRewardID().getName());
					break;
				case NONE:
					invadingReward = false;
					break;
			}
		}
		//Ignore defending category
		if (invasion.getDefendingRewardID() != null) {
			Category category = invasion.getDefendingCategory();
			CategoryFilter filter = null;
			if (category != null) { //If category is known
				filter = categoryFilters.get(category.getName());
			}
			if (filter == null) { //Fallback on filters
				filter = CategoryFilter.FILTERS;
			}
			switch (filter) {
				case ALL:
					defendinReward = true;
					break;
				case FILTERS:
					defendinReward = !filters.contains(invasion.getDefendingRewardID().getName());
					break;
				case NONE:
					defendinReward = false;
					break;
			}
		}
		//Can not support infested...
		if (invasion.isInfestedInvading()) {
			invadingReward = false;
			invadingCredits = false;
		}
		//Done (No match)
		if (invasion.isDone()) {
			defendinReward = false;
			invadingReward = false;
			invadingCredits = false;
			defendinCredits = false;
		}
	//FACTIONS
		boolean invadingKillFaction = false;
		boolean defendinKillFaction = false;
		boolean invadingHelpFaction = false;
		boolean defendinHelpFaction = false;
		if (invasion.getInvadingCategory() != null) {
			Set<KillHelp> killHelpValues = EnumSet.noneOf(KillHelp.class);
			killHelpValues.addAll(killHelp.get(invasion.getInvadingCategory().getName()));
			for (KillHelp help : killHelpValues) {
				switch (help) {
					case HELP_CORPUS:
						if (invasion.isInvadingCorpus()) {
							invadingHelpFaction = true;
						}
						break;
					case HELP_GRINEER:
						if (invasion.isInvadingGrineer()) {
							invadingHelpFaction = true;
						}
						break;
					case KILL_CORPUS:
						if (invasion.isDefendinCorpus()) {
							invadingKillFaction = true;
						}
						break;
					case KILL_GRINEER:
						if (invasion.isDefendinGrineer()) {
							invadingKillFaction = true;
						}
						break;
					case KILL_INFESTATION:
						if (invasion.isDefendinInfested()) {
							invadingKillFaction = true;
						}
						break;
				}
			}
		} else {
			invadingKillFaction = true;
			invadingHelpFaction = true;
		}
		if (invasion.getDefendingCategory() != null) {
			Set<KillHelp> killHelpValues = EnumSet.noneOf(KillHelp.class);
			killHelpValues.addAll(killHelp.get(invasion.getDefendingCategory().getName()));
			for (KillHelp help : killHelpValues) {
				switch (help) {
					case HELP_CORPUS:
						if (invasion.isDefendinCorpus()) {
							defendinHelpFaction = true;
						}
						break;
					case HELP_GRINEER:
						if (invasion.isDefendinGrineer()) {
							defendinHelpFaction = true;
						}
						break;
					case KILL_CORPUS:
						if (invasion.isInvadingCorpus()) {
							defendinKillFaction = true;
						}
						break;
					case KILL_GRINEER:
						if (invasion.isInvadingGrineer()) {
							defendinKillFaction = true;
						}
						break;
					case KILL_INFESTATION:
						if (invasion.isInvadingInfested()) {
							defendinKillFaction = true;
						}
						break;
				}
			}
		} else {
			defendinKillFaction = true;
			defendinHelpFaction = true;
		}
	//MISSION TYPE
		boolean invadingMissionType = true;
		boolean defendinMissionType = true;
		if (invasion.getInvadingCategory() != null) {
			String key = toolName + invasion.getInvadingCategory().getName() + invasion.getInvadingMissionType();
			if (filterMissionTypes.contains(key)) {
				invadingMissionType = false;
			}
		}

		if (invasion.getDefendingCategory() != null) {
			String key = toolName + invasion.getDefendingCategory().getName() + invasion.getDefendingMissionType();
			if (filterMissionTypes.contains(key)) {
				defendinMissionType = false;
			}
		}
	//UPDATE
		invasion.setMatchInvadingLoot(invadingReward && invadingKillFaction && invadingHelpFaction && invadingMissionType);
		invasion.setMatchDefendingLoot(defendinReward && defendinKillFaction && defendinHelpFaction && defendinMissionType);
		invasion.setMatchInvadingCredits(invadingCredits);
		invasion.setMatchDefendingCredits(defendinCredits);
		return ((invadingReward && invadingKillFaction && invadingHelpFaction && invadingMissionType) || invadingCredits)
				|| ((defendinReward && defendinKillFaction && defendinHelpFaction && defendinMissionType) || defendinCredits);
	}
	
}
