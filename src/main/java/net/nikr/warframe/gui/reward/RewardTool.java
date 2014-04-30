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

package net.nikr.warframe.gui.reward;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.DesktopUtil;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.io.shared.ImageGetter;


public class RewardTool implements Tool {

	private static final int BORDER_WIDTH = 4;
	private static final Color BORDER_COLOR = new Color(20, 20, 20);
	private static final Color BACKGROUND_COLOR = Color.BLACK;

	private final JPanel jPanel;
	private final JPanelDynamicGrid jItems;
	private final JRadioButton jAll;
	private final JRadioButton jMissing;
	private final JRadioButton jGot;
	private final JLabel jCount;

	private final Program program;

	private final Set<RewardID> rewards;
	private final String title;
	private final String toolTip;
	private final Icon icon;

	private final int width;
	private final int height;
	private final Font font;

	public RewardTool(final Program program, final Set<RewardID> rewards, final String title, final int width, final int height) {
		this.program = program;
		this.rewards = rewards;
		this.width = width;
		this.height = height;
		this.icon = ImageGetter.getIcon(title);
		this.toolTip = title;
		if (icon != null) {
			this.title = "";
		} else {
			this.title = title;
		}

		jPanel = new JPanel();
		GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		font = new Font(jPanel.getFont().getName(), Font.BOLD, (jPanel.getFont().getSize() + 5));

		jCount = new JLabel();

		jAll = createRadioButton("All");
		jMissing = createRadioButton("Notify");
		jGot = createRadioButton("Ignore");

		JLabel jHelp = new JLabel("Right click to show options");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jAll);
		buttonGroup.add(jMissing);
		buttonGroup.add(jGot);

		jAll.setSelected(true);

		jItems = new JPanelDynamicGrid();
		jItems.setBackground(BACKGROUND_COLOR);
		jItems.setBorder(BorderFactory.createMatteBorder(BORDER_WIDTH, BORDER_WIDTH, 0, 0, BORDER_COLOR));

		JPanel jItemsPanel = new JPanel();
		jItemsPanel.setBackground(BACKGROUND_COLOR);
		GroupLayout groupLayout = new GroupLayout(jItemsPanel);
		jItemsPanel.setLayout(groupLayout);

		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup()
				.addComponent(jItems, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup()
				.addComponent(jItems, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);

		JScrollPane jItemsScroll = new JScrollPane(jItemsPanel);
		jItemsScroll.getVerticalScrollBar().setUnitIncrement(16);

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(jAll)
					.addComponent(jMissing)
					.addComponent(jGot)
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jHelp)
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jCount)
				)
				.addComponent(jItemsScroll)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(jAll)
					.addComponent(jMissing)
					.addComponent(jGot)
					.addComponent(jHelp)
					.addComponent(jCount)
				)
				.addComponent(jItemsScroll)
		);
		update();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getToolTip() {
		return toolTip;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public JPanel getPanel() {
		return jPanel;
	}

	@Override
	public Set<SettingsConstants> getSettings() {
		return EnumSet.noneOf(SettingsConstants.class);
	}

	private JRadioButton createRadioButton(String title) {
		JRadioButton jRadioButton = new JRadioButton(title);
		jRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		return jRadioButton;
	}

	public final void update() {
		jItems.removeAll();
		int total = 0;
		int showing = 0;
		for (final RewardID reward : rewards) {
			boolean got = program.getFilters().contains(reward.getName());
			total++;
			if (got && jMissing.isSelected()) {
				continue;
			}
			if (!got && jGot.isSelected()) {
				continue;
			}
			showing++;
			final JLabel jLabel = new JLabel(reward.getName());
			jLabel.setBorder(
					BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(0, 0, BORDER_WIDTH, BORDER_WIDTH, BORDER_COLOR),
						BorderFactory.createEmptyBorder(10, 0, 10, 0)
						));
			jLabel.setIconTextGap(10);
			jLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
			jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel.setVerticalAlignment(SwingConstants.CENTER);
			jLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel.setFont(font);
			jLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					showPopupMenu(jLabel, e, reward);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					showPopupMenu(jLabel, e, reward);
				}
				@Override
				public void mousePressed(MouseEvent e) {
					showPopupMenu(jLabel, e, reward);
				}
				
			});
			
			if (got) {
				jLabel.setForeground(Color.GREEN);
				jLabel.setIcon(included(ImageGetter.getBufferedImage(reward.getName()), Images.IGNORED));
			} else {
				jLabel.setForeground(Color.ORANGE);
				jLabel.setIcon(included(ImageGetter.getBufferedImage(reward.getName()), Images.PROGRAM_16));
			}
			jItems.add(jLabel);
		}
		jCount.setText("Showing " + showing + " of " + total);
		jItems.updateUI();
	}

	private void showPopupMenu(JLabel jLabel, MouseEvent e, final RewardID reward) {
		if (!e.isPopupTrigger()) {
			return;
		}
		JPopupMenu jPopupMenu = new JPopupMenu();

		//Link
		JMenuItem jLink = new JMenuItem("Show on Wikia", Images.LINK.getIcon());
		jLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DesktopUtil.browseReward(program, reward, false);
			}
		});
		jPopupMenu.add(jLink);

		//Alert or Ignored
		JCheckBoxMenuItem jNotify = new JCheckBoxMenuItem("Notify");
		jNotify.setSelected(!program.getFilters().contains(reward.getName()));
		jNotify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.getFiltersTool().toggle(reward.getName());
			}
		});
		jPopupMenu.add(jNotify);

		jPopupMenu.show(jLabel, e.getX(), e.getY());
	}

	private Icon included(BufferedImage image, Images imageoverlay) {
		if (image == null) {
			return null;
		}
		// load source images
		image = scale(image);

		BufferedImage overlay = imageoverlay.getBufferedImage();

		// create the new image, canvas size is the max. of both image sizes
		int w = Math.max(image.getWidth(), overlay.getWidth());
		int h = Math.max(image.getHeight(), overlay.getHeight());
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		// paint both images, preserving the alpha channels
		Graphics g = combined.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.drawImage(overlay, image.getWidth() - 30, image.getHeight() - 30, null);
		
		return new ImageIcon(combined);
	}

	private BufferedImage scale(BufferedImage image) {
		if (image == null) { //Null
			return null;
		}
		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();

		//No resize needed
		if (imageWidth == width && height == imageHeight) {
			return image;
		}

		double scaleX = (double)width/imageWidth;
		double scaleY = (double)height/imageHeight;
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		return bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
	}
	
}
