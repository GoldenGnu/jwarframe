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

package net.nikr.warframe.gui.reward;

import java.awt.Font;
import javax.swing.JPanel;


public enum Zoom {
	ZOOM_0("0%", 0, 1),
	ZOOM_25("25%", 25, 2),
	ZOOM_50("50%", 50, 3),
	ZOOM_75("75%", 75, 4),
	ZOOM_100("100%", 100, 5),
	//ZOOM_125("125%", 125, 6),
	//ZOOM_150("150%", 150, 7),
	;
	private final String text;
	private final int percent;
	private final Font font;

	private Zoom(String text, int percent, int size) {
		JPanel jPanel = new JPanel();
		this.text = text;
		this.percent = percent;
		this.font = new Font(jPanel.getFont().getName(), Font.BOLD, jPanel.getFont().getSize() + 1);
	}

	public int getPercent() {
		return percent;
	}

	public Font getFont() {
		return font;
	}

	@Override
	public String toString() {
		return text;
	}
}
