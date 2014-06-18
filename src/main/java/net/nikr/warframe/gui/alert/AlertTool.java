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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
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
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.DesktopUtil;
import net.nikr.warframe.gui.shared.FilterTool;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.gui.shared.listeners.AlertListener;
import net.nikr.warframe.gui.shared.listeners.NotifyListener.NotifySource;
import net.nikr.warframe.gui.shared.table.EnumTableFormat;
import net.nikr.warframe.gui.shared.table.EventModels;
import net.nikr.warframe.gui.shared.table.InvertMatcher;
import net.nikr.warframe.gui.shared.table.PaddingTableCellRenderer;
import net.nikr.warframe.io.alert.Alert;
import net.nikr.warframe.io.shared.FastToolTips;


public class AlertTool extends FilterTool implements AlertListener, Tool {

	private final JRadioButton jAll;
	private final JRadioButton jNotify;
	private final JRadioButton jIgnore;
	private final JSlider jCredits;
	private final Timer timeLeft;
	private final JButton jClearExpired;

	private final EventList<Alert> eventList = new BasicEventList<Alert>();
	private final FilterList<Alert> activeList;
	private final FilterList<Alert> matchList;
	private final FilterList<Alert> showList;
	private final DefaultEventSelectionModel<Alert> selectionModel;
	private final DefaultEventTableModel<Alert> eventTableModel;
	private Matcher<Alert> matcher = null;

	public AlertTool(final Program program) {
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
				showList.setMatcher(new InvertMatcher<Alert>(matcher));
			}
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jAll);
		buttonGroup.add(jNotify);
		buttonGroup.add(jIgnore);

		jClearExpired = new JButton("Clear Expired");
		jClearExpired.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Alert> alerts = new ArrayList<Alert>(activeList);
				try {
					eventList.getReadWriteLock().writeLock().lock();
					eventList.clear();
					eventList.addAll(alerts);
				} finally {
					eventList.getReadWriteLock().writeLock().unlock();
				}
			}
		});

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
				+ "<b>Wikia:</b>\tDouble click a table row with reward<br>"
				+ "<b>Show:</b>\tHover mouse over reward cell<br>");

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

		SortedList<Alert> sortedList = new SortedList<Alert>(eventList);
		matchList = new FilterList<Alert>(sortedList);
		showList = new FilterList<Alert>(sortedList);
		activeList = new FilterList<Alert>(sortedList);
		activeList.setMatcher(new Matcher<Alert>() {
			@Override
			public boolean matches(Alert alert) {
				return !alert.isExpired();
			}
		});

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
					updateStatusBar();
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
					.addComponent(jAll)
					.addComponent(jNotify)
					.addComponent(jIgnore)
					.addGap(20)
					.addComponent(jClearExpired)
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
					.addComponent(jClearExpired)
					.addComponent(jFilters)
					.addComponent(jHelp)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(jTableScroll, 0, 0, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addComponent(jCredits)
						.addGap(20)
						.addGroup(categoryVerticalGroup)
					)
				)
		);
		filter();
	}

	private void updateStatusBar() {
		program.setAlert(matchList.size(), activeList.size());
	}

	private void updateIgnored() {
		for (Alert alert : eventList) {
			alert.setIgnored(program.getFilters());
		}
		for (int row = 0; row < eventTableModel.getRowCount(); row++) {
			eventTableModel.fireTableCellUpdated(row, 5);
		}
	}

	@Override
	public String getTitle() {
		return "Alerts";
	}

	@Override
	public String getToolTip() {
		return null;
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
		AlertSettings alertSettings = new AlertSettings(jCredits.getValue());
		return alertSettings.getSettings();
	}

	@Override
	public void addAlerts(List<Alert> alerts) {
		List<Alert> cache = new ArrayList<Alert>(matchList);

		Set<Alert> all = new TreeSet<Alert>(eventList);
		all.addAll(alerts);

		try {
			eventList.getReadWriteLock().writeLock().lock();
			eventList.clear();
			eventList.addAll(all);
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}

		Set<String> categories = new HashSet<String>();
		int count = 0;
		for (Alert alert : matchList) {
			if (!cache.contains(alert)) {
				count++;
				Category category = alert.getCategory();
				if (category != null) {
					categories.add(category.getName());
				}
			}
		}
		if (count > 0) {
			program.startNotify(count, NotifySource.ALERTS, categories);
		}
		updateStatusBar();
		updateIgnored();
	}

	@Override
	public final void filter() {
		matcher = new AlertMatcher(jCredits.getValue(), getCategoryFilters(), program.getFilters());
		matchList.setMatcher(matcher);
		if (jNotify.isSelected()) {
			showList.setMatcher(matcher);
		}
		if (jIgnore.isSelected()) {
			showList.setMatcher(new InvertMatcher<Alert>(matcher));
		}
		updateIgnored();
		updateStatusBar();
	}
}
	
