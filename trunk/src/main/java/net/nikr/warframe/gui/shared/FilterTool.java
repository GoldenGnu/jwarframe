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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.io.shared.FastToolTips;


public abstract class FilterTool implements Tool {

	protected final JPanel jPanel;
	protected final GroupLayout layout;
	protected final GroupLayout.Group categoryHorizontalGroup;
	protected final GroupLayout.Group categoryVerticalGroup;
	protected final GroupLayout.Group missionTypeHorizontalGroup;
	protected final GroupLayout.Group missionTypeVerticalGroup;
	protected final List<JComponent> filterComponents = new ArrayList<JComponent>();
	protected Map<String, SettingsConstants> missionTypes;
	private final GroupLayout.Group column1;
	private final GroupLayout.Group column2;
	private final GroupLayout.Group column3;
	private final GroupLayout.Group column4;
	
	private final Set<SettingsConstants> filterMissionTypesSettings = EnumSet.noneOf(SettingsConstants.class);
	private final Set<String> filterMissionTypesStrings = new TreeSet<String>();;
	private final List<CategoryContainer> categories = new ArrayList<CategoryContainer>();
	private final int BUTTON_SIZE = 24;

	protected final Program program;
	
	public FilterTool(final Program program) {
		this.program = program;

		jPanel = new JPanel();
		layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHonorsVisibility(true);
		layout.setLayoutStyle(null);

	//CATEGORY
		categoryVerticalGroup = layout.createSequentialGroup();
		categoryHorizontalGroup = layout.createSequentialGroup();

		column1 = layout.createParallelGroup();
		column2 = layout.createParallelGroup();
		column3 = layout.createParallelGroup();
		column4 = layout.createParallelGroup();

		categoryHorizontalGroup.addGroup(column1);
		categoryHorizontalGroup.addGroup(column2);
		categoryHorizontalGroup.addGroup(column3);
		categoryHorizontalGroup.addGroup(column4);

		for (Category category : program.getCategories()) {
			JLabel jLabel = new JLabel(category.getName());
			jLabel.setVisible(false);
			filterComponents.add(jLabel);
			ButtonGroup buttonGroup = new ButtonGroup();
			CategoryFilter filter = program.getCategoryFilter(getToolName()).get(category.getName());
			JToggleButton jAll = createToggleButton(Images.ALL.getIcon(), "Include All", buttonGroup, filter == CategoryFilter.ALL);
			JToggleButton jFilters = createToggleButton(Images.PROGRAM_16.getIcon(), "Use Filters", buttonGroup, filter == null || filter == CategoryFilter.FILTERS);
			JToggleButton jNone = createToggleButton(Images.NONE.getIcon(), "Exclude All", buttonGroup, filter == CategoryFilter.NONE);
			addColumn1(jLabel);
			addColumn2(jAll);
			addColumn3(jFilters);
			addColumn4(jNone);
			categoryVerticalGroup.addGroup(createRow(jLabel, jAll, jFilters, jNone));
			categories.add(new CategoryContainer(category.getName(), jAll, jFilters, jNone));
		}

	//MISSION TYPES
		//Category
		missionTypeVerticalGroup = layout.createSequentialGroup();
		missionTypeHorizontalGroup = layout.createParallelGroup();

		for (Map.Entry<String, SettingsConstants> entry : getMissionTypes().entrySet()) {
			final JCheckBox jCheckBox = new JCheckBox(entry.getKey());
			jCheckBox.setSelected(!program.getSettings(entry.getValue()));
			update(jCheckBox);
			jCheckBox.setVisible(false);
			jCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					update(jCheckBox);
					filter();
					program.saveSettings();
				}
			});
			filterComponents.add(jCheckBox);
			missionTypeVerticalGroup.addComponent(jCheckBox);
			missionTypeHorizontalGroup.addComponent(jCheckBox);
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
	protected final GroupLayout.Group createRow(JComponent jComponent1, JComponent jComponent2, JComponent jComponent3, JComponent jComponent4) {
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
		return group;
	}

	public Set<SettingsConstants> getFilterMissionTypesSettings() {
		return filterMissionTypesSettings;
	}

	public Set<String> getFilterMissionTypesStrings() {
		return filterMissionTypesStrings;
	}

	private void update(JCheckBox jCheckBox) {
		String name = jCheckBox.getText();
		SettingsConstants settingsConstants = getMissionTypes().get(name);
		if (jCheckBox.isSelected()) {
			filterMissionTypesStrings.remove(name);
			filterMissionTypesSettings.remove(settingsConstants);
		} else {
			filterMissionTypesStrings.add(name);
			filterMissionTypesSettings.add(settingsConstants);
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
	public abstract Map<String, SettingsConstants> getMissionTypes();

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
}
