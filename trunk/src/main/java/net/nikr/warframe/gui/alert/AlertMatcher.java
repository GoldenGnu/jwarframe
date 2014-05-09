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
import java.util.Map;
import java.util.Set;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.shared.CategoryFilter;
import net.nikr.warframe.io.alert.Alert;


public class AlertMatcher implements Matcher<Alert> {

	private final int credits;
	private final Map<String, CategoryFilter> categoryFilters;
	private final Set<String> filters;

	public AlertMatcher(int credits, Map<String, CategoryFilter> categoryFilters, Set<String> filters) {
		this.credits = credits;
		this.categoryFilters = categoryFilters;
		this.filters = filters;
	}

	@Override
	public boolean matches(Alert alert) {
		boolean matches = matchesIt(alert);
		alert.setMatch(matches);
		return matches;
	}

	public boolean matchesIt(Alert alert) {
		//Ignore credits
		if (alert.isDone()) {
			return false;
		}
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
		//Category
		if (alert.hasLoot()) {
			Category category = alert.getCategory();
			CategoryFilter filter = null;
			if (category != null) { //If category is known
				filter = categoryFilters.get(category.getName());
			}
			if (filter == null) { //Fallback on filters
				filter = CategoryFilter.FILTERS;
			}
			switch (filter) {
				case ALL:
					return true;
				case FILTERS:
					return !filters.contains(alert.getRewordID().getName());
				case NONE:
					return false;
			}
		}
		return true;
	}
	
}