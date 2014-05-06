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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.nikr.warframe.gui.shared.CategoryFilter;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ListWriter;


public class CategoryWriter {

	public CategoryWriter() { }

	public void save(Map<String, Map<String, CategoryFilter>> categoryFilters) {
		Set<String> output = new HashSet<String>();
		for (Map.Entry<String, Map<String, CategoryFilter>> tool : categoryFilters.entrySet()) {
			for (Map.Entry<String, CategoryFilter> entry : tool.getValue().entrySet()) {
				StringBuilder builder = new StringBuilder();
				builder.append(tool.getKey());
				builder.append("_");
				builder.append(entry.getKey());
				builder.append("=");
				builder.append(entry.getValue().toString());
				output.add(builder.toString());
			}
		}
		ListWriter writer = new ListWriter();
		writer.save(output, FileConstants.getCategoryFilters());
	}
}
