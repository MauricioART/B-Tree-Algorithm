package com.arturoar.util;
 
import javafx.scene.media.AudioClip;
import com.arturoar.ui.BPlusTreeUI;

public enum SoundType {
    
        HIGHLIGHTING("sounds/pop-402324.mp3", 0.5),
        FADE_OUT("sounds/whoosh-09-410876.mp3", 0.5),
        SUCCESS("sounds/successed-295058.mp3", 0.5),
        ERROR("sounds/error-010-206498.mp3", 0.5);
    
        private final String soundPath;
        private final double defaultVolume;
        private AudioClip audioClip;
    
        SoundType(String soundPath, double defaultVolume) {
            this.soundPath = soundPath;
            this.defaultVolume = defaultVolume;
        }
    
        public void loadSound() {
            try {
                if (audioClip == null) {
                    audioClip = new AudioClip(BPlusTreeUI.class.getResource(soundPath).toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("Error loading sound: " + this.name() + " - " + e.getMessage());
            }
        }
    
        public void play() {
            play(defaultVolume);
        }
    
        public void play(double volume) {
            if (audioClip != null) {
                audioClip.setVolume(volume);
                audioClip.play();
            }
        }
    
        public void stop() {
            if (audioClip != null) {
                audioClip.stop();
            }
        }
    
        // Cargar todos los sonidos al inicializar
        public static void loadAllSounds() {
            for (SoundType sound : values()) {
                sound.loadSound();
            }
        }
    }