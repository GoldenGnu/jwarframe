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

import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class JPanelMaxWidth extends JPanel {

	private JScrollPane jScrollPane;

	public JPanelMaxWidth() { }

	public void setScroll(JScrollPane jScrollPane) {
		this.jScrollPane = jScrollPane;
		jScrollPane.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSize();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				updateSize();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				updateSize();
			}

			@Override
			public void componentHidden(ComponentEvent e) { }
		});
	}

	private void updateSize() {
		LayoutManager layout =  getLayout();
		if (layout instanceof GridLayout && getSize().width > 0 && isVisible()) {
			GridLayout grid = (GridLayout) layout;
			//Columns
			int width = 0;
			int columns = 0;
			for (int i = 0; i < getComponentCount(); i++) {
				width = width + getComponent(i).getPreferredSize().width + grid.getHgap();
				if (width < jScrollPane.getSize().width - 20) {
					columns++;
				} else {
					break;
				}
			}
			if (columns > 0) {
				grid.setColumns(columns);
			} else {
				grid.setColumns(1);
			}
			updateUI();
		}
	}
	
}
