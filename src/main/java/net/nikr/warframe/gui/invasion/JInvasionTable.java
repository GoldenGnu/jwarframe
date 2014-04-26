/*
 * Copyright 2014 Niklas Kyster Rasmussen
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

package net.nikr.warframe.gui.invasion;

import ca.odell.glazedlists.swing.DefaultEventTableModel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import net.nikr.warframe.gui.shared.JToolTipTable;
import net.nikr.warframe.io.invasion.Invasion;

public class JInvasionTable extends JToolTipTable {

	private final DefaultEventTableModel<Invasion> tableModel;

	public JInvasionTable(final DefaultEventTableModel<Invasion> tableModel) {
		super(tableModel);
		this.tableModel = tableModel;
	}

	@Override
	public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
		JComponent component = (JComponent) super.prepareRenderer(renderer, row, column);
		boolean isSelected = isCellSelected(row, column);
		Invasion invasion = tableModel.getElementAt(row);
		int columnIndex = convertColumnIndexToModel(column);

		if (!isSelected) {
			component.setBackground(Color.WHITE);
		} else {
			component.setBackground(this.getSelectionBackground());
		}
		component.setForeground(Color.BLACK);

		if (columnIndex == InvasionTableFormat.indexOf(InvasionTableFormat.INVADER_REWARD)
				|| columnIndex == InvasionTableFormat.indexOf(InvasionTableFormat.INVADER_MISSION)) {
			if (invasion.isMatchInvading()) {
				component.setForeground(Color.BLACK);
			} else {
				if (isSelected) {
					component.setForeground(Color.LIGHT_GRAY);
				} else {
					component.setForeground(Color.GRAY);
				}
			}
		}
		if (columnIndex == InvasionTableFormat.indexOf(InvasionTableFormat.DEFENDER_REWARD)
				|| columnIndex == InvasionTableFormat.indexOf(InvasionTableFormat.DEFENDER_MISSION)) {
			if (invasion.isMatchDefending()) {
				component.setForeground(Color.BLACK);
			} else {
				if (isSelected) {
					component.setForeground(Color.LIGHT_GRAY);
				} else {
					component.setForeground(Color.GRAY);
				}
			}
		}
		if (component instanceof JLabel) {
			JLabel jLabel = (JLabel) component;
			if (columnIndex == InvasionTableFormat.indexOf(InvasionTableFormat.DEFENDER_REWARD)) {
				jLabel.setToolTipText(generateToolTip(invasion.getDefendingRewardID(), invasion.isDefendinCredits()));
			} else if (columnIndex == InvasionTableFormat.indexOf(InvasionTableFormat.INVADER_REWARD)) {
				jLabel.setToolTipText(generateToolTip(invasion.getInvadingRewardID(), invasion.isInvadingCredits()));
			} else {
				jLabel.setToolTipText(null);
			}
		}
		return component;

	}
}