package game;

import game.engine.*;

/**
 * Manages the main-menu 3D showcase: displays player ship model(s) against the
 * star field with a slow yaw spin, then runs a launch animation (ships accelerate
 * forward) when the player confirms a game mode selection.
 */
public class MenuScene
{
    // ── showcase camera ──────────────────────────────────────────────────────
    private static final double CAM_X           =   0;
    private static final double CAM_Y           =   0;
    private static final double CAM_Z           = -22;
    private static final double CAM_PITCH_DEG   =  -8;   // slight nose-down look

    // ── ship display positions ───────────────────────────────────────────────
    private static final double SHIP_Y          =   0;
    private static final double SHIP_Z          =  12;
    private static final double SINGLE_X        =   0;
    private static final double P1_X            =  -5;
    private static final double P2_X            =   5;
    private static final double SHIP_SCALE      =   0.2;
    private static final double SPIN_SPEED_DEG  =  30;   // gentle yaw per second

    // ── launch animation ─────────────────────────────────────────────────────
    private static final double LAUNCH_DURATION    = 2.4;  // seconds
    private static final double LAUNCH_ACCEL       = 180;  // units / sec²
    private static final double LAUNCH_CAM_CREEP   =   8;  // cam Z advance / sec
    private static final double LAUNCH_PITCH_RATE  =  22;  // nose-up deg / sec
    private static final double LAUNCH_MAX_PITCH   =  18;  // nose-up cap (deg)

    private final Renderable p1Ship;
    private final Renderable p2Ship;
    private final Quaternion uprightRot;   // roll-180 base shared by both ships

    private double spinAngle  = 0;   // accumulated yaw during menu (degrees)
    private double launchTimer = 0;  // elapsed time since launch triggered

    public MenuScene()
    {
        uprightRot = Quaternion.roll(180);

        p1Ship = new Renderable(Models.playerShip);
        p1Ship.scale = SHIP_SCALE;
        // Remove from the GameObject queue – these are display-only objects
        // that must never enter the active game-object list.
        GameObject.newObjects.remove(p1Ship);

        p2Ship = new Renderable(Models.player2Ship);
        p2Ship.scale = SHIP_SCALE;
        GameObject.newObjects.remove(p2Ship);
    }

    // ── public API ───────────────────────────────────────────────────────────

    /**
     * Called every frame while in MENU state.
     * Sets the renderer camera to a cinematic showcase angle and renders the
     * appropriate ship model(s) based on the currently highlighted menu option.
     *
     * @param delta         seconds since last frame
     * @param menuSelection 0 = single-player, 1 = two-player, 2 = competitive
     */
    public void update(double delta, int menuSelection)
    {
        spinAngle += SPIN_SPEED_DEG * delta;
        boolean showBoth = menuSelection != 0;

        Quaternion spin = Quaternion.yaw(spinAngle).multiply(uprightRot).normalize();

        setCamera(CAM_Z, CAM_PITCH_DEG);

        double p1X = showBoth ? P1_X : SINGLE_X;
        p1Ship.position = new Vector3(p1X, SHIP_Y, SHIP_Z);
        p1Ship.rotation  = spin;
        Renderer.renderer.render(p1Ship);

        if (showBoth)
        {
            p2Ship.position = new Vector3(P2_X, SHIP_Y, SHIP_Z);
            p2Ship.rotation  = spin;
            Renderer.renderer.render(p2Ship);
        }
    }

    /**
     * Called every frame while in LAUNCH_ANIMATION state.
     * Ships accelerate forward (+Z) and pitch up slightly; the camera creeps
     * forward. Returns {@code true} once the animation has finished.
     *
     * @param delta    seconds since last frame
     * @param showBoth whether to animate two ships (two-player / competitive)
     * @return {@code true} when the launch sequence is complete
     */
    public boolean updateLaunch(double delta, boolean showBoth)
    {
        launchTimer += delta;

        // Kinematic Z offset along rail  (s = ½ a t²)
        double shipZ = SHIP_Z + 0.5 * LAUNCH_ACCEL * launchTimer * launchTimer;

        // Nose pitches up as engines fire
        double nosePitch = Math.min(launchTimer * LAUNCH_PITCH_RATE, LAUNCH_MAX_PITCH);
        Quaternion launchRot = Quaternion.pitch(-nosePitch).multiply(uprightRot).normalize();

        // Camera creeps forward
        double camZ = CAM_Z + launchTimer * LAUNCH_CAM_CREEP;
        setCamera(camZ, CAM_PITCH_DEG);

        double p1X = showBoth ? P1_X : SINGLE_X;
        p1Ship.position = new Vector3(p1X, SHIP_Y, shipZ);
        p1Ship.rotation  = launchRot;
        Renderer.renderer.render(p1Ship);

        if (showBoth)
        {
            p2Ship.position = new Vector3(P2_X, SHIP_Y, shipZ);
            p2Ship.rotation  = launchRot;
            Renderer.renderer.render(p2Ship);
        }

        return launchTimer >= LAUNCH_DURATION;
    }

    /** Resets launch timer and spin angle (call on game restart or re-entering menu). */
    public void resetLaunch()
    {
        launchTimer = 0;
        spinAngle   = 0;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void setCamera(double camZ, double pitchDeg)
    {
        Quaternion camRot = Quaternion.pitch(pitchDeg);
        Renderer.renderer.updateCamera(new Vector3(CAM_X, CAM_Y, camZ), camRot);
    }
}
