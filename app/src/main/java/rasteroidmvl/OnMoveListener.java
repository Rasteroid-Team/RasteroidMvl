package rasteroidmvl;

public interface OnMoveListener {

    /**
     * Called when a Joystick has been moved
     * @param angle current angle
     * @param strength current strength
     */
    void onMove(int angle, int strength);
}