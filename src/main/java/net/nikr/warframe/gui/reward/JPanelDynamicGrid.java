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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class JPanelDynamicGrid extends JPanel {

	private JScrollPane jScrollPane;
	private final Listener listener;
	private int maxWidth;

	public JPanelDynamicGrid() {
		super(new GridLayout(0, 5, 0, 0));
		listener = new Listener();
		addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				updateScroll();
			}
		});
	}

	@Override
	public void setLayout(LayoutManager mgr) {
		if (!(mgr instanceof GridLayout)) {
			throw new IllegalArgumentException("Only GridLayout supported");
		}
		super.setLayout(mgr); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		super.addImpl(comp, constraints, index);
		maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
	}

	@Override
	public void removeAll() {
		super.removeAll();
		maxWidth = 0;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		updateSize();
	}

	private void updateScroll() {
		if (jScrollPane != null) {
			jScrollPane.removeComponentListener(listener);
		}
		jScrollPane = getParentScrollPane();
		if (jScrollPane != null) {
			jScrollPane.addComponentListener(listener);
		}
	}

	private JScrollPane getParentScrollPane() {
		boolean searching = true;
		Container container = this.getParent();
		while (searching) {
			if (container instanceof JScrollPane) {
				return (JScrollPane) container;
			}
			if (container == null) {
				return null;
			}
			//One level up
			container = container.getParent();
		}
		return null;
	}

	private int getScrollSize() {
		if (jScrollPane != null) {
			return jScrollPane.getSize().width - 30;
		} else {
			return 0;
		}
	}

	private void updateSize() {
		LayoutManager layout =  getLayout();
		if (layout instanceof GridLayout && getSize().width > 0 && isVisible()) {
			GridLayout grid = (GridLayout) layout;
			//Columns
			int totalWidth = 0;
			int columns = 0;
			for (int i = 0; i < getComponentCount(); i++) {
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
			super.updateUI();
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
