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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import net.nikr.warframe.Program;
import net.nikr.warframe.gui.images.Images;
import net.nikr.warframe.gui.settings.SettingsConstants;
import net.nikr.warframe.gui.shared.DesktopUtil;
import net.nikr.warframe.gui.shared.Tool;
import net.nikr.warframe.io.shared.FastToolTips;
import net.nikr.warframe.io.shared.ImageGetter;


public class RewardTool implements Tool {

	private static final int BORDER_WIDTH = 4;
	private static final Color BORDER_COLOR = new Color(20, 20, 20);
	private static final Color BACKGROUND_COLOR = Color.BLACK;

	private final JPanel jPanel;
	private final JDynamicGrid jItems;
	private final JRadioButton jAll;
	private final JRadioButton jNotify;
	private final JRadioButton jIgnore;
	private final JLabel jCount;
	private final JComboBox jImageSize;

	private final Program program;

	private final Set<RewardID> rewards;
	private final String title;
	private final String toolTip;
	private final Icon icon;

	private final int width;
	private final int height;

	private final Map<Integer, Map<String, Map<Boolean, Icon>>> images = new HashMap<Integer, Map<String, Map<Boolean, Icon>>>();
	private final List<JLabel> labels = new ArrayList<JLabel>();
	private final List<Percent> percents = new ArrayList<Percent>();

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

		jCount = new JLabel();

		jAll = createRadioButton("All");
		jNotify = createRadioButton("Notify");
		jIgnore = createRadioButton("Ignore");

		JLabel jHelp = new JLabel(Images.HELP.getIcon());
		FastToolTips.install(jHelp);
		jHelp.setToolTipText("<html><body><b>Edit:</b> Right click<br>"
				+ "<b>Zoom:</b> Ctrl + Mouse wheel");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jAll);
		buttonGroup.add(jNotify);
		buttonGroup.add(jIgnore);

		jAll.setSelected(true);

		//percents.add(new Percent("150%", 150, new Font(jPanel.getFont().getName(), Font.BOLD, (jPanel.getFont().getSize() + 7))));
		//percents.add(new Percent("125%", 125, new Font(jPanel.getFont().getName(), Font.BOLD, (jPanel.getFont().getSize() + 6))));
		Percent percent100 = new Percent("100%", 100, new Font(jPanel.getFont().getName(), Font.BOLD, (jPanel.getFont().getSize() + 5)));
		percents.add(percent100);
		percents.add(new Percent("75%", 75, new Font(jPanel.getFont().getName(), Font.BOLD, (jPanel.getFont().getSize() + 4))));
		percents.add(new Percent("50%", 50, new Font(jPanel.getFont().getName(), Font.BOLD, jPanel.getFont().getSize() + 3)));
		percents.add(new Percent("25%", 25, new Font(jPanel.getFont().getName(), Font.BOLD, jPanel.getFont().getSize() + 2)));
		percents.add(new Percent("0%", 0, new Font(jPanel.getFont().getName(), Font.BOLD, jPanel.getFont().getSize() + 1)));

		jImageSize = new JComboBox(percents.toArray());
		jImageSize.setSelectedItem(percent100);
		jImageSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});

		jItems = new JDynamicGrid();
		jItems.setBackground(BACKGROUND_COLOR);
		jItems.setBorder(BorderFactory.createMatteBorder(BORDER_WIDTH, BORDER_WIDTH, 0, 0, BORDER_COLOR));
		jItems.getVerticalScrollBar().setUnitIncrement(16);
		jItems.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					int value = jImageSize.getSelectedIndex();
					value = value + (e.getUnitsToScroll() / e.getScrollAmount());
					if (value > (percents.size() - 1) ) {
						value = (percents.size() - 1);
					}
					if (value < 0) {
						value = 0;
					}
					jImageSize.setSelectedIndex(value);
				} else {
					int value = jItems.getVerticalScrollBar().getValue();
					value = value + (e.getUnitsToScroll() * jItems.getVerticalScrollBar().getUnitIncrement());
					jItems.getVerticalScrollBar().setValue(value);
				}
			}
		});

		for (final RewardID reward : rewards) {
			boolean ignore = program.getFilters().contains(reward.getName());
			if (ignore && jNotify.isSelected()) {
				continue;
			}
			if (!ignore && jIgnore.isSelected()) {
				continue;
			}
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
			jItems.add(jLabel);
			labels.add(jLabel);
		}

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(jAll)
					.addComponent(jNotify)
					.addComponent(jIgnore)
					.addComponent(jImageSize, 75, 75, 75)
					.addGap(0, 0, Integer.MAX_VALUE)
					.addGap(0, 0, Integer.MAX_VALUE)
					.addComponent(jCount)
					.addGap(10)
					.addComponent(jHelp)
				)
				.addComponent(jItems.getComponent())
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(jAll)
					.addComponent(jNotify)
					.addComponent(jIgnore)
					.addComponent(jImageSize, 25, 25, 25)
					.addComponent(jCount)
					.addComponent(jHelp)
				)
				.addComponent(jItems.getComponent())
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
		Percent percent = (Percent) jImageSize.getSelectedItem();
		for (final JLabel jLabel : labels) {
			boolean ignore = program.getFilters().contains(jLabel.getText());
			total++;
			if (ignore && jNotify.isSelected()) {
				continue;
			}
			if (!ignore && jIgnore.isSelected()) {
				continue;
			}
			showing++;
			jLabel.setFont(percent.getFont());
			if (ignore) {
				jLabel.setForeground(Color.GRAY);
			} else {
				jLabel.setForeground(Color.ORANGE);
			}
			jLabel.setIcon(getImage(jLabel.getText(), ignore));
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
		JCheckBoxMenuItem jNotifyMenu = new JCheckBoxMenuItem("Notify");
		jNotifyMenu.setSelected(!program.getFilters().contains(reward.getName()));
		jNotifyMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				program.getFiltersTool().toggle(reward.getName());
			}
		});
		jPopupMenu.add(jNotifyMenu);

		jPopupMenu.show(jLabel, e.getX(), e.getY());
	}

	

	private Icon getImage(String rewardName, boolean ignore) {
		Percent percent = (Percent) jImageSize.getSelectedItem();
		if (percent.getPercent() == 0) {
			if (ignore) {
				return Images.PROGRAM_DISABLED_16.getIcon();
			} else {
				return Images.PROGRAM_16.getIcon();
			}
		} else {
			BufferedImage image = scale(ImageGetter.getBufferedImage(rewardName), percent.getPercent());
			if (ignore) {
				return included(image, Images.PROGRAM_DISABLED_16);
			} else {
				return included(image, Images.PROGRAM_16);
			}
		}
	}

	private Icon included(BufferedImage image, Images imageoverlay) {
		if (image == null) {
			return imageoverlay.getIcon();
		}
		BufferedImage overlay = imageoverlay.getBufferedImage();

		// create the new image, canvas size is the max. of both image sizes
		int w = Math.max(image.getWidth(), overlay.getWidth());
		int h = Math.max(image.getHeight(), overlay.getHeight());
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		// paint both images, preserving the alpha channels
		Graphics g = combined.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.drawImage(overlay, image.getWidth() - ((image.getWidth() * 5 / 100) + 16), image.getHeight() - ((image.getHeight() * 5 / 100) + 16), null);
		
		return new ImageIcon(combined);
	}

	private BufferedImage scale(BufferedImage image, int percent) {
		if (image == null) { //Null
			return null;
		}
		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();

		int fixedWidth = width * percent / 100;
		int fixedHeight = height * percent / 100;
		//No resize needed
		if (imageWidth == fixedWidth && fixedHeight == imageHeight) {
			return image;
		}

		
		double scaleX = (double)fixedWidth/imageWidth;
		double scaleY = (double)fixedHeight/imageHeight;
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		return bilinearScaleOp.filter(image, new BufferedImage(fixedWidth, fixedHeight, image.getType()));
	}

	private static class Percent {
		private final String text;
		private final int percent;
		private final Font font;

		public Percent(String text, int percent, Font font) {
			this.text = text;
			this.percent = percent;
			this.font = font;
		}

		public int getPercent() {
			return percent;
		}

		public Font getFont() {
			return font;
		}

		@Override
		public String toString() {
			return text;
		}

		
	}
	
}
