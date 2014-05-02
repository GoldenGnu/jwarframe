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
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
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
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.gui.shared.listeners.InvasionListener;
import net.nikr.warframe.gui.shared.listeners.NotifyListener.NotifySource;
import net.nikr.warframe.gui.shared.table.EnumTableFormat;
import net.nikr.warframe.gui.shared.table.EventModels;
import net.nikr.warframe.gui.shared.table.InvertMatcher;
import net.nikr.warframe.gui.shared.table.PaddingTableCellRenderer;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.invasion.Invasion.InvasionPercentage;


public class InvasionTool implements Tool, InvasionListener {

	private final JPanel jPanel;
	private final JRadioButton jAll;
	private final JRadioButton jNotify;
	private final JRadioButton jIgnore;
	private final JSlider jCredits;
	private final JCheckBox jBlueprints;
	private final JCheckBox jMods;
	private final JCheckBox jAuras;
	private final JCheckBox jResources;
	private final JCheckBox jFilters;
	private final JCheckBox jCorpus;
	private final JCheckBox jGrineer;
	private final JCheckBox jInfested;
	private final JTable jTable;

	private final DefaultEventSelectionModel<Invasion> selectionModel;
	private final DefaultEventTableModel<Invasion> eventTableModel;

	private final List<JComponent> filterComponents = new ArrayList<JComponent>();
	private final EventList<Invasion> eventList = new BasicEventList<Invasion>();
	private final FilterList<Invasion> filterList;
	private final FilterList<Invasion> showList;
	private Matcher<Invasion> matcher = null;

	private final Program program;

	public InvasionTool(final Program program) {
		this.program = program;

		jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		jCredits = new JSlider(JSlider.HORIZONTAL, 0, 3, 0);
		jCredits.setMinorTickSpacing(0);
		jCredits.setMajorTickSpacing(1);
		jCredits.setPaintTicks(true);
		jCredits.setSnapToTicks(true);
		Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put(0, new JLabel("All"));
		labels.put(1, new JLabel("25k"));
		labels.put(2, new JLabel("35k"));
		labels.put(3, new JLabel("None"));
		jCredits.setLabelTable(labels);
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

		//Faction
		jCorpus = createCheckBox("Kill Corpus", SettingsConstants.INVASION_CORPUS);
		jGrineer = createCheckBox("Kill Grineer", SettingsConstants.INVASION_GRINEER);
		jInfested = createCheckBox("Kill Infested", SettingsConstants.INVASION_INFESTED);
		//Category
		jBlueprints = createCheckBox("Blueprints", SettingsConstants.INVASION_BLUEPRINT);
		jMods = createCheckBox("Mods", SettingsConstants.INVASION_MOD);
		jAuras = createCheckBox("Auras", SettingsConstants.INVASION_AURA);
		jResources = createCheckBox("Resources", SettingsConstants.INVASION_RESOURCE);
		//Filters
		jFilters = createCheckBox("Ignore", SettingsConstants.INVASION_FILTERS);

		JToggleButton jShowFilters = new JToggleButton("Filters");
		jShowFilters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b = !jCredits.isVisible();
				for (JComponent component : filterComponents) {
					component.setVisible(b);
				}
				jPanel.validate();
			}
		});

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
						.addComponent(jShowFilters)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jTableScroll, 0, 0, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup()
								.addComponent(jCredits, 170, 170, 170)
								.addComponent(jCorpus)
								.addComponent(jGrineer)
								.addComponent(jInfested)
								.addComponent(jBlueprints)
								.addComponent(jMods)
								.addComponent(jAuras)
								.addComponent(jResources)
								.addComponent(jFilters)
						)
				)
		);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(jAll)
						.addComponent(jNotify)
						.addComponent(jIgnore)
						.addComponent(jShowFilters)
				)
				.addGroup(layout.createParallelGroup()
						.addComponent(jTableScroll, 0, 0, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(jCredits)
								.addGap(20)
								.addComponent(jCorpus)
								.addComponent(jGrineer)
								.addComponent(jInfested)
								.addGap(20)
								.addComponent(jBlueprints)
								.addComponent(jMods)
								.addComponent(jAuras)
								.addComponent(jResources)
								.addGap(20)
								.addComponent(jFilters)
						)
				)
		);
		filter();
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
		for (Invasion invasion : filterList) {
			if (!cache.contains(invasion)) {
				count++;
			}
		}
		if (count > 0) {
			program.startNotify(count, NotifySource.INVASIONS);
		}
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
				jCorpus.isSelected(),
				jGrineer.isSelected(),
				jInfested.isSelected(),
				jBlueprints.isSelected(),
				jMods.isSelected(),
				jAuras.isSelected(),
				jResources.isSelected(),
				jFilters.isSelected());
		return settings.getSettings();
	}

	public final void filter() {
		matcher = new InvasionMatcher(jCredits.getValue(),
				jCorpus.isSelected(),
				jGrineer.isSelected(),
				jInfested.isSelected(),
				jBlueprints.isSelected(),
				jMods.isSelected(),
				jAuras.isSelected(),
				jResources.isSelected(),
				jFilters.isSelected(),
				program.getFilters());
		filterList.setMatcher(matcher);
		if (jNotify.isSelected()) {
			showList.setMatcher(matcher);
		}
		if (jIgnore.isSelected()) {
			showList.setMatcher(new InvertMatcher<Invasion>(matcher));
		}
		jTable.updateUI();
	}

	private JCheckBox createCheckBox(String title, SettingsConstants settings) {
		JCheckBox jCheckBox = new JCheckBox(title);
		jCheckBox.setSelected(program.getSettings(settings));
		jCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filter();
				program.saveSettings();
			}
		});
		jCheckBox.setVisible(false);
		filterComponents.add(jCheckBox);
		return jCheckBox;
	}
}
