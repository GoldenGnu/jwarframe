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

package net.nikr.warframe.gui;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.shared.NotifyListener;
import net.nikr.warframe.gui.shared.Tool;


public class MainFrame implements NotifyListener {
	private final JFrame jFrame;
	private final JTabbedPane jTabs;
	private final List<Image> active = Arrays.asList(Images.PROGRAM_16.getImage(), Images.PROGRAM_32.getImage(), Images.PROGRAM_64.getImage());
	private final List<Image> passive = Arrays.asList(Images.PROGRAM_DISABLED_16.getImage(), Images.PROGRAM_DISABLED_32.getImage(), Images.PROGRAM_DISABLED_64.getImage());

	private final Program program;

	private boolean alarm = false;
	private int alerts = 0;
	private int invasions = 0;

	public MainFrame(Program program) {
		this.program = program;
		
		this.jFrame = new JFrame(Program.PROGRAM_NAME + " " + Program.PROGRAM_VERSION);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setSize(850, 600);
		jFrame.setIconImages(active);
		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowIconified(WindowEvent e) {
				if (SystemTray.isSupported()) {
					jFrame.setVisible(false);
				}
			}
			
		});
		jFrame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				updateIcons();
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				jFrame.setIconImages(active);
			}
		});
		jTabs = new JTabbedPane();
		jFrame.getContentPane().add(jTabs);
	}

	public void show() {
		jFrame.setVisible(true);
	}

	public void add(Tool tool) {
		jTabs.addTab(tool.getTitle(), tool.getIcon(), tool.getPanel(), tool.getToolTip());
	}

	public JFrame getWindow() {
		return jFrame;
	}

	@Override
	public void stopNotify() {
		if (alarm) {
			showWindow();
		}
		alarm = false;
		updateIcons();
	}

	@Override
	public void startNotify(final int count, final NotifySource source) {
		if (!alarm) {
			if (jFrame.isVisible()) {
				SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(jFrame, "Stop Beep...", "Beep", JOptionPane.PLAIN_MESSAGE);
					program.stopNotify();
				}
			});
			}
		}
		if (source == NotifySource.ALERTS) {
			alerts = alerts + count;
		} else if (source == NotifySource.INVASIONS) {
			invasions = invasions + count;
		}
		alarm = true;
		updateIcons();
	}

	private void showWindow() {
		if (alerts == 0 && invasions == 0) {
			return;
		}
		//Finish current event chain...
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				StringBuilder builder = new StringBuilder();
				if (alerts > 0) {
					builder.append(alerts);
					builder.append(" new alert");
					if (alerts > 1) {
						builder.append("s");
					}
				}
				if (alerts > 0 && invasions > 0) {
					builder.append(" and ");
				} else {
					
				}
				if (invasions > 0) {
					builder.append(invasions);
					builder.append(" new invasion");
					if (invasions > 1) {
						builder.append("s");
					}
				}
				builder.append(" found");
				JOptionPane.showMessageDialog(jFrame, builder.toString(), "Beep", JOptionPane.PLAIN_MESSAGE);
				alerts = 0;
				invasions = 0;
			}
		});
	}

	private void updateIcons() {
		if (alarm) {
			jFrame.setIconImages(active);
		} else {
			jFrame.setIconImages(passive);
		}
	}
}
