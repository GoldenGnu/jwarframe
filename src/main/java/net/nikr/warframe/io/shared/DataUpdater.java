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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.listeners.NotifyListener.NotifySource;
import net.nikr.warframe.io.alert.Alert;
import net.nikr.warframe.io.alert.AlertGetter;
import net.nikr.warframe.io.alert.AlertProcessor;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.invasion.InvasionGetter;
import net.nikr.warframe.io.invasion.InvasionProcessor;
import net.nikr.warframe.io.login.LoginReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataUpdater extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);

	private static final int UPDATE_DELAY_MS = 3 * 60 * 1000; //min * sec * ms = 3min

	private final AlertGetter alertGetter;
	private final AlertProcessor alertProcessor;
	private final InvasionGetter invasionGetter;
	private final InvasionProcessor invasionProcessor;
	private final LoginReader loginReader;

	private final Program program;

	private Boolean loginReward = false;
	private boolean loginNotify = false;

	public DataUpdater(Program program) {
		this.program = program;
		alertGetter = new AlertGetter();
		alertProcessor = new AlertProcessor();
		invasionGetter = new InvasionGetter();
		invasionProcessor = new InvasionProcessor();
		loginReader = new LoginReader();
	}

	@Override
	public void run() {
		while (true) {
			try {
				waitForInternet();
			//INVASIONS
				LOG.info("Updating invasion from deathsnacks.com...");
				List<String> rawInvasions = invasionGetter.get();
				List<Invasion> invasions = invasionProcessor.process(rawInvasions, program.getCategories(), program.getDone());
				LOG.info(invasions.size() + " invasions updated");
			//ALERTS
				LOG.info("Updating alerts from deathsnacks.com...");
				List<String> rawAlerts = alertGetter.get();
				List<Alert> alerts = alertProcessor.process(rawAlerts, program.getCategories(), program.getDone());
				LOG.info(alerts.size() + " alerts updated");
			//LOGIN REWARD
				loginReward = loginReader.loginRewardAvailible();
				if (loginReward != null && program.getSettings(SettingsConstants.LOGIN_REWARD)) { //Notify on login reward enabled
					if (loginReward && !loginNotify) { //Login avalible and not notified
						loginNotify = true;
						program.startNotify(0, NotifySource.LOGIN_REWARD, new HashSet<String>(Collections.singleton("login")));
					}
					if (!loginReward && loginNotify) { //Login not avalible and notified
						loginNotify = false; //Enable notify (for next reward)
					}
				}
			//UPDATE DATA
				program.addInvasions(invasions);
				program.addAlerts(alerts);
				program.addLoginReward(loginReward);
			//WAIT
				synchronized (this) {
					wait(UPDATE_DELAY_MS);
				}
			} catch (InterruptedException ex) {
				LOG.error("AlertUpdater Thread Interrupted", ex);
			}
		}
	}

	private void waitForInternet() {
		InetAddress address;
		try {
			address = Inet4Address.getByName("http://deathsnacks.com/");
		} catch (UnknownHostException ex) {
			return;
		}
		boolean wait = true;
		while (wait) {
			try {		
				wait = !address.isReachable(1000); //Should take one 1sec to process
			} catch (IOException ex) {
				//No problem
			}
		}
	}
}
