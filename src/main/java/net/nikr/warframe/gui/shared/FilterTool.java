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

package net.nikr.warframe.gui.shared;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.shared.components.JDropDownButton;
import net.nikr.warframe.io.shared.FastToolTips;


public abstract class FilterTool implements Tool {

	protected final JPanel jPanel;
	protected final GroupLayout layout;
	protected final GroupLayout.Group horizontalGroup;
	protected final GroupLayout.Group verticalGroup;
	protected final List<JComponent> filterComponents = new ArrayList<JComponent>();
	private final MissionTypes missionTypes;
	private final GroupLayout.Group column1;
	private final GroupLayout.Group column2;
	private final GroupLayout.Group column3;
	private final GroupLayout.Group column4;
	private final GroupLayout.Group column5;
	private final Set<String> filterMissionTypesStrings = new TreeSet<String>();
	private final Map<String, Set<String>> categoryIcon = new TreeMap<String, Set<String>>();
	private final List<CategoryContainer> categories = new ArrayList<CategoryContainer>();
	private final int BUTTON_SIZE = 24;

	protected final Program program;
	
	public FilterTool(final Program program, final MissionTypes missionTypes) {
		this.program = program;
		this.missionTypes = missionTypes;

	//LAYOUT
		jPanel = new JPanel();
		layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHonorsVisibility(true);
		layout.setLayoutStyle(null);

		verticalGroup = layout.createSequentialGroup();
		horizontalGroup = layout.createSequentialGroup();

		column1 = layout.createParallelGroup();
		column2 = layout.createParallelGroup();
		column3 = layout.createParallelGroup();
		column4 = layout.createParallelGroup();
		column5 = layout.createParallelGroup();

		horizontalGroup.addGroup(column1);
		horizontalGroup.addGroup(column2);
		horizontalGroup.addGroup(column3);
		horizontalGroup.addGroup(column4);
		horizontalGroup.addGroup(column5);
	//CATEGORY
		for (Category category : program.getCategories()) {
			//Category name
			JLabel jLabel = new JLabel(category.getName());
			jLabel.setVisible(false);
			filterComponents.add(jLabel);
			//Filter type
			ButtonGroup buttonGroup = new ButtonGroup();
			CategoryFilter filter = program.getCategoryFilter(getToolName()).get(category.getName());
			JToggleButton jAll = createToggleButton(Images.ALL.getIcon(), "Include All", buttonGroup, filter == CategoryFilter.ALL);
			JToggleButton jFilters = createToggleButton(Images.PROGRAM_16.getIcon(), "Use Filters", buttonGroup, filter == null || filter == CategoryFilter.FILTERS);
			JToggleButton jNone = createToggleButton(Images.NONE.getIcon(), "Exclude All", buttonGroup, filter == CategoryFilter.NONE);
			//Mission Type
			final JDropDownButton jMissionTypes = new JDropDownButton(Images.SETTINGS.getIcon());
			jMissionTypes.setPopupHorizontalAlignment(SwingConstants.RIGHT);
			jMissionTypes.setVisible(false);
			jMissionTypes.setToolTipText("More...");
			filterComponents.add(jMissionTypes);
			
			final String categoryName = category.getName();
			for (String mission : missionTypes.getMissionTypes()) {
				final JCheckBoxMenuItem jCheckBox = new JCheckBoxMenuItem(mission);
				final String key = getToolName() + category.getName() + jCheckBox.getText();
				
				jCheckBox.setSelected(!program.getMissionTypes().contains(key));
				//Update mission type settings
				if (program.getMissionTypes().contains(key)) {
					filterMissionTypesStrings.add(key);
					getMissionType(category.getName()).add(key);
				}
				jCheckBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						updateMissionTypeSettings(jCheckBox, categoryName, key);
						updateMissionTypeIcon(jMissionTypes, categoryName);
						filter();
					}
				});

				jMissionTypes.add(jCheckBox, true);
			}

			Collection<JMenuItem> menuItems = getMenuItems(categoryName);
			if (!menuItems.isEmpty()) {
				jMissionTypes.addSeparator();
			}
			for (JMenuItem jMenuItem : menuItems) {
				if (jMenuItem == null) {
					jMissionTypes.addSeparator();
				} else {
					jMissionTypes.add(jMenuItem, true);
					if (jMenuItem instanceof JCheckBoxMenuItem) {
						final JCheckBoxMenuItem jCheckBox = (JCheckBoxMenuItem) jMenuItem;
						final String key = getToolName() + categoryName + jCheckBox.getText();
						if (!jCheckBox.isSelected()) {
							getMissionType(categoryName).add(key);
						}
						jMenuItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (jCheckBox.isSelected()) {
									getMissionType(categoryName).remove(key);
								} else {
									getMissionType(categoryName).add(key);
								}
								updateMissionTypeIcon(jMissionTypes, categoryName);
							}
						});
					}
				}
			}
			updateMissionTypeIcon(jMissionTypes, categoryName);

			addColumn1(jLabel);
			addColumn2(jAll);
			addColumn3(jFilters);
			addColumn4(jNone);
			addColumn5(jMissionTypes);

			verticalGroup.addGroup(createRow(jLabel, jAll, jFilters, jNone, jMissionTypes));
			categories.add(new CategoryContainer(category.getName(), jAll, jFilters, jNone));
		}
	}

	protected final JToggleButton createToggleButton(Icon icon, String toolTip, ButtonGroup buttonGroup, boolean selected) {
		final JToggleButton jButton = new JToggleButton(icon);
		jButton.setToolTipText(toolTip);
		jButton.setSelected(selected);
		jButton.setVisible(false);
		FastToolTips.install(jButton);
		jButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filter();
				program.saveSettings();
			}
		});
		filterComponents.add(jButton);
		if (buttonGroup != null) {
			buttonGroup.add(jButton);
		}
		return jButton;
	}

	protected final void addColumn1(JComponent jComponent) {
		column1.addComponent(jComponent);
	}

	protected final void addColumn2(JComponent jComponent) {
		column2.addComponent(jComponent, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
	}
	protected final void addColumn3(JComponent jComponent) {
		column3.addComponent(jComponent, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
	}
	protected final void addColumn4(JComponent jComponent) {
		column4.addComponent(jComponent, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
	}
	protected final void addColumn5(JComponent jComponent) {
		column5.addComponent(jComponent, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
	}
	protected final GroupLayout.Group createRow(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4, JComponent jComponent5) {
		GroupLayout.Group group = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
		if (jComponent1 != null) {
			group.addComponent(jComponent1);
		}
		if (jComponent2 != null) {
			group.addComponent(jComponent2, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
		}
		if (jComponent3 != null) {
			group.addComponent(jComponent3, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
		}
		if (jComponent4 != null) {
			group.addComponent(jComponent4, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
		}
		if (jComponent5 != null) {
			group.addComponent(jComponent5, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
		}
		return group;
	}

	public Set<String> getFilterMissionTypesStrings() {
		return filterMissionTypesStrings;
	}

	private Set<String> getMissionType(String category) {
		Set<String> set = categoryIcon.get(category);
		if (set == null) {
			set = new TreeSet<String>();
			categoryIcon.put(category, set);
		}
		return set;
	}

	private void updateMissionTypeSettings(JCheckBoxMenuItem jCheckBox, String category, String key) {
		if (jCheckBox.isSelected()) {
			filterMissionTypesStrings.remove(key);
			getMissionType(category).remove(key);
		} else {
			filterMissionTypesStrings.add(key);
			getMissionType(category).add(key);
		}
		program.saveMissionType();
	}

	private void updateMissionTypeIcon(JDropDownButton jMissionTypes, String category) {
		if (getMissionType(category).isEmpty()) {
			jMissionTypes.setIcon(Images.SETTINGS_ALL.getIcon());
		} else {
			jMissionTypes.setIcon(Images.SETTINGS_SET.getIcon());
		}
	}
	
	public Map<String, CategoryFilter> getCategoryFilters() {
		Map<String, CategoryFilter> categoryFilters = new HashMap<String, CategoryFilter>();
		for (CategoryContainer container : categories) {
			categoryFilters.put(container.getCategory(), container.getCategoryFilter());
		}
		return categoryFilters;
	}

	public final String getToolName() {
		String title = getTitle();
		if (!title.isEmpty()) {
			return title;
		} else {
			return getToolTip();
		}
	}

	public abstract void filter();

	public abstract Collection<JMenuItem> getMenuItems(String categoryName);

	private static class CategoryContainer {
		private final String category;
		private final JToggleButton jAll;
		private final JToggleButton jFilters;
		private final JToggleButton jNone;

		public CategoryContainer(String category, JToggleButton jAll, JToggleButton jFilters, JToggleButton jNone) {
			this.category = category;
			this.jAll = jAll;
			this.jFilters = jFilters;
			this.jNone = jNone;
		}

		public String getCategory() {
			return category;
		}

		public CategoryFilter getCategoryFilter() {
			if (jAll.isSelected()) {
				return CategoryFilter.ALL;
			} else if (jFilters.isSelected()) {
				return CategoryFilter.FILTERS;
			} else {
				return CategoryFilter.NONE;
			}
		}
	}

	public static interface MissionTypes {
		public Set<String> getMissionTypes();
	}
}
