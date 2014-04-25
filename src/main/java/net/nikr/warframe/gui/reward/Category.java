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

import java.awt.Color;
import java.util.Set;
import java.util.TreeSet;
import net.nikr.warframe.gui.reward.RewardID;


public class Category {
	public enum CategoryType {
		RED("", new Color(255, 200, 200)),
		GREEN("Resource", new Color(200, 255, 200)),
		BLUE("Blueprint", new Color(160, 220, 255)),
		YELLOW("Aura", new Color(255, 255, 160)),
		ORANGE("Mod", new Color(255, 160, 120)),
		GRAY("Unknown", new Color(230, 230, 230));

		private final String name;
		private final Color color;
		private final Color selected;

		private CategoryType(String name, Color color) {
			this.name = name;
			this.color = color;
			this.selected = moreBlue(color);
		}

		public Color getColor(boolean b) {
			if (b) {
				return selected;
			} else {
				return color;
			}
		}

		public String getName() {
			return name;
		}

		private Color moreBlue(Color c) {
			int r = Math.min(255, (int) (c.getRed() * 0.75));
			int g = Math.min(255, (int) (c.getGreen() * 0.75));
			int b = Math.min(255, (int) (c.getBlue() * 1.0));
			return new Color(r,g,b);
		}
	}

	
	private final String name;
	private final String filename;
	private final int width;
	private final int height;
	private final CategoryType color;
	private final Set<RewardID> rewards = new TreeSet<RewardID>();

	public Category(String data) {
		String[] split = data.split(";");
		this.name = split[0];
		this.filename = split[0].toLowerCase() + ".dat";
		this.width = Integer.valueOf(split[1]);
		this.height = Integer.valueOf(split[2]);
		this.color = CategoryType.valueOf(split[3]);
	}

	public void addAll(Set<RewardID> rewards) {
		this.rewards.addAll(rewards);
	}

	public Set<RewardID> getRewards() {
		return rewards;
	}

	public CategoryType getType() {
		return color;
	}

	public String getName() {
		return name;
	}

	public String getFilename() {
		return filename;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
}
