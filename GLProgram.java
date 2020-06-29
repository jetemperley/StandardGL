import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.*;

import java.util.*;
import java.io.*;
import java.nio.*;

public abstract class GLProgram{
    
    // program ID and the texture ID it renders to
    int ID, textureID;

    // uniform locations common to all inhereting programs
    // altColor is an additive color (rgba) on top of any texture, or lack there of
    int altColorLoc;
    // drawOffset is the xyz translation of the verticies positions  
    int drawOffsetLoc;
    // projection view concatiinated matrix
    int projViewLoc;

    GLProgram(){}

    void setTexTarget(int texTarget){
        textureID = texTarget;
    }

    void initUniforms(GL4 g){
        drawOffsetLoc = g.glGetUniformLocation(ID, "draw_offset");
        altColorLoc = g.glGetUniformLocation(ID, "altcolor");
        projViewLoc = g.glGetUniformLocation(ID, "pv");
        
    }

    void initVertAttributes(GL4 g){
        // configure pointer for position
        g.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 56, 0);
        g.glEnableVertexAttribArray(0);
        // config pointer for normals
        g.glVertexAttribPointer(1, 3, GL4.GL_FLOAT, false, 56, 12);
        g.glEnableVertexAttribArray(1);
        // pointer for tex coords
        g.glVertexAttribPointer(2, 2, GL4.GL_FLOAT, false, 56, 24);
        g.glEnableVertexAttribArray(2);
        // pointer for boneIDs coords
        g.glVertexAttribPointer(3, 3, GL4.GL_FLOAT, false, 56, 32);
        g.glEnableVertexAttribArray(3);
        // pointer for bone weights
        g.glVertexAttribPointer(4, 3, GL4.GL_FLOAT, false, 56, 44);
        g.glEnableVertexAttribArray(4);
    }
    abstract void ready(GL4 g);

    void make(GL4 g, String vertShader, String fragShader){
        this.ID = createShaderPrograms(g, vertShader, fragShader);
    }

    private int createShaderPrograms(GL4 gl, String vertSh, String fragSh) {

        String[] vertShaderSource = readShaderSource(vertSh);
        String[] fragShaderSource = readShaderSource(fragSh);

        int vShader = makeVShader(gl, vertShaderSource);
        int fShader = makeFShader(gl, fragShaderSource);

        // check for errors
        int[] compiled = new int[1];
        gl.glGetShaderiv(vShader, GL4.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 1) {
            System.out.println(". . . " + vertSh + " compilation success.");
        } else {
            System.out.println(". . . " + vertSh + " compilation failed.");
            printShaderLog(gl, vShader);
        }

        gl.glGetShaderiv(fShader, GL4.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 1) {
            System.out.println(". . . " + fragSh + " compilation success.");
        } else {
            System.out.println(". . . " + fragSh + " compilation failed.");
            printShaderLog(gl, fShader);
        }

        int rendering_program = makeProgram(gl, vShader, fShader);

        gl.glDeleteShader(vShader);
        gl.glDeleteShader(fShader);
        return rendering_program;

    }

    private int makeVShader(GL4 gl, String[] source) {
        int id = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
        gl.glShaderSource(id, source.length, source, null, 0);
        gl.glCompileShader(id);
        return id;
    }

    private int makeFShader(GL4 gl, String[] source) {
        int id = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
        gl.glShaderSource(id, source.length, source, null, 0);
        gl.glCompileShader(id);
        return id;
    }

    private int makeProgram(GL4 gl, int vShader, int fShader) {
        int vfprogram = gl.glCreateProgram();
        gl.glAttachShader(vfprogram, vShader);
        gl.glAttachShader(vfprogram, fShader);
        gl.glLinkProgram(vfprogram);
        return vfprogram;
    }

    private String[] readShaderSource(String filename) {
        Vector<String> lines = new Vector<String>();
        Scanner sc;
        try {
            sc = new Scanner(new File(filename));
        } catch (IOException e) {
            System.out.println("IOException reading file: " + e);
            return null;
        }
        while (sc.hasNext()) {
            // System.out.println();
            lines.addElement(sc.nextLine());
        }
        String[] program = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            program[i] = (String) lines.elementAt(i) + "\n";
            // System.out.println(program[i]);
        }
        sc.close();
        return program;
    }

    private void printShaderLog(GL4 gl, int shader) {

        int[] len = new int[1];
        int[] chWrittn = new int[1];
        byte[] log = null;
        // determine the length of the shader compilation log
        gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, len, 0);
        if (len[0] > 0) {
            log = new byte[len[0]];
            gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
            System.out.println("Shader Info Log: ");
            for (int i = 0; i < log.length; i++) {
                System.out.print((char) log[i]);
            }
        }
    }

    void setDrawOffset(GL4 g, float x, float y, float z){
        g.glUniform3f(drawOffsetLoc, x, y, z);
    }

    void setHighlightColor(GL4 g ,float red, float green, float blue, float alpha){
        g.glUniform4f(altColorLoc, red, green, blue, alpha);
    }

    void setPV(GL4 g, Matrix4 pv){
        g.glUniformMatrix4fv(projViewLoc, 1, false, pv.getMatrix(), 0);
    }

    void resetUniforms(GLGraphics g){
        setDrawOffset(g.g, 0, 0, 0);
        setHighlightColor(g.g, 0.5f, 0.5f, 0.5f, 0.5f);
        setPV(g.g, g.cam.getCamera());
    }
}