package com.alienskin.mesh;

/**
 * The alien body as smooth meshes. All coordinates are in pixels in the vanilla part's
 * local space (+y down, -z = front). Texture atlas is 512x256, laid out like this:
 *   A skull (0,0,256,128)      B torso (256,0,256,128)    C limb skin (0,128,256,128)
 *   D claw dark (256,128,64,64) E joint skin (320,128,64,64) F finger gradient (384,128,64,64)
 *   G jaw (256,192,128,64)
 */
public final class AlienMeshes {
    public static final int[] A = {0, 0, 256, 128};
    public static final int[] B = {256, 0, 256, 128};
    public static final int[] C = {0, 128, 256, 128};
    public static final int[] E = {320, 128, 64, 64};
    public static final int[] F = {384, 128, 64, 64};
    public static final int[] G = {256, 192, 128, 64};

    // skull egg
    public static final double RX = 5.6, RY = 6.8, RZ = 6.0, CY = -6.9, ZC = 1.8;

    // torso lookup tables (y, half-width x, front depth, back depth, centre z)
    private static final double[] TY  = {-2.4, -1.2, -0.3, 0.4, 1.2, 2.5, 4.5, 6.0, 8.0, 10.0, 11.5, 12.3};
    private static final double[] TRX = { 1.1,  1.15, 2.2, 4.4, 3.6, 3.0, 2.8, 2.3, 1.7,  2.3,  2.6,  1.4};
    private static final double[] TRF = { 0.9,  1.0,  1.0, 1.0, 1.7, 2.0, 2.1, 1.6, 1.2,  1.4,  1.5,  0.8};
    private static final double[] TRB = { 1.1,  1.1,  1.0, 1.0, 1.5, 1.6, 1.5, 1.3, 1.2,  1.8,  2.2,  1.0};
    private static final double[] TCZ = { 1.0,  0.9,  0.6, 0.3, 0.1, 0.0, 0.0, 0.0, 0.0,  0.0,  0.0,  0.0};
    public static final double TORSO_Y0 = -2.4, TORSO_Y1 = 12.3;

    public static final Mesh SKULL;
    public static final Mesh JAW;
    public static final Mesh TORSO;
    public static final Mesh ARM_UPPER;
    public static final Mesh ARM_FORE;
    public static final Mesh LEG_THIGH;
    public static final Mesh LEG_SHIN;
    public static final Mesh LEG_FOOT;

    /** Jaw hinge in head space (pixels). */
    public static final double[] JAW_HINGE = new double[3];

    static {
        SKULL = MeshBuilder.grid(AlienMeshes::skull,
                (u, v, o) -> { o[0] = 0; o[1] = CY; o[2] = ZC; }, 32, 24, A);

        // jaw: a rounded chin cap in front of the skull, hinged behind it
        double[] f = skullFront(-1.5);
        double hx = 0, hy = -2.2, hz = f[2] + 2.4;
        JAW_HINGE[0] = hx; JAW_HINGE[1] = hy; JAW_HINGE[2] = hz;
        JAW = MeshBuilder.ellipsoid(0 - hx, -1.45 - hy, f[2] + 0.45 - hz, 2.2, 1.15, 1.4, 20, 12, G);

        TORSO = MeshBuilder.grid(AlienMeshes::torso, AlienMeshes::torsoCore, 24, 56, B);

        // arm: shoulder + upper arm + elbow (arm space); forearm + wrist + hand + fingers (elbow space)
        ARM_UPPER = MeshBuilder.merge(
                MeshBuilder.ellipsoid(0, 0.1, 0, 1.25, 1.25, 1.25, 12, 8, E),
                MeshBuilder.tube(new double[]{0, 0.2, 0}, new double[]{0, 6.8, 0.2}, 1.0, 0.8,
                        new double[]{0, 0, 0.25}, 0.08, 12, 8, C),
                MeshBuilder.ellipsoid(0, 6.8, 0.2, 0.95, 0.95, 0.95, 12, 8, E));
        ARM_FORE = MeshBuilder.merge(
                MeshBuilder.tube(new double[]{0, 0, 0}, new double[]{0, 7.2, 0}, 0.78, 0.5,
                        new double[]{0, 0, -0.2}, 0.1, 12, 8, C),
                MeshBuilder.ellipsoid(0, 7.2, 0, 0.62, 0.62, 0.62, 10, 6, E),
                MeshBuilder.ellipsoid(0, 8.1, 0, 0.95, 1.0, 0.55, 12, 8, C),
                finger(-0.55, -0.26), finger(0.0, 0.0), finger(0.55, 0.26));

        LEG_THIGH = MeshBuilder.merge(
                MeshBuilder.ellipsoid(0, 0, 0, 1.3, 1.3, 1.3, 12, 8, E),
                MeshBuilder.tube(new double[]{0, 0.2, 0}, new double[]{0, 6.4, 0.3}, 1.2, 0.9,
                        new double[]{0, 0, -0.25}, 0.1, 12, 8, C),
                MeshBuilder.ellipsoid(0, 6.4, 0.3, 1.0, 1.0, 1.0, 12, 8, E));
        LEG_SHIN = MeshBuilder.merge(
                MeshBuilder.tube(new double[]{0, 0, 0}, new double[]{0, 5.7, 0}, 0.85, 0.55,
                        new double[]{0, 0, 0.15}, 0.08, 12, 8, C),
                MeshBuilder.ellipsoid(0, 5.7, 0, 0.62, 0.62, 0.62, 10, 6, E));
        LEG_FOOT = MeshBuilder.merge(
                MeshBuilder.ellipsoid(0, -0.35, 0.5, 0.75, 0.55, 1.2, 12, 8, C),
                toe(-0.9, -1.5), toe(0.0, 0.0), toe(0.9, 1.5));
    }

    private AlienMeshes() {}

    /** Long tapered finger hanging down from the palm, with a dark claw tip (texture F). */
    private static Mesh finger(double x, double spread) {
        double len = 5.0;
        return MeshBuilder.tube(new double[]{x, 8.7, 0},
                new double[]{x + Math.sin(spread) * len, 8.7 + Math.cos(spread) * len, -0.6},
                0.32, 0.06, new double[]{0, 0, -0.5}, 0.0, 8, 8, F);
    }

    /** Long toe pointing forward along the ground. */
    private static Mesh toe(double x, double tipX) {
        return MeshBuilder.tube(new double[]{x * 0.6, -0.3, -0.4},
                new double[]{tipX, -0.05, -4.6}, 0.4, 0.07,
                new double[]{0, -0.45, 0}, 0.0, 8, 8, F);
    }

    // ---- surfaces --------------------------------------------------------------------------

    /** Skull surface: egg that is wider on top, narrow at the chin, long at the back. */
    public static void skull(double u, double v, double[] o) {
        double th = Math.PI * v, ph = 2 * Math.PI * u;
        double s = Math.sin(th), c = Math.cos(th);
        double m = 1.0 + 0.10 * c;
        double taper = c < 0 ? 1.0 - 0.38 * Math.pow(-c, 1.4) : 1.0;
        double sx = s * m * taper;
        double cp = Math.cos(ph);
        double back = 1.0 + 0.22 * Math.pow(Math.max(0.0, cp), 1.5);
        o[0] = RX * sx * Math.sin(ph);
        o[1] = CY - RY * c;
        o[2] = ZC + 0.6 * c + RZ * sx * cp * back;
    }

    /** The skull surface point straight in front (u = 0.5) at pixel height y. */
    public static double[] skullFront(double y) {
        double c = Math.max(-1.0, Math.min(1.0, (CY - y) / RY));
        double v = Math.acos(c) / Math.PI;
        double[] o = new double[3];
        skull(0.5, v, o);
        return o;
    }

    private static double smooth(double e0, double e1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - e0) / (e1 - e0)));
        return t * t * (3 - 2 * t);
    }

    private static double table(double[] vals, double y) {
        if (y <= TY[0]) return vals[0];
        for (int i = 0; i < TY.length - 1; i++) {
            if (y <= TY[i + 1]) {
                double t = (y - TY[i]) / (TY[i + 1] - TY[i]);
                t = t * t * (3 - 2 * t);
                return vals[i] + (vals[i + 1] - vals[i]) * t;
            }
        }
        return vals[vals.length - 1];
    }

    public static double ribRidge(double y) {
        return Math.pow(Math.max(0.0, Math.cos(2 * Math.PI * (y - 1.9) / 0.95)), 1.6);
    }

    public static double ribEnvelope(double y) {
        return smooth(1.0, 1.6, y) * (1.0 - smooth(5.6, 6.4, y));
    }

    /** Torso: neck, shoulders, ribcage with ridges, sunken belly, hips. */
    public static void torso(double u, double v, double[] o) {
        double y = TORSO_Y0 + v * (TORSO_Y1 - TORSO_Y0);
        double ph = 2 * Math.PI * u;
        double sp = Math.sin(ph), cp = Math.cos(ph);
        double rx = table(TRX, y), rf = table(TRF, y), rb = table(TRB, y), cz = table(TCZ, y);
        double w = 0.5 + 0.5 * cp;
        double rz = rf * (1 - w) + rb * w;

        double front = Math.max(0.0, -cp);
        double k = 1.0 + 0.12 * ribEnvelope(y) * Math.pow(front, 1.5) * (ribRidge(y) - 0.35);
        double dphi = Math.abs(ph - Math.PI);
        double groove = 0.35 * Math.exp(-(dphi * dphi) / 0.01) * smooth(0.8, 1.4, y) * (1.0 - smooth(6.4, 7.2, y));
        double da = Math.min(ph, 2 * Math.PI - ph);
        double spine = 0.12 * Math.exp(-(da * da) / 0.008) * (0.5 + 0.5 * Math.cos(2 * Math.PI * y / 1.1))
                * smooth(0.5, 1.5, y) * (1.0 - smooth(9.0, 10.0, y));

        o[0] = rx * k * sp;
        o[1] = y;
        o[2] = cz + rz * k * cp + groove + spine;
    }

    private static void torsoCore(double u, double v, double[] o) {
        double y = TORSO_Y0 + v * (TORSO_Y1 - TORSO_Y0);
        o[0] = 0;
        o[1] = y;
        o[2] = table(TCZ, y);
    }
}
