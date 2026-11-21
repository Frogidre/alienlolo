package com.alienskin.mesh;

/** A static triangle-grid mesh. Positions are in BLOCKS (pixels / 16), part-local. */
public final class Mesh {
    public final float[] pos;    // x,y,z per vertex
    public final float[] nor;    // nx,ny,nz per vertex
    public final float[] uv;     // u,v per vertex (0..1 over the whole texture atlas)
    public final int[] quads;    // 4 vertex indices per quad

    public Mesh(float[] pos, float[] nor, float[] uv, int[] quads) {
        this.pos = pos;
        this.nor = nor;
        this.uv = uv;
        this.quads = quads;
    }

    public int quadCount() {
        return quads.length / 4;
    }
}
