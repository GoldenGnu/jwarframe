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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ToolTipManager;


public class FastToolTips {

	public static void install(Component component) {
		component.addMouseListener(new Listener(component));
	}

	private static class Listener extends MouseAdapter {

		private final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
		private final int defaultInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();

		Component component;

		public Listener(Component component) {
			this.component = component;
		}

		@Override
		public void mouseEntered(MouseEvent me) {
			ToolTipManager.sharedInstance().setDismissDelay(60000);
			ToolTipManager.sharedInstance().setInitialDelay(0);
		}

		@Override
		public void mouseExited(MouseEvent me) {
			ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
			ToolTipManager.sharedInstance().setInitialDelay(defaultInitialDelay);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			MouseEvent phantom = new MouseEvent(component, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, e.getX(), e.getY(), 0, false);
			ToolTipManager.sharedInstance().mouseMoved(phantom);
		}
	}
}
