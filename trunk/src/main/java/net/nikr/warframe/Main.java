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
import java.net.URISyntaxException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import static net.nikr.warframe.Program.PROGRAM_NAME;
import static net.nikr.warframe.Program.PROGRAM_VERSION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Main {
	/**
	 * We cannot init this until we have set the two system properties: log.home and log.level
	 * They are set in the entry point method and then the LOG is created.
	 *
	 */
	private static Logger log;

	/**
	 * jWarframe main launcher.
	 */
	private final Program program;

	/** Creates a new instance of Main. */
	private Main() {
		log.info("Starting {} {}", PROGRAM_NAME, PROGRAM_VERSION);
		log.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		log.info("Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
		program = new Program();
		program.start();
	}

	/**
	 * Entry point for jWarframe.
	 * @param args the command line arguments
	 */
	public static void main(final String[] args) {
		boolean debug = false;
		boolean portable = false;
		boolean forceNoUpdate = false;
		boolean forceUpdate = false;
		boolean lazySave = false;

		for (String arg : args) {
			if (arg.toLowerCase().equals("-debug")) {
				debug = true;
			}
			if (arg.toLowerCase().equals("-portable")) {
				portable = true;
			}
			if (arg.toLowerCase().equals("-noupdate")) {
				forceNoUpdate = true;
			}
			if (arg.toLowerCase().equals("-update")) {
				forceUpdate = true;
			}
			if (arg.toLowerCase().equals("-lazysave")) {
				lazySave = true;
			}
		}

		// the tests for null indicate that the property is not set
		// It is possible to set properties using the -Dlog.home=foo/bar
		// and thus we want to allow this to take priority over the
		// configuration options here.
		if (System.getProperty("log.home") == null) {
			if (portable) {
				try {
					//jwarframe.jar directory
					File file = new File(net.nikr.warframe.Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
					System.setProperty("log.home", file.getAbsolutePath() + File.separator);
				} catch (URISyntaxException ex) {
					//Working directory
					System.setProperty("log.home", System.getProperty("user.dir") + File.separator); //Working directory
				}
			} else {
				//Note: We can not use Program.onMac() as that will initialize the Program LOG
				if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) { //Mac
					System.setProperty("log.home", System.getProperty("user.home") + File.separator + "Library" + File.separator + "Preferences" + File.separator + "jWarFrame" + File.separator);
				} else { //Windows/Linux
					System.setProperty("log.home", System.getProperty("user.home") + File.separator + ".jwarframe" + File.separator);
				}
			}
		}
		// ditto here.
		if (System.getProperty("log.level") == null) {
			if (debug) {
				System.setProperty("log.level", "DEBUG");
			} else {
				System.setProperty("log.level", "INFO");
			}
		}

		// only now can we create the Logger.
		log = LoggerFactory.getLogger(Main.class);

		// fix the uncaught exception handlers
		System.setProperty("sun.awt.exception.handler", "net.nikr.eve.jeveasset.NikrUncaughtExceptionHandler");
		Thread.setDefaultUncaughtExceptionHandler(new NikrUncaughtExceptionHandler());

		//XXX - Workaround for IPv6 fail (force IPv4)
		//eveonline.com is not IPv6 ready...
		System.setProperty("java.net.preferIPv4Stack" , "true");

		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					createAndShowGUI();
				}
			});
	}

	private static void createAndShowGUI() {
		SplashUpdater.start();
		initLookAndFeel();

		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		Main main = new Main();
	}

	private static void initLookAndFeel() {
		String lookAndFeel;
		//lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
		lookAndFeel = UIManager.getSystemLookAndFeelClassName(); //System
		//lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName(); //Java
		//lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"; //Nimbus
		//lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
		//lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"; //GTK
		//lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception ex) {
			log.error("Failed to set LookAndFeel: " + lookAndFeel, ex);
		}
	}
}
