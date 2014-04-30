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

package net.nikr.warframe.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.io.run.AutoRun;
import net.nikr.warframe.io.shared.FileConstants;


public class SettingsTool implements Tool {

	private final JPanel jPanel;
	private final JCheckBox jLoginReward;
	private final JCheckBox jAutoRun;
	private final JCheckBox jAudioNotify;

	private final Program program;

	public SettingsTool(final Program program) {
		this.program = program;

		jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		jAutoRun = new JCheckBox("Run on startup (Windows only)");
		jAutoRun.setSelected(AutoRun.isInstalled());
		jAutoRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autoRun();
			}
		});

		jLoginReward = new JCheckBox("Notify on Login Reward");
		jLoginReward.setSelected(program.getSettings(SettingsConstants.LOGIN_REWARD));
		jLoginReward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});

		jAudioNotify = new JCheckBox("Notify with audio");
		jAudioNotify.setSelected(program.getSettings(SettingsConstants.NOTIFY_AUDIO));
		jAudioNotify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(jAutoRun)
				.addComponent(jLoginReward)
				.addComponent(jAudioNotify)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(jAutoRun)
				.addComponent(jLoginReward)
				.addComponent(jAudioNotify)
		);
	}

	@Override
	public String getToolTip() {
		return "Settings";
	}

	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public JPanel getPanel() {
		return jPanel;
	}

	@Override
	public Set<SettingsConstants> getSettings() {
		Set<SettingsConstants> settings = EnumSet.noneOf(SettingsConstants.class);
		if (jLoginReward.isSelected()) {
			settings.add(SettingsConstants.LOGIN_REWARD);
		}
		if (jAudioNotify.isSelected()) {
			settings.add(SettingsConstants.NOTIFY_AUDIO);
		}
		return settings;
	}

	@Override
	public Icon getIcon() {
		return Images.SETTINGS.getIcon();
	}

	private void autoRun() {
		if (jAutoRun.isSelected()) {
			boolean installed = AutoRun.install();
			if (!installed) {
				File file = AutoRun.getStartup();
				if (file != null) {
					JOptionPane.showMessageDialog(program.getWindow(), "Failed install Auto Run\r\n"
										+ "You can do it manually by:\r\n"
										+ "1) Copy jWarframe.bat \r\n"
										+ "From: " + FileConstants.getHome() +"\r\n"
										+ "To: " + file.getAbsoluteFile()
										, "AutoRun"
										, JOptionPane.PLAIN_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(program.getWindow(), "Failed install Auto Run\r\nOnly works on Windows", "AutoRun", JOptionPane.PLAIN_MESSAGE);
				}
				jAutoRun.setSelected(installed);
			}
		} else {
			boolean uninstalled = AutoRun.uninstall();
			if (!uninstalled) {
				File file = AutoRun.getStartup();
				if (file != null) {
					JOptionPane.showMessageDialog(program.getWindow(), "Failed uninstall Auto Run\r\n"
										+ "You can do it manually by:\r\n"
										+ "1) Detele jWarframe.bat \r\n"
										+ "From: " + FileConstants.getHome() +"\r\n"
										, "AutoRun"
										, JOptionPane.PLAIN_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(program.getWindow(), "Failed uninstall Auto Run\r\nOnly works on Windows", "AutoRun", JOptionPane.PLAIN_MESSAGE);
				}
				jAutoRun.setSelected(uninstalled);
			}
		}
		program.saveSettings();
	}
	
}
