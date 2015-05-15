/*
 * Copyright 2014-2015 Niklas Kyster Rasmussen
 *
 * This file is part of jWarframe.
 *
 * Original code from StackOverflow by mKorbel (http://stackoverflow.com/questions/10123735/get-effective-screen-size-from-java)
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
package net.nikr.warframe.gui.popup;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.listeners.NotifyListener;

public class NotificationPopup implements NotifyListener {

	private static final long serialVersionUID = 1L;
	private final Program program;
	private final JDialog jDialog;
	private final JLabel jWarframe;
	private final JLabel jAlerts;
	private final JLabel jInvasions;
	private final JLabel jLogin;
	private final int y;
	private final int xEnd;
	private final int xStart;
	private Timer fadeIn;
	private Timer fadeOut;
	private final Map<NotifySource, Integer> counts = new EnumMap<NotifySource, Integer>(NotifySource.class);

	public NotificationPopup(final Program program) {
		this.program = program;

		jDialog = new JDialog((Frame) null, false);
		jDialog.setUndecorated(true);
		jDialog.setSize(230, 90);
		jDialog.setAlwaysOnTop(true);
		jDialog.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				program.getWindow().setVisible(true);
				program.getWindow().setState(Frame.NORMAL);
				program.stopNotify();
			}
		});

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(jDialog.getGraphicsConfiguration());
		int taskBarSize = scnMax.bottom;
		xEnd = screenSize.width - jDialog.getWidth();
		xStart = screenSize.width;
		y = screenSize.height - taskBarSize - jDialog.getHeight();

		JPanel jPanel = new BackgroundPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		jDialog.setContentPane(jPanel);

		JButton jClose = new JButton(Images.CLOSE.getIcon());
		jClose.setRolloverIcon(Images.CLOSE_ROLLOVER.getIcon());
		jClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.stopNotify();
			}
		});
		jClose.setUI(new BasicButtonUI());
		jClose.setOpaque(false);
		jClose.setContentAreaFilled(false);
		jClose.setFocusable(false);
		jClose.setBorderPainted(true);
		jClose.setRolloverEnabled(true);

		jWarframe = new JLabel(Images.PROGRAM_64.getIcon());

		jAlerts = new JLabel(Images.NOTIFY_ALERT.getIcon());

		jInvasions = new JLabel(Images.NOTIFY_INVASION.getIcon());
		
		jLogin = new JLabel(Images.NOTIFY_LOGIN.getIcon());

		layout.setHorizontalGroup(
				layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(jWarframe)
					.addGap(15)
					.addGroup(layout.createParallelGroup()
						.addComponent(jAlerts)
						.addComponent(jInvasions)
						.addComponent(jLogin)
					)
					.addGap(10, 10, Integer.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jClose, 24, 24, 24)
				)
		);

		layout.setVerticalGroup(
				layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jWarframe)
					.addGap(0, 0, Integer.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jAlerts)
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(jInvasions)
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(jLogin)
					.addGap(0, 0, Integer.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jClose, 24, 24, 24)
					.addGap(0, 0, Integer.MAX_VALUE)
				)
		);
	}

	private void update() {
		jAlerts.setVisible(false);
		jInvasions.setVisible(false);
		jLogin.setVisible(false);
		for (Map.Entry<NotifySource, Integer> entry : counts.entrySet()) {
			if (entry.getKey() == NotifySource.ALERTS) {
				if (entry.getValue() > 1) {
					jAlerts.setText(entry.getValue() + " new alerts");
				} else {
					jAlerts.setText(entry.getValue() + " new alert");
				}
				jAlerts.setVisible(true);
			}
			if (entry.getKey() == NotifySource.INVASIONS) {
				if (entry.getValue() > 1) {
					jInvasions.setText(entry.getValue() + " new invasions");
				} else {
					jInvasions.setText(entry.getValue() + " new invasion");
				}
				jInvasions.setVisible(true);
			}
			if (entry.getKey() == NotifySource.LOGIN_REWARD) {
				jLogin.setText(" login reward");
				jLogin.setVisible(true);
			}
		}
	}

	private void hide() {
		if (jDialog.isVisible()) {
			jDialog.setLocation(xEnd, y);
			fadeOut = new Timer(10, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int x = jDialog.getX();
					x = x + 10;
					if (x > xStart) {
						x = xStart;
						if (fadeOut != null) {
							fadeOut.stop();
						}
						fadeOut = null;
						jDialog.setVisible(false);
					}
					jDialog.setLocation(x, y);
				}

			});
			fadeOut.setRepeats(true);
			fadeOut.setCoalesce(true);
			fadeOut.start();
		}
	}

	private void show() {
		if (!jDialog.isVisible()) {
			jDialog.setLocation(xStart, y);
			jDialog.setVisible(true);
			jDialog.requestFocus();
			fadeIn = new Timer(10, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int x = jDialog.getX();
					x = x - 10;
					if (x < xEnd) {
						x = xEnd;
						if (fadeIn != null) {
							fadeIn.stop();
						}
						fadeIn = null;
					}
					jDialog.setLocation(x, y);
				}

			});
			fadeIn.setRepeats(true);
			fadeIn.setCoalesce(true);
			fadeIn.start();
		}
	}

	@Override
	public void stopNotify() {
		counts.clear();
		hide();
	}

	@Override
	public void startNotify(int count, NotifySource source, Set<String> categories) {
		if (!program.getSettings(SettingsConstants.SHOW_POPUP)) {
			return;
		}
		Integer now = counts.get(source);
		if (now != null) {
			now = now + count;
		} else {
			now = count;
		}
		counts.put(source, now);
		update();
		show();
	}

	private class BackgroundPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private final LinearGradientPaint lpg = new LinearGradientPaint(0, 0, 0, jDialog.getHeight() / 2,
				new float[]{0f, 0.3f, 1f},
				new Color[]{
					new Color(0.8f, 0.8f, .8f),
					new Color(0.7f, 0.7f, .7f),
					new Color(0.6f, 0.6f, .6f)});

		BackgroundPanel() {
			setOpaque(true);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setPaint(lpg);
			g2d.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
			g2d.setColor(Color.BLACK);
			g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}
}
