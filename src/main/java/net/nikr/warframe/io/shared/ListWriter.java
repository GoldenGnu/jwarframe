/*
 * Copyright 2014-2015 Niklas Kyster Rasmussen
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

package net.nikr.warframe.io.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;


public class ListWriter {

	public boolean save(String content, File file) {
		return save(Collections.singletonList(content), file);
	}

	public boolean save(Collection<String> strings, File file) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file));
			for (String string : strings) {
				out.write(string);
				out.write("\r\n");
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					
				}
			}
		}
	}
}
