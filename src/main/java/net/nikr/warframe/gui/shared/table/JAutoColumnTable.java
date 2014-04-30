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

package net.nikr.warframe.gui.shared.table;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

public class JAutoColumnTable extends JTable {


	private JViewport jViewport = null;
	private int size = 0;
	private final Map<Integer, Integer> rowsWidth = new HashMap<Integer, Integer>();
	private boolean autoResizeLock = false;
	private final Set<Class<?>> disableColumnResizeCache = new HashSet<Class<?>>();

	public JAutoColumnTable(final TableModel tableModel) {
		super(tableModel);
		this.getTableHeader().setResizingAllowed(false);
		//Listeners
		ListenerClass listener = new ListenerClass();
		this.addHierarchyListener(listener);
		this.getModel().addTableModelListener(listener);
		this.addPropertyChangeListener("model", listener);

		autoResizeColumns();

		fixScrollPaneRedraw();
	}

	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int column) {
		Component component = super.prepareEditor(editor, row, column);
		if (component instanceof JTextComponent) {
			JTextComponent jTextComponent = (JTextComponent) component;
			jTextComponent.selectAll();
		}
		return component;
	}

	public void lock() {
		autoResizeLock = true;
	}

	public void unlock() {
		if (isLocked()) { //only if locked
			autoResizeLock = false; //unlock
			autoResizeColumns(); //Update after unlock
		}
	}

	public boolean isLocked() {
		return autoResizeLock;
	}

	public final void autoResizeColumns() {
		if (isLocked()) {
			return;
		}
		resizeColumnsText();
	}

	public void disableColumnResizeCache(Class<?> columnClass) {
		disableColumnResizeCache.add(columnClass);
	}

	public void enableColumnResizeCache(Class<?> columnClass) {
		disableColumnResizeCache.remove(columnClass);
	}

	private JTable getTable() {
		return this;
	}

	private JScrollPane getParentScrollPane() {
		Container container = this.getParent();
		if (container != null) {
			container = container.getParent();
		}

		if (container instanceof JScrollPane) {
			return (JScrollPane) container;
		} else {
			return null;
		}
	}

	/**
	 * This is a work-around for issue #254. The JScrollPane viewport gets
	 * corrupted when it is moved to the right with the horizontal scrollbar.
	 * The following AdjustmentListener cannot fix the issue but it forces
	 * AWT to repaint the viewport content. Because the event firing frequency
	 * is lower than the viewport scroll rate, it may still flicker during
	 * the scrolling, but at least ensures that the viewport is drawn properly
	 * when the scrolling is stopped.
	 * This is bug somewhere between OpenJDK and certain graphics drivers
	 * under Linux and can be fixed by disabling the driver's acceleration.
	 * @author Jan
	 */
	private void fixScrollPaneRedraw() {
		/* This component has not been added to the JScrollPanel at
		 * construction time. This one listens to an ANCESTOR_ADD
		 * event and registers the repaint method at the JScrollPanel
		 * parent as soon as this component has been added to it.
		 */
		this.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(final AncestorEvent ae) {
				JComponent jComponent = ae.getComponent();
				if (jComponent instanceof JAutoColumnTable) {
					JAutoColumnTable jTable = (JAutoColumnTable) jComponent;
					JScrollPane jScrollPane = jTable.getParentScrollPane();
					if (jScrollPane != null) {
						jScrollPane.getHorizontalScrollBar().addAdjustmentListener(new JScrollPaneAdjustmentListener(jScrollPane));
					}
				}
			}

			@Override
			public void ancestorMoved(final AncestorEvent event) { }

			@Override
			public void ancestorRemoved(final AncestorEvent event) { }

		});
	}

	private JViewport getParentViewport() {
		Container container = this.getParent();
		if (container instanceof JViewport) {
			return (JViewport) container;
		} else {
			return null;
		}
	}

	private void resizeColumnsText() {
		size = 0;
		
		for (int i = 0; i < getColumnCount(); i++) {
			size = size + resizeColumn(this, getColumnModel().getColumn(i), i);
		}
		updateScroll();
	}

	private void updateScroll() {
		if (jViewport != null && size < jViewport.getSize().width) {
			this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		} else {
			this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		}
	}

	private int resizeColumn(final JTable jTable, final TableColumn column, final int columnIndex) {
		//Header width
		TableCellRenderer renderer = column.getHeaderRenderer();
		if (renderer == null) {
			renderer = jTable.getTableHeader().getDefaultRenderer();
		}
		Component component = renderer.getTableCellRendererComponent(jTable, column.getHeaderValue(), false, false, 0, 0);
		int maxWidth = component.getPreferredSize().width;

		//Rows width
		for (int i = 0; i < jTable.getRowCount(); i++) {
			final Object rowValue = jTable.getValueAt(i, columnIndex); //Get cell value
			if (rowValue == null) { //Ignore null
				continue;
			}
			boolean useCache = !disableColumnResizeCache.contains(rowValue.getClass());
			final int key = rowValue.toString().hashCode(); //value hash
			if (rowsWidth.containsKey(key) && useCache) { //Load row width
				maxWidth = Math.max(maxWidth, rowsWidth.get(key));
			} else { //Calculate the row width
				renderer = jTable.getCellRenderer(i, columnIndex);
				component = renderer.getTableCellRendererComponent(jTable, jTable.getValueAt(i, columnIndex), false, false, i, columnIndex);
				int width = component.getPreferredSize().width;
				if (useCache) {
					rowsWidth.put(key, width);
				}
				maxWidth = Math.max(maxWidth, width);
			}
		}
		//Add margin
		maxWidth = maxWidth + 4;
		//Set width
		column.setPreferredWidth(maxWidth);
		return maxWidth; //Return width
	}

	private class ListenerClass implements TableModelListener, ComponentListener,
			PropertyChangeListener, HierarchyListener {

		private int rowsLastTime = 0;
		private int rowsCount = 0;

		@Override
		public void tableChanged(final TableModelEvent e) {
			//XXX - Workaround for Java 7
			if (getTable().isEditing()) {
				getTable().getCellEditor().cancelCellEditing();
			}
			if (e.getType() == TableModelEvent.DELETE) {
				rowsCount = rowsCount - (Math.abs(e.getFirstRow() - e.getLastRow()) + 1);
			}
			if (e.getType() == TableModelEvent.INSERT) {
				rowsCount = rowsCount + (Math.abs(e.getFirstRow() - e.getLastRow()) + 1);
			}
			if (Math.abs(rowsLastTime + rowsCount) == getRowCount() //Last Table Update
					&& (e.getType() != TableModelEvent.UPDATE
					|| (e.getType() == TableModelEvent.UPDATE && e.getFirstRow() >= 0))) {
				rowsLastTime = getRowCount();
				rowsCount = 0;
				autoResizeColumns();
			}
		}

		@Override
		public void componentResized(final ComponentEvent e) {
			updateScroll();
		}

		@Override
		public void componentMoved(final ComponentEvent e) { }

		@Override
		public void componentShown(final ComponentEvent e) { }

		@Override
		public void componentHidden(final ComponentEvent e) { }

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			Object newValue = evt.getNewValue();
			Object oldValue = evt.getOldValue();
			if (newValue instanceof TableModel && oldValue instanceof TableModel) {
				TableModel newModel = (TableModel) newValue;
				TableModel oldModel = (TableModel) oldValue;
				oldModel.removeTableModelListener(this);
				newModel.addTableModelListener(this);
			}
		}

		@Override
		public void hierarchyChanged(final HierarchyEvent e) {
			if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == HierarchyEvent.PARENT_CHANGED) {
				if (jViewport != null) {
					jViewport.removeComponentListener(this);
				}
				jViewport = getParentViewport();
				if (jViewport != null) {
					jViewport.addComponentListener(this);
				}
			}
		}
	}

	/**
	 * @see JAutoColumnTable#fixScrollPaneRedraw()
	 */
	private class JScrollPaneAdjustmentListener implements AdjustmentListener {
		/**
		 * Holds the JScrollPane we want to force repainting its content.
		 */
		private final JScrollPane jScrollPane;

		/**
		 * Holds the last scrollbar position for direction tracking.
		 */
		private int lastValue;

		private boolean repaint;

		public JScrollPaneAdjustmentListener(final JScrollPane jScrollPane) {
			this.jScrollPane = jScrollPane;
			repaint = false;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent e) {
			if (e.getValue() > lastValue) {
				// scrollbar has been dragged to the right
				repaint = true;
			}
			if (!e.getValueIsAdjusting() && repaint) {
				//Done scrolling - repaint if needed
				jScrollPane.repaint();
				repaint = false;
			}
			lastValue = e.getValue();
		}
	}
}
