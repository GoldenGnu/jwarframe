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
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.nikr.warframe.gui.shared.NotifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AudioPlayer implements NotifyListener {

	private static final Logger LOG = LoggerFactory.getLogger(AudioPlayer.class);

	private Beeper beeper;

	private void playSafe() {
		try {
			playIt();
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (UnsupportedAudioFileException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (LineUnavailableException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (InterruptedException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	private void playIt() throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
		AudioListener listener = new AudioListener();
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(AudioPlayer.class.getResourceAsStream("alert.wav")));
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
			beeper = null;
		}
	}

	@Override
	public void startNotify(final int count, final NotifySource source) {
		if (beeper == null) {
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
