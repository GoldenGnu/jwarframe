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
import net.nikr.warframe.io.invasion.Invasion;


public class InvasionMatcher implements Matcher<Invasion>{
	private final int credits;
	private final boolean corpus;
	private final boolean grineer;
	private final boolean infested;
	private final Set<String> filters;

	public InvasionMatcher(int credits, boolean corpus, boolean grineer, boolean infested, Set<String> filters) {
		this.credits = credits;
		this.corpus = corpus;
		this.grineer = grineer;
		this.infested = infested;
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
		if (invasion.getInvadingRewardID() != null && filters.contains(invasion.getInvadingRewardID().getName())) {
			invadingReward = false;
		}
		if (invasion.getDefendingRewardID() != null && filters.contains(invasion.getDefendingRewardID().getName())) {
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
