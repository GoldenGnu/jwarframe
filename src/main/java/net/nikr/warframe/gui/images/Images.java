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

package net.nikr.warframe.gui.images;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Images {
	ALL ("all.png"),
	NONE ("none.png"),
	HELP ("help.png"),
	LINK ("link.png"),
	PLAY ("play.png"),
	STOP ("stop.png"),
	CLOSE ("close.png"),
	CLOSE_ROLLOVER ("close_rollover.png"),
	BROWSE ("browse.png"),
	INVASION ("invasion.png"),
	ALERT ("alert.png"),
	NOTIFY_INVASION ("notify_invasion.png"),
	NOTIFY_ALERT ("notify_alert.png"),
	NOTIFY_LOGIN ("notify_login.png"),
	SETTINGS ("settings.png"),
	CORPUS ("corpus.png"),
	GRINEER ("grineer.png"),
	LOGIN_REWARD ("login_reward.png"),
	INFESTATION ("infestation.png"),
	PROGRAM_16 ("program_16.png"),
	PROGRAM_32 ("program_32.png"),
	PROGRAM_64 ("program_64.png"),
	PROGRAM_DISABLED_16 ("program_disabled_16.png"),
	PROGRAM_DISABLED_32 ("program_disabled_32.png"),
	PROGRAM_DISABLED_64 ("program_disabled_64.png"),
	MISSION_TYPES_ALL ("mission_types_all.png"),
	MISSION_TYPES_SOME ("mission_types_some.png"),
	MISSION_TYPES_NONE ("mission_types_none.png");

	private static final Logger LOG = LoggerFactory.getLogger(Images.class);
	private final String filename;
	private BufferedImage image = null;
	private Icon icon;

	Images(final String filename) {
		this.filename = filename;
	}

	public Icon getIcon() {
		load();
		return icon;
	}

	public Image getImage() {
		load();
		return image;
	}

	public BufferedImage getBufferedImage() {
		load();
		return image;
	}

	public String getFilename() {
		return filename;
	}

	private boolean load() {
		if (image == null) {
			image = getBufferedImage(filename);
			icon = new ImageIcon(image);
		}
		return (image != null);
	}

	public static boolean preload() {
		boolean ok = true;
		for (Images i : Images.values()) {
			if (!i.load()) {
				ok = false;
			}
		}
		return ok;
	}

	public static BufferedImage getBufferedImage(final String s) {
		try {
			java.net.URL imgURL = Images.class.getResource(s);
			if (imgURL != null) {
				return ImageIO.read(imgURL);
			} else {
				LOG.warn("image: " + s + " not found (URL == null)");
			}
		} catch (IOException ex) {
			LOG.warn("image: " + s + " not found (IOException)");
		}
		return null;
	}
}
