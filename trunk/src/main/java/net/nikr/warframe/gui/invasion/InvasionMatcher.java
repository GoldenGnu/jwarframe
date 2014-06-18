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
import java.util.Map;
import java.util.Set;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.shared.CategoryFilter;
import net.nikr.warframe.io.invasion.Invasion;


public class InvasionMatcher implements Matcher<Invasion> {
	private final int credits;
	private final boolean killCorpus;
	private final boolean killGrineer;
	private final boolean killInfested;
	private final boolean helpCorpus;
	private final boolean helpGrineer;
	private final Map<String, CategoryFilter> categoryFilters;
	private final Set<String> filters;

	public InvasionMatcher(int credits, boolean killCorpus, boolean killGrineer, boolean killInfested, boolean helpCorpus, boolean helpGrineer, Map<String, CategoryFilter> categoryFilters, Set<String> filters) {
		this.credits = credits;
		this.killCorpus = killCorpus;
		this.killGrineer = killGrineer;
		this.killInfested = killInfested;
		this.helpCorpus = helpCorpus;
		this.helpGrineer = helpGrineer;
		this.categoryFilters = categoryFilters;
		this.filters = filters;
	}

	@Override
	public boolean matches(Invasion invasion) {
	//REWARDS
		boolean invadingReward = true;
		boolean defendinReward = true;
		//Credits
		if (credits == 1) { //25K+
			if (invasion.isInvadingCredits() && invasion.getInvadingCredits() < 25000) {
				invadingReward = false;
			}
			if (invasion.isDefendinCredits() && invasion.getDefendingCredits() < 25000) {
				defendinReward = false;
			}
		}
		if (credits == 2) { //35K+
			if (invasion.isInvadingCredits() && invasion.getInvadingCredits() < 35000) {
				invadingReward = false;
			}
			if (invasion.isDefendinCredits() && invasion.getDefendingCredits() < 35000) {
				defendinReward = false;
			}
		}
		if (credits == 3) { //50K+
			if (invasion.isInvadingCredits() && invasion.getInvadingCredits() < 50000) {
				invadingReward = false;
			}
			if (invasion.isDefendinCredits() && invasion.getDefendingCredits() < 50000) {
				defendinReward = false;
			}
		}
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
		}
		//Done (No match)
		if (invasion.isDone()) {
			defendinReward = false;
			invadingReward = false;
		}
	//FACTIONS
		boolean invadingKillFaction = false;
		boolean defendinKillFaction = false;
		//Kill Corpus
		if (killCorpus) {
			if (invasion.isInvadingCorpus()) {
				defendinKillFaction = true;
			}
			if (invasion.isDefendinCorpus()) {
				invadingKillFaction = true;
			}
		}
		//Kill Grineer
		if (killGrineer) {
			if (invasion.isInvadingGrineer()) {
				defendinKillFaction = true;
			}
			if (invasion.isDefendinGrineer()) {
				invadingKillFaction = true;
			}
		}
		//Kill Infested
		if (killInfested) {
			if (invasion.isInvadingInfested()) {
				defendinKillFaction = true;
			}
			if (invasion.isDefendinInfested()) {
				invadingKillFaction = true;
			}
		}
		boolean invadingHelpFaction = false;
		boolean defendinHelpFaction = false;
		//Help Corpus
		if (helpCorpus) {
			if (invasion.isInvadingCorpus()) {
				invadingHelpFaction = true;
			}
			if (invasion.isDefendinCorpus()) {
				defendinHelpFaction = true;
			}
		}
		//Help Grineer
		if (helpGrineer) {
			if (invasion.isInvadingGrineer()) {
				invadingHelpFaction = true;
			}
			if (invasion.isDefendinGrineer()) {
				defendinHelpFaction = true;
			}
		}
	//UPDATE
		invasion.setMatchDefending(defendinReward && defendinKillFaction && defendinHelpFaction);
		invasion.setMatchInvading(invadingReward && invadingKillFaction && invadingHelpFaction);
		return (invadingReward && invadingKillFaction && invadingHelpFaction)
				|| (defendinReward && defendinKillFaction && defendinHelpFaction);
	}
	
}
