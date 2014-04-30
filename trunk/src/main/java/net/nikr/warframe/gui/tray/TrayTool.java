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

package net.nikr.warframe.gui.tray;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.shared.listeners.NotifyListener;


public class TrayTool implements NotifyListener {

	private final TrayIcon active;
	private final TrayIcon passive;

	private boolean started = true;

	public TrayTool(final Program program) {
		active = new TrayIcon(Images.PROGRAM_16.getImage());
		active.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				program.getWindow().setVisible(true);
				program.getWindow().setState(Frame.NORMAL);
				program.stopNotify();
			}
		});
		passive = new TrayIcon(Images.PROGRAM_DISABLED_16.getImage());
		passive.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				program.getWindow().setVisible(true);
				program.getWindow().setState(Frame.NORMAL);
			}
		});
		stopNotify();
	}

	@Override
	public final void stopNotify() {
		if (SystemTray.isSupported() && started) {
			started = false;
			final SystemTray tray = SystemTray.getSystemTray();
			try {
				tray.remove(active);
				tray.add(passive);
			} catch (AWTException e) {
				
			}
		}
	}

	@Override
	public final void startNotify(final int count, final NotifySource source) {
		if (SystemTray.isSupported() && !started) {
			started = true;
			final SystemTray tray = SystemTray.getSystemTray();
			try {
				tray.remove(passive);
				tray.add(active);
			} catch (AWTException e) {
				
			}
		}
	}
	
}
