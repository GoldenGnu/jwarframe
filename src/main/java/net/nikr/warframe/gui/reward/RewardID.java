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

package net.nikr.warframe.gui.reward;


public class RewardID implements Comparable<RewardID> {
	private final String name;
	private final String searchURL;
	private final String directURL;

	public RewardID(String name) {
		this.name = name;
		this.searchURL = "http://warframe.wikia.com/wiki/Special:Search?fulltext=Search&search=" + name.replace(" ", "+");
		this.directURL = "http://warframe.wikia.com/wiki/" + name.replace(" ", "_");
	}

	public String getName() {
		return name;
	}

	public String getSearchURL() {
		return searchURL;
	}

	public String getDirectURL() {
		return directURL;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
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
		final RewardID other = (RewardID) obj;
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(RewardID o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public String toString() {
		return name;
	}
}
