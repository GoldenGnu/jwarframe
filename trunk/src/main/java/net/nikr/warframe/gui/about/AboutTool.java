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

package net.nikr.warframe.gui.about;

import java.util.EnumSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.DesktopUtil;
import net.nikr.warframe.gui.shared.Tool;


public class AboutTool implements Tool {

	private final JPanel jPanel;
	private final Program program;

	public AboutTool(Program program) {
		this.program = program;

		jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel jIcon = new JLabel();
		jIcon.setIcon(Images.PROGRAM_64.getIcon());

		JEditorPane jProgram = createEditorPane(false,
				"<div style=\"font-size: 30pt;\"><b>" + Program.PROGRAM_NAME + "</b></div>"
				+ "Copyright &copy; 2014 Niklas Kyster Rasmussen<br>"
				);

		JEditorPane jInfo = createEditorPane(
				  "<b>Version</b><br>"
				+ "&nbsp;" + Program.PROGRAM_VERSION + "<br>"
				+ "<br>"
				+ "<b>Contributors</b><br>"
				+ "&nbsp;Niklas Kyster Rasmussen (Dev)<br>"
				+ "&nbsp;Tazmanyak (Data)<br>"
				+ "<br>"
				+ "<b>License</b><br>"
				+ "&nbsp;<a href=\"http://www.gnu.org/licenses/old-licenses/gpl-2.0.html\">GNU General Public License 2.0</a><br>"
				+ "<br>"
				+ "<b>www</b><br>"
				+ "&nbsp;<a href=\"https://code.google.com/p/jwarframe/\">Google Code Project</a> (developers)<br>"
				+ "<br>"
				+ "<br>"
				);

		JEditorPane jExternal = createEditorPane(
				  "<b>Content</b><br>"
				+ "&nbsp;<a href=\"http://deathsnacks.com/\">Deathsnacks</a> (api)<br> "
				+ "&nbsp;<a href=\"http://www.famfamfam.com/lab/icons/silk/\">Silk icons</a> (icons)<br>"
				+ "&nbsp;<a href=\"http://warframe.com/\">Warframe</a> (images)<br>"
				+ "&nbsp;<a href=\"http://warframe.wikia.com/wiki/WARFRAME_Wiki\">Warframe wikia</a> (images)<br>"
				+ "<br>"
				+ "<b>Libraries</b><br>"
				+ "&nbsp;<a href=\"http://publicobject.com/glazedlists/\">Glazed Lists</a> (table sorting and filtering)<br> "
				+ "&nbsp;<a href=\"http://junit.sourceforge.net/\">JUnit</a> (unit testing)<br>"
				+ "&nbsp;<a href=\"http://www.slf4j.org/\">slf4J</a> (logging)<br>"
				+ "&nbsp;<a href=\"http://logging.apache.org/log4j/1.2/\">log4j</a> (logging)<br>"
				+ "<br>"
				+ "<b>Source Code</b><br>"
				+ "&nbsp;<a href=\"http://code.google.com/p/jeveassets/\">jEveAssets</a> (misc utility classes)<br>"
				
						  /*
				+ "&nbsp;<a href=\"" + Program.PROGRAM_HOMEPAGE + "\">OSXAdapter</a> (native mac os x support)<br>"
		*/
				//+ "<br>"
				);

		JEditorPane jThanks =  createEditorPane(
				"<b>Special Thanks</b><br>"
				+ "&nbsp;<a href=\"http://www.reddit.com/user/Deathmax\">Deathmax</a> (<a href=\"http://deathsnacks.com/\">Deathsnacks</a>) for providing a great API.<br>"
				+ "&nbsp;<a href=\"http://www.digitalextremes.com/\">Digital Extremes</a> for creating <a href=\"http://warframe.com/\">Warframe</a>.<br>"
				+ "&nbsp;<a href=\"http://warframe.wikia.com/wiki/WARFRAME_Wiki\">Warframe wikia</a> for all the great information about Warframe.");

		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jIcon)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(jProgram)
					)
				)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jInfo)
					.addGap(10)
					.addComponent(jExternal)
				)
				.addComponent(jThanks)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(jIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(jProgram, GroupLayout.Alignment.CENTER, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(jInfo, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)
					.addComponent(jExternal)
				)
				.addComponent(jThanks, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
	}

	@Override
	public String getToolTip() {
		return "About";
	}

	@Override
	public String getTitle() {
		return "About";
	}

	@Override
	public JPanel getPanel() {
		return jPanel;
	}

	@Override
	public Set<SettingsConstants> getSettings() {
		return EnumSet.noneOf(SettingsConstants.class);
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	private JEditorPane createEditorPane(final String text) {
		return createEditorPane(true, text);
	}

	private JEditorPane createEditorPane(final boolean addBorder, final String text) {
		JEditorPane jEditorPane = new JEditorPane("text/html",
				"<html><div style=\"font-family: Arial, Helvetica, sans-serif; font-size: 11pt;\">"
				+ text
				+ "</div>"
				);
		jEditorPane.setEditable(false);
		jEditorPane.setOpaque(false);
		jEditorPane.addHyperlinkListener(DesktopUtil.getHyperlinkListener(program));
		if (addBorder) {
			jEditorPane.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(jPanel.getBackground().darker(), 1),
					BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		}
		return jEditorPane;
	}
	
}
