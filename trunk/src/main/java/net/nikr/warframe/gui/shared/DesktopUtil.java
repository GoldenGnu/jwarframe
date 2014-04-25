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

import java.awt.Desktop;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.reward.RewardID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DesktopUtil {

	private static final Logger LOG = LoggerFactory.getLogger(DesktopUtil.class);

	private DesktopUtil() { }

	public static HyperlinkListener getHyperlinkListener(Program program) {
		return new LinkListener(program);
	}

	private static boolean isSupported(final Desktop.Action action) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(action)) {
				return true;
			}
		}
		return false;
	}

	public static void open(final String filename, final Program program) {
		File file = new File(filename);
		LOG.info("Opening: {}", file.getName());
		if (isSupported(Desktop.Action.OPEN)) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(file);
				return;
			} catch (IOException ex) {
				LOG.warn("	Opening Failed: {}", ex.getMessage());
			}
		} else {
			LOG.warn("	Opening failed");
		}
		JOptionPane.showMessageDialog(program.getWindow(), "Could not open " + file.getName(), "Open File", JOptionPane.PLAIN_MESSAGE);
	}

	public static void browseReward(Program program, RewardID rewardID, boolean askToOpen) {
		//Test link while we ask for user interaction
		Thread openLink = new OpenLink(program, rewardID, askToOpen);
		openLink.start();
		
	}

	public static void browse(final String url) {
		browse(url, (Window)null);
	}

	public static void browse(final String url, Program program) {
		if (program != null) {
			browse(url, program.getWindow());
		} else {
			browse(url, (Window)null);
		}
	}

	public static void browse(final String url, final Window window) {
		LOG.info("Browsing: {}", url);
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException ex) {
			uri = null;
		}
		if (isSupported(Desktop.Action.BROWSE) && uri != null) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(uri);
				return;
			} catch (IOException ex) {
				LOG.warn("	Browsing Failed: {}", ex.getMessage());
			}
		} else {
			LOG.warn("	Browsing failed");
		}
		JOptionPane.showMessageDialog(window, "Could not browse to:\n" + url, "Browse", JOptionPane.PLAIN_MESSAGE);
	}

	private static class LinkListener implements HyperlinkListener {

		private final Program program;

		public LinkListener(Program program) {
			this.program = program;
		}

		@Override
		public void hyperlinkUpdate(final HyperlinkEvent hle) {
			Object o = hle.getSource();
			if (o instanceof JEditorPane) {
				JEditorPane jEditorPane = (JEditorPane) o;
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType()) && jEditorPane.isEnabled()) {
					browse(hle.getURL().toString(), program);
				}
			}
		}
	}

	private static class TestLink extends Thread {
		private final RewardID rewardID;
		private boolean working;

		public TestLink(RewardID rewardID) {
			this.rewardID = rewardID;
		}
		
		@Override
		public void run() {
			working = testLink();
		}

		public boolean isWorking() {
			return working;
		}

		private boolean testLink() {
			try {
				URL url = new URL(rewardID.getDirectURL());
				URLConnection connection = url.openConnection();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String str;
				while ((str = bufferedReader.readLine()) != null) {
					if (str.contains("This page needs content")) {
						bufferedReader.close();
						return false;
					}
				}
				bufferedReader.close();
				return true;
			} catch (MalformedURLException ex) {
				return false;
			} catch (IOException ex) {
				return false;
			}
		}
	}

	private static class OpenLink extends Thread {
		private final Program program;
		private final RewardID rewardID;
		private final boolean askToOpen;

		public OpenLink(Program program, RewardID rewardID, boolean askToOpen) {
			this.askToOpen = askToOpen;
			this.rewardID = rewardID;
			this.program = program;
		}
		
		@Override
		public void run() {
			TestLink testLink = new TestLink(rewardID);
			testLink.start();
			int value;
			if (askToOpen) {
				value = JOptionPane.showConfirmDialog(program.getWindow(), "Show in browser?", "Open Browser", JOptionPane.OK_CANCEL_OPTION);
			} else {
				value = JOptionPane.OK_OPTION;
			}
			
			if (value == JOptionPane.OK_OPTION) {
				try {
					testLink.join(); //Wait for test to finish
				} catch (InterruptedException ex) {
					//failed somehow? :(
				}
				boolean working = testLink.isWorking();
				if (working) {
					DesktopUtil.browse(rewardID.getDirectURL(), program);
				} else {
					DesktopUtil.browse(rewardID.getSearchURL(), program);
				}

			}
		}
	}
}
