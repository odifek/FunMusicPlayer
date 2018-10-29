package com.techbeloved.funmusicplayer.ui;

public class AudioMedia {
    public String title;
    public String artist;
    public String filePath;
    public String albumArt;

    public AudioMedia(String title, String artist, String filePath, String albumArtPath) {
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.albumArt = albumArtPath;
    }
}
