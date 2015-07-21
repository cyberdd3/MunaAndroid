package com.akraft.muna.models.wrappers;

public class ShowcasePage {
    private int[] images;

    private String[] captions;

    public ShowcasePage(int[] images, String[] captions) {
        this.images = images;
        this.captions = captions;
    }

    public String[] getCaptions() {
        return captions;
    }

    public int[] getImages() {
        return images;
    }
}
