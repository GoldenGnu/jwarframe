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

package net.nikr.warframe.gui.invasion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.io.invasion.Invasion.InvasionPercentage;
import net.nikr.warframe.io.shared.Faction;


public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {

	private Color invadingColor = Color.BLACK;
	private Color defendingColor = Color.WHITE;
	private BufferedImage invadingImage = Images.INVASION.getBufferedImage();
	private BufferedImage defendingImage = Images.INVASION.getBufferedImage();
	private boolean arrow = true;

	public ProgressCellRenderer() {
		setPreferredSize(new Dimension(150, getPreferredSize().height));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof InvasionPercentage) {
			InvasionPercentage invasionPercentage = (InvasionPercentage) value;
			int progress = Math.round(invasionPercentage.getPercentage());
			setValue(progress);
			invadingColor = invasionPercentage.getInvading().getColor(isSelected);
			defendingColor = invasionPercentage.getDefending().getColor(isSelected);
			invadingImage = invasionPercentage.getInvading().getImage();
			defendingImage = invasionPercentage.getDefending().getImage();
			arrow = calcArrow(invasionPercentage.getInvading(), progress);
			setToolTipText(invasionPercentage.getInvading() + "  vs  " + invasionPercentage.getDefending());
		}
		return this;
	}

	@Override
	public void paint(Graphics g) {
		//Anti-Aliasing
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//Calc
		int space = 8;
		int width = getSize().width;
		int height = getSize().height;
		int fixedWidth = getSize().width - (space * 2);
		int value = getValue();
		int split = (int) (fixedWidth / 100.0 * value) + space;

		//Defending
		g.setColor(defendingColor);
		g.fillRect(0, 0, width, height);

		//Invading
		g.setColor(invadingColor);
		g.fillRect(0, 0, split, height);

		//Arrow
		Polygon polygon = new Polygon();
		if (arrow) {
			g.setColor(defendingColor);
			polygon.addPoint(split, 0);
			polygon.addPoint(split - 8, height / 2 + 1);
			polygon.addPoint(split, height);
		} else {
			g.setColor(invadingColor);
			polygon.addPoint(split, 0);
			polygon.addPoint(split + 8, height / 2 + 1);
			polygon.addPoint(split, height);
		}
		g.fillPolygon(polygon);

		//Text
		g.setFont(getFont());
		g.setColor(Color.BLACK);
		int fixed;
		if (value > 9) {
			fixed = 10;
		} else {
			fixed = 4;
		}
		g.drawString(getValue() + "%", width / 2 - fixed, (height - 6));

		//Logos
		g.drawImage(invadingImage, 2, 3, null);
		g.drawImage(defendingImage, width - 18, 3, null);

		//Border
		//g.setColor(Color.WHITE);
		//g.drawRect(0, 0, width - 1, height - 1);
		//g.drawRect(1, 1, width - 3, height - 3);
	}

	private boolean calcArrow(Faction invading, int percentage) {
		return invading == Faction.INFESTATION || percentage < 50;
	}
}
