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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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

	private void playSafe() {
		try {
			if (FileConstants.getAudioLocal().exists()) {
				playIt(new FileInputStream(FileConstants.getAudioLocal()));
			} else {
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
		} catch (InterruptedException ex) {
			//No problem, we just got interupted
		}
	}

	public void startTest() {
		stopTest();
		tester = new Tester();
		tester.start();
		try {
			tester.join();
		} catch (InterruptedException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	public void stopTest() {
		if (tester != null) {
			tester.interrupt();
			tester = null;
		}
	}

	public void playTest() {
		try {
			playIt(new FileInputStream(FileConstants.getAudioLocal()));
		} catch (MalformedURLException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (UnsupportedAudioFileException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (LineUnavailableException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (InterruptedException ex) {
			//No problem, we just got interupted
		}
	}

	private void playIt(InputStream inputStream) throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
		AudioListener listener = new AudioListener();
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream));
		try {
			Clip clip = AudioSystem.getClip();
			clip.addLineListener(listener);
			clip.open(audioInputStream);
			try {
				clip.start();
				listener.waitUntilDone();
			} finally {
				clip.close();
			}
		} finally {
			audioInputStream.close();
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
	public void startNotify(final int count, final NotifySource source) {
		if (beeper == null && program.getSettings(SettingsConstants.NOTIFY_AUDIO)) {
			beeper = new Beeper();
			beeper.start();
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

		@Override
		public void run() {
			playTest();
		}
	}

	private class Beeper extends Thread {

		private boolean run = true;

		@Override
		public void run() {
			try {
				while (isRun()) {
					playSafe();
					synchronized (this) {
						wait(1000 * 20);
					}
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
