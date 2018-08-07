package com.myscrap.model;

import java.util.TreeSet;

public class GalleryModel  {
	public TreeSet<String> folderImages;
	public String folderName, folderImagePath;
	public int totalCount;

	public GalleryModel() {
		folderImages = new TreeSet<>();
	}
}
