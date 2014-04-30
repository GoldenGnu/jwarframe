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

import ca.odell.glazedlists.GlazedLists;
import java.util.Comparator;
import net.nikr.warframe.gui.shared.table.EnumRow;
import net.nikr.warframe.io.alert.Alert;
import net.nikr.warframe.io.alert.Alert.Mission;


public enum AlertTableFormat implements EnumRow<Alert> {
	TIME(String.class, "Time") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.getTimeLeft();
		}
	},
	REGION(String.class, "Region") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.getRegion();
		}
	},
	NODE(String.class, "Node") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.getNode();
		}
	},
	MISSION(Mission.class, "Mission") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.getMission();
		}
	},
	CREDITS(Integer.class, "Credits") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.getCredits();
		}
	},
	TYPE(String.class, "Type") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.getCategoryName();
		}
	},
	LOOT(String.class, "Reward") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.getLoot();
		}
	},
	IGNORE(Boolean.class, "Ignore") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.isIgnored();
		}
		@Override
		public boolean isEditable(Alert alert) {
			return alert.hasLoot();
		}
		@Override
		public Alert setColumnValue(Alert alert, Object o) {
			if (o instanceof Boolean) {
				alert.setIgnored((Boolean) o);
			}
			return alert;
		}
	},
	DONE(Boolean.class, "Done") {
		@Override
		public Object getColumnValue(Alert alert) {
			return alert.isDone();
		}
		@Override
		public Alert setColumnValue(Alert alert, Object object) {
			if (object instanceof Boolean) {
				boolean done = (Boolean) object;
				alert.setDone(done);
			}
			return alert;
		}
		@Override
		public boolean isEditable(Alert alert) {
			return true;
		}
	};

	private final Class columnClass;
	private final String columnName;
	private final Comparator columnComparator;

	private AlertTableFormat(Class columnClass, String columnName) {
		this(columnClass, columnName, GlazedLists.comparableComparator());
	}

	private AlertTableFormat(Class columnClass, String columnName, Comparator columnComparator) {
		this.columnClass = columnClass;
		this.columnName = columnName;
		this.columnComparator = columnComparator;
	}

	@Override
	public Class getColumnClass() {
		return columnClass;
	}

	@Override
	public Comparator getColumnComparator() {
		return columnComparator;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	public String name(Enum e) {
		return e.name();
	}

	@Override
	public boolean isEditable(Alert e) {
		return false;
	}

	@Override
	public Alert setColumnValue(Alert e, Object o) {
		return null;
	}

}
