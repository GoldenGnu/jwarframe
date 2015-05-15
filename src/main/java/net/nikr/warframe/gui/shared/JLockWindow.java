/*
 * Copyright 2014-2015 Niklas Kyster Rasmussen
 *
 * This file is part of jWarframe.
 *
 * Original code from jEveAssets (https://code.google.com/p/jeveassets/)
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

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;


public class JLockWindow {

	private final JWindow jWindow;
	private final JLabel jLabel;
	private final Window parent;
	private final JButton jStop;

	private StartAndStop startAndStop;

	public JLockWindow(final Window parent) {
		this.parent = parent;
		jWindow = new JWindow(parent);

		JPanel jPanel = new JPanel();
		jPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		jWindow.add(jPanel);

		jLabel = new JLabel();

		jStop = new JButton("Stop");
		jStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (startAndStop != null) {
					startAndStop.stop();
				}
			}
		});


		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabel)
				.addComponent(jStop, 80, 80, 80)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(jLabel)
				.addComponent(jStop)
		);
	}

	public void show(final StartAndStop startAndStop, final String text) {
		this.startAndStop = startAndStop;
		jLabel.setText(text);
		jWindow.pack();
		//Get the parent size
		Dimension parentSize = parent.getSize();

		//Calculate the frame location
		int x = (parentSize.width - jWindow.getWidth()) / 2;
		int y = (parentSize.height - jWindow.getHeight()) / 2;

		//Set the new frame location
		jWindow.setLocation(x, y);
		jWindow.setLocationRelativeTo(parent);
		parent.setEnabled(false);
		jWindow.setVisible(true);
		Thread thread = new Thread(new Wait(startAndStop));
		thread.start();
	}

	private void hide() {
		parent.setEnabled(true);
		jWindow.setVisible(false);
	}

	class Wait implements Runnable {

		private final StartAndStop startAndStop;

		public Wait(StartAndStop startAndStop) {
			this.startAndStop = startAndStop;
		}

		@Override
		public void run() {
			startAndStop.start();
			hide();
		}
		
	}

	public interface StartAndStop {
		public abstract void stop();
		public abstract void start();
	}
}
