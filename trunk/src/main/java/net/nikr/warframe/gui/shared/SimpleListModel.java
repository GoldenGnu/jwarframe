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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;


public class SimpleListModel<E extends Comparable<E>> extends AbstractListModel {

	private final List<E> data;

	public SimpleListModel() {
		data = new ArrayList<E>();
	}

	public SimpleListModel(Collection<E> data) {
		this.data = new ArrayList<E>(data);
		Collections.sort(this.data);
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public Object getElementAt(int index) {
		return data.get(index);
	}

	public boolean add(E e) {
		boolean add = data.add(e);
		Collections.sort(data);
		int index = data.indexOf(e);
		fireIntervalAdded(e, index, index);
		return add;
	}

	public boolean remove(E e) {
		int index = data.indexOf(e);
		boolean remove = data.remove(e);
		fireIntervalRemoved(e, index, index);
		return remove;
	}

	public void clear() {
		while (!data.isEmpty()) {
			remove(data.get(0));
		}
	}

	public void addAll(Collection<E> list) {
		for (E e : list) {
			add(e);
		}
	}
}
