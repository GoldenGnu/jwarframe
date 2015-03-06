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

import ca.odell.glazedlists.GlazedLists;
import java.util.Comparator;
import net.nikr.warframe.gui.shared.table.EnumRow;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.invasion.Invasion.InvasionPercentage;


public enum InvasionTableFormat implements EnumRow<Invasion> {
	REGION(String.class, "Region") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			return invasion.getRegion();
		}
	},
	NODE(String.class, "Node") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			return invasion.getNode();
		}
	},
	ETA(String.class, "ETA") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			return invasion.getEta().replace("ETA:", "").trim();
		}
	},
	INVADER_REWARD(String.class, "Reward") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			if (!invasion.isInfestedInvading()) {
				return invasion.getInvadingReward();
			} else {
				return "";
			}
		}
	},
	INVADER_MISSION(String.class, "Mission") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			if (!invasion.isInfestedInvading()) {
				return invasion.getInvadingMissionType();
			} else {
				return "";
			}
		}
	},
	PERCENT(InvasionPercentage.class, "Percent") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			return invasion.getPercentage();
		}
	},
	DEFENDER_MISSION(String.class, "Mission") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			return invasion.getDefendingMissionType();
		}
	},
	DEFENDER_REWARD(String.class, "Reward") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			return invasion.getDefendingReward();
		}
	},
	DONE(Boolean.class, "Done") {
		@Override
		public Object getColumnValue(Invasion invasion) {
			return invasion.isDone();
		}
		@Override
		public Invasion setColumnValue(Invasion invasion, Object object) {
			if (object instanceof Boolean) {
				boolean done = (Boolean) object;
				invasion.setDone(done);
			}
			return invasion;
		}
		@Override
		public boolean isEditable(Invasion invasion) {
			return true;
		}
	};

	private final Class columnClass;
	private final String columnName;
	private final Comparator columnComparator;

	private InvasionTableFormat(Class columnClass, String columnName) {
		this(columnClass, columnName, GlazedLists.comparableComparator());
	}

	private InvasionTableFormat(Class columnClass, String columnName, Comparator columnComparator) {
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
	public boolean isEditable(Invasion e) {
		return false;
	}

	@Override
	public Invasion setColumnValue(Invasion e, Object o) {
		return null;
	}
}
