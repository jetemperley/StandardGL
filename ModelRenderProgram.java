import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.*;

public class ModelRenderProgram extends GLProgram {

    // rendering program locs
    int sunDirLoc, lightCamLoc, ambLightLoc;

    int modelFormLoc;

    ModelRenderProgram() {
    }

    ModelRenderProgram(GL4 g, int targetTexture) {
        // create the program
        setTexTarget(targetTexture);
        super.make(g, "vert_shader.glsl", "frag_shader.glsl");

        // set up the custom depth buffer
        // g.glBindFramebuffer(GL4.GL_FRAMEBUFFER, customBuffers[0]);
        g.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, textureID, 0);
        g.glDrawBuffer(GL4.GL_FRONT);

        initUniforms(g);
        initVertAttributes(g);
    }

    void initUniforms(GL4 g) {
        super.initUniforms(g);
        // get locations for all the uniform variables
        sunDirLoc = g.glGetUniformLocation(ID, "sun_dir");
        // lightCamLoc = g.glGetUniformLocation(ID, "light_cam");
        ambLightLoc = g.glGetUniformLocation(ID, "ambientLight");
        modelFormLoc = g.glGetUniformLocation(ID, "model_form");

    }

    void ready(GL4 g) {

        g.glUseProgram(ID);

        g.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, textureID, 0);
        g.glDrawBuffer(GL4.GL_FRONT);

        g.glEnable(GL4.GL_DEPTH_TEST);
        g.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
        g.glEnable(GL4.GL_BLEND);
        g.glDepthFunc(GL4.GL_LEQUAL);
        // g.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_TRIANGLES);
        // g.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
        // g.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
        // g.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_EDGE);

        // lightCam2.loadIdentity();
        // lightCam2.multMatrix(transOffset);
        // lightCam2.multMatrix(cam.projection);
        // lightCam2.multMatrix(sunV);
        // g.glUniformMatrix4fv(lightCamLoc, 1, false, sunPV.getMatrix(), 0);

        // sModel.loadIdentity();
        // sModel.scale(3, 3, 3);
        // sModel.multMatrix(cam.camera);
        // g.glUniformMatrix4fv(pvLoc, 1, false, sModel.getMatrix(), 0);

        // VBO v;
        // v = vm.getVBO(VBOManager.DIAMOND);
        // g.glDrawArrays(v.vertexPattern, v.start/6, v.length);

    }

    void setSun(GL4 g, float x, float y, float z) {
        g.glUniform3f(sunDirLoc, x, y, z);
    }

    void setAmbientLight(GL4 g, float red, float green, float blue) {
        g.glUniform3f(ambLightLoc, red, green, blue);
    }

    void setModelForm(GL4 g, Matrix4 modelForm) {
        g.glUniformMatrix4fv(modelFormLoc, 1, false, modelForm.getMatrix(), 0);
    }

    void resetUniforms(GLGraphics g) {
        super.resetUniforms(g);
        setAmbientLight(g.g, g.ambLight.x, g.ambLight.y, g.ambLight.z);
        setSun(g.g, g.sun.x, g.sun.y, g.sun.z);
        setModelForm(g.g, new Matrix4());
    }
}