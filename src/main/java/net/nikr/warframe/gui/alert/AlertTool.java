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

package net.nikr.warframe.gui.alert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.AlertListener;
import net.nikr.warframe.gui.shared.DesktopUtil;
import net.nikr.warframe.gui.shared.EnumTableFormat;
import net.nikr.warframe.gui.shared.EventModels;
import net.nikr.warframe.gui.shared.NotifyListener.NotifySource;
import net.nikr.warframe.gui.shared.PaddingTableCellRenderer;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.io.alert.Alert;


public class AlertTool implements AlertListener, Tool {

	private final JPanel jPanel;
	private final JRadioButton jShowAll;
	private final JRadioButton jShowFiltered;
	private final JSlider jCredits;
	private final JCheckBox jBlueprints;
	private final JCheckBox jMods;
	private final JCheckBox jAuras;
	private final JCheckBox jResources;
	private final JCheckBox jFilters;
	private final Timer timeLeft;

	private final EventList<Alert> eventList = new BasicEventList<Alert>();
	private final FilterList<Alert> filterList;
	private final FilterList<Alert> showList;
	private final DefaultEventSelectionModel<Alert> selectionModel;
	private final DefaultEventTableModel<Alert> eventTableModel;
	private Matcher<Alert> matcher = null;

	private final List<JComponent> filterComponents = new ArrayList<JComponent>();

	private final Program program;

	public AlertTool(final Program program) {
		this.program = program;

		jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		jShowAll = new JRadioButton("All");
		jShowAll.setSelected(true);
		jShowAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showList.setMatcher(null);
			}
		});
		jShowFiltered = new JRadioButton("Matching");
		jShowFiltered.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showList.setMatcher(matcher);
			}
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jShowAll);
		buttonGroup.add(jShowFiltered);

		jCredits = new JSlider(JSlider.HORIZONTAL, 0, 5, 0);
		jCredits.setMinorTickSpacing(0);
        jCredits.setMajorTickSpacing(1);
        jCredits.setPaintTicks(true);
		jCredits.setSnapToTicks(true);
		Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(0, new JLabel("All"));
        labels.put(1, new JLabel("3k"));
        labels.put(2, new JLabel("5k"));
        labels.put(3, new JLabel("7k"));
        labels.put(4, new JLabel("10k"));
        labels.put(5, new JLabel("None"));
        jCredits.setLabelTable(labels);
		jCredits.setPaintLabels(true);
		jCredits.setVisible(false);
		if (program.getSettings(SettingsConstants.ALERT_CREDIT_3K)) {
			jCredits.setValue(1);
		} else if (program.getSettings(SettingsConstants.ALERT_CREDIT_5K)) {
			jCredits.setValue(2);
		} else if (program.getSettings(SettingsConstants.ALERT_CREDIT_7K)) {
			jCredits.setValue(3);
		} else if (program.getSettings(SettingsConstants.ALERT_CREDIT_10K)) {
			jCredits.setValue(4);
		} else if (program.getSettings(SettingsConstants.ALERT_CREDIT_NONE)) {
			jCredits.setValue(5);
		} else { //No settings or ALERT_CREDIT_ALL
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

		jBlueprints = createCheckBox("Blueprints", SettingsConstants.ALERT_BLUEPRINT);
		jMods = createCheckBox("Mods", SettingsConstants.ALERT_MOD);
		jAuras = createCheckBox("Auras", SettingsConstants.ALERT_AURA);
		jResources = createCheckBox("Resources", SettingsConstants.ALERT_RESOURCE);
		jFilters = createCheckBox("Filters", SettingsConstants.ALERT_FILTERS);

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

		SortedList<Alert> sortedList = new SortedList<Alert>(eventList);
		filterList = new FilterList<Alert>(sortedList);
		showList = new FilterList<Alert>(sortedList);

		TableFormat<Alert> tableFormat = new EnumTableFormat<Alert, AlertTableFormat>(AlertTableFormat.class);
		eventTableModel = EventModels.createTableModel(showList, tableFormat);
		eventTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == -1) {
					for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
						Alert alert = eventTableModel.getElementAt(i);
						program.getFiltersTool().update(alert);
						if (alert.isDone()) {
							program.doneAdd(alert.getId());
						} else {
							program.doneRemove(alert.getId());
						}
					}
				}
			}
		});

		final JTable jTable = new JAlertTable(eventTableModel);
		jTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isShiftDown() && !e.isControlDown()) {
					EventList<Alert> selected = selectionModel.getSelected();
					if (selected.size() == 1) {
						Alert alert = selected.get(0);
						if (alert.hasLoot()) {
							DesktopUtil.browseReward(program, alert.getRewordID(), true);
						}
					}
				}
			}
		});
		PaddingTableCellRenderer.install(jTable, 3);
		//Selection Model
		selectionModel = EventModels.createSelectionModel(showList);
		selectionModel.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE);
		jTable.setSelectionModel(selectionModel);

		timeLeft = new Timer(1000 * 15, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int row = 0; row < eventTableModel.getRowCount(); row++) {
					eventTableModel.fireTableCellUpdated(row, 0);
				}
			}
		});
		timeLeft.start();

		JScrollPane jTableScroll = new JScrollPane(jTable);

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(jShowAll)
					.addComponent(jShowFiltered)
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jShowFilters)	
				)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jTableScroll, 0, 0, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup()
						.addComponent(jCredits, 170, 170, 170)
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
					.addComponent(jShowAll)
					.addComponent(jShowFiltered)
					.addComponent(jShowFilters)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(jTableScroll, 0, 0, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addComponent(jCredits)
						.addComponent(jBlueprints)
						.addComponent(jMods)
						.addComponent(jAuras)
						.addComponent(jResources)
						.addComponent(jFilters)
					)
				)
		);
		filter();
	}

	@Override
	public String getTitle() {
		return "Alerts";
	}

	@Override
	public String getToolTip() {
		return "Alerts";
	}

	@Override
	public Icon getIcon() {
		return Images.ALERT.getIcon();
	}

	@Override
	public JPanel getPanel() {
		return jPanel;
	}

	@Override
	public Set<SettingsConstants> getSettings() {
		AlertSettings alertSettings = new AlertSettings(jCredits.getValue(),
				jBlueprints.isSelected(),
				jMods.isSelected(),
				jAuras.isSelected(),
				jResources.isSelected(),
				jFilters.isSelected());
		return alertSettings.getSettings();
	}

	@Override
	public void addAlerts(List<Alert> alerts) {
		boolean first = eventList.isEmpty();
		List<Alert> cache = new ArrayList<Alert>(filterList);

		Set<Alert> all = new TreeSet<Alert>(eventList);
		all.addAll(alerts);

		try {
			eventList.getReadWriteLock().writeLock().lock();
			eventList.clear();
			eventList.addAll(all);
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}

		if (!first) {
			int count = 0;
			for (Alert alert : filterList) {
				if (!cache.contains(alert)) {
					count++;
				}
			}
			if (count > 0) {
				program.startNotify(count, NotifySource.ALERTS);
			}
		}
		updateIgnored();
	}

	public void updateIgnored() {
		for (Alert alert : eventList) {
			alert.setIgnored(program.getFilters());
		}
		for (int row = 0; row < eventTableModel.getRowCount(); row++) {
			eventTableModel.fireTableCellUpdated(row, 5);
		}
	}

	public final void filter() {
		matcher = new AlertMatcher(jCredits.getValue(),
				jBlueprints.isSelected(),
				jMods.isSelected(),
				jAuras.isSelected(),
				jResources.isSelected(),
				jFilters.isSelected(),
				program.getFilters());
		filterList.setMatcher(matcher);
		if (jShowFiltered.isSelected()) {
			showList.setMatcher(matcher);
		}
		updateIgnored();
	}

	private JCheckBox createCheckBox(String title, SettingsConstants settings) {
		JCheckBox jCheckBox = new JCheckBox(title);
		jCheckBox.setSelected(program.getSettings(settings) || !program.getSettings(SettingsConstants.SETTINGS_SET));
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
	
