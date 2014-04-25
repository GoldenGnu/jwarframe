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

package net.nikr.warframe.gui.alert;

import ca.odell.glazedlists.matchers.Matcher;
import java.util.HashSet;
import java.util.Set;
import net.nikr.warframe.io.alert.Alert;


public class AlertMatcher implements Matcher<Alert> {

	private final int credits;
	private final boolean blueprints;
	private final boolean mods;
	private final boolean auras;
	private final boolean resources;
	private final boolean filter;
	private final Set<String> filters;

	public AlertMatcher(int credits, boolean blueprints, boolean mods, boolean auras, boolean resources, boolean filter, Set<String> filters) {
		this.credits = credits;
		this.blueprints = blueprints;
		this.mods = mods;
		this.auras = auras;
		this.resources = resources;
		this.filter = filter;
		this.filters = new HashSet<String>(filters);
	}

	@Override
	public boolean matches(Alert alert) {
		//Ignore credits
		if (!alert.hasLoot()) {
			if (credits == 1 && alert.getCredits() < 3000) { //3K
				return false;
			}
			if (credits == 2 && alert.getCredits() < 5000) { //5K
				return false;
			}
			if (credits == 3 && alert.getCredits() < 7000) { //7K
				return false;
			}
			if (credits == 4 && alert.getCredits() < 10000) { //10K
				return false;
			}
			if (credits == 5) { //No credits
				return false;
			}
		}
		//Ignore Blueprints
		if (!blueprints && alert.isBlueprint()) {
			return false;
		}
		//Ignore Mods
		if (!mods && alert.isMod()) {
			return false;
		}
		//Ignore Aura
		if (!auras && alert.isAura()) {
			return false;
		}
		//Ignore Resources
		if (!resources && alert.isResource()) {
			return false;
		}
		//Ignore
		if (filter && alert.hasLoot() && filters.contains(alert.getRewordID().getName())) {
			return false;
		}
		return true;
	}
	
}
