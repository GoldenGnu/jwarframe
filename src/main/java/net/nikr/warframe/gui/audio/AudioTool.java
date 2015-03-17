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
package net.nikr.warframe.gui.audio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.listeners.NotifyListener;
import net.nikr.warframe.io.shared.FileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioTool implements NotifyListener {

	private static final Logger LOG = LoggerFactory.getLogger(AudioTool.class);

	private Beeper beeper;
	private Tester tester;
	private final Program program;

	public AudioTool(Program program) {
		this.program = program;
	}

	public boolean startTest(String filename) {
		stopTest();
		tester = new Tester(filename);
		tester.start();
		try {
			tester.join();
		} catch (InterruptedException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return tester.isOk();
	}

	public void stopTest() {
		if (tester != null) {
			tester.interrupt();
		}
	}

	@Override
	public void stopNotify() {
		if (beeper != null) {
			beeper.stopASAP();
			beeper.interrupt();
			beeper = null;
		}
	}

	@Override
	public void startNotify(final int count, final NotifySource source, final Set<String> categories) {
		if (program.getSettings(SettingsConstants.NOTIFY_AUDIO) || program.getSettings(SettingsConstants.NOTIFY_AUDIO_REPEAT)) {
			if (beeper == null) {
				beeper = new Beeper(categories);
				beeper.start();
			} else {
				beeper.add(categories);
			}
		}
	}

	private void playSafe(String filename) {
		try {
			if (FileConstants.getAudioLocal(filename).exists()) { //Category
				playIt(new FileInputStream(FileConstants.getAudioLocal(filename)));
			} else if (FileConstants.getAudioLocal("alert.wav").exists()) { //Default
				playIt(new FileInputStream(FileConstants.getAudioLocal("alert.wav")));
			} else { //Fallback
				playIt(AudioTool.class.getResourceAsStream("alert.wav"));
			}
		} catch (MalformedURLException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (UnsupportedAudioFileException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (LineUnavailableException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (IllegalArgumentException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (InterruptedException ex) {
			//No problem, we just got interupted
		}
	}

	private boolean playTest(String filename) {
		try {
			playIt(new FileInputStream(FileConstants.getAudioLocal(filename)));
			return true;
		} catch (MalformedURLException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (UnsupportedAudioFileException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (LineUnavailableException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (IllegalArgumentException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (InterruptedException ex) {
			//No problem, we just got interupted
			return true;
		}
		return false;
	}

	private void playIt(InputStream inputStream) throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException, IllegalArgumentException {
		AudioListener listener = new AudioListener();
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream));
			AudioFormat format = audioInputStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.addLineListener(listener);
			clip.open(audioInputStream);
			try {
				clip.start();
				listener.waitUntilDone();
			} finally {
				clip.close();
			}
		} finally {
			inputStream.close();
		}
	}

	private class AudioListener implements LineListener {

		private boolean done = false;

		@Override
		public synchronized void update(LineEvent event) {
			LineEvent.Type eventType = event.getType();
			if (eventType == LineEvent.Type.STOP || eventType == LineEvent.Type.CLOSE) {
				done = true;
				notifyAll();
			}
		}

		public synchronized void waitUntilDone() throws InterruptedException {
			while (!done) {
				wait();
			}
		}
	}

	private class Tester extends Thread {

		private final String filename;
		private boolean ok = false;

		public Tester(String filename) {
			this.filename = filename;
		}

		@Override
		public void run() {
			ok = playTest(filename);
		}

		public boolean isOk() {
			return ok;
		}
	}

	private class Beeper extends Thread {

		private boolean run = true;
		private final Set<String> categories;
		private final Set<String> cache = new HashSet<String>();

		public Beeper(Set<String> categories) {
			if (categories.isEmpty()) {
				this.categories = new HashSet<String>();
			} else {
				this.categories = new HashSet<String>(categories);
			}
		}

		public synchronized void add(Set<String> categories) {
			cache.addAll(categories);
		}

		public synchronized Set<String> getCache() {
			return cache;
		}

		@Override
		public void run() {
			try {
				if (program.getSettings(SettingsConstants.NOTIFY_AUDIO_REPEAT)) { //Repeat until stopped
					while (isRun()) {
						if (!getCache().isEmpty()) {
							categories.addAll(getCache());
							getCache().clear();
						}
						if (categories.isEmpty()) {
							playSafe("alert.wav");
						} else {
							for (String category : categories) {
								playSafe(category.toLowerCase() + ".wav");
							}
						}
						synchronized (this) {
							wait(1000 * 20);
						}
					}
				} else { //Play each sound once
					do {
						if (!getCache().isEmpty()) {
							categories.addAll(getCache());
							getCache().clear();
						}
						if (categories.isEmpty()) {
							playSafe("alert.wav");
						} else {
							for (String category : categories) {
								playSafe(category.toLowerCase() + ".wav");
							}
							categories.clear();
						}
					} while ((!categories.isEmpty() && !getCache().isEmpty()));
				}
				
			} catch (InterruptedException ex) {
				//Thread interupted - we are done beeping...
			}
		}

		public synchronized boolean isRun() {
			return run;
		}

		public synchronized void stopASAP() {
			run = false;
		}
	}
}
