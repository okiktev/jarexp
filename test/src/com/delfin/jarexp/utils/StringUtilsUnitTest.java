package com.delfin.jarexp.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsUnitTest {

	@Test
	public void testIndexOf() throws Exception {
		assertEquals(1, StringUtils.indexOf("aadgerg", "aD", 0));

		assertEquals(1, StringUtils.indexOf("sad fgfsad fgsg adbsgf gfbd", "ad", 0));
		assertEquals(1, StringUtils.indexOf("sad fgfsad fgsg adbsgf gfbd", "Ad", 0));

		assertEquals(1, StringUtils.indexOf("sad fgfsgs fgsg sgbsgf gfbd", "ad", 0));
		assertEquals(1, StringUtils.indexOf("sad fgfsgs fgsg sgbsgf gfbd", "Ad", 0));
		assertEquals(1, StringUtils.indexOf("sAd fgfsgs fgsg sgbsgf gfbd", "ad", 0));
		assertEquals(1, StringUtils.indexOf("sA342-=+d fgfsgs fgsg sgbsgf gfbd", "a342-=+d", 0));
		assertEquals(1, StringUtils.indexOf("sA342-=+d fgfsgs fgsg sgbsgf gfbd", "A342-=+D", 0));

		assertEquals(0, StringUtils.indexOf("ad fgfsgs fgsg sgbsgf gfbd", "ad", 0));
		assertEquals(0, StringUtils.indexOf("ad fgfsgs fgsg sgbsgf gfbd", "Ad", 0));
		assertEquals(0, StringUtils.indexOf("Ad fgfsgs fgsg sgbsgf gfbd", "ad", 0));
		assertEquals(0, StringUtils.indexOf("A342-=+d fgfsgs fgsg sgbsgf gfbd", "a342-=+d", 0));
		assertEquals(0, StringUtils.indexOf("A342-=+d fgfsgs fgsg sgbsgf gfbd", "A342-=+D", 0));

		assertEquals(0, StringUtils.indexOf("ad", "ad", 0));
		assertEquals(0, StringUtils.indexOf("ad", "Ad", 0));
		assertEquals(0, StringUtils.indexOf("Ad", "ad", 0));
		assertEquals(0, StringUtils.indexOf("Ad", "AD", 0));
		assertEquals(0, StringUtils.indexOf("AD", "ad", 0));
		assertEquals(0, StringUtils.indexOf("A342-=+d", "a342-=+d", 0));
		assertEquals(0, StringUtils.indexOf("A342-=+d", "A342-=+D", 0));

		assertEquals(0, StringUtils.indexOf("adgerg", "ad", 0));
		assertEquals(0, StringUtils.indexOf("adgtsr", "Ad", 0));
		assertEquals(0, StringUtils.indexOf("Adhdyh", "ad", 0));
		assertEquals(0, StringUtils.indexOf("A342-=+dhffhy", "a342-=+d", 0));
		assertEquals(0, StringUtils.indexOf("A342-=+dhfyft", "A342-=+D", 0));

		assertEquals(2, StringUtils.indexOf("eradgerg", "ad", 0));
		assertEquals(2, StringUtils.indexOf("eradgtsr", "Ad", 0));
		assertEquals(2, StringUtils.indexOf("erAdhdyh", "ad", 0));
		assertEquals(2, StringUtils.indexOf("erA342-=+dhffhy", "a342-=+d", 0));
		assertEquals(2, StringUtils.indexOf("erA342-=+dhfyft", "A342-=+D", 0));

		assertEquals(3, StringUtils.indexOf("eradgerg", "DG", 1));
		assertEquals(3, StringUtils.indexOf("eradgtsr", "Dg", 2));
		assertEquals(3, StringUtils.indexOf("erAdhdyh", "dh", 3));
		assertEquals(9, StringUtils.indexOf("erA342-=+dhffhy", "Dh", 4));
		assertEquals(8, StringUtils.indexOf("erA342-=+dh", "+DH", 3));

	}

	@Test
	public void testLastIndexOf() throws Exception {
		assertEquals(1, StringUtils.lastIndexOf("aadgerg", "aD", 6));

		assertEquals(8, StringUtils.lastIndexOf("sad fgfsad fgsg sgbsgf gfbd", "ad", 27));
		assertEquals(8, StringUtils.lastIndexOf("sad fgfsad fgsg sgbsgf gfbd", "Ad", 26));

		assertEquals(1, StringUtils.lastIndexOf("sad fgfsgs fgsg sgbsgf gfbd", "ad", 25));
		assertEquals(1, StringUtils.lastIndexOf("sad fgfsgs fgsg sgbsgf gfbd", "Ad", 25));
		assertEquals(1, StringUtils.lastIndexOf("sAd fgfsgs fgsg sgbsgf gfbd", "ad", 25));
		assertEquals(1, StringUtils.lastIndexOf("sA342-=+d fgfsgs fgsg sgbsgf gfbd", "a342-=+d", 30));
		assertEquals(1, StringUtils.lastIndexOf("sA342-=+d fgfsgs fgsg sgbsgf gfbd", "A342-=+D", 30));

		assertEquals(0, StringUtils.lastIndexOf("ad fgfsgs fgsg sgbsgf gfbd", "ad", 24));
		assertEquals(0, StringUtils.lastIndexOf("ad fgfsgs fgsg sgbsgf gfbd", "Ad", 24));
		assertEquals(0, StringUtils.lastIndexOf("Ad fgfsgs fgsg sgbsgf gfbd", "ad", 24));
		assertEquals(0, StringUtils.lastIndexOf("A342-=+d fgfsgs fgsg sgbsgf gfbd", "a342-=+d", 29));
		assertEquals(0, StringUtils.lastIndexOf("A342-=+d fgfsgs fgsg sgbsgf gfbd", "A342-=+D", 29));

		assertEquals(0, StringUtils.lastIndexOf("ad", "ad", 1));
		assertEquals(0, StringUtils.lastIndexOf("ad", "Ad", 1));
		assertEquals(0, StringUtils.lastIndexOf("Ad", "ad", 1));
		assertEquals(0, StringUtils.lastIndexOf("Ad", "AD", 1));
		assertEquals(0, StringUtils.lastIndexOf("AD", "ad", 1));
		assertEquals(0, StringUtils.lastIndexOf("A342-=+d", "a342-=+d", 3));
		assertEquals(0, StringUtils.lastIndexOf("A342-=+d", "A342-=+D", 3));

		assertEquals(0, StringUtils.lastIndexOf("adgerg", "ad", 4));
		assertEquals(0, StringUtils.lastIndexOf("adgtsr", "Ad", 4));
		assertEquals(0, StringUtils.lastIndexOf("Adhdyh", "ad", 4));
		assertEquals(0, StringUtils.lastIndexOf("A342-=+dhffhy", "a342-=+d", 10));
		assertEquals(0, StringUtils.lastIndexOf("A342-=+dhfyft", "A342-=+D", 10));

		assertEquals(2, StringUtils.lastIndexOf("eradgerg", "ad", 7));
		assertEquals(2, StringUtils.lastIndexOf("eradgtsr", "Ad", 7));
		assertEquals(2, StringUtils.lastIndexOf("erAdhdyh", "ad", 7));
		assertEquals(2, StringUtils.lastIndexOf("erA342-=+dhffhy", "a342-=+d", 15));
		assertEquals(2, StringUtils.lastIndexOf("erA342-=+dhfyft", "A342-=+D", 15));

		assertEquals(3, StringUtils.lastIndexOf("eradgerg", "DG", 7));
		assertEquals(3, StringUtils.lastIndexOf("eradgtsr", "Dg", 7));
		assertEquals(3, StringUtils.lastIndexOf("erAdhdyh", "dh", 7));
		assertEquals(9, StringUtils.lastIndexOf("erA342-=+dhffhy", "Dh", 13));
		assertEquals(8, StringUtils.lastIndexOf("erA342-=+dh", "+DH", 10));

	}

}
