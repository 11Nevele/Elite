package game;

import tapplet.*;

import java.io.File;

import javafx.scene.media.AudioClip;

public class Audio 
{
    public static javafx.scene.media.AudioClip explosionAsteroid;
    public static AudioClip explosionShip;
    public static AudioClip lazer;
    public static javafx.scene.media.AudioClip ambient = 
    new javafx.scene.media.AudioClip(new File(getCodeBase() + "\\Sound\\Ambient.wav").toURI().toString()); 
}
