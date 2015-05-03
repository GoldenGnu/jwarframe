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
import java.util.Collection;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
	private final JTable jTable;

	private final DefaultEventSelectionModel<Invasion> selectionModel;
	private final DefaultEventTableModel<Invasion> eventTableModel;

	private List<KillHelpContainer> killHelpContainers;
	private final EventList<Invasion> eventList = new BasicEventList<Invasion>();
	private final FilterList<Invasion> filterList;
	private final FilterList<Invasion> showList;
	private Matcher<Invasion> matcher = null;

	public InvasionTool(final Program program) {
		super(program, new InvasionMissionTypes());

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
				try {
					eventList.getReadWriteLock().readLock().lock();
					for (Invasion invasion : eventList) {
						program.getFiltersTool().update(invasion);
						if (invasion.isDone()) {
							program.doneAdd(invasion.getId());
						} else {
							program.doneRemove(invasion.getId());
						}
					}
				} finally {
					eventList.getReadWriteLock().readLock().unlock();
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
						//.addComponent(jMissionTypes)
						.addGroup(horizontalGroup)
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
							.addGap(15)
							//.addGroup(createRow(jMissionTypesLabel, jMissionTypes, null, null))
							//.addGap(15)
							.addGroup(verticalGroup)
						)
				)
		);
		filter();
	}

	private void updateStatusBar() {
		try {
			eventList.getReadWriteLock().readLock().lock();
			program.setInvasions(filterList.size(), eventList.size());
		} finally {
			eventList.getReadWriteLock().readLock().unlock();
		}
	}

	@Override
	public void addInvasions(List<Invasion> invasions) {
		List<Invasion> cache = new ArrayList<Invasion>(filterList);

		for (Invasion invasion : invasions) {
			invasion.setIgnored(program.getFilters());
		}

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
				if (invasion.isMatchDefendingLoot()) {
					Category defendingCategory = invasion.getDefendingCategory();
					if (defendingCategory != null) {
						categories.add(defendingCategory.getName());
					}
				}
				if (invasion.isMatchDefendingCredits()) {
					categories.add("credits");
				}
				if (invasion.isMatchInvadingLoot()) {
					Category invadingCategory = invasion.getInvadingCategory();
					if (invadingCategory != null) {
						categories.add(invadingCategory.getName());
					}
				}
				if (invasion.isMatchInvadingCredits()) {
					categories.add("credits");
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
		InvasionSettings settings = new InvasionSettings(jCredits.getValue());
		return settings.getSettings();
	}

	@Override
	public final void filter() {
		try {
			eventList.getReadWriteLock().readLock().lock();
			for (Invasion invasion : eventList) {
				invasion.setIgnored(program.getFilters());
			}
		} finally {
			eventList.getReadWriteLock().readLock().unlock();
		}
		matcher = new InvasionMatcher(jCredits.getValue(),
				getCategoryFilters(),
				program.getFilters(),
				getFilterMissionTypesStrings(),
				getToolName(),
				getKillHelpEnum());
		filterList.setMatcher(matcher);
		if (jNotify.isSelected()) {
			showList.setMatcher(matcher);
		}
		if (jIgnore.isSelected()) {
			showList.setMatcher(new InvertMatcher<Invasion>(matcher));
		}
		for (int row = 0; row < eventTableModel.getRowCount(); row++) {
			eventTableModel.fireTableCellUpdated(row, 0);
		}
		updateStatusBar();
	}

	@Override
	public Collection<JMenuItem> getMenuItems(String categoryName) {
		KillHelpContainer container = new KillHelpContainer(categoryName);
		if (killHelpContainers == null) {
			killHelpContainers = new ArrayList<KillHelpContainer>();
		}
		killHelpContainers.add(container);
		return container.getItems();
	}

	public Set<String> getKillHelpSettings() {
		Set<String> settings = new HashSet<String>();
		for (KillHelpContainer container : killHelpContainers) {
			settings.addAll(container.getSettings());
		}
		return settings;
	}

	public Map<String, Set<KillHelp>> getKillHelpEnum() {
		Map<String, Set<KillHelp>> settings = new HashMap<String, Set<KillHelp>>();
		for (KillHelpContainer container : killHelpContainers) {
			settings.put(container.getCategory(), container.getEnumValues());
		}
		return settings;
	}

	private class KillHelpContainer {
		JCheckBoxMenuItem jKillCorpus;
		JCheckBoxMenuItem jKillGrineer;
		JCheckBoxMenuItem jKillInfested;
		JCheckBoxMenuItem jHelpCorpus;
		JCheckBoxMenuItem jHelpGrineer;
		private final String category;
		private final List<JCheckBoxMenuItem> items = new ArrayList<JCheckBoxMenuItem>();

		public KillHelpContainer(String category) {
			this.category = category;

			jKillCorpus = createCheckBoxMenuItem(Images.CORPUS.getIcon(), "Kill Corpus");
			items.add(jKillCorpus);
			jKillGrineer = createCheckBoxMenuItem(Images.GRINEER.getIcon(), "Kill Grineer");
			items.add(jKillGrineer);
			jKillInfested = createCheckBoxMenuItem(Images.INFESTATION.getIcon(), "Kill Infestation");
			items.add(jKillInfested);
			items.add(null); //Separator
			jHelpCorpus = createCheckBoxMenuItem(Images.CORPUS.getIcon(), "Help Corpus");
			items.add(jHelpCorpus);
			jHelpGrineer = createCheckBoxMenuItem(Images.GRINEER.getIcon(), "Help Grineer");
			items.add(jHelpGrineer);
		}

		public List<JMenuItem> getItems() {
			return new ArrayList<JMenuItem>(items);
		}

		public Set<String> getSettings() {
			Set<String> settings = new HashSet<String>();
			for (JCheckBoxMenuItem item : items) {
				if (item != null && !item.isSelected()) {
					settings.add(category + item.getText());
				}
			}
			return settings;
		}

		public Set<KillHelp> getEnumValues() {
			Set<KillHelp> settings = EnumSet.noneOf(KillHelp.class);
			if (jKillCorpus.isSelected()) {
				settings.add(KillHelp.KILL_CORPUS);
			}
			if (jKillGrineer.isSelected()) {
				settings.add(KillHelp.KILL_GRINEER);
			}
			if (jKillInfested.isSelected()) {
				settings.add(KillHelp.KILL_INFESTATION);
			}
			if (jHelpCorpus.isSelected()) {
				settings.add(KillHelp.HELP_CORPUS);
			}
			if (jHelpGrineer.isSelected()) {
				settings.add(KillHelp.HELP_GRINEER);
			}
			return settings;
		}

		public String getCategory() {
			return category;
		}

		private JCheckBoxMenuItem createCheckBoxMenuItem(Icon icon, String text) {
			final JCheckBoxMenuItem jButton = new JCheckBoxMenuItem(text, icon);
			jButton.setToolTipText(text);
			jButton.setSelected(!program.getKillHelp().contains(category + text));
			jButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					filter();
					program.saveKillHelp();
				}
			});
			return jButton;
		}
	}

	public static enum KillHelp {
		KILL_CORPUS(),
		KILL_GRINEER(),
		KILL_INFESTATION(),
		HELP_CORPUS(),
		HELP_GRINEER();
	}
}
