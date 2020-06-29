import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;

import java.nio.FloatBuffer;
import com.jogamp.common.nio.Buffers;
import java.util.*;

public class VBOManager {

    // main mesh vertex information organising and referencing class
     
    // add the target mesh to initVerts() and using the add() method
    // the vertecies will be exposed to opengl with genBuffers() by a
    // different part of the program
    
    // vertex arrays MUST be interleaved with normals: 3v, 3n, 

    private ArrayList<VBO> vbos;
    private ArrayList<String> names;
    private ArrayList<float[]> arrs;
    private int vboRef;
    // number of floats per vertex
    final int VERT_SIZE = 14;
    static int CUBE_TOP = 0, CUBE_BOT = 1, CUBE_LEFT = 2;
    static int CUBE_RIGHT = 3, CUBE_FRONT = 4, CUBE_BACK = 5;
    static int TREE = 6, ROCK = TREE+1, MAN = ROCK+1;
    static VBOManager manager;

    VBOManager(int vboID) {
        vboRef = vboID;
        vbos = new ArrayList<VBO>();
        names = new ArrayList<String>();
        arrs = new ArrayList<float[]>();
        initVerts();
        manager = this;
    }

    private void initVerts() {
        this.add("cubeTop", GeoVerts.getTopVerts(), GL.GL_TRIANGLE_STRIP);
        this.add("cubeBot", GeoVerts.getBotVerts(), GL.GL_TRIANGLE_STRIP);
        this.add("cubeLeft", GeoVerts.getLeftVerts(), GL.GL_TRIANGLE_STRIP);
        this.add("cubeRight", GeoVerts.getRightVerts(), GL.GL_TRIANGLE_STRIP);
        this.add("cubeFront", GeoVerts.getFrontVerts(), GL.GL_TRIANGLE_STRIP);
        this.add("cubeBack", GeoVerts.getBackVerts(), GL.GL_TRIANGLE_STRIP);

        this.add("tree", Assets.meshs.get(0).verts, GL.GL_TRIANGLES);
        this.add("rock", Assets.meshs.get(1).verts, GL.GL_TRIANGLES);
        this.add("man", Assets.meshs.get(2).verts, GL.GL_TRIANGLES);
    }

    VBO getVBO(String name) {
        for (int i = 0; i < names.size(); i++) {
            if (name == names.get(i)) {
                return vbos.get(i);
            }
        }
        return null;
    }

    VBO getVBO(int i) {
        if (i < vbos.size()) {
            return vbos.get(i);
        }
        return null;
    }

    void add(String name, float[] data, int vertexPattern) {
        names.add(name);
        arrs.add(data);
        int start = 0;
        if (vbos.size() > 0) {
            start = vbos.get(vbos.size() - 1).start + vbos.get(vbos.size() - 1).length * VERT_SIZE;
        }
        vbos.add(new VBO(start, data.length / VERT_SIZE, vertexPattern));
        // System.out.println("s " + start + " l " + data.length / 6);
    }

    FloatBuffer genBuffer() {
        int capacity = 0;
        for (int i = 0; i < arrs.size(); i++) {
            capacity += arrs.get(i).length;
        }

        float[] data = new float[capacity];
        int k = 0;
        for (int i = 0; i < arrs.size(); i++) {
            for (int j = 0; j < arrs.get(i).length; j++) {
                data[k] = arrs.get(i)[j];
                // System.out.print(data[k] + " ");
                k++;
            }
            // System.out.println();
        }
        FloatBuffer buff = Buffers.newDirectFloatBuffer(data);
        return buff;
    }

    



}