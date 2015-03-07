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

import java.awt.Color;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import net.nikr.warframe.Main;
import net.nikr.warframe.Program;
import net.nikr.warframe.SplashUpdater;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.gui.shared.components.JDropDownButton;
import net.nikr.warframe.gui.shared.listeners.LoginRewardListener;
import net.nikr.warframe.gui.shared.listeners.NotifyListener;
import net.nikr.warframe.io.shared.FastToolTips;


public class MainFrame implements NotifyListener, LoginRewardListener {

	private final List<Image> active = Arrays.asList(Images.PROGRAM_16.getImage(), Images.PROGRAM_32.getImage(), Images.PROGRAM_64.getImage());
	private final List<Image> passive = Arrays.asList(Images.PROGRAM_DISABLED_16.getImage(), Images.PROGRAM_DISABLED_32.getImage(), Images.PROGRAM_DISABLED_64.getImage());
	
	private final JFrame jFrame;
	private final JTabbedPane jTabs;
	private final JLabel jLoginReward;
	private final JLabel jAlerts;
	private final JLabel jInvasions;
	private final JDropDownButton jFilters;

	private final Program program;

	private boolean alarm = false;

	public MainFrame(final Program program) {
		this.program = program;
		
		this.jFrame = new JFrame(Program.PROGRAM_NAME + " " + Program.PROGRAM_VERSION);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
				if (alarm) {
					program.stopNotify();
				}
			}
		});

		JPanel jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		
		jTabs = new JTabbedPane();
		FastToolTips.install(jTabs);

		JPanel jStatusBar = new JPanel();
		GroupLayout statusBarLayout = new GroupLayout(jStatusBar);
		jStatusBar.setLayout(statusBarLayout);
		statusBarLayout.setAutoCreateGaps(true);
		statusBarLayout.setAutoCreateContainerGaps(false);

		Border border = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1)
				, BorderFactory.createEmptyBorder(2, 3, 2, 3));

		jAlerts = new JLabel(Images.ALERT.getIcon());
		jAlerts.setToolTipText("Alerts");
		jAlerts.setOpaque(true);
		jAlerts.setBorder(border);
		FastToolTips.install(jAlerts);
		jStatusBar.add(jAlerts);
		setAlert(0, 0);

		jInvasions = new JLabel(Images.INVASION.getIcon());
		jInvasions.setToolTipText("Invasions");
		jInvasions.setOpaque(true);
		jInvasions.setBorder(border);
		FastToolTips.install(jInvasions);
		jStatusBar.add(jInvasions);
		setInvasions(0, 0);

		jLoginReward = new JLabel(Images.LOGIN_REWARD.getIcon());
		jLoginReward.setToolTipText("Login Reward");
		jLoginReward.setOpaque(true);
		jLoginReward.setBorder(border);
		FastToolTips.install(jLoginReward);
		jStatusBar.add(jLoginReward);

		jFilters = new JDropDownButton("Load Filter Set");
		jFilters.keepVisible(2);
		jFilters.setTopFixedCount(2);
		jFilters.setInterval(125);
		jStatusBar.add(jFilters);

		statusBarLayout.setHorizontalGroup(
			statusBarLayout.createSequentialGroup()
					.addGap(5)
				.addComponent(jAlerts)
				.addComponent(jInvasions)
				.addComponent(jLoginReward)
				.addGap(0, 0, Integer.MAX_VALUE)
				.addComponent(jFilters)
				.addGap(5)
		);
		statusBarLayout.setVerticalGroup(
			statusBarLayout.createSequentialGroup()
			.addGap(5)
			.addGroup(statusBarLayout.createParallelGroup()
				.addComponent(jAlerts)
				.addComponent(jInvasions)
				.addComponent(jLoginReward)
				.addComponent(jFilters)
			)
			.addGap(5)
		);

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(jTabs, 600, 600, Integer.MAX_VALUE)
				.addComponent(jStatusBar, 0, 0, Integer.MAX_VALUE)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(jTabs, 450, 450, Integer.MAX_VALUE)
				.addComponent(jStatusBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		jFrame.getContentPane().add(jPanel);
	}

	public void show() {
		//Find minimum size
		jFrame.pack();
		jFrame.setMinimumSize(jFrame.getSize());
		jFrame.setSize(850, 600);

		updateFilters();
		if (Main.isStartup()) {
			SplashUpdater.hide();
		} else {
			jFrame.setVisible(true);
			jFrame.requestFocus();
		}
	}

	public void add(Tool tool) {
		jTabs.addTab(tool.getTitle(), tool.getIcon(), tool.getPanel(), tool.getToolTip());
	}

	public JFrame getWindow() {
		return jFrame;
	}
	
	public final void updateFilters() {
		JMenuItem jMenuItem;
		jFilters.removeAll();
		jFilters.setVisible(!program.getFilterSets().isEmpty());
		jFilters.setEnabled(!program.getFilterSets().isEmpty());

		List<String> list = new ArrayList<String>();
		for (String s : program.getFilterSets()) {
			list.add(s.replace(".dat", ""));
		}

		Collections.sort(list);

		for (final String setName : list) {
			jMenuItem = new JMenuItem(setName);
			jMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					program.getFiltersTool().loadFilterSet(setName);
				}
			});
			jFilters.add(jMenuItem);
		}
	}

	public final void setAlert(int match, int total) {
		jAlerts.setText(match + " of " + total + " ");
		if (total == 0) {
			jAlerts.setBackground(Category.CategoryColor.GRAY.getColor(false));
		} else if (match > 0) {
			jAlerts.setBackground(Category.CategoryColor.GREEN.getColor(false));
		} else {
			jAlerts.setBackground(Category.CategoryColor.RED.getColor(false));
		}
	}

	public final void setInvasions(int match, int total) {
		jInvasions.setText(match + " of " + total + " ");
		if (total == 0) {
			jInvasions.setBackground(Category.CategoryColor.GRAY.getColor(false));
		} else if (match > 0) {
			jInvasions.setBackground(Category.CategoryColor.GREEN.getColor(false));
		} else {
			jInvasions.setBackground(Category.CategoryColor.RED.getColor(false));
		}
	}

	@Override
	public void stopNotify() {
		alarm = false;
		updateIcons();
	}

	@Override
	public void startNotify(final int count, final NotifySource source, final Set<String> categories) {
		if (!alarm) {
			if (jFrame.isVisible() && !program.getSettings(SettingsConstants.SHOW_POPUP)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(jFrame, "Stop Beep...", "Beep", JOptionPane.PLAIN_MESSAGE);
						program.stopNotify();
					}
				});
			}
		}
		alarm = true;
		updateIcons();
	}

	private void updateIcons() {
		if (alarm) {
			jFrame.setIconImages(active);
		} else {
			jFrame.setIconImages(passive);
		}
	}

	@Override
	public void addLoginReward(Boolean available) {
		if (available == null) {
			jLoginReward.setBackground(Category.CategoryColor.GRAY.getColor(false));
		} else if (available) {
			jLoginReward.setBackground(Category.CategoryColor.GREEN.getColor(false));
		} else {
			jLoginReward.setBackground(Category.CategoryColor.RED.getColor(false));
		}
	}
}
