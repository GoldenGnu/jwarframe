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

package net.nikr.warframe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JFrame;
import net.nikr.warframe.gui.MainFrame;
import net.nikr.warframe.gui.about.AboutTool;
import net.nikr.warframe.gui.alert.AlertTool;
import net.nikr.warframe.gui.audio.AudioPlayer;
import net.nikr.warframe.gui.filters.FiltersTool;
import net.nikr.warframe.gui.invasion.InvasionTool;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.gui.reward.RewardTool;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.AlertListener;
import net.nikr.warframe.gui.shared.InvasionListener;
import net.nikr.warframe.gui.shared.NotifyListener;
import net.nikr.warframe.gui.shared.NotifyListener.NotifySource;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.gui.tray.TrayTool;
import net.nikr.warframe.io.alert.Alert;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.shared.DataUpdater;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ImageGetter;
import net.nikr.warframe.io.shared.RewardsGetter;
import net.nikr.warframe.io.shared.StringReader;
import net.nikr.warframe.io.shared.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Program {

	private static final Logger LOG = LoggerFactory.getLogger(Program.class);

	public static final String PROGRAM_VERSION = "1.0.0 DEV BUILD 1";
	public static final String PROGRAM_NAME = "jWarframe";

	private final List<InvasionListener> invasionListeners = new ArrayList<InvasionListener>();
	private final List<AlertListener> alertListeners = new ArrayList<AlertListener>();
	private final List<NotifyListener> notifyListeners = new ArrayList<NotifyListener>();
	private final List<Tool> tools = new ArrayList<Tool>();
	private final List<RewardTool> inventories = new ArrayList<RewardTool>();
	private final MainFrame mainFrame;
	private final AlertTool alertTool;
	private final InvasionTool invasionTool;
	private final TrayTool trayTool;
	private final FiltersTool filtersTool;
	

	private final List<Category> categories;
	private final Set<String> filters;
	private final Set<RewardID> rewards = new TreeSet<RewardID>();
	private final Set<String> done = new TreeSet<String>();
	private final Set<SettingsConstants> settings = EnumSet.noneOf(SettingsConstants.class);;

	public Program() {
		//Static Data
		SplashUpdater.setText("Loading DATA");
		RewardsGetter rewardsGetter = new RewardsGetter();
		rewardsGetter.checkUpdates();
		filters = loadFilters();
		categories = loadCategories();
		loadDone();
		loadSettings();
		SplashUpdater.setProgress(5);
		downloadImages();
		

		SplashUpdater.setText("Loading GUI");
		//GUI
		mainFrame = new MainFrame(this);
		SplashUpdater.setProgress(5);

		alertTool = new AlertTool(this);
		tools.add(alertTool);
		mainFrame.add(alertTool);
		SplashUpdater.setProgress(10);

		invasionTool = new InvasionTool(this);
		tools.add(invasionTool);
		mainFrame.add(invasionTool);
		SplashUpdater.setProgress(15);

		AudioPlayer audioPlayer = new AudioPlayer();
		SplashUpdater.setProgress(20);

		float add = 60 / categories.size();
		float total = 0;
		for (Category category : categories) {
			//GUI
			RewardTool inventoryTool = new RewardTool(this, category.getRewards(), category.getName(), category.getWidth(), category.getHeight());
			tools.add(inventoryTool);
			inventories.add(inventoryTool);
			mainFrame.add(inventoryTool);
			int lastTotal = (int)Math.ceil(total);
			total = total + add;
			int addNow = (int)Math.ceil(total - lastTotal);
			SplashUpdater.addProgress(addNow);
		}

		filtersTool = new FiltersTool(this);
		tools.add(filtersTool);
		mainFrame.add(filtersTool);
		SplashUpdater.setProgress(85);

		mainFrame.add(new AboutTool(this));
		SplashUpdater.setProgress(90);

		trayTool = new TrayTool(this);
		SplashUpdater.setProgress(95);

		addAlertListener(alertTool);
		addNotifyListener(audioPlayer);
		addNotifyListener(trayTool);
		addNotifyListener(mainFrame);
		addInvasionListener(invasionTool);
	}

	public void start() {
		//Deathsnacks.com
		DataUpdater dataUpdater = new DataUpdater(this);
		dataUpdater.start();
		//Show GUI
		SplashUpdater.setProgress(100);
		mainFrame.show();
	}

	private void addAlertListener(AlertListener listener) {
		alertListeners.add(listener);
	}

	private void addInvasionListener(InvasionListener listener) {
		invasionListeners.add(listener);
	}

	private void addNotifyListener(NotifyListener listener) {
		notifyListeners.add(listener);
	}

	public void addAlerts(final List<Alert> alerts) {
		for (AlertListener listener : alertListeners) {
			listener.addAlerts(alerts);
		}
	}

	public void addInvasions(final List<Invasion> invasions) {
		for (InvasionListener listener : invasionListeners) {
			listener.addInvasions(invasions);
		}
	}

	public FiltersTool getFiltersTool() {
		return filtersTool;
	}

	public void stopNotify() {
		LOG.info("Notification stopped");
		for (NotifyListener listener : notifyListeners) {
			listener.stopNotify();
		}
	}

	public void startNotify(final int count, final NotifySource source) {
		LOG.info("Notification started");
		for (NotifyListener listener : notifyListeners) {
			listener.startNotify(count, source);
		}
	}

	public JFrame getWindow() {
		return mainFrame.getWindow();
	}

	public void saveFilters() {
		for (RewardTool inventory : inventories) {
			inventory.update();
		}
		alertTool.filter();
		invasionTool.filter();
		filtersTool.filter();
		StringWriter writer = new StringWriter();
		writer.save(filters, FileConstants.getFilters());
	}

	public void saveSettings() {
		Set<String> settingsValues = new HashSet<String>();
		settingsValues.add(SettingsConstants.SETTINGS_SET.name());
		for (Tool tool : tools) {
			for (SettingsConstants setting : tool.getSettings()) {
				settingsValues.add(setting.name());
			}
		}
		StringWriter writer = new StringWriter();
		writer.save(settingsValues, FileConstants.getSettings());
	}

	public boolean getSettings(SettingsConstants constants) {
		return settings.contains(constants);
	}

	public Set<RewardID> getRewards() {
		return rewards;
	}

	public Set<String> getDone() {
		return Collections.unmodifiableSet(done);
	}

	public void doneAdd(String id) {
		System.out.println("done > add");
		done.add(id);
		saveDone();
	}

	public void doneRemove(String id) {
		System.out.println("done > remove");
		done.remove(id);
		saveDone();
	}

	public List<Category> getCategories() {
		return categories;
	}

	private void loadSettings() {
		settings.clear();
		StringReader reader = new StringReader();
		for (String s : reader.load(FileConstants.getSettings())) {
			try {
				settings.add(SettingsConstants.valueOf(s));
			} catch (IllegalArgumentException ex) {
				LOG.warn(s + " removed from settings enum");
			}
		}
	}

	public Set<String> getFilters() {
		return filters;
	}

	private Set<String> loadFilters() {
		StringReader reader = new StringReader();
		return new HashSet<String>(reader.load(FileConstants.getFilters()));
	}

	private Set<RewardID> loadRewards(Category category) {
		File file = FileConstants.getCategoryLocal(category.getFilename());
		StringReader reader = new StringReader();
		Set<RewardID> categoryRewards = new TreeSet<RewardID>();
		for (String s :reader.load(file)) {
			categoryRewards.add(new RewardID(s));
		}
		return categoryRewards;
	}

	private List<Category> loadCategories() {
		List<Category> categoryList = new ArrayList<Category>();
		StringReader reader = new StringReader();
		for (String s : reader.load(FileConstants.getCategoriesLocal())) {
			Category category = new Category(s);
			Set<RewardID> categoryRewards = loadRewards(category);
			category.addAll(categoryRewards);
			rewards.addAll(categoryRewards);
			categoryList.add(category);
		}
		return categoryList;
	}

	private void downloadImages() {
		LOG.info("Downloading images...");
		List<Thread> threads = new ArrayList<Thread>();
		List<List<RewardID>> lists = split(new ArrayList<RewardID>(rewards), 50);
		//threads.add(new Download(new ArrayList<String>(rewards))); //Single download thread
		for (List<RewardID> list : lists) {
			threads.add(new Download(list, (100.0f / rewards.size()) ));
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ex) {
				//No problem, really
			}
		}
		SplashUpdater.setSubProgress(100);
		LOG.info("Images downloaded");
	}

	private <E> List<List<E>> split(List<E> in, int partitionSize) {
        List<List<E>> partitions = new ArrayList<List<E>>();
        for (int i = 0; i < in.size(); i += partitionSize) {
            partitions.add(new ArrayList<E>(in.subList(i, i + Math.min(partitionSize, in.size() - i))));
        }
        return partitions;
    }

	private void saveDone() {
		StringWriter writer = new StringWriter();
		writer.save(done, FileConstants.getDone());
	}

	private void loadDone() {
		StringReader reader = new StringReader();
		List<String> list = reader.load(FileConstants.getDone());
		done.addAll(list);
	}

	private static class Download extends Thread {

		private final List<RewardID> rewards;
		private final float add;

		public Download(List<RewardID> rewards, float add) {
			this.rewards = rewards;
			this.add = add;
		}

		@Override
		public void run() {
			float total = 0;
			for (RewardID reward : rewards) {
				ImageGetter.download(reward.getName());
				int lastTotal = (int)Math.ceil(total);
				total = total + add;
				int addNow = (int)Math.ceil(total - lastTotal);
				if (addNow < 100) {
					SplashUpdater.addSubProgress(addNow);
				}
			}
		}
		
	}
}
