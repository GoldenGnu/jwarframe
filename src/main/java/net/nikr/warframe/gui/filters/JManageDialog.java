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

package net.nikr.warframe.gui.filters;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.nikr.warframe.Program;
import net.nikr.warframe.io.shared.FileConstants;


public class JManageDialog {

	private enum ManageDialogAction {
		DONE,
		LOAD,
		RENAME,
		DELETE
	}

	private final DefaultListModel listModel;
	private final JList jList;
	private final JButton jDelete;
	private final JButton jLoad;
	private final JButton jRename;
	private final JButton jDone;
	private final JDialog jDialog;
	private final Program program;

	private boolean supportMerge = true;

	public JManageDialog(Program program) {
		this.program = program;

		ListenerClass listener = new ListenerClass();

		jDialog = new JDialog(program.getWindow(), JDialog.DEFAULT_MODALITY_TYPE);
		jDialog.setTitle("Manage Lists");
		jDialog.setResizable(false);

		JPanel jPanel = new JPanel();

		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		jDialog.add(jPanel);

		//Load
		jLoad = new JButton("Load");
		jLoad.setActionCommand(ManageDialogAction.LOAD.name());
		jLoad.addActionListener(listener);

		//Rename
		jRename = new JButton("Rename");
		jRename.setActionCommand(ManageDialogAction.RENAME.name());
		jRename.addActionListener(listener);

		//Delete
		jDelete = new JButton("Delete");
		jDelete.setActionCommand(ManageDialogAction.DELETE.name());
		jDelete.addActionListener(listener);

		//List
		listModel = new DefaultListModel();
		jList = new JList(listModel);
		jList.addMouseListener(listener);
		jList.addListSelectionListener(listener);
		JScrollPane jScrollPanel = new JScrollPane(jList);
		jPanel.add(jScrollPanel);

		//Done
		jDone = new JButton("Close");
		jDone.setActionCommand(ManageDialogAction.DONE.name());
		jDone.addActionListener(listener);
		jPanel.add(jDone);

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(jScrollPanel, 282, 282, 282)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
						.addComponent(jLoad, 90, 90, 90)
					)
					.addGroup(layout.createParallelGroup()
						.addComponent(jRename, 90, 90, 90)
					)
					.addGroup(layout.createParallelGroup()
						.addComponent(jDelete, 90, 90, 90)
						.addComponent(jDone, 90, 90, 90)
					)
				)
			)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLoad)
					.addComponent(jRename)
					.addComponent(jDelete)
				)
				.addComponent(jScrollPanel)
				.addComponent(jDone)
		);
	}

	public void show() {
		jDialog.pack();
		//Get the parent size
		Dimension parentSize = program.getWindow().getSize();

		//Calculate the frame location
		int x = (parentSize.width - jDialog.getWidth()) / 2;
		int y = (parentSize.height - jDialog.getHeight()) / 2;

		//Set the new frame location
		jDialog.setLocation(x, y);
		jDialog.setLocationRelativeTo(program.getWindow());
		jDialog.setVisible(true);
	}

	public boolean isSupportMerge() {
		return supportMerge;
	}

	public void setSupportMerge(boolean supportMerge) {
		this.supportMerge = supportMerge;
	}

	private String getSelectedString() {
		int selectedIndex =  jList.getSelectedIndex();
		if (selectedIndex != -1) {
			return (String) listModel.get(jList.getSelectedIndex());
		} else {
			return null;
		}
	}

	private void setEnabledAll(boolean b) {
		jDelete.setEnabled(b);
		jLoad.setEnabled(b);
		jRename.setEnabled(b);
	}

	protected final void update(List<String> list) {
		listModel.clear();
		Collections.sort(list);
		for (String filter: list) {
			listModel.addElement(filter);
		}
		if (!listModel.isEmpty()) {
			if (getSelectedString() == null) {
				jList.setSelectedIndex(0);
			}
			setEnabledAll(true);
		} else {
			setEnabledAll(false);
		}
	}

	protected void load(final String name) {
		program.loadFilters(FileConstants.getFilterSet(toFilename(name)));
	}

	protected void merge(final String name, final Object[] objects) {
		
	}

	protected void rename(final String name, final String oldName) {
		program.getFilterSets().remove(toFilename(oldName));
		program.getFilterSets().add(toFilename(name));
		File oldFile = FileConstants.getFilterSet(toFilename(oldName));
		File newFile = FileConstants.getFilterSet(toFilename(name));
		oldFile.renameTo(newFile);
		update();
	}

	protected void delete(final List<String> list) {
		for (String name : list) {
			program.getFilterSets().remove(toFilename(name));
			File file = FileConstants.getFilterSet(toFilename(name));
			file.delete();
		}
		update();
	}

	protected boolean validateName(final String name, final String oldName, final String title) {
		String filename = name + ".dat";
		boolean ok = Pattern.matches("[\\w ]+", name);

		if (program.getFilterSets().contains(filename) && !name.toLowerCase().equals(oldName.toLowerCase())) {
			int value = JOptionPane.showConfirmDialog(jDialog, "Overwrite?", "Save Filter Set", JOptionPane.OK_CANCEL_OPTION);
			if (value != JOptionPane.OK_OPTION) {
				ok = false;
			}
		}
		return ok;
	}

	private String toFilename(String name) {
		return name + ".dat";
	}
	private String toName(String name) {
		return name.replace(".dat", "");
	}

	public void update() {
		List<String> list = new ArrayList<String>();
		for (String s : program.getFilterSets()) {
			list.add(toName(s));
		}
		update(list);
		program.getMainFrame().updateFilters();
	}

	private void delete() {
		List<String> list = new ArrayList<String>();
		for (int index : jList.getSelectedIndices()) {
			String filterName = (String) listModel.get(index);
			list.add(filterName);
		}
		int value;
		if (list.size() > 1) {
			value = JOptionPane.showConfirmDialog(jDialog, "Delete " + list.size() + " filter sets?", "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		} else if (list.size() == 1) {
			value = JOptionPane.showConfirmDialog(jDialog, list.get(0), "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		} else {
			return;
		}
		if (value == JOptionPane.YES_OPTION) {
			delete(list);
		}
	}

	private void load() {
		String name = getSelectedString();
		if (name == null) {
			return;
		}
		load(name);
	}

	private void merge() {
		String name = showNameDialog("", "", "Merge");
		if (name == null) {
			return;
		}

		merge(name, jList.getSelectedValues());
	}

	private String showNameDialog(final String oldValue, final String oldName, final String title) {
		//Show dialog
		String name = (String) JOptionPane.showInputDialog(jDialog, "Enter Name", title, JOptionPane.PLAIN_MESSAGE, null, null, oldValue);
		if (name == null) { //Cancel (do nothing)
			return null;
		}

		if (name.equals("")) { //No input (needed for name)
			JOptionPane.showMessageDialog(jDialog, "Name can not be empty", title, JOptionPane.PLAIN_MESSAGE);
			return showNameDialog(name, oldName, title);
		}

		if (!validateName(name, oldName, title)) {
			return showNameDialog(name, oldName, title);
		}
		return name;
	}

	private void rename() {
		//Get selected filter name
		String selectedName = getSelectedString();
		if (selectedName == null) {
			return;
		}

		String name = showNameDialog(selectedName, selectedName, "Rename");
		if (name == null) {
			return;
		}
		rename(name, selectedName);
	}

	private class ListenerClass implements ActionListener, MouseListener, ListSelectionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (ManageDialogAction.DONE.name().equals(e.getActionCommand())) {
				jDialog.setVisible(false);
			}
			if (ManageDialogAction.LOAD.name().equals(e.getActionCommand())) {
				if (jList.getSelectedIndices().length == 1) {
					load();
				} else {
					merge();
				}
			}
			if (ManageDialogAction.RENAME.name().equals(e.getActionCommand())) {
				rename();
			}
			if (ManageDialogAction.DELETE.name().equals(e.getActionCommand())) {
				delete();
			}
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			Object o = e.getSource();
			if (o instanceof JList && e.getClickCount() == 2
					&& !e.isControlDown() && !e.isShiftDown()) {
				load();
			}
		}

		@Override
		public void mousePressed(final MouseEvent e) { }

		@Override
		public void mouseReleased(final MouseEvent e) { }

		@Override
		public void mouseEntered(final MouseEvent e) { }

		@Override
		public void mouseExited(final MouseEvent e) { }

		@Override
		public void valueChanged(final ListSelectionEvent e) {
			if (jList.getSelectedIndices().length > 1) {
				if (supportMerge) {
					jLoad.setText("Merge");
				} else {
					jLoad.setEnabled(false);
				}
				jRename.setEnabled(false);
			} else {
				jLoad.setText("Load");
				jLoad.setEnabled(true);
				jRename.setEnabled(true);
			}
		}
	}
	
}
