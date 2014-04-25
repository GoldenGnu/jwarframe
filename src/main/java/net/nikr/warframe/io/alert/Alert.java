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

import java.util.Date;
import java.util.Set;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.Category.CategoryType;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.io.shared.Faction;


public class Alert implements Comparable<Alert>{
	private String id;
	private String node;
	private String region;
	private Mission mission;
	private String minLevel;
	private String maxLevel;
	private Date activation; // (unix ts)
	private Date expiry; //(unix ts)
	private Integer credits; //Rewards (seperated with " - ")
	private String loot; //Rewards (seperated with " - ")
	private RewardID rewardID; //Rewards (seperated with " - ")
	private Category category; //Rewards (seperated with " - ")
	private String description;

	private boolean ignored;
	private boolean done;

	public Alert() { }

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

	public Mission getMission() {
		return mission;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public String getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(String minLevel) {
		this.minLevel = minLevel;
	}

	public String getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(String maxLevel) {
		this.maxLevel = maxLevel;
	}

	public Date getActivation() {
		return activation;
	}

	public void setActivation(Date activation) {
		this.activation = activation;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public Integer getCredits() {
		return credits;
	}

	public void setCredits(Integer credits) {
		this.credits = credits;
	}

	public String getLoot() {
		return loot;
	}

	public void setLoot(String loot) {
		this.loot = loot;
	}

	public RewardID getRewordID() {
		return rewardID;
	}

	public void setRewardID(RewardID lootID) {
		this.rewardID = lootID;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean isIgnored() {
		return ignored;
	}

	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	public void setIgnored(Set<String> filters) {
		if (rewardID == null) {
			ignored = false;
		} else {
			ignored = filters.contains(rewardID.getName());
		}
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

	public boolean isMod() {
		return getCategoryName().toLowerCase().contains("mod");
	}
	public boolean isAura() {
		return getCategoryName().toLowerCase().contains("aura");
	}
	public boolean isBlueprint() {
		return getCategoryName().toLowerCase().contains("blueprint");
	}
	public boolean isResource() {
		return getCategoryName().toLowerCase().contains("resource");
	}
	public boolean isExpired() {
		return expiry.before(new Date());
	}
	public boolean hasLoot() {
		return loot != null;
	}
	public String getCategoryName() {
		if (category != null) {
			return category.getType().getName();
		} else if (hasLoot()) {
			return CategoryType.GRAY.getName();
		} else {
			return "";
		}
	}
	
	public String getTimeLeft() {
		long time = expiry.getTime() - new Date().getTime();
		String timeLeft = "";
		long days = time / (24 * 60 * 60 * 1000);
		
		long hours = time / (60 * 60 * 1000) % 24;
		
		long minutes = time / (60 * 1000) % 60;
		if (minutes != 0) {
			timeLeft = Math.abs(minutes) + " minute";
			if (Math.abs(minutes) > 1) {
				timeLeft = timeLeft + "s";
			}
		}
		if (hours != 0) {
			timeLeft = Math.abs(hours) + " hour";
			if (Math.abs(hours) > 1) {
				timeLeft = timeLeft + "s";
			}
		}
		if (days != 0) {
			timeLeft = Math.abs(days) + " day";
			if (Math.abs(days) > 1) {
				timeLeft = timeLeft + "s";
			}
		}
		if (days == 0 && hours == 0 && minutes == 0) {
			return "Less than a minute";
		} else {
			if (time > 0) {
				return timeLeft;
			} else {
				return timeLeft + " ago";
			}
		}
	}

	@Override
	public int compareTo(Alert alert) {
		return alert.expiry.compareTo(expiry);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + (this.id != null ? this.id.hashCode() : 0);
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
		final Alert other = (Alert) obj;
		if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public static class Mission {
		private final String mission;
		private final Faction faction;

		public Mission(String mission, String faction) {
			this.mission = mission;
			this.faction = Faction.getFaction(faction);
		}

		public String getMission() {
			return mission;
		}

		public Faction getFaction() {
			return faction;
		}

		@Override
		public String toString() {
			return mission;
		}
	}
}
