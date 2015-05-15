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

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.reward.Category.CategoryColor;


public enum Faction {
	GRINEER("Grineer", CategoryColor.RED, Images.GRINEER),
	CORPUS("Corpus", CategoryColor.BLUE, Images.CORPUS),
	INFESTATION("Infestation", CategoryColor.GREEN, Images.INFESTATION),
	UNKNOWN("Unknown", CategoryColor.GRAY, Images.INVASION);
	;

	private final String name;
	private final CategoryColor category;
	private final Images image;

	private Faction(String name, CategoryColor category, Images image) {
		this.name = name;
		this.category = category;
		this.image = image;
	}

	public Color getColor(boolean b) {
		return category.getColor(b);
	}

	public BufferedImage getImage() {
		return image.getBufferedImage();
	}

	public Icon getIcon() {
		return image.getIcon();
	}

	@Override
	public String toString() {
		return name;
	}

	public static Faction getFaction(String s) {
		if (s.toLowerCase().equals("infestation")) {
			return Faction.INFESTATION;
		} else if (s.toLowerCase().equals("corpus")) {
			return Faction.CORPUS;
		} else if (s.toLowerCase().equals("grineer")) {
			return Faction.GRINEER;
		} else {
			return null;
		}
	}
}
