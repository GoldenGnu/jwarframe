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

package net.nikr.warframe.gui.filters;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.reward.RewardID;


public class JFilterAdder {

	private final JDialog jDialog;
	private final JComboBox jFilter;
	private final JButton jOK;
	private final JButton jCancel;

	private final EventList<RewardID> eventList;

	private final Program program;

	public JFilterAdder(final Program program) {
		this.program = program;

		jDialog = new JDialog(program.getWindow(), "Add Ignore", Dialog.ModalityType.APPLICATION_MODAL);

		JPanel jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		jFilter = new JComboBox();
		eventList = new BasicEventList<RewardID>();
		SortedList<RewardID> sortedList = new SortedList<RewardID>(eventList);
		AutoCompleteSupport.install(jFilter, sortedList, new Filterator());

		jOK = new JButton("OK");
		jOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = jFilter.getSelectedItem();
				if (selected instanceof String) {
					program.getFiltersTool().add((String) selected);
				} else if (selected instanceof RewardID) {
					RewardID rewardID = (RewardID) selected;
					program.getFiltersTool().add(rewardID.getName());
				}
				jDialog.setVisible(false);
			}
		});

		jCancel = new JButton("Cancel");
		jCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jDialog.setVisible(false);
			}
		});


		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(jFilter, 200, 200, 200)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jOK, 80, 80, 80)
					.addComponent(jCancel, 80, 80, 80)
				)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(jFilter)
				.addGroup(layout.createParallelGroup()
					.addComponent(jOK)
					.addComponent(jCancel)
				)
		);
		jDialog.getContentPane().add(jPanel);
	}

	public void show() {
		//Clear Data
		jFilter.setSelectedItem("");

		//TODO - is this thread safe?
		if (eventList.isEmpty()) { //Load data
			try {
				eventList.getReadWriteLock().writeLock().lock();
				eventList.addAll(program.getRewards());
			} finally {
				eventList.getReadWriteLock().writeLock().unlock();
			}
			jDialog.pack();

			if (jDialog.isResizable()) {
				jDialog.setMinimumSize(jDialog.getSize());
			}
		}
		//Get the parent size
		Dimension screenSize = program.getWindow().getSize();

		//Calculate the frame location
		int x = (screenSize.width - jDialog.getWidth()) / 2;
		int y = (screenSize.height - jDialog.getHeight()) / 2;

		//Set the new frame location
		jDialog.setLocation(x, y);
		jDialog.setLocationRelativeTo(program.getWindow());

		jDialog.setVisible(true);
		jFilter.requestFocusInWindow();
	}

	private static class Filterator implements TextFilterator<RewardID> {
		@Override
		public void getFilterStrings(final List<String> baseList, final RewardID element) {
			if (element.getName().length() > 0) {
				baseList.addAll(Arrays.asList(element.getName().split(" ")));
			}
		}
	}
}
