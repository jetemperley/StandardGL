
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.*;

import java.util.*;
import java.io.*;
import java.nio.*;

public class GLGraphics {

    GL4 g;

    private int[] vao;
    private int[] vbo;

    // main rendering location and uniforms
    private UIRenderProgram UIprog;
    private ModelRenderProgram blockProg;
    private AnimatedModelRenderProgram animProg;
    private GLProgram currentProgram;
    private ModelRenderProgram lastModelProgram;

    // shadoow shader location and uniforms
    int shadow_shader, temp_sShader;
    int shLightCamLoc, shGridLoc;
    private int[] customBuffers, customTextures;

    int currentGridLoc, currentColorLoc;

    Camera cam;
    Matrix4 temp;
    VBOManager vm;
    Assets assets;
    // matrixes for calulating shadows
    // mats to translate between camera space and 0,1 screen pixel space
    Matrix4 sunV, sunPV, transOffset, lightCam2;
    Vec3 sun, ambLight;
    int iter;
    VBO vt;

    public GLGraphics(GL4 g) {

        this.g = g;
        // System.out.println("max uniform locs = " + GL4.GL_MAX_UNIFORM_LOCATIONS);

        // init assets

        temp = new Matrix4();

        vao = new int[3];
        vbo = new int[2];

        customBuffers = new int[3];
        customTextures = new int[3];

        g.glGenFramebuffers(customBuffers.length, customBuffers, 0);
        g.glGenTextures(customTextures.length, customTextures, 0);

        cam = new Camera();

        g.glGenVertexArrays(vao.length, vao, 0);
        g.glGenBuffers(vbo.length, vbo, 0);


        // set up binds for vao[0], regular render

        // shLightCamLoc = g.glGetUniformLocation(shadow_shader, "light_cam");
        // shGridLoc = g.glGetUniformLocation(shadow_shader, "grid_loc");
        // Mat4Utl.writeMat4(cam.projection);

        // initShadowProg(g);

        sunV = new Matrix4();
        sunV.rotate((float) (3 * Math.PI / 8), 1, 0, 0);
        sunV.rotate((float) (Math.PI), 0, 1, 0);
        sunV.rotate((float) (3 * Math.PI / 4), 0, 0, 1);
        sunV.translate(1, 2, 3);
        sunPV = new Matrix4();
        sunPV.loadIdentity();

        sun = new Vec3(-1f, -2f, 5f);
        sun.normalise();

        ambLight = new Vec3(0.4f, 0.4f, 0.4f);

        transOffset = new Matrix4();
        float[] f = { 0.5f, 0, 0, 0.5f, 0, 0.5f, 0, 0.5f, 0, 0, 0.5f, 0.5f, 0, 0, 0, 1 };
        float[] b = { 1 / (Camera.CLIP_EDGE * 2), 0, 0, 0.5f, 0, 1 / (Camera.CLIP_EDGE * 2), 0, 0.5f, 0, 0,
                1 / (-Camera.CLIP_DEPTH + Camera.CLIP_DEPTH), 0.5f, 0, 0, 0, 1 };
        transOffset.multMatrix(f);
        lightCam2 = new Matrix4();

        // FloatBuffer buff = Buffers.newDirectFloatBuffer(GeoVerts.getFaceTexCoords());
        // g.glBufferData(GL4.GL_ARRAY_BUFFER, buff.limit() * 4, buff,
        // GL4.GL_STATIC_DRAW);

        assets = new Assets(g);

        for (int i : Assets.joglTexLocs) {
            g.glBindTexture(GL4.GL_TEXTURE_2D, i);
            g.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR_MIPMAP_LINEAR);
            g.glGenerateMipmap(GL4.GL_TEXTURE_2D);
        }

        vm = new VBOManager(vbo[0]);

        FloatBuffer verts = vm.genBuffer();
        g.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]);
        g.glBufferData(GL4.GL_ARRAY_BUFFER, verts.limit() * 4, verts, GL4.GL_STATIC_DRAW);

        // vertex array for the blocks
        g.glBindVertexArray(vao[0]);
        blockProg = new ModelRenderProgram(g, customTextures[0]);
        // vertex array for the menues
        g.glBindVertexArray(vao[1]);
        UIprog = new UIRenderProgram(g, customTextures[1]);
        // vao for animated models
        g.glBindVertexArray(vao[2]);
        animProg = new AnimatedModelRenderProgram(g, customTextures[2]);

    }

    void clear() {
        g.glClear(GL4.GL_COLOR_BUFFER_BIT);
        g.glClear(GL4.GL_DEPTH_BUFFER_BIT);
    }

    void readyBlockProg() {
        // vao0 is for the 3d environment renderer
        g.glBindVertexArray(vao[0]);
        currentProgram = blockProg;
        lastModelProgram = blockProg;
        blockProg.ready(g);
        blockProg.resetUniforms(this);

    }

    void readyUIProg() {
        // vertex array for the menues
        g.glBindVertexArray(vao[1]);
        currentProgram = UIprog;
        UIprog.ready(g);
        UIprog.resetUniforms(this);

    }

    void readyAnimProg() {

        g.glBindVertexArray(vao[2]);
        currentProgram = animProg;
        lastModelProgram = animProg;
        animProg.ready(g);
        animProg.resetUniforms(this);

    }

    void drawMenuFace() {
        vt = vm.getVBO(VBOManager.CUBE_TOP);
        g.glDrawArrays(vt.vertexPattern, vt.start / vm.VERT_SIZE, vt.length);
    }

    // x, y, width and heiht are all 0-1 proportions of the screen space
    void drawMenuFace(float x, float y, float width, float height, int assetID) {

        temp.loadIdentity();

        // convert between 0-1 screen coords and opengl coords
        temp.translate((x * 2) - 1, (y * 2) + 1, 0);
        // scale from 0-1 proportion of the screen to opengl coords
        temp.scale(width * 2, height * 2, 1);

        UIprog.setPV(g, temp);

        g.glActiveTexture(GL4.GL_TEXTURE0);
        g.glBindTexture(GL4.GL_TEXTURE_2D, Assets.joglTexLocs[assetID]);
        vt = vm.getVBO(VBOManager.CUBE_TOP);
        g.glDrawArrays(vt.vertexPattern, vt.start / vm.VERT_SIZE, vt.length);
    }

    void drawMenuFace(float x, float y, float width, float height, int assetID, float r, float g, float b, float a) {

        currentProgram.setHighlightColor(this.g, r, g, b, a);

        temp.loadIdentity();

        // convert between 0-1 screen coords and opengl coords
        temp.translate((x * 2) - 1, (y * 2) + 1, 0);
        // scale from 0-1 proportion of the screen to opengl coords
        temp.scale(width * 1.75f, height * 1.75f, 1);

        currentProgram.setPV(this.g, temp);
        this.g.glActiveTexture(GL4.GL_TEXTURE0);
        this.g.glBindTexture(GL4.GL_TEXTURE_2D, assetID);
        vt = vm.getVBO(VBOManager.CUBE_TOP);
        this.g.glDrawArrays(vt.vertexPattern, vt.start / vm.VERT_SIZE, vt.length);
        currentProgram.setHighlightColor(this.g, 0, 0, 0, 0);
    }

    void drawCube(boolean[] faces) {

        iter = 0;
        // g.glActiveTexture(GL4.GL_TEXTURE1);
        // g.glBindTexture(GL4.GL_TEXTURE_2D, Assets.joglTexLocs[Assets.DIRT]);
        while (iter < 6) {
            if (faces[iter]) {

                vt = vm.getVBO(iter);
                g.glDrawArrays(vt.vertexPattern, vt.start / vm.VERT_SIZE, vt.length);
            }
            iter++;
        }
    }

    void drawCube(boolean[] faces, int texID) {
        g.glBindTexture(GL4.GL_TEXTURE_2D, Assets.joglTexLocs[texID]);
        drawCube(faces);
    }

    void drawMesh(int assetID) {
        vt = vm.getVBO(assetID);
        g.glBindTexture(GL4.GL_TEXTURE_2D, 0);
        g.glDrawArrays(vt.vertexPattern, vt.start / vm.VERT_SIZE, vt.length);
    }

    void drawMesh(int assetID, int texID) {
        vt = vm.getVBO(assetID);
        g.glBindTexture(GL4.GL_TEXTURE_2D, Assets.joglTexLocs[texID]);
        g.glDrawArrays(vt.vertexPattern, vt.start / vm.VERT_SIZE, vt.length);
    }

    void setDrawLoc(float x, float y, float z) {

        currentProgram.setDrawOffset(g, x + cam.xoff, y + cam.yoff, z + cam.zoff);
    }

    void setCustDrawLoc(float x, float y, float z) {

        currentProgram.setDrawOffset(g, x, y, z);
    }

    void setColorLoc(float red, float green, float blue, float alpha) {

        currentProgram.setHighlightColor(g, red, green, blue, alpha);
    }

    void setPV(Matrix4 pv) {
        currentProgram.setPV(g, pv);
    }

    void setBones(Matrix4[] bones) {
        animProg.setBones(this.g, bones);
    }

    void setModelForm(Matrix4 transform) {
        lastModelProgram.setModelForm(g, transform);
    }

    void setLightDir(int x, int y, int z) {
        lastModelProgram.setSun(g, x, y, z);
    }

    float getDepthAt(int x, int y) {
        FloatBuffer buffer = FloatBuffer.allocate(4);
        g.glReadPixels(x, y, 1, 1, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, buffer);
        return buffer.get(0);
    }

    // buffer must have at least 4 bytes of memory allocated
    float getDepthAt(int x, int y, FloatBuffer buffer) {
        g.glReadPixels(x, y, 1, 1, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, buffer);
        return buffer.get(0);
    }

    private void shadowPass(GL4 g) {

        g.glUseProgram(shadow_shader);

        g.glBindVertexArray(vao[2]);

        g.glBindFramebuffer(GL4.GL_FRAMEBUFFER, customBuffers[2]);
        g.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, customTextures[2], 0);
        // disable drawing colors, but enable the depth computation
        g.glDrawBuffer(GL4.GL_NONE);
        g.glEnable(GL4.GL_DEPTH_TEST);

        // pass in light POV mat4 as uniform
        sunPV.loadIdentity();
        sunPV.multMatrix(cam.projection);
        sunPV.multMatrix(sunV);

        g.glUniformMatrix4fv(shLightCamLoc, 1, false, sunPV.getMatrix(), 0);

        // draw calls almost as normal
        g.glEnable(GL4.GL_DEPTH_TEST);
        g.glDepthFunc(GL4.GL_LEQUAL);

    }

    // void raycastMouse(GL4 g, int mouseX, int mouseY) {

    // float[] coords1 = cam.screenToWorld(mouseX, mouseY);

    // float[] norm = { 0, 0, 3, 1 };
    // float[] normout = new float[4];
    // cam.view.multVec(norm, normout);
    // Vec3 znorm = new Vec3(normout[0], normout[1], normout[2]);
    // znorm.normalise();
    // Plane plane = new Plane(znorm);

    // Line3 mouseRay = new Line3(new Vec3(coords1[0], coords1[1],
    // -Camera.CLIP_DEPTH),
    // new Vec3(coords1[0], coords1[1], Camera.CLIP_DEPTH));

    // Vec3 intersect = plane.getIntersect(mouseRay);

    // g.glUniformMatrix4fv(pvLoc, 1, false, cam.getCamera(), 0);
    // g.glUniform3f(gridLoc, intersect.x, intersect.y, intersect.z);

    // Matrix4 camInv = new Matrix4();
    // camInv.loadIdentity();
    // camInv.multMatrix(cam.getCamera());
    // camInv.invert();
    // normout[0] = intersect.x;
    // normout[1] = intersect.y;
    // normout[2] = intersect.z;
    // camInv.multVec(normout, norm);

    // norm[0] = (float) Math.floor(norm[0]);
    // norm[1] = (float) Math.floor(norm[1]);
    // // norm[2] = (float)Math.floor(norm[2]/20);
    // norm[2] = 0;

    // g.glUniform3f(gridLoc, norm[0], norm[1], norm[2]);
    // // drawSingleCube(g);

    // }

    void drawCube() {
        g.glBindTexture(GL4.GL_TEXTURE_2D, 0);

        VBO v;
        v = vm.getVBO(VBOManager.CUBE_TOP);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_BOT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_LEFT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_RIGHT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_FRONT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_BACK);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);
    }

    void drawCube(int texID) {

        g.glBindTexture(GL4.GL_TEXTURE_2D, Assets.joglTexLocs[texID]);

        VBO v;
        v = vm.getVBO(VBOManager.CUBE_TOP);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_BOT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_LEFT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_RIGHT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_FRONT);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);

        v = vm.getVBO(VBOManager.CUBE_BACK);
        g.glDrawArrays(v.vertexPattern, v.start / vm.VERT_SIZE, v.length);
    }

    void initShadowProg(GL4 g) {
        // set configurations for vao[1], shadow texture program
        g.glBindVertexArray(vao[2]);

        g.glActiveTexture(GL4.GL_TEXTURE0);
        g.glBindTexture(GL4.GL_TEXTURE_2D, customTextures[2]);
        g.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_DEPTH_COMPONENT32, 0, 0, 0,
                GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, null);

        g.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
        g.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
        g.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_COMPARE_MODE, GL4.GL_COMPARE_REF_TO_TEXTURE);
        g.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_COMPARE_FUNC, GL4.GL_LEQUAL);

        g.glBindFramebuffer(GL4.GL_FRAMEBUFFER, customBuffers[2]);
        g.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, customTextures[2], 0);
        g.glDrawBuffer(GL4.GL_NONE);
        g.glEnable(GL4.GL_DEPTH_TEST);

        g.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]);
        // configure pointer for position
        g.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 24, 0);
        g.glEnableVertexAttribArray(0);
    }

}