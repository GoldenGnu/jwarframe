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

package net.nikr.warframe.gui.alert;

import ca.odell.glazedlists.swing.DefaultEventTableModel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.Category.CategoryColor;
import net.nikr.warframe.gui.shared.table.JToolTipTable;
import net.nikr.warframe.io.alert.Alert;

public class JAlertTable extends JToolTipTable {

	private final DefaultEventTableModel<Alert> tableModel;

	public JAlertTable(final DefaultEventTableModel<Alert> tableModel) {
		super(tableModel);
		this.tableModel = tableModel;
	}

	@Override
	public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
		Component component = super.prepareRenderer(renderer, row, column);
		boolean isSelected = isCellSelected(row, column);
		Alert alert = tableModel.getElementAt(row);
		int columnIndex = convertColumnIndexToModel(column);

		if (!isSelected) {
			component.setBackground(Color.WHITE);
		} else {
			component.setBackground(this.getSelectionBackground());
		}
		component.setForeground(Color.BLACK);
		if (columnIndex == AlertTableFormat.MISSION.ordinal()) {
			if (alert.isMatchMission()) {
				component.setForeground(Color.BLACK);
			} else {
				if (isSelected) {
					component.setForeground(Color.LIGHT_GRAY);
				} else {
					component.setForeground(Color.GRAY);
				}
			}
		}
		if (columnIndex == AlertTableFormat.CREDITS.ordinal()) {
			if (alert.isMatchCredits()) {
				component.setForeground(Color.BLACK);
			} else {
				if (isSelected) {
					component.setForeground(Color.LIGHT_GRAY);
				} else {
					component.setForeground(Color.GRAY);
				}
			}
		}
		if (columnIndex == AlertTableFormat.LOOT.ordinal()) {
			if (alert.isMatchLoot()) {
				component.setForeground(Color.BLACK);
			} else {
				if (isSelected) {
					component.setForeground(Color.LIGHT_GRAY);
				} else {
					component.setForeground(Color.GRAY);
				}
			}
		}
		if (columnIndex == AlertTableFormat.TIME.ordinal()) {
			if (alert.isExpired()) {
				if (isSelected) {
					component.setForeground(Color.LIGHT_GRAY);
				} else {
					component.setForeground(Color.GRAY);
				}
			} else {
				component.setForeground(Color.GREEN.darker().darker());
			}
		}
		if (columnIndex == AlertTableFormat.TYPE.ordinal()) {
			if (alert.hasLoot()) {
				Category category = alert.getCategory();
				if (category != null) {
					component.setBackground(category.getType().getColor(isSelected));
				} else {
					component.setBackground(CategoryColor.GRAY.getColor(isSelected));
				}
			}
		}
		if (columnIndex == AlertTableFormat.IGNORE.ordinal()) {
			if (alert.hasLoot()) {
				if (alert.isIgnored()) {
					component.setBackground(CategoryColor.RED.getColor(isSelected));
				} else {
					component.setBackground(CategoryColor.GREEN.getColor(isSelected));
				}
			} else {
				component.setBackground(CategoryColor.GRAY.getColor(isSelected));
			}
		}
		if (component instanceof JLabel) {
			JLabel jLabel = (JLabel) component;
			if (columnIndex == AlertTableFormat.LOOT.ordinal()) {
				jLabel.setToolTipText(generateToolTip(alert.getRewordID(), false));
			} else {
				jLabel.setToolTipText(null);
			}
			if (columnIndex == AlertTableFormat.MISSION.ordinal()) {
				jLabel.setIcon(alert.getMission().getFaction().getIcon());
			} else {
				jLabel.setIcon(null);
			}
		}
		return component;
	}
}
