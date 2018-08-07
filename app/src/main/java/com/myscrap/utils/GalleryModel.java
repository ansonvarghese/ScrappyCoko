package com.myscrap.utils;

import java.util.TreeSet;

public class GalleryModel {
	public TreeSet<String> folderImages;
	public String folderName, folderImagePath;
	public int totalCount;

	public GalleryModel() {
		folderImages = new TreeSet<>();
	}
}
