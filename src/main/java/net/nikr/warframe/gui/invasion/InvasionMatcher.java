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
import java.util.HashSet;
import java.util.Set;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.io.invasion.Invasion;


public class InvasionMatcher implements Matcher<Invasion>{
	private final int credits;
	private final boolean corpus;
	private final boolean grineer;
	private final boolean infested;
	private final boolean blueprints;
	private final boolean mods;
	private final boolean auras;
	private final boolean resources;
	private final boolean filter;
	private final Set<String> filters;

	public InvasionMatcher(int credits, boolean corpus, boolean grineer, boolean infested, boolean blueprints, boolean mods, boolean auras, boolean resources, boolean filter, Set<String> filters) {
		this.credits = credits;
		this.corpus = corpus;
		this.grineer = grineer;
		this.infested = infested;
		this.blueprints = blueprints;
		this.mods = mods;
		this.auras = auras;
		this.resources = resources;
		this.filter = filter;
		this.filters = new HashSet<String>(filters);
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
		if (credits == 3) { //No credits
			if (invasion.isInvadingCredits()) {
				invadingReward = false;
			}
			if (invasion.isDefendinCredits()) {
				defendinReward = false;
			}
		}
		if (invasion.isInfestedInvading()) {
			invadingReward = false;
		}
		//Ignore invading category
		Category invadingCategory =  invasion.getInvadingCategory();
		if (invadingCategory != null) {
			if (!blueprints && invadingCategory.getType().isBlueprint()) {
				invadingReward = false;
			}
			//Ignore Mods
			if (!mods && invadingCategory.getType().isMod()) {
				invadingReward = false;
			}
			//Ignore Aura
			if (!auras && invadingCategory.getType().isAura()) {
				invadingReward = false;
			}
			//Ignore Resources
			if (!resources && invadingCategory.getType().isResource()) {
				invadingReward = false;
			}
		}
		//Ignore defending category
		Category defendingCategory=  invasion.getDefendingCategory();
		if (defendingCategory != null) {
			if (!blueprints && defendingCategory.getType().isBlueprint()) {
				defendinReward = false;
			}
			//Ignore Mods
			if (!mods && defendingCategory.getType().isMod()) {
				defendinReward = false;
			}
			//Ignore Aura
			if (!auras && defendingCategory.getType().isAura()) {
				defendinReward = false;
			}
			//Ignore Resources
			if (!resources && defendingCategory.getType().isResource()) {
				defendinReward = false;
			}
		}
	//FACTIONS
		boolean invadingFaction = false;
		boolean defendinFaction = false;
		//Corpus
		if (corpus) {
			if (invasion.isInvadingCorpus()) {
				defendinFaction = true;
			}
			if (invasion.isDefendinCorpus()) {
				invadingFaction = true;
			}
		}
		//Grineer
		if (grineer) {
			if (invasion.isInvadingGrineer()) {
				defendinFaction = true;
			}
			if (invasion.isDefendinGrineer()) {
				invadingFaction = true;
			}
		}
		//Infested
		if (infested) {
			if (invasion.isInvadingInfested()) {
				defendinFaction = true;
			}
			if (invasion.isDefendinInfested()) {
				invadingFaction = true;
			}
		}
	//FILTER (REWARDS)
		if (filter && invasion.getInvadingRewardID() != null && filters.contains(invasion.getInvadingRewardID().getName())) {
			invadingReward = false;
		}
		if (filter && invasion.getDefendingRewardID() != null && filters.contains(invasion.getDefendingRewardID().getName())) {
			defendinReward = false;
		}
	//Done (No match)
		if (invasion.isDone()) {
			defendinReward = false;
			invadingReward = false;
		}
	//UPDATE
		invasion.setMatchDefending(defendinReward && defendinFaction);
		invasion.setMatchInvading(invadingReward && invadingFaction);
		return (invadingReward && invadingFaction) || (defendinReward && defendinFaction);
	}
	
}
