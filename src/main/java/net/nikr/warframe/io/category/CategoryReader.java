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

package net.nikr.warframe.io.category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.nikr.warframe.gui.shared.CategoryFilter;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ListReader;


public class CategoryReader {

	public CategoryReader() { }

	public Map<String, Map<String, CategoryFilter>> load() {
		ListReader reader = new ListReader();
		List<String> list = reader.load(FileConstants.getCategoryFilters());

		Map<String, Map<String, CategoryFilter>> categoryFilters = new HashMap<String, Map<String, CategoryFilter>>();

		for (String s : list) {
			String[] split = s.split("=");
			if (split.length == 2) {
				String[] id = split[0].split("_");
				if (id.length == 2) {
					String toolName = id[0];
					String category = id[1];
					CategoryFilter setting = CategoryFilter.valueOf(split[1]);
					Map<String, CategoryFilter> tools = categoryFilters.get(toolName);
					if (tools == null) {
						tools = new HashMap<String, CategoryFilter>();
						categoryFilters.put(toolName, tools);
					}
					tools.put(category, setting);
				}
			}
		}
		return categoryFilters;
	}
}
