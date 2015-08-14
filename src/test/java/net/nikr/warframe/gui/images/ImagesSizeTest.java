/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nikr.warframe.gui.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import net.nikr.warframe.gui.reward.Category;
import net.nikr.warframe.gui.reward.RewardID;
import net.nikr.warframe.io.shared.FileConstants;
import net.nikr.warframe.io.shared.ListReader;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Niklas
 */
public class ImagesSizeTest {
	@Test
	public void testExists() {
		//Current
		test(FileConstants.getCategoriesLocal(), FileConstants.getCategoryLocal("").getAbsolutePath(), FileConstants.getImageLocal("").getAbsolutePath());
		//Local
		String local = System.getProperty("user.dir") + File.separator + "data" + File.separator + "data" + File.separator;
		test(new File(local + "data_categories.dat"),local + "data_", local + "images");
	}

	private void test(File categoryFile, String dataPath, String imagePath) {
		List<Category> categories = loadCategories(categoryFile, dataPath);
		int count = 0;
		List<File> files = new ArrayList<File>();
		for (Category category : categories) {
			for (RewardID rewardID : category.getRewards()) {
				File file = getFile(rewardID.getName(), imagePath);
				testFile(files, rewardID.getName(), file, category.getWidth(), category.getHeight());
				count++;
			}
			File file = getFile(category.getName(), imagePath);
			testFile(files, category.getName(), file, 16, 16);
			count++;
		}
		File file = getFile("credits", imagePath);
		testFile(files, "credits", file, 250, 156);
		testForUnusedFile(files, imagePath);
		System.out.println("Tested: " + count + " images");
	}

	private void testFile(List<File> files, String name, File file, int width, int height) {
		files.add(file);
		BufferedImage image = getImage(file);
		assertNotNull(name + " missing", image);
		assertEquals(name + " wrong width", width, image.getWidth());
		assertEquals(name + " wrong height", height, image.getHeight());
	}

	private void testForUnusedFile(List<File> tested, String imagePath) {
		File imageDir = new File(imagePath);
		List<File> inside = new ArrayList<File>(Arrays.asList(imageDir.listFiles()));
		assertNotNull("image directory empty ", inside);
		for (File file : inside) {
			assertTrue(file.getName() + " is never used", tested.contains(file));
		}
	}

	private File getFile(String reward, String imagePatch) {
		String filename = reward.toLowerCase().replace(" ", "_") + ".png";
		return new File(imagePatch + File.separator + filename);
	}

	private BufferedImage getImage(File file) {
		try {
			return ImageIO.read(file); //Load
		} catch (IOException ex) {
			return null;
		}
	}

	private List<Category> loadCategories(File categoryFile, String dataPath) {
		List<Category> categoryList = new ArrayList<Category>();
		ListReader reader = new ListReader();
		List<String> load = reader.load(categoryFile);
		assertFalse(load.isEmpty());
		for (String s : load) {
			Category category = new Category(s);
			Set<RewardID> categoryRewards = loadRewards(category, dataPath);
			category.addAll(categoryRewards);
			categoryList.add(category);
		}
		return categoryList;
	}

	private Set<RewardID> loadRewards(Category category, String dataPath) {
		File file = new File(dataPath + category.getFilename());
		ListReader reader = new ListReader();
		Set<RewardID> categoryRewards = new TreeSet<RewardID>();
		List<String> load = reader.load(file);
		assertFalse(load.isEmpty());
		for (String s : load) {
			categoryRewards.add(new RewardID(s));
		}
		return categoryRewards;
	}

	
}
