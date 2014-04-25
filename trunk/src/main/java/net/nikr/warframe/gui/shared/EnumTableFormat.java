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

package net.nikr.warframe.gui.shared;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class EnumTableFormat<E, T extends Enum<T> & EnumRow<E>> implements AdvancedTableFormat<E>, WritableTableFormat<E> {

	private final List<T> columns;

	public EnumTableFormat(Class<T> format) {
		columns = new ArrayList<T>(Arrays.asList(format.getEnumConstants()));
	}
	
	@Override
	public Class getColumnClass(int i) {
		return getRow(i).getColumnClass();
	}

	@Override
	public Comparator getColumnComparator(int i) {
		return getRow(i).getColumnComparator();
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public String getColumnName(int i) {
		return getRow(i).getColumnName();
	}

	@Override
	public Object getColumnValue(E e, int i) {
		return getRow(i).getColumnValue(e);
	}

	@Override
	public boolean isEditable(E e, int i) {
		return getRow(i).isEditable(e);
	}

	@Override
	public E setColumnValue(E e, Object o, int i) {
		return getRow(i).setColumnValue(e, o);
	}

	private EnumRow<E> getRow(int i) {
		return columns.get(i);
	}
	
}
