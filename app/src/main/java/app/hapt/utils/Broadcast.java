package app.hapt.utils;

public class Broadcast {

    public final int duration;
    public final int delay;
    public final String pattern;
    public final boolean sound;

    public Broadcast(String pattern, int duration, int delay, boolean sound) {
        this.duration = duration;
        this.delay = delay;
        this.pattern = pattern;
        this.sound = sound;
    }
}
