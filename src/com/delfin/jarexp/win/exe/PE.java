package com.delfin.jarexp.win.exe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class PE {
	
	static final String EOL = System.getProperty("line.separator", "\n");
	// StandardCharsets.US_ASCII
	static final Charset US_ASCII = Charset.forName("US-ASCII");

	private static final int PE_OFFSET_LOCATION = 0x3c;
	private static final byte[] PE_SIG = "PE\0\0".getBytes();

	private UByteArray fileBytes;
	private HCoffFile coffHeader;
	private HOptional optionalHeader;
	private HSectionTable sectionTable;
	private boolean invalidFile;

	private PE(File file) throws FileNotFoundException, IOException {
		fromInputStream(new FileInputStream(file));
	}

	private PE(InputStream inputStream) throws FileNotFoundException, IOException {
		fromInputStream(inputStream);
	}

	public static String getInfo(File peFile) throws FileNotFoundException, IOException {
		PE pe = new PE(peFile);
		if (pe.isPE()) {
			StringBuilder b = new StringBuilder();
			b.append("PE signature offset: ")
				.append(pe.getPEOffset())
				.append(PE.EOL)
				.append("PE signature correct: yes")
				.append(PE.EOL)
				.append(PE.EOL)
				.append("================")
				.append(PE.EOL)
				.append("COFF header info")
				.append(PE.EOL)
				.append("================")
				.append(PE.EOL);
			for (TByteDef<?> bd : pe.coffHeader.headers) {
				bd.format(b);
			}
			b.append(PE.EOL)
				.append("================")
				.append(PE.EOL)
				.append("Optional header info")
				.append(PE.EOL)
				.append("================")
				.append(PE.EOL);
			for (TByteDef<?> bd : pe.optionalHeader.headers) {
				bd.format(b);
			}
			b.append(PE.EOL)
				.append(PE.EOL)
				.append("================")
				.append(PE.EOL)
				.append("Section Table")
				.append(PE.EOL)
				.append("================")
				.append(PE.EOL)
				.append(PE.EOL);
			for (HSectionTableEntry section : pe.sectionTable.sections) {
				for (TByteDef<?> bd : section.headers) {
					bd.format(b);
				}
			}
			b.append(PE.EOL);
			return b.toString();
		} else {
			return "PE signature not found. The given file is not a PE file." + PE.EOL;
		}
	}

	private int getPEOffset() {
		fileBytes.mark();
		fileBytes.seek(PE_OFFSET_LOCATION);
		int read = fileBytes.readUShort(2).intValue();
		fileBytes.reset();
		return read;
	}

	private boolean isPE() {
		if (invalidFile) {
			return false;
		}
		int saved = -1;
		try {
			int offset = getPEOffset();
			saved = fileBytes.position();
			fileBytes.seek(0);
			for (int i = 0; i < PE_SIG.length; i++) {
				if (fileBytes.readRaw(offset + i) != PE_SIG[i]) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (saved != -1) {
				fileBytes.seek(saved);
			}
		}
	}

	public static String getVersion(File peFile) throws FileNotFoundException, IOException {
		PE pe = new PE(peFile);
		if (pe.invalidFile) {
			throw new IOException("No version found:" + peFile);
		}
		for (TImgDataDir mainEntry : pe.optionalHeader.tables) {
			if (mainEntry.getType() == EDirEntry.RESOURCE) {
				RDirHeader root = (RDirHeader) mainEntry.data;
				for (RDirEntry rootEntry : root.entries) {
					if ("Version".equals(rootEntry.name.get())) {
						byte[] versionInfoData = rootEntry.directory.entries[0].directory.entries[0].resourceDataEntry.getData(pe.fileBytes);
						int fileVersionIndex = indexOf(versionInfoData, includeNulls("FileVersion")) + 26;
						int fileVersionEndIndex = indexOf(versionInfoData, new byte[] { 0x00, 0x00 }, fileVersionIndex);
						return removeNulls(new String(versionInfoData, fileVersionIndex, fileVersionEndIndex - fileVersionIndex));
					}
				}
			}
		}
		throw new IOException("No version found:" + peFile);
	}

	public static List<String> getIconNames(File peFile) throws IOException {
		PE pe = new PE(peFile);
		if (pe.invalidFile) {
			throw new IOException("Is not a valid PE:" + peFile);
		}

		List<String> icons = new ArrayList<String>();
		fillIconsNames(pe, icons);
		return icons;
	}

	public static void dumpIconsFrom(File peFile, File destDir) throws FileNotFoundException, IOException {
		PE pe = new PE(peFile);
		if (pe.invalidFile) {
			throw new IOException("Is not valid PE:" + peFile);
		}

		List<Icon> icons = new ArrayList<Icon>();
		fillIconsHeaders(pe, icons);
		fillIconsImages(pe, icons);

		for (Icon icon : icons) {
			icon.dumpTo(destDir, peFile);
		}
	}

	private static void fillIconsImages(PE pe, List<Icon> icons) {
		for (TImgDataDir mainEntry : pe.optionalHeader.tables) {
			if (mainEntry.getType() == EDirEntry.RESOURCE) {
				RDirHeader root = (RDirHeader) mainEntry.data;
				if (root == null && mainEntry.getSize().get().intValue() == 0) {
					continue;
				}
				for (RDirEntry rootEntry : root.entries) {
					if ("Icon".equals(rootEntry.name.get())) {
						for (RDirEntry iconEntry : rootEntry.directory.entries) {
							RDataEntry data = iconEntry.directory.entries[0].resourceDataEntry;
							for (Icon icon : icons) {
								if (icon.isContainsImage(iconEntry.name.get())) {
									icon.addImage(iconEntry.name.get(), data.getData(pe.fileBytes));
								}
							}
						}
					}
				}
			}
		}
	}

	private static void fillIconsHeaders(PE pe, List<Icon> icons) throws IOException {
		for (TImgDataDir mainEntry : pe.optionalHeader.tables) {
			if (mainEntry.getType() == EDirEntry.RESOURCE) {
				RDirHeader root = (RDirHeader) mainEntry.data;
				if (root == null && mainEntry.getSize().get().intValue() == 0) {
					continue;
				}
				for (RDirEntry rootEntry : root.entries) {
					if ("Group Icon".equals(rootEntry.name.get())) {
						for (RDirEntry iconGroupEntry : rootEntry.directory.entries) {
							for (RDirEntry iconEntry : iconGroupEntry.directory.entries) {
								byte[] bytes = iconEntry.resourceDataEntry.getData(pe.fileBytes);
								if (bytes.length < 2 || bytes[0] != 0x00 && (
										bytes[1] != 0x01 || bytes[1] != 0x02)) {
									throw new IOException("Wrong icon format found.");
								}
								icons.add(new Icon(iconGroupEntry.name.get(), bytes));
							}
						}
					}
				}
			}
		}
	}

	private static byte[] includeNulls(String str) {
		char[] chars = str.toCharArray();
		byte[] result = new byte[chars.length * 2];
		for (int i = 0, j = 0; i < result.length; i += 2, j++) {
			result[i] = (byte) chars[j];
		}
		return result;
	}

	private static String removeNulls(String str) {
		return str == null ? null : str.replaceAll("\\x00", "");
	}

	private static int indexOf(byte[] outerArray, byte[] smallerArray) {
		return indexOf(outerArray, smallerArray, 0);
	}

	private static int indexOf(byte[] outerArray, byte[] smallerArray, int begin) {
		for (int i = begin; i < outerArray.length - smallerArray.length + 1; ++i) {
			boolean found = true;
			for (int j = 0; j < smallerArray.length; ++j) {
				if (outerArray[i + j] != smallerArray[j]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i;
			}
		}
		return -1;
	}

	private void fromInputStream(InputStream inputStream) throws FileNotFoundException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
		byte[] buffer = new byte[4096];
		int read = 0;
		while ((read = inputStream.read(buffer)) > 0) {
			baos.write(buffer, 0, read);
		}
		baos.flush();
		inputStream.close();
		byte[] bytes = baos.toByteArray();
		invalidFile = bytes.length == 0;
		fileBytes = new UByteArray(bytes);
		if (isPE()) {
			fileBytes.seek(getPEOffset() + PE_SIG.length);
			coffHeader = new HCoffFile(fileBytes);
			optionalHeader = new HOptional(fileBytes);
			int numberOfEntries = coffHeader.numOfSections.get().intValue();
			sectionTable = new HSectionTable(fileBytes, numberOfEntries);
			for (HSectionTableEntry section : sectionTable.sections) {
				long sectionAddress = section.virtualAddress.get().longValue();
				long sectionSize = section.sizeOfRawData.get().longValue();
				for (TImgDataDir entry : optionalHeader.tables) {
					long optionAddress = entry.get().longValue();
					if (sectionAddress <= optionAddress && sectionAddress + sectionSize > optionAddress) {
						entry.setSection(section);
					}
				}
			}
			for (TImgDataDir entry : optionalHeader.tables) {
				if (entry.getType() == EDirEntry.RESOURCE) {
					HSectionTableEntry section = entry.getSection();
					if (section != null) {
						long delta = section.virtualAddress.get().longValue()
								- section.pointerToRawData.get().longValue();
						long offsetInFile = entry.get().longValue() - delta;
						if (offsetInFile > Integer.MAX_VALUE) {
							throw new RuntimeException("Unable to set offset to more than 2Gb.");
						}
						fileBytes.seek((int) offsetInFile);
						fileBytes.mark();
						entry.data = new RDirHeader(fileBytes, section, 0);
					}
				}
			}
		}
	}

	private static void fillIconsNames(PE pe, List<String> icons) throws IOException {
		for (TImgDataDir mainEntry : pe.optionalHeader.tables) {
			if (mainEntry.getType() == EDirEntry.RESOURCE) {
				RDirHeader root = (RDirHeader) mainEntry.data;
				if (root == null && mainEntry.getSize().get().intValue() == 0) {
					continue;
				}
				for (RDirEntry rootEntry : root.entries) {
					if ("Group Icon".equals(rootEntry.name.get())) {
						for (RDirEntry iconGroupEntry : rootEntry.directory.entries) {
							for (RDirEntry iconEntry : iconGroupEntry.directory.entries) {
								byte[] bytes = iconEntry.resourceDataEntry.getData(pe.fileBytes);
								if (bytes.length < 2 || bytes[0] != 0x00 
										&& (bytes[1] != 0x01 || bytes[1] != 0x02)) {
									throw new IOException("Wrong icon format found.");
								}
								icons.add(iconGroupEntry.name.get());
							}
						}
					}
				}
			}
		}
	}
}
