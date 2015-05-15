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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelListener;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.Border;


public class JDynamicGrid {

	private int maxWidth;
	private final JPanel jInner;
	private final JPanel jOuter;
	private final JScrollPane jScroll;

	public JDynamicGrid() {
		jInner = new JPanel(new GridLayout(0, 1, 0, 0));
		jInner.setOpaque(true);
		jOuter = new JPanel();
		GroupLayout groupLayout = new GroupLayout(jOuter);
		jOuter.setLayout(groupLayout);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup()
				.addComponent(jInner, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup()
				.addComponent(jInner, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		jScroll = new JScrollPane(jOuter);
		jScroll.addComponentListener(new Listener());
	}

	public void setBackground(Color bg) {
		jInner.setBackground(bg);
		jOuter.setBackground(bg);
	}

	public void setBorder(Border border) {
		jInner.setBorder(border);
	}

	public Component add(Component comp) {
		jInner.add(comp);
		maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
		updateSize(); //Can be removed if we do updateUI() after adding
		return comp;
	}

	public void removeAll() {
		jInner.removeAll();
		maxWidth = 0;
	}

	public void updateUI() {
		jInner.updateUI();
		updateSize();
	}

	public JScrollBar getVerticalScrollBar() {
		return jScroll.getVerticalScrollBar();
	}

	public Component getComponent() {
		return jScroll;
	}

	public void addMouseWheelListener(MouseWheelListener l) {
		jOuter.addMouseWheelListener(l);
	}

	private int getScrollSize() {
		return jScroll.getSize().width - 30;
	}

	private boolean isVisible() {
		return jScroll.isVisible();
	}

	private void updateSize() {
		LayoutManager layout =  jInner.getLayout();
		if (layout instanceof GridLayout &&  getScrollSize() > 0 && isVisible()) {
			GridLayout grid = (GridLayout) layout;
			//Columns
			int totalWidth = 0;
			int columns = 0;
			for (int i = 0; i < jInner.getComponentCount(); i++) {
				totalWidth = totalWidth + maxWidth;
				if (totalWidth < getScrollSize()) {
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
			jInner.updateUI();
		}
	}

	private class Listener implements ComponentListener {
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
	}
}
