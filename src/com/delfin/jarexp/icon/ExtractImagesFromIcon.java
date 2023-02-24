package com.delfin.jarexp.icon;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.delfin.jarexp.utils.FileUtils;

class ExtractImagesFromIcon {

	public static void main(String[] args) throws IOException {

		File outDir = new File("imagesFromIcon");
		if (outDir.exists()) {
			FileUtils.delete(outDir);
		}
		outDir.mkdirs();
		InputStream stream = new FileInputStream("icon.ico");
		int count = 0;
		for (BufferedImage icon : Ico.read(stream)) {
			String fileImageName = "out"+ count +".png";
			System.out.println("$$$ file image " + fileImageName);
			ImageIO.write(icon, "PNG", new File(outDir, fileImageName));
			count++;
		}
		stream.close();

	}

}
