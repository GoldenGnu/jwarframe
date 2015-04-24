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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.shared.listeners.NotifyListener;


public class TrayTool implements NotifyListener {

	private final TrayIcon active;
	private final TrayIcon passive;
	private final JMenuItem jShow;
	private final JPopupMenu jMenu;
	private final Program program;

	private boolean started = true;

	public TrayTool(final Program program) {
		this.program = program;

		jMenu = new JPopupMenu();
		jShow = new JMenuItem("Show");
		jShow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				show();
			}
		});
		jMenu.add(jShow);
		
		jMenu.addSeparator();

		JMenuItem jClose = new JMenuItem("Exit");
		jClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		jMenu.add(jClose);

		active = new TrayIcon(Images.PROGRAM_16.getImage());
		active.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showPopupMenu(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				showPopupMenu(e);
				if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
					show();
				}
			}
		});
		passive = new TrayIcon(Images.PROGRAM_DISABLED_16.getImage());
		passive.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showPopupMenu(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				showPopupMenu(e);
				if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
					//Hide popup menu when window is shown
					hideMenu();
					//Show window
					show();
				}
			}
		});
		//Hide popup menu when window is shown
		program.getWindow().addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeiconified(WindowEvent e) {
				hideMenu();
			}
		});
		stopNotify();
	}

	private void showPopupMenu(MouseEvent e) {
		if (e.isPopupTrigger()) {
			jShow.setEnabled(!program.getWindow().isVisible());

			jMenu.setLocation(e.getX() - jMenu.getPreferredSize().width, e.getY() - jMenu.getPreferredSize().height);
			jMenu.setInvoker(jMenu);
			jMenu.setVisible(true);
		}
	}

	private void show() {
		program.getWindow().setVisible(true);
		program.getWindow().setState(Frame.NORMAL);
		program.stopNotify();
	}

	public void hideMenu() {
		jMenu.setVisible(false);
	}

	@Override
	public final void stopNotify() {
		if (SystemTray.isSupported() && started) {
			started = false;
			final SystemTray tray = SystemTray.getSystemTray();
			try {
				for (TrayIcon trayIcon : tray.getTrayIcons()) {
					tray.remove(trayIcon);
				}
				tray.add(passive);
			} catch (AWTException e) {
				
			}
		}
	}

	@Override
	public void startNotify(final int count, final NotifySource source, final Set<String> categories) {
		if (SystemTray.isSupported() && !started) {
			started = true;
			final SystemTray tray = SystemTray.getSystemTray();
			try {
				for (TrayIcon trayIcon : tray.getTrayIcons()) {
					tray.remove(trayIcon);
				}
				tray.add(active);
			} catch (AWTException e) {
				
			}
		}
	}
	
}
