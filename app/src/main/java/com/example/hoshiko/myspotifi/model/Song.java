package com.example.hoshiko.myspotifi.model;

public class Song {

    private int Id;
    private String title;
    private int numLikes;
    private int numPlays;
    private String image;
    private boolean isFavorite;

    public Song(String id, String title, String numlikes, String numplays, String image){

        this.Id = Integer.parseInt(id);
        this.title = title;
        this.numLikes = Integer.parseInt(numlikes);
        this.numPlays = Integer.parseInt(numplays);
        this.image = image;
        this.isFavorite = false;
    }


    // Getter & Setter for Id
    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }


    //Getter & Setter for Title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    //Getter & Setter for numlikes
    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    //Getter & Setter for numPlays
    public int getNumPlays() {
        return numPlays;
    }

    public void setNumPlays(int numPlays) {
        this.numPlays = numPlays;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
