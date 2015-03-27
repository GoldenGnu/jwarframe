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

package net.nikr.warframe.io.invasion;

import java.util.Date;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.io.shared.Faction;


public class Invasion implements Comparable<Invasion> {
	private String id = null;
	private String node = null;
	private String region = null;
	private String invadingFaction = null;
	private String invadingMissionType = null;
	private String invadingReward = null;
	private RewardID invadingRewardID = null;
	private Category invadingCategory = null;
	private Integer invadingCredits = null;
	private String invadingLevelRange = null;
	private String defendingFaction = null;
	private String defendingMissionType = null;
	private String defendingReward = null;
	private RewardID defendingRewardID = null;
	private Category defendingCategory = null;
	private Integer defendingCredits = null;
	private String defendingLevelRange = null;
	private Date activationTime = null;
	private String count = null;
	private String goal = null;
	private InvasionPercentage percentage = null;
	private String eta = null;
	private String description = null;

	private boolean matchInvadingLoot;
	private boolean matchDefendingLoot;
	private boolean matchInvadingCredits;
	private boolean matchDefendingCredits;
	private boolean done = false;

	public Invasion() { }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getInvadingFaction() {
		return invadingFaction;
	}

	public void setInvadingFaction(String invadingFaction) {
		this.invadingFaction = invadingFaction;
	}

	public String getInvadingMissionType() {
		return invadingMissionType;
	}

	public void setInvadingMissionType(String invadingMissionType) {
		this.invadingMissionType = invadingMissionType;
	}

	public String getInvadingReward() {
		return invadingReward;
	}

	public void setInvadingReward(String invadingReward) {
		this.invadingReward = invadingReward;
	}

	public RewardID getInvadingRewardID() {
		return invadingRewardID;
	}

	public void setInvadingRewardID(RewardID invadingRewardID) {
		this.invadingRewardID = invadingRewardID;
	}

	public Category getInvadingCategory() {
		return invadingCategory;
	}

	public void setInvadingCategory(Category invadingCategory) {
		this.invadingCategory = invadingCategory;
	}

	public Integer getInvadingCredits() {
		return invadingCredits;
	}

	public void setInvadingCredits(Integer invadingCredits) {
		this.invadingCredits = invadingCredits;
		if (invadingCredits != null) {
			invadingReward = invadingCredits.toString();
		}
	}

	public String getInvadingLevelRange() {
		return invadingLevelRange;
	}

	public void setInvadingLevelRange(String invadingLevelRange) {
		this.invadingLevelRange = invadingLevelRange;
	}

	public String getDefendingFaction() {
		return defendingFaction;
	}

	public void setDefendingFaction(String defendingFaction) {
		this.defendingFaction = defendingFaction;
	}

	public String getDefendingMissionType() {
		return defendingMissionType;
	}

	public void setDefendingMissionType(String defendingMissionType) {
		this.defendingMissionType = defendingMissionType;
	}

	public String getDefendingReward() {
		return defendingReward;
	}

	public void setDefendingReward(String defendingReward) {
		this.defendingReward = defendingReward;
	}

	public RewardID getDefendingRewardID() {
		return defendingRewardID;
	}

	public void setDefendingRewardID(RewardID defendingRewardID) {
		this.defendingRewardID = defendingRewardID;
	}

	public Category getDefendingCategory() {
		return defendingCategory;
	}

	public void setDefendingCategory(Category defendingCategory) {
		this.defendingCategory = defendingCategory;
	}

	public Integer getDefendingCredits() {
		return defendingCredits;
	}

	public void setDefendingCredits(Integer defendingCredits) {
		this.defendingCredits = defendingCredits;
		if (defendingCredits != null) {
			defendingReward = defendingCredits.toString();
		}
	}

	public String getDefendingLevelRange() {
		return defendingLevelRange;
	}

	public void setDefendingLevelRange(String defendingLevelRange) {
		this.defendingLevelRange = defendingLevelRange;
	}

	public Date getActivationTime() {
		return activationTime;
	}

	public void setActivationTime(Date activationTime) {
		this.activationTime = activationTime;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public InvasionPercentage getPercentage() {
		return percentage;
	}

	public void setPercentage(InvasionPercentage percentage) {
		this.percentage = percentage;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isMatchInvadingLoot() {
		return matchInvadingLoot;
	}

	public void setMatchInvadingLoot(boolean matchInvadingLoot) {
		this.matchInvadingLoot = matchInvadingLoot;
	}

	public boolean isMatchDefendingLoot() {
		return matchDefendingLoot;
	}

	public void setMatchDefendingLoot(boolean matchDefendingLoot) {
		this.matchDefendingLoot = matchDefendingLoot;
	}

	public boolean isMatchInvadingCredits() {
		return matchInvadingCredits;
	}

	public void setMatchInvadingCredits(boolean matchInvadingCredits) {
		this.matchInvadingCredits = matchInvadingCredits;
	}

	public boolean isMatchDefendingCredits() {
		return matchDefendingCredits;
	}

	public void setMatchDefendingCredits(boolean matchDefendingCredits) {
		this.matchDefendingCredits = matchDefendingCredits;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public String getLocation() {
		return node + " (" + region + ")";
	}

	public boolean isInvadingCredits() {
		return invadingCredits != null;
	}

	public boolean isDefendinCredits() {
		return defendingCredits != null;
	}

	public boolean isInfestedInvading() {
		return invadingFaction.toLowerCase().equals("infestation");
	}

	public boolean isInvadingCorpus() {
		return invadingMatchFaction("corpus");
	}

	public boolean isDefendinCorpus() {
		return defendinMatchFaction("corpus");
	}

	public boolean isInvadingGrineer() {
		return invadingMatchFaction("grineer");
	}

	public boolean isDefendinGrineer() {
		return defendinMatchFaction("grineer");
	}

	public boolean isInvadingInfested() {
		return invadingMatchFaction("infestation");
	}

	public boolean isDefendinInfested() {
		return defendinMatchFaction("infestation");
	}

	private boolean invadingMatchReward(String search) {
		return invadingReward.toLowerCase().contains(search.toLowerCase());
	}

	private boolean defendinMatchReward(String search) {
		return defendingReward.toLowerCase().contains(search.toLowerCase());
	}

	private boolean invadingMatchFaction(String search) {
		return invadingFaction.toLowerCase().contains(search.toLowerCase());
	}

	private boolean defendinMatchFaction(String search) {
		return defendingFaction.toLowerCase().contains(search.toLowerCase());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Invasion other = (Invasion) obj;
		if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Invasion invasion) {
		return getId().compareTo(invasion.getId());
	}

	public static class InvasionPercentage {
		private final float percentage;
		private final Faction invading;
		private final Faction defending;

		public InvasionPercentage(float percentage, Faction invading, Faction defending) {
			this.percentage = percentage;
			this.invading = invading;
			this.defending = defending;
		}

		public float getPercentage() {
			return percentage;
		}

		public Faction getInvading() {
			return invading;
		}

		public Faction getDefending() {
			return defending;
		}

		@Override
		public String toString() {
			return percentage + "%";
		}
	}
}
