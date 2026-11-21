package com.alienskin.mesh;

/** Builds smooth meshes from parametric surfaces. Works in pixels; the Mesh stores blocks. */
public final class MeshBuilder {
    public static final int ATLAS_W = 512;
    public static final int ATLAS_H = 256;

    /** A surface p(u,v) with u,v in 0..1. */
    public interface Surf {
        void at(double u, double v, double[] out);
    }

    private MeshBuilder() {}

    /**
     * Samples the surface on an nu x nv grid.
     * core = a point inside the surface for each (u,v), used to point normals outward.
     * region = {x, y, w, h} rectangle of the texture atlas, in pixels.
     */
    public static Mesh grid(Surf surf, Surf core, int nu, int nv, int[] region) {
        int vw = nu + 1, vh = nv + 1;
        float[] pos = new float[vw * vh * 3];
        float[] nor = new float[vw * vh * 3];
        float[] uv = new float[vw * vh * 2];
        double[] p = new double[3], a = new double[3], b = new double[3], c = new double[3];
        double[] d = new double[3], k = new double[3];
        double e = 1e-4;

        for (int j = 0; j < vh; j++) {
            double v = (double) j / nv;
            for (int i = 0; i < vw; i++) {
                double u = (double) i / nu;
                surf.at(u, v, p);

                // tangents by central differences (u wraps around, v is clamped)
                surf.at(u + e, v, a);
                surf.at(u - e, v, b);
                double v1 = Math.min(1.0, v + e), v0 = Math.max(0.0, v - e);
                surf.at(u, v1, c);
                surf.at(u, v0, d);
                double[] tu = {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
                double[] tv = {c[0] - d[0], c[1] - d[1], c[2] - d[2]};
                double nx = tu[1] * tv[2] - tu[2] * tv[1];
                double ny = tu[2] * tv[0] - tu[0] * tv[2];
                double nz = tu[0] * tv[1] - tu[1] * tv[0];

                core.at(u, v, k);
                double ox = p[0] - k[0], oy = p[1] - k[1], oz = p[2] - k[2];
                double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (len < 1e-9) {                       // pole: fall back to "away from the core"
                    nx = ox; ny = oy; nz = oz;
                    len = Math.sqrt(nx * nx + ny * ny + nz * nz);
                    if (len < 1e-9) { nx = 0; ny = -1; nz = 0; len = 1; }
                } else if (nx * ox + ny * oy + nz * oz < 0) {
                    nx = -nx; ny = -ny; nz = -nz;       // make it point outward
                }
                int idx = j * vw + i;
                pos[idx * 3] = (float) (p[0] / 16.0);
                pos[idx * 3 + 1] = (float) (p[1] / 16.0);
                pos[idx * 3 + 2] = (float) (p[2] / 16.0);
                nor[idx * 3] = (float) (nx / len);
                nor[idx * 3 + 1] = (float) (ny / len);
                nor[idx * 3 + 2] = (float) (nz / len);
                uv[idx * 2] = (float) ((region[0] + u * region[2]) / ATLAS_W);
                uv[idx * 2 + 1] = (float) ((region[1] + v * region[3]) / ATLAS_H);
            }
        }

        int[] quads = new int[nu * nv * 4];
        int q = 0;
        for (int j = 0; j < nv; j++) {
            for (int i = 0; i < nu; i++) {
                quads[q++] = j * vw + i;
                quads[q++] = j * vw + i + 1;
                quads[q++] = (j + 1) * vw + i + 1;
                quads[q++] = (j + 1) * vw + i;
            }
        }
        return new Mesh(pos, nor, uv, quads);
    }

    // ---- primitives ------------------------------------------------------------------------

    public static Mesh ellipsoid(double cx, double cy, double cz, double rx, double ry, double rz,
                                 int nu, int nv, int[] region) {
        Surf s = (u, v, o) -> {
            double th = Math.PI * v, ph = 2 * Math.PI * u;
            double sn = Math.sin(th);
            o[0] = cx + rx * sn * Math.sin(ph);
            o[1] = cy - ry * Math.cos(th);
            o[2] = cz + rz * sn * Math.cos(ph);
        };
        Surf core = (u, v, o) -> { o[0] = cx; o[1] = cy; o[2] = cz; };
        return grid(s, core, nu, nv, region);
    }

    /**
     * Tapered tube from a to b. bend = how far the middle is pushed sideways (a curved limb).
     * bulge = extra thickness in the middle (muscle).
     */
    public static Mesh tube(double[] a, double[] b, double r0, double r1, double[] bend, double bulge,
                            int ring, int seg, int[] region) {
        double[] dir = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        double dl = Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
        dir[0] /= dl; dir[1] /= dl; dir[2] /= dl;
        double[] h = Math.abs(dir[0]) < 0.9 ? new double[]{1, 0, 0} : new double[]{0, 0, 1};
        double[] e1 = cross(dir, h);
        normalize(e1);
        double[] e2 = cross(dir, e1);
        normalize(e2);

        Surf core = (u, v, o) -> {
            double sb = Math.sin(Math.PI * v);
            for (int i = 0; i < 3; i++) o[i] = a[i] + (b[i] - a[i]) * v + bend[i] * sb;
        };
        Surf s = (u, v, o) -> {
            double[] c = new double[3];
            core.at(u, v, c);
            double r = (r0 + (r1 - r0) * v) * (1.0 + bulge * Math.sin(Math.PI * v));
            double cs = Math.cos(2 * Math.PI * u), sn = Math.sin(2 * Math.PI * u);
            for (int i = 0; i < 3; i++) o[i] = c[i] + r * (cs * e1[i] + sn * e2[i]);
        };
        return grid(s, core, ring, seg, region);
    }

    /** Merges several meshes into one (all share the same atlas). */
    public static Mesh merge(Mesh... parts) {
        int vc = 0, qc = 0;
        for (Mesh m : parts) { vc += m.pos.length / 3; qc += m.quads.length; }
        float[] pos = new float[vc * 3], nor = new float[vc * 3], uv = new float[vc * 2];
        int[] quads = new int[qc];
        int vo = 0, qo = 0;
        for (Mesh m : parts) {
            int n = m.pos.length / 3;
            System.arraycopy(m.pos, 0, pos, vo * 3, n * 3);
            System.arraycopy(m.nor, 0, nor, vo * 3, n * 3);
            System.arraycopy(m.uv, 0, uv, vo * 2, n * 2);
            for (int i = 0; i < m.quads.length; i++) quads[qo + i] = m.quads[i] + vo;
            vo += n;
            qo += m.quads.length;
        }
        return new Mesh(pos, nor, uv, quads);
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    private static void normalize(double[] v) {
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= l; v[1] /= l; v[2] /= l;
    }
}
