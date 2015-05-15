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

package net.nikr.warframe.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.shared.JLockWindow;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.io.run.AutoRun;
import net.nikr.warframe.io.shared.FastToolTips;
import net.nikr.warframe.io.shared.FileConstants;


public class SettingsTool implements Tool {

	private final JPanel jPanel;
	private final JCheckBox jLoginReward;
	private final JCheckBox jAutoRun;
	private final JCheckBox jShowPopup;
	private final JCheckBox jTrayOnClose;
	private final JCheckBox jLite;
	private final JRadioButton jAudioNotifyOnce;
	private final JRadioButton jAudioNotifyRepeat;
	private final JRadioButton jAudioNotifyNone;
	private final JFileChooser jFileChooser;
	private final boolean loginReward;
	private final boolean showPopup;
	private final boolean trayOnClose;
	private final boolean audioNotifyOnce;
	private final boolean audioNotifyRepeat;
	private final List<SoundPanel> soundPanels = new ArrayList<SoundPanel>();

	private Thread testAudio = null;

	private final Program program;

	public SettingsTool(final Program program) {
		this.program = program;

		jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		jLite = new JCheckBox("Lite (Require restart of jWarframe)");
		jLite.setSelected(Program.isLite());
		jLite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});

		if (Program.isLite()) {
			jLoginReward = null;
			jAutoRun = null;
			jShowPopup = null;
			jTrayOnClose = null;
			jAudioNotifyOnce = null;
			jAudioNotifyRepeat = null;
			jAudioNotifyNone = null;
			jFileChooser = null;
			layout.setHorizontalGroup(
					layout.createSequentialGroup()
						.addComponent(jLite)

			);
			layout.setVerticalGroup(
					layout.createParallelGroup()
						.addComponent(jLite, 25, 25, 25)
			);
			loginReward = program.getSettings(SettingsConstants.LOGIN_REWARD);
			showPopup = program.getSettings(SettingsConstants.SHOW_POPUP);
			trayOnClose = program.getSettings(SettingsConstants.TRAY_ON_CLOSE);
			audioNotifyOnce = program.getSettings(SettingsConstants.NOTIFY_AUDIO);
			audioNotifyRepeat = program.getSettings(SettingsConstants.NOTIFY_AUDIO_REPEAT);
			return;
		} else {
			loginReward = false;
			showPopup = false;
			trayOnClose = false;
			audioNotifyOnce = false;
			audioNotifyRepeat = false;
		}
		
		jFileChooser = new JFileChooser();
		jFileChooser.setAcceptAllFileFilterUsed(false);
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setFileFilter(new AudioFileFilter());

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

		jShowPopup = new JCheckBox("Show Popup");
		jShowPopup.setSelected(program.getSettings(SettingsConstants.SHOW_POPUP));
		jShowPopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});

		jTrayOnClose = new JCheckBox("Minimize to tray on close");
		jTrayOnClose.setSelected(program.getSettings(SettingsConstants.TRAY_ON_CLOSE));
		jTrayOnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});

		JLabel jAudioNotify = new JLabel("Audio Notification:");

		ButtonGroup AudioNotify = new ButtonGroup();
		jAudioNotifyNone = new JRadioButton("None");
		jAudioNotifyNone.setSelected(!program.getSettings(SettingsConstants.NOTIFY_AUDIO) && !program.getSettings(SettingsConstants.NOTIFY_AUDIO_REPEAT));
		jAudioNotifyNone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});
		AudioNotify.add(jAudioNotifyNone);

		jAudioNotifyOnce = new JRadioButton("Once");
		jAudioNotifyOnce.setSelected(program.getSettings(SettingsConstants.NOTIFY_AUDIO) && !program.getSettings(SettingsConstants.NOTIFY_AUDIO_REPEAT));
		jAudioNotifyOnce.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});
		AudioNotify.add(jAudioNotifyOnce);

		jAudioNotifyRepeat = new JRadioButton("Repeat");
		jAudioNotifyRepeat.setSelected(program.getSettings(SettingsConstants.NOTIFY_AUDIO_REPEAT));
		jAudioNotifyRepeat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.saveSettings();
			}
		});
		AudioNotify.add(jAudioNotifyRepeat);

		soundPanels.add(new SoundPanel("Default:", "alert.wav"));

		soundPanels.add(new SoundPanel("Login:", "login.wav"));

		soundPanels.add(new SoundPanel("Credits:", "credits.wav"));

		for (Category category : program.getCategories()) {
			soundPanels.add(new SoundPanel(category.getName() + ":", category.getName().toLowerCase() + ".wav"));
		}

		ParallelGroup horizontalGroup = layout.createParallelGroup();
		SequentialGroup verticalGroup = layout.createSequentialGroup();
		horizontalGroup
				.addGroup(layout.createSequentialGroup()
					.addComponent(jAudioNotify)
					.addComponent(jAudioNotifyRepeat)
					.addComponent(jAudioNotifyOnce)
					.addComponent(jAudioNotifyNone)
				);

		verticalGroup.addGroup(layout.createParallelGroup()
					.addComponent(jAudioNotify, 25, 25, 25)
					.addComponent(jAudioNotifyRepeat, 25, 25, 25)
					.addComponent(jAudioNotifyOnce, 25, 25, 25)
					.addComponent(jAudioNotifyNone, 25, 25, 25)
				);
		for (SoundPanel soundPanel : soundPanels) {
			soundPanel.add(layout, horizontalGroup, verticalGroup);
		}

		layout.setHorizontalGroup(
				layout.createSequentialGroup()
					.addGroup(horizontalGroup)
					.addGap(50)
					.addGroup(layout.createParallelGroup()
						.addComponent(jAutoRun)
						.addComponent(jLoginReward)
						.addComponent(jShowPopup)
						.addComponent(jTrayOnClose)
						.addComponent(jLite)
					)
					
		);
		layout.setVerticalGroup(
				layout.createParallelGroup()
				.addGroup(verticalGroup)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jAutoRun, 25, 25, 25)
						.addGap(5)
						.addComponent(jLoginReward, 25, 25, 25)
						.addGap(5)
						.addComponent(jShowPopup, 25, 25, 25)
						.addGap(5)
						.addComponent(jTrayOnClose, 25, 25, 25)
						.addGap(5)
						.addComponent(jLite, 25, 25, 25)
					)
				
		);
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Settings";
	}

	@Override
	public JPanel getPanel() {
		return jPanel;
	}

	@Override
	public Set<SettingsConstants> getSettings() {
		Set<SettingsConstants> settings = EnumSet.noneOf(SettingsConstants.class);
		if (jLite.isSelected()) {
			settings.add(SettingsConstants.LITE);
		}
		if (Program.isLite()) {
			if (loginReward) {
				settings.add(SettingsConstants.LOGIN_REWARD);
			}
			if (showPopup) {
				settings.add(SettingsConstants.SHOW_POPUP);
			}
			if (trayOnClose) {
				settings.add(SettingsConstants.TRAY_ON_CLOSE);
			}
			if (audioNotifyRepeat) {
				settings.add(SettingsConstants.NOTIFY_AUDIO_REPEAT);
			}
			if (audioNotifyOnce) {
				settings.add(SettingsConstants.NOTIFY_AUDIO);
			}
		} else {
			if (jLoginReward.isSelected()) {
				settings.add(SettingsConstants.LOGIN_REWARD);
			}
			if (jShowPopup.isSelected()) {
				settings.add(SettingsConstants.SHOW_POPUP);
			}
			if (jTrayOnClose.isSelected()) {
				settings.add(SettingsConstants.TRAY_ON_CLOSE);
			}
			if (jAudioNotifyRepeat.isSelected()) {
				settings.add(SettingsConstants.NOTIFY_AUDIO_REPEAT);
			} else if (jAudioNotifyOnce.isSelected()) {
				settings.add(SettingsConstants.NOTIFY_AUDIO);
			}
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

	private void disable() {
		for (SoundPanel soundPanel : soundPanels) {
			soundPanel.setEnabled(false);
		}
	}

	private void enable() {
		for (SoundPanel soundPanel : soundPanels) {
			soundPanel.updateNotify();
		}
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

	private class SoundPanel {
		private final JButton jBrowse;
		private final JButton jReset;
		private final JButton jPlay;
		private final JLabel jLabel;
		private final String filename;

		public SoundPanel(final String label, final String filename) {
		//Init
			jLabel = new JLabel(label);
			this.filename = filename;

			jBrowse = new JButton(Images.BROWSE.getIcon());
			jBrowse.setToolTipText("Set notification sound (WAV files only)");
			FastToolTips.install(jBrowse);

			jReset = new JButton(Images.NONE.getIcon());
			jReset.setToolTipText("Reset to default");
			FastToolTips.install(jReset);

			jPlay = new JButton(Images.PLAY.getIcon());
			jPlay.setToolTipText("Play/Stop");
			FastToolTips.install(jPlay);

		//Action Listeners
			jPlay.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (testAudio == null) {
						testAudio = new Thread(new Runnable() {
							@Override
							public void run() {
								program.audioStartTest(filename);
								enable();
								jPlay.setIcon(Images.PLAY.getIcon());
								testAudio = null;
							}
						});
						disable();
						jPlay.setEnabled(true);
						jPlay.setIcon(Images.STOP.getIcon());
						testAudio.start();
					} else {
						enable();
						jPlay.setIcon(Images.PLAY.getIcon());
						program.audioStopTest();
					}
				}
			});

			jBrowse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int value = jFileChooser.showOpenDialog(program.getWindow());
					if (value == JFileChooser.APPROVE_OPTION) {
						File file = jFileChooser.getSelectedFile();
						FileConstants.copyFile(file, FileConstants.getAudioLocal(filename));
						final TestAudio testAudio = new TestAudio(filename, getThis());
						JLockWindow jLockWindow = new JLockWindow(program.getWindow());
						jLockWindow.show(testAudio, "Testing audio file...");
					}
				}
			});

			jReset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					FileConstants.getAudioLocal(filename).delete();
					updateNotify();
				}
			});
		//Default
			updateNotify();
		}

		private SoundPanel getThis() {
			return this;
		}

		private void updateNotify() {
			boolean audio = FileConstants.getAudioLocal(filename).exists();
			if (audio) {
				jReset.setEnabled(true);
				jPlay.setEnabled(true);
			} else {
				jReset.setEnabled(false);
				jPlay.setEnabled(false);
			}
			jBrowse.setEnabled(true);
		}

		private void setEnabled(boolean b) {
			jBrowse.setEnabled(b);
			jReset.setEnabled(false);
			jPlay.setEnabled(false);
		}

		private void add(GroupLayout layout, ParallelGroup horizontalGroup, SequentialGroup verticalGroup) {
			//Layout
			horizontalGroup
				.addGroup(layout.createSequentialGroup()
					.addComponent(jLabel, 75, 75, 75)
					.addComponent(jBrowse)
					.addComponent(jReset)
					.addComponent(jPlay)
				);
			verticalGroup
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(jLabel)
					.addComponent(jBrowse)
					.addComponent(jReset)
					.addComponent(jPlay)
				);
		}
	}

	private class TestAudio implements JLockWindow.StartAndStop {

		private final String filename;
		private final SoundPanel soundPanel;
		private boolean ok = false;

		public TestAudio(String filename, SoundPanel soundPanel) {
			this.filename = filename;
			this.soundPanel = soundPanel;
		}

		@Override
		public void start() {
			ok = program.audioStartTest(filename);
			if (!ok) {
				JOptionPane.showMessageDialog(program.getWindow(), "Audio file not supported", "Audio File Test", JOptionPane.ERROR_MESSAGE);
				FileConstants.getAudioLocal(filename).delete();
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					soundPanel.updateNotify();
				}
			});
		}

		@Override
		public void stop() {
			program.audioStopTest();
		}
	}
}