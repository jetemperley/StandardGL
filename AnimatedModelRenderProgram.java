import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.*;

class AnimatedModelRenderProgram extends ModelRenderProgram {

    int bonesLoc;

    AnimatedModelRenderProgram(GL4 g, int targetTexture) {
        setTexTarget(targetTexture);
        super.make(g, "anim_vert_shader.glsl", "anim_frag_shader.glsl");
        initUniforms(g);
        initVertAttributes(g);

        g.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, textureID, 0);
        g.glDrawBuffer(GL4.GL_FRONT);

    }

    void initUniforms(GL4 g) {
        super.initUniforms(g);

        // bonesLoc = new int[20];
        // for (int i = 0; i < bonesLoc.length; i++) {
        bonesLoc = g.glGetUniformLocation(ID, "bones");
        // System.out.println("loc " + bonesLoc[i]);
        // }

        // get locations for all the uniform variables
        sunDirLoc = g.glGetUniformLocation(ID, "sun_dir");
        // lightCamLoc = g.glGetUniformLocation(ID, "light_cam");
        ambLightLoc = g.glGetUniformLocation(ID, "ambientLight");
    }

    void ready(GL4 g) {
        g.glUseProgram(ID);

        g.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, textureID, 0);
        g.glDrawBuffer(GL4.GL_FRONT);

        g.glEnable(GL4.GL_DEPTH_TEST);
        g.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
        g.glEnable(GL4.GL_BLEND);
        g.glDepthFunc(GL4.GL_LEQUAL);
        g.glPolygonMode(GL4.GL_FRONT_AND_BACK, GL4.GL_TRIANGLES);
    }

    void setBones(GL4 g, Matrix4[] bones) {
        // System.out.println(bones.length +" bones set");
        for (int i = 0; i < bones.length; i++) {
            // bonesBuffer[i].put(bones.getMatrix());
            g.glUniformMatrix4fv(bonesLoc + i, 1, false, bones[i].getMatrix(), 0);
        }
    }

    void setSun(GL4 g, float x, float y, float z) {
        g.glUniform3f(sunDirLoc, x, y, z);
    }

    void setAmbientLight(GL4 g, float red, float green, float blue) {
        g.glUniform3f(ambLightLoc, red, green, blue);
    }

    void resetUniforms(GLGraphics g) {
        super.resetUniforms(g);
        setAmbientLight(g.g, g.ambLight.x, g.ambLight.y, g.ambLight.z);
        setBones(g.g, new Matrix4[] { new Matrix4() });
        setSun(g.g, g.sun.x, g.sun.y, g.sun.z);

    }

}