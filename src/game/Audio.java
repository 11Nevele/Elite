package game;

import tapplet.*;
import java.io.File;
import javafx.scene.media.AudioClip;

/**
 * Manages audio resources for the game.
 * Contains static AudioClip instances for different sound effects.
 */
public class Audio 
{
    /** Sound effect for asteroid explosion */
    public static javafx.scene.media.AudioClip explosionAsteroid;
    
    /** Sound effect for ship explosion */
    public static javafx.scene.media.AudioClip explosionShip;
    
    /** Sound effect for laser weapon firing */
    public static javafx.scene.media.AudioClip lazer;
    
    /** Background ambient music */
    public static javafx.scene.media.AudioClip ambient;
}
