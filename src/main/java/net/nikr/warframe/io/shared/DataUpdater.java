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

package net.nikr.warframe.io.shared;

import java.util.List;
import net.nikr.warframe.Program;
import net.nikr.warframe.io.alert.Alert;
import net.nikr.warframe.io.alert.AlertGetter;
import net.nikr.warframe.io.alert.AlertProcessor;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.invasion.InvasionGetter;
import net.nikr.warframe.io.invasion.InvasionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataUpdater extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);
	private static final int UPDATE_DELAY_MS = 3 * 60 * 1000; //min * sec * ms = 3min

	private final AlertGetter alertGetter;
	private final AlertProcessor alertProcessor;
	private final InvasionGetter invasionGetter;
	private final InvasionProcessor invasionProcessor;

	private final Program program;

	public DataUpdater(Program program) {
		this.program = program;
		alertGetter = new AlertGetter();
		alertProcessor = new AlertProcessor();
		invasionGetter = new InvasionGetter();
		invasionProcessor = new InvasionProcessor();
	}

	@Override
	public void run() {
		while (true) {
			try {
				LOG.info("Updating invasion from deathsnacks.com...");
				List<String> rawInvasions = invasionGetter.get();
				List<Invasion> invasions = invasionProcessor.process(rawInvasions, program.getDone());
				LOG.info(invasions.size() + " invasions updated");
				program.addInvasions(invasions);
				LOG.info("Updating alerts from deathsnacks.com...");
				List<String> rawAlerts = alertGetter.get();
				List<Alert> alerts = alertProcessor.process(rawAlerts, program.getCategories(), program.getDone());
				LOG.info(alerts.size() + " alerts updated");
				program.addAlerts(alerts);
				synchronized (this) {
					wait(UPDATE_DELAY_MS);
				}
			} catch (InterruptedException ex) {
				LOG.error("AlertUpdater Thread Interrupted", ex);
			}
		}
	}
}
