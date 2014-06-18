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

package net.nikr.warframe.gui.invasion;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.FilterTool;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.gui.shared.listeners.InvasionListener;
import net.nikr.warframe.gui.shared.listeners.NotifyListener.NotifySource;
import net.nikr.warframe.gui.shared.table.EnumTableFormat;
import net.nikr.warframe.gui.shared.table.EventModels;
import net.nikr.warframe.gui.shared.table.InvertMatcher;
import net.nikr.warframe.gui.shared.table.PaddingTableCellRenderer;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.invasion.Invasion.InvasionPercentage;
import net.nikr.warframe.io.shared.FastToolTips;


public class InvasionTool extends FilterTool implements Tool, InvasionListener {

	private final JRadioButton jAll;
	private final JRadioButton jNotify;
	private final JRadioButton jIgnore;
	private final JSlider jCredits;
	private final JToggleButton jKillCorpus;
	private final JToggleButton jKillGrineer;
	private final JToggleButton jKillInfested;
	private final JToggleButton jHelpCorpus;
	private final JToggleButton jHelpGrineer;
	private final JTable jTable;

	private final DefaultEventSelectionModel<Invasion> selectionModel;
	private final DefaultEventTableModel<Invasion> eventTableModel;

	private final EventList<Invasion> eventList = new BasicEventList<Invasion>();
	private final FilterList<Invasion> filterList;
	private final FilterList<Invasion> showList;
	private Matcher<Invasion> matcher = null;

	public InvasionTool(final Program program) {
		super(program);

		jAll = new JRadioButton("All");
		jAll.setSelected(true);
		jAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showList.setMatcher(null);
			}
		});
		jNotify = new JRadioButton("Notify");
		jNotify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showList.setMatcher(matcher);
			}
		});
		jIgnore = new JRadioButton("Ignore");
		jIgnore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showList.setMatcher(new InvertMatcher<Invasion>(matcher));
			}
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jAll);
		buttonGroup.add(jNotify);
		buttonGroup.add(jIgnore);

		JToggleButton jFilters = new JToggleButton("Filters");
		jFilters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b = !jCredits.isVisible();
				for (JComponent component : filterComponents) {
					component.setVisible(b);
				}
				jPanel.validate();
			}
		});

		JLabel jHelp = new JLabel(Images.HELP.getIcon());
		FastToolTips.install(jHelp);
		jHelp.setToolTipText("<html><body>"
				+ "<b>Show:</b> Hover mouse over reward cell<br>");

		jCredits = new JSlider(JSlider.HORIZONTAL, 0, 3, 0);
		jCredits.setMinorTickSpacing(0);
		jCredits.setMajorTickSpacing(1);
		jCredits.setPaintTicks(true);
		jCredits.setSnapToTicks(true);
		Dictionary<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(0, new JLabel("All"));
		labelTable.put(1, new JLabel("25k"));
		labelTable.put(2, new JLabel("35k"));
		labelTable.put(3, new JLabel("50K"));
		jCredits.setLabelTable(labelTable);
		jCredits.setPaintLabels(true);
		jCredits.setVisible(false);
		if (program.getSettings(SettingsConstants.INVASION_CREDITS_25K)) {
			jCredits.setValue(1);
		} else if (program.getSettings(SettingsConstants.INVASION_CREDITS_35K)) {
			jCredits.setValue(2);
		} else if (program.getSettings(SettingsConstants.INVASION_CREDITS_NONE)) {
			jCredits.setValue(3);
		} else { //No settings or INVASION_CREDITS_ALL
			jCredits.setValue(0);
		}
		jCredits.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				filter();
				program.saveSettings();
			}
		});
		filterComponents.add(jCredits);

		JLabel jKillLabel = new JLabel("Kill");
		jKillLabel.setVisible(false);
		filterComponents.add(jKillLabel);
		//Faction
		jKillCorpus = createToggleButton(Images.CORPUS.getIcon(), "Corpus", null, program.getSettings(SettingsConstants.INVASION_CORPUS));
		jKillGrineer = createToggleButton(Images.GRINEER.getIcon(), "Grineer", null, program.getSettings(SettingsConstants.INVASION_GRINEER));
		jKillInfested = createToggleButton(Images.INFESTATION.getIcon(), "Infestation", null, program.getSettings(SettingsConstants.INVASION_INFESTED));

		JLabel jHelpLabel = new JLabel("Help");
		jHelpLabel.setVisible(false);
		filterComponents.add(jHelpLabel);

		//Faction
		jHelpCorpus = createToggleButton(Images.CORPUS.getIcon(), "Corpus", null, program.getSettings(SettingsConstants.INVASION_HELP_CORPUS));
		jHelpGrineer = createToggleButton(Images.GRINEER.getIcon(), "Grineer", null, program.getSettings(SettingsConstants.INVASION_HELP_GRINEER));

		addColumn1(jKillLabel);
		addColumn2(jKillCorpus);
		addColumn3(jKillGrineer);
		addColumn4(jKillInfested);
		addColumn1(jHelpLabel);
		addColumn2(jHelpCorpus);
		addColumn3(jHelpGrineer);

		filterList = new FilterList<Invasion>(eventList);
		showList = new FilterList<Invasion>(eventList);
		//Table Format
		TableFormat<Invasion> tableFormat = new EnumTableFormat<Invasion, InvasionTableFormat>(InvasionTableFormat.class);
		//Table Model
		eventTableModel = EventModels.createTableModel(showList, tableFormat);
		//Table
		jTable = new JInvasionTable(eventTableModel);
		jTable.setDefaultRenderer(InvasionPercentage.class, new ProgressCellRenderer());
		jTable.getTableHeader().setReorderingAllowed(true);
		//Padding
		PaddingTableCellRenderer.install(jTable, 3);
		//Selection Model
		selectionModel = EventModels.createSelectionModel(showList);
		selectionModel.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE);
		jTable.setSelectionModel(selectionModel);
		//Listener
		eventTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == -1) {
					for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
						Invasion invasion  = eventTableModel.getElementAt(i);
						if (invasion.isDone()) {
							program.doneAdd(invasion.getId());
						} else {
							program.doneRemove(invasion.getId());
						}
					}
					updateStatusBar();
				}
			}
		});

		JScrollPane jTableScroll = new JScrollPane(jTable);

		layout.setHorizontalGroup(
				layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(jAll)
						.addComponent(jNotify)
						.addComponent(jIgnore)
						.addGap(0, 0, Integer.MAX_VALUE)
						.addComponent(jFilters)
						.addGap(10)
						.addComponent(jHelp)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jTableScroll, 0, 0, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup()
								.addComponent(jCredits, 170, 170, 170)
								.addGroup(categoryHorizontalGroup)
						)
				)
		);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jAll)
						.addComponent(jNotify)
						.addComponent(jIgnore)
						.addComponent(jFilters)
						.addComponent(jHelp)
				)
				.addGroup(layout.createParallelGroup()
						.addComponent(jTableScroll, 0, 0, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(jCredits)
								.addGap(20)
								.addGroup(createRow(jKillLabel, jKillCorpus, jKillGrineer, jKillInfested))
								.addGroup(createRow(jHelpLabel, jHelpCorpus, jHelpGrineer, null))
								.addGap(20)
								.addGroup(categoryVerticalGroup)
						)
				)
		);
		filter();
	}

	private void updateStatusBar() {
		program.setInvasions(filterList.size(), eventList.size());
	}

	@Override
	public void addInvasions(List<Invasion> invasions) {
		List<Invasion> cache = new ArrayList<Invasion>(filterList);

		try {
			eventList.getReadWriteLock().writeLock().lock();
			eventList.clear();
			eventList.addAll(invasions);
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}

		int count = 0;
		Set<String> categories = new HashSet<String>();
		for (Invasion invasion : filterList) {
			if (!cache.contains(invasion)) {
				count++;
				if (invasion.isMatchDefending()) {
					Category defendingCategory = invasion.getDefendingCategory();
					if (defendingCategory != null) {
						categories.add(defendingCategory.getName());
					}
				}
				if (invasion.isMatchInvading()) {
					Category invadingCategory = invasion.getInvadingCategory();
					if (invadingCategory != null) {
						categories.add(invadingCategory.getName());
					}
				}
				
			}
		}
		if (count > 0) {
			program.startNotify(count, NotifySource.INVASIONS, categories);
		}
		updateStatusBar();
	}

	@Override
	public String getTitle() {
		return "Invasions";
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return Images.INVASION.getIcon();
	}

	@Override
	public JPanel getPanel() {
		return jPanel;
	}

	@Override
	public Set<SettingsConstants> getSettings() {
		InvasionSettings settings = new InvasionSettings(jCredits.getValue(),
				jKillCorpus.isSelected(),
				jKillGrineer.isSelected(),
				jKillInfested.isSelected(),
				jHelpCorpus.isSelected(),
				jHelpGrineer.isSelected());
		return settings.getSettings();
	}

	@Override
	public final void filter() {
		matcher = new InvasionMatcher(jCredits.getValue(),
				jKillCorpus.isSelected(),
				jKillGrineer.isSelected(),
				jKillInfested.isSelected(),
				jHelpCorpus.isSelected(),
				jHelpGrineer.isSelected(),
				getCategoryFilters(),
				program.getFilters());
		filterList.setMatcher(matcher);
		if (jNotify.isSelected()) {
			showList.setMatcher(matcher);
		}
		if (jIgnore.isSelected()) {
			showList.setMatcher(new InvertMatcher<Invasion>(matcher));
		}
		jTable.updateUI();
		updateStatusBar();
	}
}
