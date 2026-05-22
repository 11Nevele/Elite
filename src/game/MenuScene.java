package game;

import game.engine.*;

/**
 * Manages the main-menu 3D showcase: displays player ship model(s) against the
 * star field with a slow yaw spin, then runs a launch animation (ships accelerate
 * forward) when the player confirms a game mode selection.
 */
public class MenuScene
{
    // ── showcase camera (diagonal/front view) ───────────────────────────────
    private static final double CAM_X = 20;
    private static final double CAM_Y =  -3;
    private static final double CAM_Z = -20;

    // ── ship display positions ───────────────────────────────────────────────
    private static final double SHIP_Y         =  0;
    private static final double SHIP_Z         =  0;
    private static final double SINGLE_X       =  0;
    private static final double P1_X           = -6;
    private static final double P2_X           =  6;
    private static final double SHIP_SCALE     =  0.2;
    private static final double SPIN_SPEED_DEG =  0;

    // ── idle wiggle ──────────────────────────────────────────────────────────
    private static final double WIGGLE_PITCH_AMP = 3.0;   // degrees peak
    private static final double WIGGLE_ROLL_AMP  = 2.0;   // degrees peak
    private static final double WIGGLE_FREQ_A    = 0.45;  // Hz  (primary)
    private static final double WIGGLE_FREQ_B    = 0.97;  // Hz  (secondary, inharmonic)

    // ── launch animation ─────────────────────────────────────────────────────
    // Camera sweeps from the menu side position (CAM_X, CAM_Y, CAM_Z) to the
    // front position, always looking at the origin where the ships sit.
    private static final double LAUNCH_DURATION  = 2.4;
    private static final double LAUNCH_END_CAM_X =   0;
    private static final double LAUNCH_END_CAM_Y =   0;
    private static final double LAUNCH_END_CAM_Z = -20;

    private final Renderable p1Ship;
    private final Renderable p2Ship;
    private final Quaternion uprightRot;   // roll-180 base shared by both ships

    private double spinAngle  = 0;   // accumulated yaw during menu (degrees)
    private double launchTimer = 0;  // elapsed time since launch triggered
    private double wiggleTime  = 0;  // runs continuously for idle animation

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
         * @param menuSelection 0 = single-player, 1 = two-player, 2 = competitive, 3 = quit
     */
    public void update(double delta, int menuSelection)
    {
        spinAngle  += SPIN_SPEED_DEG * delta;
        wiggleTime += delta;
                boolean showBoth = menuSelection == 1 || menuSelection == 2;

        Quaternion base = Quaternion.yaw(spinAngle).multiply(uprightRot).normalize();

        Vector3 camPos = new Vector3(CAM_X, CAM_Y, CAM_Z);
        Renderer.renderer.updateCamera(camPos, lookAt(camPos, new Vector3(0, 0, 0)));

        double p1X = showBoth ? P1_X : SINGLE_X;
        p1Ship.position = new Vector3(p1X, SHIP_Y, SHIP_Z);
        p1Ship.rotation  = base.multiply(wiggle(wiggleTime, 0.0)).normalize();
        Renderer.renderer.render(p1Ship);

        if (showBoth)
        {
            p2Ship.position = new Vector3(P2_X, SHIP_Y, SHIP_Z);
            p2Ship.rotation  = base.multiply(wiggle(wiggleTime, Math.PI)).normalize();
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

        // Smooth-step progress 0 → 1 over LAUNCH_DURATION
        double t = Math.min(launchTimer / LAUNCH_DURATION, 1.0);
        double smooth = t * t * (3.0 - 2.0 * t);

        // Camera interpolates from side position to front position
        double camX = lerp(CAM_X, LAUNCH_END_CAM_X, smooth);
        double camY = lerp(CAM_Y, LAUNCH_END_CAM_Y, smooth);
        double camZ = lerp(CAM_Z, LAUNCH_END_CAM_Z, smooth);

        Vector3 camPos = new Vector3(camX, camY, camZ);
        Renderer.renderer.updateCamera(camPos, lookAt(camPos, new Vector3(0, 0, 0)));

        // Ships stay at their menu positions — only the camera moves
        Quaternion spin = Quaternion.yaw(spinAngle).multiply(uprightRot).normalize();
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

        return launchTimer >= LAUNCH_DURATION;
    }

    /** Resets launch timer and spin angle (call on game restart or re-entering menu). */
    public void resetLaunch()
    {
        launchTimer = 0;
        spinAngle   = 0;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns a rotation quaternion so a camera at {@code from} looks toward {@code to}.
     * Decomposed as yaw-around-world-Y then pitch-around-local-X (no roll).
     */
    private static Quaternion lookAt(Vector3 from, Vector3 to)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6) return new Quaternion();
        dx /= len; dy /= len; dz /= len;

        double yawDeg   = Math.toDegrees(Math.atan2(dx, dz));
        double hLen     = Math.sqrt(dx * dx + dz * dz);
        double pitchDeg = Math.toDegrees(Math.atan2(-dy, hLen));

        // q.multiply(q2) applies q2 first → yaw applied before pitch
        return Quaternion.pitch(pitchDeg).multiply(Quaternion.yaw(yawDeg)).normalize();
    }

    private static double lerp(double a, double b, double t)
    {
        return a + (b - a) * t;
    }

    /**
     * Returns a small pitch+roll perturbation for the idle wiggle.
     * Two inharmonic sine waves per axis keep the motion from ever feeling looped.
     *
     * @param t     running time in seconds
     * @param phase per-ship phase offset (radians) so ships don't move in sync
     */
    private static Quaternion wiggle(double t, double phase)
    {
        double ta = 2 * Math.PI * WIGGLE_FREQ_A * t;
        double tb = 2 * Math.PI * WIGGLE_FREQ_B * t;
        double pitch = WIGGLE_PITCH_AMP * (Math.sin(ta + phase) + 0.4 * Math.sin(tb));
        double roll  = WIGGLE_ROLL_AMP  * (Math.sin(ta * 0.7 + phase + 1.1) + 0.3 * Math.sin(tb + phase * 0.8));
        return Quaternion.pitch(pitch).multiply(Quaternion.roll(roll));
    }
}
