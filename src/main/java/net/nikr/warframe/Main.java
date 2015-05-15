/*
 * Copyright 2014-2015 Niklas Kyster Rasmussen
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
import javax.swing.UnsupportedLookAndFeelException;
import static net.nikr.warframe.Program.PROGRAM_NAME;
import static net.nikr.warframe.Program.PROGRAM_VERSION;
import net.nikr.warframe.io.shared.FileConstants;
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

	private static boolean portable = false;
	private static boolean startup = false;
	private static boolean debug = false;
	private static boolean cleardata = false;

	/**
	 * Entry point for jWarframe.
	 * @param args the command line arguments
	 */
	public static void main(final String[] args) {
		for (String arg : args) {
			if (arg.toLowerCase().equals("-debug")) {
				debug = true;
			}
			if (arg.toLowerCase().equals("-portable")) {
				portable = true;
			}
			if (arg.toLowerCase().equals("-startup")) {
				startup = true;
			}
			if (arg.toLowerCase().equals("-cleardata")) {
				cleardata = true;
			}
		}

		// the tests for null indicate that the property is not set
		// It is possible to set properties using the -Dlog.home=foo/bar
		// and thus we want to allow this to take priority over the
		// configuration options here.
		if (System.getProperty("log.home") == null) {
			boolean ok = true;
			if (portable) {
				try {
					//jwarframe.jar directory
					File file = new File(net.nikr.warframe.Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
					System.setProperty("log.home", file.getAbsolutePath() + File.separator + "logs" + File.separator);
				} catch (URISyntaxException ex) {
					ok = false;
				}
			}
			if (!portable || !ok) {
				if (isMac()) { //Mac
					System.setProperty("log.home", System.getProperty("user.home") + File.separator + "Library" + File.separator + "Preferences" + File.separator + "jWarframe" + File.separator + "logs" + File.separator);
				} else { //Windows/Linux
					System.setProperty("log.home", System.getProperty("user.home") + File.separator + ".jwarframe" + File.separator + "logs" + File.separator);
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
		NikrUncaughtExceptionHandler.install();

		if (cleardata) {
			clearData();
		}

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

	public static boolean isPortable() {
		return portable;
	}

	public static boolean isStartup() {
		return startup;
	}

	public static boolean isMac() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
	}

	private static void createAndShowGUI() {
		SplashUpdater.start();
		initLookAndFeel();

		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		log.info("Starting {} {}", PROGRAM_NAME, PROGRAM_VERSION);
		log.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		log.info("Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
		log.info("Debug: " + debug);
		log.info("Portable: " + portable);
		log.info("Startup: " + startup);
		log.info("ClearData: " + cleardata);
		Program program = new Program();
		program.start();
	}

	private static void clearData() {
		//Clear all downloadable data
		File data = FileConstants.getDataDirectory();
		FileConstants.deleteDirectory(data);
		File images = FileConstants.getImageDirectory();
		FileConstants.deleteDirectory(images);
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
		} catch (ClassNotFoundException ex) {
			log.error("Failed to set LookAndFeel: " + lookAndFeel, ex);
		} catch (InstantiationException ex) {
			log.error("Failed to set LookAndFeel: " + lookAndFeel, ex);
		} catch (IllegalAccessException ex) {
			log.error("Failed to set LookAndFeel: " + lookAndFeel, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			log.error("Failed to set LookAndFeel: " + lookAndFeel, ex);
		}
	}
}
