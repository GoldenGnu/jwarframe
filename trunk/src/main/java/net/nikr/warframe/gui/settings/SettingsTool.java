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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.io.run.AutoRun;
import net.nikr.warframe.io.shared.FastToolTips;
import net.nikr.warframe.io.shared.FileConstants;


public class SettingsTool implements Tool {

	private final JPanel jPanel;
	private final JCheckBox jLoginReward;
	private final JCheckBox jAutoRun;
	private final JCheckBox jAudioNotify;
	private final JButton jNorifyBrowse;
	private final JButton jNorifyPlay;
	private final JButton jNorifyReset;
	private final JFileChooser jFileChooser;

	private Thread testAudio = null;

	private final Program program;

	public SettingsTool(final Program program) {
		this.program = program;

		jFileChooser = new JFileChooser();
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setFileFilter(new AudioFileFilter());

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

		JLabel jNorifyLabel = new JLabel("Notification Sound:");

		jNorifyBrowse = new JButton(Images.BROWSE.getIcon());
		jNorifyBrowse.setToolTipText("Set notification sound (WAV files only)");
		FastToolTips.install(jNorifyBrowse);
		jNorifyBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int value = jFileChooser.showOpenDialog(program.getWindow());
				if (value == JFileChooser.APPROVE_OPTION) {
					File file = jFileChooser.getSelectedFile();
					FileConstants.copyFile(file, FileConstants.getAudioLocal());
					updateNotify();
				}
			}
		});

		jNorifyReset = new JButton(Images.NONE.getIcon());
		jNorifyReset.setToolTipText("Reset to default");
		FastToolTips.install(jNorifyReset);
		jNorifyReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileConstants.getAudioLocal().delete();
				updateNotify();
			}
		});

		jNorifyPlay = new JButton(Images.PLAY.getIcon());
		jNorifyPlay.setToolTipText("Play/Stop");
		FastToolTips.install(jNorifyPlay);
		jNorifyPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (testAudio == null) {
					testAudio = new Thread(new Runnable() {
						@Override
						public void run() {
							program.audioStartTest();
							jNorifyReset.setEnabled(true);
							jNorifyBrowse.setEnabled(true);
							jNorifyPlay.setIcon(Images.PLAY.getIcon());
							testAudio = null;
						}
					});
					jNorifyReset.setEnabled(false);
					jNorifyBrowse.setEnabled(false);
					jNorifyPlay.setIcon(Images.STOP.getIcon());
					testAudio.start();
				} else {
					program.audioStopTest();
				}
			}
		});

		updateNotify();

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(jAutoRun)
				.addComponent(jLoginReward)
				.addComponent(jAudioNotify)
				.addComponent(jNorifyLabel)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jNorifyBrowse)
					.addComponent(jNorifyReset)
					.addComponent(jNorifyPlay)
				)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(jAutoRun)
				.addGap(5)
				.addComponent(jLoginReward)
				.addGap(5)
				.addComponent(jAudioNotify)
				.addGap(5)
				.addComponent(jNorifyLabel)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(jNorifyBrowse)
					.addComponent(jNorifyReset)
					.addComponent(jNorifyPlay)
				)
		);
	}

	private void updateNotify() {
		boolean audio = FileConstants.getAudioLocal().exists();
		if (audio) {
			jNorifyPlay.setEnabled(true);
			jNorifyReset.setEnabled(true);
		} else {
			jNorifyPlay.setEnabled(false);
			jNorifyReset.setEnabled(false);
		}
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

	private static class AudioFileFilter extends FileFilter {
		
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			String filename = f.getName().toLowerCase();
			for (AudioFileFormat.Type type: AudioSystem.getAudioFileTypes()) {
				if (filename.endsWith("."+type.getExtension().toLowerCase())) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			return "Wave Audio Files";
		}
		
	}
	
}