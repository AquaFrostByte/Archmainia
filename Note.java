public class Note {
    public double hitTime; 
    public int lane;       
    public double duration; // 0 for tap, > 0 for hold
    
    public boolean active = true; 
    public boolean isBeingHeld = false; // Tracks if the player is currently holding it

    public Note(double hitTime, int lane, double duration) {
        this.hitTime = hitTime;
        this.lane = lane;
        this.duration = duration;
    }
    
    public double getEndTime() {
        return hitTime + duration;
    }
}