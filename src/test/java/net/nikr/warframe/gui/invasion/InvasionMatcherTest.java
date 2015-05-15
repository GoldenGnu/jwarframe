/*
 * Copyright 2014-2015 Niklas Kyster Rasmussen
 *
 * This file is part of jWarframe.
 *
 * Original code from jEveAssets (https://code.google.com/p/jeveassets/)
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.nikr.warframe.gui.invasion.InvasionTool.KillHelp;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.gui.shared.CategoryFilter;
import net.nikr.warframe.io.invasion.Invasion;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Niklas
 */
public class InvasionMatcherTest {
	private final String CORPUS = "corpus";
	private final String GRINEER = "grineer";
	private final String INFESTATION = "infestation";
	
	public InvasionMatcherTest() { }

	@Test
	public void testReward() {
		Set<String> filters = new HashSet<String>();
		filters.add("New");
		testReward(null, null, filters, false);
		testReward("New", "New", filters, false);
		testReward("New", null, filters, false);
		testReward(null, "New", filters, false);

		filters.clear();
		testReward("New", null, filters, true);
		testReward(null, "New", filters, true);
		testReward("New", "New", filters, true);
	}

	public void testReward(String defendingReward, String invadingReward, Set<String> filters, boolean match) {
		Map<String, CategoryFilter> categoryFilters = new HashMap<String, CategoryFilter>();
		Set<String> filterMissionTypes = new HashSet<String>();
		Map<String, Set<KillHelp>> killHelp = new HashMap<String, Set<KillHelp>>();
		
		Invasion invasion = new Invasion();
		if (defendingReward != null) {
			invasion.setDefendingRewardID(new RewardID(defendingReward));
		} else {
			invasion.setDefendingRewardID(null);
		}
		if (invadingReward != null) {
			invasion.setInvadingRewardID(new RewardID(invadingReward));
		} else {
			invasion.setInvadingRewardID(null);
		}
		invasion.setInvadingFaction("unknown");
		InvasionMatcher invasionMatcher = new InvasionMatcher(5, categoryFilters, filters, filterMissionTypes, "", killHelp);
		assertEquals(match, invasionMatcher.matches(invasion));
	}

	@Test
	public void testKillHelp() {
		Map<String, Set<KillHelp>> killHelp = new HashMap<String, Set<KillHelp>>();
		EnumSet<KillHelp> invading = EnumSet.allOf(KillHelp.class);
		EnumSet<KillHelp> defending = EnumSet.allOf(KillHelp.class);
		killHelp.put("Invading", invading);
		killHelp.put("Defending", defending);

		invading.clear();
		invading.add(KillHelp.HELP_CORPUS);
		invading.add(KillHelp.KILL_GRINEER);
		defending.clear();
		defending.add(KillHelp.HELP_GRINEER);
		defending.add(KillHelp.KILL_CORPUS);
		testKillHelp(CORPUS, GRINEER, killHelp, true);

		invading.clear();
		invading.add(KillHelp.HELP_CORPUS);
		invading.add(KillHelp.KILL_GRINEER);
		defending.clear();
		testKillHelp(CORPUS, GRINEER, killHelp, true);

		invading.clear();
		defending.clear();
		defending.add(KillHelp.HELP_GRINEER);
		defending.add(KillHelp.KILL_CORPUS);
		testKillHelp(CORPUS, GRINEER, killHelp, true);

		invading.clear();
		invading.add(KillHelp.KILL_CORPUS);
		invading.add(KillHelp.KILL_INFESTATION);
		invading.add(KillHelp.HELP_GRINEER);
		defending.clear();
		defending.add(KillHelp.KILL_GRINEER);
		defending.add(KillHelp.KILL_INFESTATION);
		defending.add(KillHelp.HELP_CORPUS);
		testKillHelp(CORPUS, GRINEER, killHelp, false);
		invading.clear();
		defending.clear();
		testKillHelp(CORPUS, INFESTATION, killHelp, false);
		invading.clear();
		defending.clear();
		testKillHelp(GRINEER, INFESTATION, killHelp, false);
		invading.clear();
		defending.clear();
		testKillHelp(GRINEER, CORPUS, killHelp, false);
		invading.clear();
		defending.clear();
		testKillHelp(INFESTATION, GRINEER, killHelp, false);
		invading.clear();
		defending.clear();
		testKillHelp(INFESTATION, CORPUS, killHelp, false);
		
	}

	public void testKillHelp(String invadingFaction, String defendingFaction, Map<String, Set<KillHelp>> killHelp, boolean match) {
		Map<String, CategoryFilter> categoryFilters = new HashMap<String, CategoryFilter>();
		Set<String> filters = new HashSet<String>();
		Set<String> filterMissionTypes = new HashSet<String>();

		Invasion invasion = new Invasion();
		invasion.setDefendingFaction(defendingFaction);
		invasion.setInvadingFaction(invadingFaction);
		invasion.setDefendingRewardID(new RewardID("New"));
		invasion.setInvadingRewardID(new RewardID("New"));
		invasion.setInvadingCategory(new Category("Invading;0;0;RED"));
		invasion.setDefendingCategory(new Category("Defending;0;0;RED"));
		InvasionMatcher invasionMatcher = new InvasionMatcher(5, categoryFilters, filters, filterMissionTypes, "", killHelp);
		assertEquals(match, invasionMatcher.matches(invasion));
		
	}

	@Test
	public void testMissionType() {
		Set<String> filterMissionTypes = new HashSet<String>();

		filterMissionTypes.clear();
		filterMissionTypes.add("Invading" + "One");
		filterMissionTypes.add("Defending" + "Two");
		testMissionType("One", "Two", filterMissionTypes, false);

		filterMissionTypes.clear();
		filterMissionTypes.add("Defending" + "Two");
		testMissionType("One", "Two", filterMissionTypes, true);

		filterMissionTypes.clear();
		filterMissionTypes.add("Invading" + "One");
		testMissionType("One", "Two", filterMissionTypes, true);
	}

	public void testMissionType(String invadingMissionType, String defendingMissionType, Set<String> filterMissionTypes, boolean match) {
		Map<String, CategoryFilter> categoryFilters = new HashMap<String, CategoryFilter>();
		Set<String> filters = new HashSet<String>();
		Map<String, Set<KillHelp>> killHelp = new HashMap<String, Set<KillHelp>>();
		killHelp.put("Invading", EnumSet.allOf(KillHelp.class));
		killHelp.put("Defending", EnumSet.allOf(KillHelp.class));
		
		Invasion invasion = new Invasion();
		invasion.setDefendingFaction("corpus");
		invasion.setInvadingFaction("grineer");
		invasion.setDefendingRewardID(new RewardID("New"));
		invasion.setInvadingRewardID(new RewardID("New"));
		invasion.setInvadingCategory(new Category("Invading;0;0;RED"));
		invasion.setDefendingCategory(new Category("Defending;0;0;RED"));
		invasion.setDefendingMissionType(defendingMissionType);
		invasion.setInvadingMissionType(invadingMissionType);
		InvasionMatcher invasionMatcher = new InvasionMatcher(5, categoryFilters, filters, filterMissionTypes, "", killHelp);
		assertEquals(match, invasionMatcher.matches(invasion));
	}

	@Test
	public void testNewItem() {
		testNewItem(null, null, false);
		testNewItem("Something", null, true);
		testNewItem(null, "Something", true);
	}

	public void testNewItem(String defendingReward, String invadingReward, boolean match) {
		Map<String, CategoryFilter> categoryFilters = new HashMap<String, CategoryFilter>();
		Set<String> filters = new HashSet<String>();
		Set<String> filterMissionTypes = new HashSet<String>();
		Map<String, Set<KillHelp>> killHelp = new HashMap<String, Set<KillHelp>>();
		
		Invasion invasion = new Invasion();
		if (defendingReward != null) {
			invasion.setDefendingRewardID(new RewardID(defendingReward));
		} else {
			invasion.setDefendingRewardID(null);
		}
		if (invadingReward != null) {
			invasion.setInvadingRewardID(new RewardID(invadingReward));
		} else {
			invasion.setInvadingRewardID(null);
		}
		invasion.setInvadingFaction("unknown");
		InvasionMatcher invasionMatcher = new InvasionMatcher(5, categoryFilters, filters, filterMissionTypes, "", killHelp);
		assertEquals(match, invasionMatcher.matches(invasion));
	}

	@Test
	public void testCredits() {
		testCredits(0, 0, 0, true);
		testCredits(25000, 0, 1, true);
		testCredits(0, 25000, 1, true);
		testCredits(24999, 0, 1, false);
		testCredits(0, 24999, 1, false);
		testCredits(35000, 0, 2, true);
		testCredits(0, 35000, 2, true);
		testCredits(34999, 0, 2, false);
		testCredits(0, 34999, 2, false);
		testCredits(50000, 0, 3, true);
		testCredits(0, 50000, 3, true);
		testCredits(49999, 0, 3, false);
		testCredits(0, 49999, 3, false);
	}

	public void testCredits(Integer defendingCredits, Integer invadingCredits, int credit, boolean match) {
		Map<String, CategoryFilter> categoryFilters = new HashMap<String, CategoryFilter>();
		Set<String> filters = new HashSet<String>();
		Set<String> filterMissionTypes = new HashSet<String>();
		Map<String, Set<KillHelp>> killHelp = new HashMap<String, Set<KillHelp>>();
		
		Invasion invasion = new Invasion();
		invasion.setDefendingCredits(defendingCredits);
		invasion.setInvadingCredits(invadingCredits);
		invasion.setInvadingFaction("unknown");
		InvasionMatcher invasionMatcher = new InvasionMatcher(credit, categoryFilters, filters, filterMissionTypes, "Invasions", killHelp);
		assertEquals(match, invasionMatcher.matches(invasion));
	}
	

	
}
