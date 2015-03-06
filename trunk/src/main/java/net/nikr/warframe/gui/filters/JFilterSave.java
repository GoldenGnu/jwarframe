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
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.nikr.warframe.Program;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ListWriter;


public class JFilterSave {
	private final JDialog jDialog;
	private final JComboBox jFilter;
	private final JButton jOK;
	private final JButton jCancel;

	private final EventList<String> eventList;

	private final Program program;

	public JFilterSave(final Program program) {
		this.program = program;

		jDialog = new JDialog(program.getWindow(), "Save List", Dialog.ModalityType.APPLICATION_MODAL);

		JPanel jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		jFilter = new JComboBox();
		eventList = new BasicEventList<String>();
		SortedList<String> sortedList = new SortedList<String>(eventList);
		AutoCompleteSupport.install(jFilter, sortedList, new Filterator());

		jOK = new JButton("OK");
		jOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = (String) jFilter.getSelectedItem();
				String filename = name + ".dat";
				boolean ok = Pattern.matches("[\\w ]+", name);
				if (program.getFilterSets().contains(filename)) {
					int value = JOptionPane.showConfirmDialog(jDialog, "Overwrite?", "Save Filter Set", JOptionPane.OK_CANCEL_OPTION);
					if (value != JOptionPane.OK_OPTION) {
						ok = false;
					}
				}
				if (ok) {
					ListWriter listWriter = new ListWriter();
					program.getFilterSets().add(filename);
					program.getMainFrame().updateFilters();
					listWriter.save(program.getFilters(), FileConstants.getFilterSet(filename));
					jDialog.setVisible(false);
				} else {
					JOptionPane.showMessageDialog(jDialog, "Only letters, numbers, and space allowed in name", "Invalid Name", JOptionPane.PLAIN_MESSAGE);
				}
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
			layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
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

		try {
			eventList.getReadWriteLock().writeLock().lock();
			eventList.clear();
			for (String s : program.getFilterSets()) {
				eventList.add(s.replace(".dat", ""));
			}
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}

		jDialog.pack();

		if (jDialog.isResizable()) {
			jDialog.setMinimumSize(jDialog.getSize());
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

	private static class Filterator implements TextFilterator<String> {
		@Override
		public void getFilterStrings(final List<String> baseList, final String element) {
			if (!element.isEmpty()) {
				baseList.add(element);
			}
		}
	}
}
