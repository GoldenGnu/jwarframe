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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.SimpleListModel;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.io.alert.Alert;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.shared.FastToolTips;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ImageGetter;


public class FiltersTool implements Tool {

	private final JManageDialog jManageDialog;
	private final JPanel jPanel;

	private final SimpleListModel<String> listModel;

	private final Program program;

	public FiltersTool(final Program program) {
		this.program = program;

		if (Program.isLite()) {
			jManageDialog = null;
			jPanel = null;
			listModel = new SimpleListModel<String>(program.getFilters());
			return;
		}
		final JFilterAdder jFilterAdder = new JFilterAdder(program);
		final JFilterSave jFilterSave = new JFilterSave(program);
		jManageDialog = new JManageDialog(program);
		jManageDialog.setSupportMerge(false);

		jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JButton jAddFilter = new JButton("Add Ignore");
		jAddFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jFilterAdder.show();
			}
		});

		JButton jSaveFilters = new JButton("Save List");
		jSaveFilters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jFilterSave.show();
			}
		});

		JButton jManageFilters = new JButton("Manage Lists");
		jManageFilters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jManageDialog.update();
				jManageDialog.show();
			}
		});

		final JLabel jImage = new JLabel();
		jImage.setHorizontalTextPosition(JLabel.CENTER);
		jImage.setHorizontalAlignment(JLabel.CENTER);
		jImage.setVerticalTextPosition(JLabel.TOP);
		jImage.setFont(new Font(jPanel.getFont().getName(), Font.BOLD, (jPanel.getFont().getSize() + 5)));

		JLabel jHelp = new JLabel(Images.HELP.getIcon());
		FastToolTips.install(jHelp);
		jHelp.setToolTipText("<html><body>"
				+ "<b>Show:</b> Single click list<br>"
				+ "<b>Delete:</b> Double click list<br>");

		listModel = new SimpleListModel<String>(program.getFilters());
		final JList jFilterList = new JList(listModel);
		jFilterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jFilterList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				String s = (String) jFilterList.getSelectedValue();
				jImage.setText(s);
				jImage.setIcon(ImageGetter.getIcon(s));
			}
		});
		jFilterList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isShiftDown() && !e.isControlDown()) {
					String loot = (String) jFilterList.getSelectedValue();
					int value = JOptionPane.showConfirmDialog(program.getWindow(), loot, "Remove Ignore", JOptionPane.OK_CANCEL_OPTION);
					if (value == JOptionPane.OK_OPTION) {
						remove(loot);
					}
				}
			}
		});
		final JScrollPane jFiltersScroll = new JScrollPane(jFilterList);

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(jAddFilter)
					.addComponent(jSaveFilters)
					.addComponent(jManageFilters)
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jHelp)
				)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jFiltersScroll)
					.addComponent(jImage, 300, 300, 300)
				)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(jAddFilter)
					.addComponent(jSaveFilters)
					.addComponent(jManageFilters)
					.addComponent(jHelp)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(jFiltersScroll)
					.addComponent(jImage)
				)
		);
	}
	
	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Filters";
	}

	@Override
	public JPanel getPanel() {
		return jPanel;
	}

	@Override
	public Set<SettingsConstants> getSettings() {
		return EnumSet.noneOf(SettingsConstants.class);
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	public void loadFilterSet(String setName) {
		//jManageDialog.load(setName);
		program.loadFilters(FileConstants.getFilterSet(setName + ".dat"));
	}

	public void updateFilters() {
		listModel.clear();
		listModel.addAll(program.getFilters());
	}

	public void remove(String loot) {
		if (loot == null) {
			return;
		}
		boolean removed = program.getFilters().remove(loot);
		if (removed) {
			listModel.remove(loot);
			program.saveFilters();
		}
	}

	public void add(String loot) {
		if (loot == null) {
			return;
		}
		boolean added = program.getFilters().add(loot);
		if (added) {
			listModel.add(loot);
			program.saveFilters();
		}
	}

	public void toggle(String loot) {
		if (program.getFilters().contains(loot)) {
			remove(loot);
		} else {
			add(loot);
		}
	}

	public void update(Alert alert) {
		if (!alert.hasLoot()) {
			return;
		}
		RewardID rewardID = alert.getRewordID();
		if (alert.isIgnored()) {
			add(rewardID.getName());
		} else {
			remove(rewardID.getName());
		}
	}

	public void update(Invasion invasion) {
		if (invasion.getDefendingRewardID() != null) {
			RewardID rewardID = invasion.getDefendingRewardID();
			if (invasion.isDefendingIgnored()) {
				add(rewardID.getName());
			} else {
				remove(rewardID.getName());
			}
		}
		if (invasion.getInvadingRewardID() != null) {
			RewardID rewardID = invasion.getInvadingRewardID();
			if (invasion.isInvadingIgnored()) {
				add(rewardID.getName());
			} else {
				remove(rewardID.getName());
			}
		}
	}
}
