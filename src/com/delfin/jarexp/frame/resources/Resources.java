package com.delfin.jarexp.frame.resources;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.Zip;


public class Resources {

	private static final Logger log = Logger.getLogger(Resources.class.getCanonicalName());

	private static final String LOGO_ICON = "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";

	public static class ResourcesException extends RuntimeException {

		private static final long serialVersionUID = -2350416070213106686L;

		ResourcesException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	private static Resources instance;

	private Image logoImage;

	private static Map<String, String> licenses;

	private static Map<String, Icon> defaultIcons = new HashMap<String, Icon>();

	private static Map<String, Icon> icons = new HashMap<String, Icon>();
	private static Map<String, Icon> peIcons = new HashMap<String, Icon>();

	private static Map<String, Image> images = new HashMap<String, Image>();

	private static File TMP_DIR = Settings.getJarexpTmpDir();

	private Resources() {

	}

	public static Resources getInstance() {
		if (instance == null) {
			instance = new Resources();
		}
		return instance;
	}

	public Image getLogoImage() {
		if (logoImage != null) {
			return logoImage;
		}
		try {
			return logoImage = loadImage("logo.png");
		} catch (IOException e) {
			byte[] base64 = Version.JAVA_MAJOR_VER >= 8 
					? java.util.Base64.getDecoder().decode(LOGO_ICON) 
					: javax.xml.bind.DatatypeConverter.parseBase64Binary(LOGO_ICON);
			return Toolkit.getDefaultToolkit().createImage(base64);
		}
	}

	public Icon getOpenIcon() throws ResourcesException {
		return getIcon("open.png", "Unable to load open menu icon from class path");
	}

	public Icon getExitIcon() throws ResourcesException {
		return getIcon("exit.png", "Unable to load exit menu icon from class path");
	}

	public Icon getDelIcon() throws ResourcesException {
		return getIcon("del.png", "Unable to load delete menu icon from class path");
	}

	public Icon getUnpackIcon() {
		return getIcon("unpack.png", "Unable to load unpack menu icon from class path");
	}

	public Icon getCopyIcon() {
		return getIcon("copy.png", "Unable to load copy menu icon from class path");
	}

	public Icon getAddIcon() {
		return getIcon("add.png", "Unable to load add menu icon from class path");
	}

	public Icon getExtIcon() {
		return getIcon("ext.png", "Unable to load extract menu icon from class path");
	}

	public Image getDragImage() throws ResourcesException {
		return getImage("drag.png", "Unable to load drag and drop image from class path");
	}

	public String getLicence() {
		return loadLicense("LICENSE");
	}

	public String getRstaLicense() {
		return loadLicense("rsta.license");
	}

	public String getProcyonLicense() {
		return loadLicense("procyon.license");
	}

	public String getFernflowerLicense() {
		return loadLicense("fernflower.license");
	}

	public String getJavaDecompilerLicense() {
		return loadLicense("java-decompiler.license");
	}

	private String loadLicense(String fileName) {
		if (licenses == null) {			
			loadLicenses();
		}
		return licenses.get(fileName);
	}

	private synchronized void loadLicenses() {
		if (licenses == null) {
			InputStream is = null;
			try {
				is = Resources.class.getClassLoader().getResourceAsStream("licenses.zip");
				licenses = Zip.unzip(is);
			} catch (Exception e) {
				throw new JarexpException("Unable to load licenses.", e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						log.log(Level.WARNING, "Unable to close stream to licenses.zip", e);
					}
				}
			}
		}
	}

	public static File createTmpDir() {
		File dir = new File(TMP_DIR, Long.toString(System.currentTimeMillis()));
		dir.mkdirs();
		// dir.deleteOnExit();
		return dir;
	}

	public static Icon getIconFor(String name) {
		String ext = getExtension(name);
		Icon res = defaultIcons.get(ext);
		if (res != null) {
			return res;
		}
		File file = new File(TMP_DIR, "jarexp" + (ext.isEmpty() ? "" : '.' + ext));
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new JarexpException("Couldn't create new file " + file, e);
		}
		file.deleteOnExit();

		Icon icon = CropIconsBugResolver.getInstance().getIcon(file);
		if (icon == null) {
			icon = FileSystemView.getFileSystemView().getSystemIcon(file);
		}
 		if ("class".equals(ext)) {
 			icon = ClassFileIcon.defineIcon(icon);
		}
		defaultIcons.put(ext, icon);
		return icon;
	}

	public static Icon getIconForDir() {
		Icon res = defaultIcons.get(null);
		if (res != null) {
			return res;
		}

		Icon icon = CropIconsBugResolver.getInstance().getIcon(TMP_DIR);
		if (icon == null) {
			icon = FileSystemView.getFileSystemView().getSystemIcon(TMP_DIR);
		}
		
		defaultIcons.put(null, icon);
		return icon;
	}

	public Image getInfoImage() {
		return getImage("info.png", "Unable to load info image from class path");
	}

	public Icon getInfoIcon() throws ResourcesException {
		return getIcon("info.png", "Unable to load info menu icon from class path");
	}

	public Image getSearchImage() {
		return getImage("srch.png", "Unable to load search image from class path");
	}

	public Image getDuplicatesImage() {
		return getImage("dupl.png", "Unable to load duplicates image from class path");
	}

	public Image getEnvironmentImage() {
		return getImage(System.getProperty("java.runtime.name").toLowerCase().contains("openjdk")
				? "envoj.png"
				: "envor.png"
				, "Unable to load environment image from class path");
	}

	public Icon getJavaIcon() {
		return getIcon("envoj.png", "Unable to load java icon from class path");
	}

	public Icon getSearchIcon() {
		return getIcon("srch.png", "Unable to load search menu icon from class path");
	}
	
	public Icon getDuplicatesIcon() {
		return getIcon("dupl.png", "Unable to load duplicates menu icon from class path");
	}

	public Icon getEnvironmentIcon() {
		return getIcon(System.getProperty("java.vm.name").toLowerCase().contains("openjdk")
				? "envoj.png"
				: "envor.png"
				, "Unable to load environment menu icon from class path");
	}

	public Icon getJdCoreIcon() {
		return getIcon("jdco.png", "Unable to load jd core menu icon from class path");
	}

	public Icon getProcyonIcon() {
		return getIcon("proc.png", "Unable to load procyon menu icon from class path");
	}

	public Icon getFernflowerIcon() {
		return getIcon("fern.png", "Unable to load fernflower menu icon from class path");
	}

	public Icon getUpdateIcon() {
		return getIcon("upd.png", "Unable to load update menu icon from class path");
	}

	public Icon getDonateIcon() {
		return getIcon("dlar.png", "Unable to load donate menu icon from class path");
	}

	public Icon getFloppyIcon() {
		return getIcon("flpy.png", "Unable to load floppy icon from class path");
	}

	public Icon getProcessesIcon() {
		return getIcon("prss.png", "Unable to load process icon from class path");
	}

	public Icon getSumsIcon() {
		return getIcon("sums.png", "Unable to load hash sums icon from class path");
	}

	public Icon getJavaClassIcon() {
		return getIcon("r_class.png", "Unable to load java item class icon from class path");
	}

    public Icon getJavaRecordIcon() {
        return getIcon("r_rec.png", "Unable to load java item record icon from class path");
    }

	public Icon getJavaInterfaceIcon() {
		return getIcon("r_int.png", "Unable to load java item interface icon from class path");
	}

	public Icon getJavaPrivateMethodIcon() {
		return getIcon("r_pt_m.png", "Unable to load java item private method icon from class path");
	}

	public Icon getJavaProtectedMethodIcon() {
		return getIcon("r_pd_m.png", "Unable to load java item protected method icon from class path");
	}

	public Icon getJavaPublicMethodIcon() {
		return getIcon("r_pb_m.png", "Unable to load java item public method icon from class path");
	}

	public Icon getJavaDefaultMethodIcon() {
		return getIcon("r_df_m.png", "Unable to load java item default method icon from class path");
	}

	public Icon getJavaEnumIcon() {
		return getIcon("r_enum.png", "Unable to load java item enum icon from class path");
	}

	public Icon getDecompileAllIcon() {
		return getIcon("dec.png", "Unable to load decompiler icon from class path");
	}

	public Icon getMtoolAddRepoIcon() {
		return getIcon("mtad.png", "Unable to load maven repo add icon from class path");
	}

	public Icon getMtoolRepoIcon() {
		return getIcon("mtrp.png", "Unable to load maven repo icon from class path");
	}

	public Icon getTreeIcon() {
		return getIcon("mttr.png", "Unable to load maven tree icon from class path");
	}

	public Icon getMtoolLogoIcon() {
		return getIcon("mtmvn.png", "Unable to load maven logo icon from class path");
	}

	public Image getMtoolLogoImage() {
		return getImage("mtmvn.png", "Unable to load maven logo image from class path");
	}

	public static Image getMtoolConsoleImage() {
		return getImage("mtcn.png", "Unable to load maven console image from class path");
	}

	public Image getSumsImage() {
		return getImage("sums.png", "Unable to load hash sums image from class path");
	}

	public Image getProcessesImage() {
		return getImage("prss.png", "Unable to load process image from class path");
	}

	public Image getFernflowerImage() {
		return getImage("fern.png", "Unable to load fernflower image from class path");
	}

	public Image getJdCoreImage() {
		return getImage("jdco.png", "Unable to load jd core image from class path");
	}

	public Image getProcyonImage() {
		return getImage("proc.png", "Unable to load procyon image from class path");
	}

	public Icon getPeIcon(String fullPath) {
		return peIcons.get(fullPath);
	}

	public boolean isPeIconChecked(String fullPath) {
		return peIcons.keySet().contains(fullPath);
	}

	public Icon storePeIcon(String fullPath, Icon icon) {
		return peIcons.put(fullPath, icon);
	}

	public void clearPeIcons() {
		peIcons.clear();
	}

	private static Image getImage(String fileName, String errorMsg) {
		Image res = images.get(fileName);
		if (res == null) {
			try {
				res = loadImage(fileName);
				images.put(fileName, res);
			} catch (IOException e) {
				throw new ResourcesException(errorMsg, e);
			}
		}
		return res;
	}

	static Icon getIcon(String fileName, String errorMsg) {
		Icon res = icons.get(fileName);
		if (res == null) {
			try {
				res = new ImageIcon(getImage(fileName, "Unable to load image from " + fileName));
				icons.put(fileName, res);
			} catch (Exception e) {
				throw new ResourcesException(errorMsg, e);
			}
		}
		return res;
	}

	private static String getExtension(String name) {
		int i = name.lastIndexOf('.');
		if (i == -1) {
			return "";
		}
		return name.substring(i + 1).toLowerCase();
	}

	private static Image loadImage(String fileName) throws IOException {
		InputStream is = null;
		try {
			return ImageIO.read(is = getResource("img/" + fileName));
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Couldn't close stream for file " + fileName, e);
				}
			}
		}
	}

	private static InputStream getResource(String path) {
		return getLoader().getResourceAsStream(path);
	}

	private static ClassLoader getLoader() {
		return Resources.class.getClassLoader();
	}

	public static void main(String[] args) throws Exception {
		 InputStream is =
		 Resources.class.getClassLoader().getResourceAsStream("licenses.zip");
		
		System.out.println(Zip.unzip(is));
		is.close();
		
		
		// InputStream is =
		// Resources.class.getClassLoader().getResourceAsStream("img/icon.png");
		//
		// ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		//
		// int nRead;
		// byte[] data = new byte[8000];
		//
		// while ((nRead = is.read(data, 0, data.length)) != -1) {
		// buffer.write(data, 0, nRead);
		// }
		//
		// buffer.flush();
		//
		// String s = DatatypeConverter.printBase64Binary(buffer.toByteArray());
		//
		// System.out.println(s);
		//
		// // String s = DatatypeConverter.parseBase64Binary(new
		// // String(buffer.toByteArray()));

		//System.out.println(getExtension("dfafas.exe"));

	}

}
