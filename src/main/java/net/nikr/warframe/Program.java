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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JFrame;
import net.nikr.warframe.gui.MainFrame;
import net.nikr.warframe.gui.about.AboutTool;
import net.nikr.warframe.gui.alert.AlertTool;
import net.nikr.warframe.gui.audio.AudioTool;
import net.nikr.warframe.gui.filters.FiltersTool;
import net.nikr.warframe.gui.invasion.InvasionTool;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.gui.reward.RewardTool;
import net.nikr.warframe.gui.reward.Zoom;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.settings.SettingsTool;
import net.nikr.warframe.gui.shared.CategoryFilter;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.gui.shared.listeners.AlertListener;
import net.nikr.warframe.gui.shared.listeners.InvasionListener;
import net.nikr.warframe.gui.shared.listeners.LoginRewardListener;
import net.nikr.warframe.gui.shared.listeners.NotifyListener;
import net.nikr.warframe.gui.shared.listeners.NotifyListener.NotifySource;
import net.nikr.warframe.gui.tray.TrayTool;
import net.nikr.warframe.io.alert.Alert;
import net.nikr.warframe.io.category.CategoryReader;
import net.nikr.warframe.io.category.CategoryWriter;
import net.nikr.warframe.io.filters.FiltersGetter;
import net.nikr.warframe.io.invasion.Invasion;
import net.nikr.warframe.io.run.AutoRun;
import net.nikr.warframe.io.shared.DataUpdater;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ImageGetter;
import net.nikr.warframe.io.shared.ListReader;
import net.nikr.warframe.io.shared.ListWriter;
import net.nikr.warframe.io.shared.RewardsGetter;
import net.nikr.warframe.io.update.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Program {

	private static final Logger LOG = LoggerFactory.getLogger(Program.class);

	public static final String PROGRAM_VERSION = "BETA 3";
	public static final String PROGRAM_NAME = "jWarframe";

	private final List<InvasionListener> invasionListeners = new ArrayList<InvasionListener>();
	private final List<AlertListener> alertListeners = new ArrayList<AlertListener>();
	private final List<LoginRewardListener> loginRewardListeners = new ArrayList<LoginRewardListener>();
	private final List<NotifyListener> notifyListeners = new ArrayList<NotifyListener>();
	private final List<Tool> tools = new ArrayList<Tool>();
	private final List<RewardTool> inventories = new ArrayList<RewardTool>();
	private final MainFrame mainFrame;
	private final AlertTool alertTool;
	private final InvasionTool invasionTool;
	private final TrayTool trayTool;
	private final FiltersTool filtersTool;
	private final AudioTool audioTool;


	private final List<Category> categories;
	private final Set<String> filters;
	private final Set<RewardID> rewards = new TreeSet<RewardID>();
	private final Set<String> done = new TreeSet<String>();
	private final Set<SettingsConstants> settings = EnumSet.noneOf(SettingsConstants.class);
	private final Set<String> filterSets = new TreeSet<String>();
	private final Map<String, Map<String, CategoryFilter>> categoryFilter = new HashMap<String, Map<String, CategoryFilter>>();

	private int settingsVersion = 0;

	public Program() {
		//Static Data
		SplashUpdater.setText("Loading DATA");

		Updater updater = new Updater();
		updater.update();

		RewardsGetter rewardsGetter = new RewardsGetter();
		rewardsGetter.checkUpdates();
	//LOCAL
		//AutoRun
		AutoRun.update();
		//Filters
		filters = loadFilters();
		updateFilters();
		//Categories
		categories = loadCategories();
		//Done
		loadDone();
		//Settings
		loadSettings();
		SplashUpdater.setProgress(5);
		//Images
		downloadImages();
		//Filter Sets
		filterSets.addAll(FileConstants.getFileList(FileConstants.getFilterSets(), ".dat"));
		//Zoom
		Map<String, Zoom> zoom = loadZoom();
		

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

		audioTool = new AudioTool(this);
		SplashUpdater.setProgress(20);

		float add = 60 / categories.size();
		float total = 0;
		for (Category category : categories) {
			//GUI
			RewardTool inventoryTool = new RewardTool(this, category.getRewards(), category.getName(), category.getWidth(), category.getHeight(), zoom.get(category.getName()));
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

		SettingsTool settingsTool = new SettingsTool(this);
		tools.add(settingsTool);
		mainFrame.add(settingsTool);
		SplashUpdater.setProgress(85);

		mainFrame.add(new AboutTool(this));
		SplashUpdater.setProgress(90);

		trayTool = new TrayTool(this);
		SplashUpdater.setProgress(95);

		addAlertListener(alertTool);
		addNotifyListener(audioTool);
		addNotifyListener(trayTool);
		addNotifyListener(mainFrame);
		addInvasionListener(invasionTool);
		addLoginRewardListener(mainFrame);
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

	private void addLoginRewardListener(LoginRewardListener listener) {
		loginRewardListeners.add(listener);
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

	public void addLoginReward(final Boolean available) {
		for (LoginRewardListener listener : loginRewardListeners) {
			listener.addLoginReward(available);
		}
	}

	public boolean audioStartTest(String filename) {
		return audioTool.startTest(filename);
	}

	public void audioStopTest() {
		audioTool.stopTest();
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

	public void startNotify(final int count, final NotifySource source, final Set<String> categories) {
		LOG.info("Notification started");
		for (NotifyListener listener : notifyListeners) {
			listener.startNotify(count, source, categories);
		}
	}

	public JFrame getWindow() {
		return mainFrame.getWindow();
	}

	public final void setAlert(int match, int total) {
		mainFrame.setAlert(match, total);
	}

	public final void setInvasions(int match, int total) {
		mainFrame.setInvasions(match, total);
	}

	public void saveFilters() {
		LOG.info("Saving Filters");
		for (RewardTool inventory : inventories) {
			inventory.update();
		}
		alertTool.filter();
		invasionTool.filter();
		ListWriter writer = new ListWriter();
		writer.save(filters, FileConstants.getFilters());
	}

	public void saveSettings() {
		LOG.info("Saving Settings");
		Set<String> settingsValues = new HashSet<String>();
		settingsValues.add(String.valueOf(SettingsConstants.getSettingsVersion()));
		settingsValues.add(SettingsConstants.SETTINGS_SET.name());
		settings.clear(); //Clear old settings
		settings.add(SettingsConstants.SETTINGS_SET); //Settings have been saved!
		for (Tool tool : tools) {
			for (SettingsConstants setting : tool.getSettings()) {
				settingsValues.add(setting.name());
				settings.add(setting); //Add new settings
			}
		}
		ListWriter writer = new ListWriter();
		writer.save(settingsValues, FileConstants.getSettings());

		categoryFilter.clear();
		categoryFilter.put(alertTool.getToolName(), alertTool.getCategoryFilters());
		categoryFilter.put(invasionTool.getToolName(), invasionTool.getCategoryFilters());
		CategoryWriter categoryWriter = new CategoryWriter();
		categoryWriter.save(categoryFilter);
	}

	public boolean getSettings(SettingsConstants constants) {
		if (settings.contains(SettingsConstants.SETTINGS_SET) //Settings is set
				&& constants.getVersion() <= settingsVersion) { //Not new setting
			return settings.contains(constants);
		} else { //New setting or settings not set
			return constants.getValue();
		}
	}

	public Set<String> getFilterSets() {
		return filterSets;
	}

	public Set<RewardID> getRewards() {
		return rewards;
	}

	public Set<String> getDone() {
		return Collections.unmodifiableSet(done);
	}

	public void doneAdd(String id) {
		boolean updated = done.add(id);
		if (updated) {
			saveDone();
		}
	}

	public void doneRemove(String id) {
		boolean updated = done.remove(id);
		if (updated) {
			saveDone();
		}
	}

	public List<Category> getCategories() {
		return categories;
	}

	public Map<String, CategoryFilter> getCategoryFilter(String toolName) {
		Map<String, CategoryFilter> categoryFilters = categoryFilter.get(toolName);
		if (categoryFilters != null) {
			return categoryFilters;
		} else {
			return new HashMap<String, CategoryFilter>();
		}
	}

	private void loadSettings() {
		LOG.info("Loading Settings");
		settings.clear();
		ListReader reader = new ListReader();
		for (String s : reader.load(FileConstants.getSettings())) {
			try {
				settings.add(SettingsConstants.valueOf(s));
			} catch (IllegalArgumentException ex) {
				try {
					settingsVersion = Integer.valueOf(s);
				} catch (NumberFormatException exception) {
					LOG.warn(s + " removed from settings enum");
				}				
			}
		}

		CategoryReader categoryReader = new CategoryReader();
		categoryFilter.clear();
		categoryFilter.putAll(categoryReader.load());
	}

	public Set<String> getFilters() {
		return filters;
	}

	public void loadFilters(File file) {
		LOG.info("Loading Filter List");
		ListReader reader = new ListReader();
		filters.clear();
		filters.addAll(new TreeSet<String>(reader.load(file)));
		filtersTool.updateFilters();
		saveFilters();
	}

	private Set<String> loadFilters() {
		LOG.info("Loading Filters");
		ListReader reader = new ListReader();
		return new TreeSet<String>(reader.load(FileConstants.getFilters()));
	}

	private void updateFilters() {
		LOG.info("Updating Filters");
		FiltersGetter getter = new FiltersGetter();
		List<String> filerFix = getter.get();
		for (String fix : filerFix) {
			String[] split = fix.split("=");
			if (split.length == 2) {
				String oldName = split[0];
				String newName = split[1];
				if (filters.contains(oldName)) {
					filters.remove(oldName);
					filters.add(newName);
				}
			} else {
				LOG.warn("Failed to split: " + fix);
			}
		}
	}

	private Set<RewardID> loadRewards(Category category) {
		LOG.info("Loading Rewards");
		File file = FileConstants.getCategoryLocal(category.getFilename());
		ListReader reader = new ListReader();
		Set<RewardID> categoryRewards = new TreeSet<RewardID>();
		for (String s :reader.load(file)) {
			categoryRewards.add(new RewardID(s));
		}
		return categoryRewards;
	}

	private List<Category> loadCategories() {
		LOG.info("Loading Categories");
		List<Category> categoryList = new ArrayList<Category>();
		ListReader reader = new ListReader();
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
		LOG.info("Saving Done");
		ListWriter writer = new ListWriter();
		writer.save(done, FileConstants.getDone());
	}

	private void loadDone() {
		LOG.info("Loading Done");
		ListReader reader = new ListReader();
		List<String> list = reader.load(FileConstants.getDone());
		done.addAll(list);
	}

	public void saveZoom() {
		LOG.info("Saving Zoom");
		List<String> list = new ArrayList<String>();
		for (RewardTool rewardTool : inventories) {
			list.add(rewardTool.getName()+ ";" + rewardTool.getZoom().name());
		}
		ListWriter writer = new ListWriter();
		writer.save(list, FileConstants.getZoom());
	}

	private Map<String, Zoom> loadZoom() {
		LOG.info("Loading Zoom");
		Map<String, Zoom> map = new HashMap<String, Zoom>();
		ListReader reader = new ListReader();
		List<String> list = reader.load(FileConstants.getZoom());
		for (String s : list) {
			String[] split = s.split(";");
			if (split.length == 2) {
				try {
					map.put(split[0], Zoom.valueOf(split[1]));
				} catch (IllegalArgumentException ex) {
					LOG.warn("Failed to convert: " + split[1] + " to Zoom enum");
				}
			}
		}
		return map;
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
