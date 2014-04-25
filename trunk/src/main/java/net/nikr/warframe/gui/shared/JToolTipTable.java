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

package net.nikr.warframe.gui.shared;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.table.TableModel;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.io.shared.ImageGetter;


public class JToolTipTable extends JAutoColumnTable {

	private final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
	private final int defaultInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();

	public JToolTipTable(final TableModel tableModel) {
		super(tableModel);
		addMouseListener(new MouseAdapter() {
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
				MouseEvent phantom = new MouseEvent(getThis(), MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, e.getX(), e.getY(), 0, false);
				ToolTipManager.sharedInstance().mouseMoved(phantom);
			}
		});
	}

	@Override
	public Point getToolTipLocation(MouseEvent e) {
		if (getToolTipText(e) == null) {
			return super.getToolTipLocation(e);
		}
		int column = columnAtPoint(e.getPoint());
		int row = rowAtPoint(e.getPoint());
		Rectangle rectangle = getCellRect(row, column, true);
		return new Point(rectangle.x - 1, rectangle.y + rectangle.height + 1);
	}

	protected JComponent getThis() {
		return this;
	}

	protected String generateToolTip(RewardID rewardID, boolean credit) {
		try {
			File file;
			if (credit) {
				file = ImageGetter.getFile("credits");
			} else if (rewardID != null){
				file = ImageGetter.getFile(rewardID.getName());
			} else {
				file = null;
			}
			URL url;
			if (file != null && file.exists()) {
				url = file.getAbsoluteFile().toURI().toURL();
			} else {
				//No tooltip for you, then!
				//url = ImageGetter.getFile("Scorpion Ash Helmet").toURI().toURL();
				//url = ImageGetter.getFile("Accelerated Blast").toURI().toURL();
				return null;
			}
			return "<html><body bgcolor='#000000'>"
					+ "<img src='"
					+ url
					+ "' hspace='0'>"
					+ "</body></html>";
		} catch (MalformedURLException ex) {
			//No tooltip for you, then!
			return null;
		}
	}
}
