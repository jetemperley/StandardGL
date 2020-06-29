import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.*;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.awt.Color;
import javax.imageio.ImageIO;

import java.util.*;

public class Assets {

    static ArrayList<Texture> texs;
    static ArrayList<Mesh> meshs;
    static int[] joglTexLocs;
    // texture asset IDs
    static int SMILE = 0, PICK = 1, SELECT = 2, WALK = 3, PLUS = 4, GRASS = 5, DIRT = 6, MAN_TEX = 7;
    static int CUBE_GRASS = 8;

    Assets(GL4 g) {
        texs = new ArrayList<Texture>();
        meshs = new ArrayList<Mesh>();
        texs.add(loadTexture("smile.png"));
        texs.add(loadTexture("pick.png"));
        texs.add(loadTexture("select.png"));
        texs.add(loadTexture("walk.png"));
        texs.add(loadTexture("plus.png"));
        texs.add(loadTexture("grass.png"));
        texs.add(loadTexture("dirt.png"));
        texs.add(loadTexture("man_tex.png"));
        texs.add(loadCubeMap("cube_grass"));

        // System.out.println("texs length " + texs.size());
        joglTexLocs = new int[texs.size()];

        for (int i = 0; i < joglTexLocs.length; i++) {
            joglTexLocs[i] = texs.get(i).getTextureObject(g);
            System.out.println(joglTexLocs[i]);

        }

        meshs.add(new Mesh("tree.dae"));
        meshs.add(new Mesh("rock.dae"));
        meshs.add(new Mesh("man.dae"));

        // float[] f = getData(XmlParser.loadXmlFile("tree.dae"));
        // meshs.add(new Mesh(f));
        // f = getData(XmlParser.loadXmlFile("rock.dae"));
        // meshs.add(new Mesh(f));
        // f = getData(XmlParser.loadXmlFile("man.dae"));
        // meshs.add(new Mesh(f));
        

    }

    static Texture loadTexture(String textureFileName) {
        Texture tex = null;
        try {
            tex = TextureIO.newTexture(new File(textureFileName), false);
        } catch (Exception e) {
            System.out.println("error on texture " + textureFileName);
            try {
                tex = TextureIO.newTexture(new File("sad.png"), false);
            } catch (Exception ei) {
                System.out.println("couldnt even load backup texture :(");
            }
            // e.printStackTrace();
        }
        return tex;
    }

    // loads the textures named top, bot left right front back in bin\name
    static Texture loadCubeMap(String name) {

        GL4 gl = (GL4) GLContext.getCurrentGL();
        GLProfile glp = gl.getGLProfile();
        Texture cubeMap = TextureIO.newTexture(GL4.GL_TEXTURE_CUBE_MAP);

        // System.out.println("estimated memory " + cubeMap.getEstimatedMemorySize());

        try {
            TextureData top = TextureIO.newTextureData(glp, new File(name + "/top.png"), false, "png");
            TextureData bot = TextureIO.newTextureData(glp, new File(name + "/bot.png"), false, "png");
            TextureData left = TextureIO.newTextureData(glp, new File(name + "/left.png"), false, "png");
            TextureData right = TextureIO.newTextureData(glp, new File(name + "/right.png"), false, "png");
            TextureData front = TextureIO.newTextureData(glp, new File(name + "/front.png"), false, "png");
            TextureData back = TextureIO.newTextureData(glp, new File(name + "/back.png"), false, "png");

            // System.out.println("front is null " + (top == null));

            cubeMap.updateImage(gl, right, GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
            cubeMap.updateImage(gl, left, GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
            cubeMap.updateImage(gl, top, GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
            cubeMap.updateImage(gl, bot, GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
            cubeMap.updateImage(gl, front, GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
            cubeMap.updateImage(gl, back, GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);

        } catch (Exception e) {
            System.out.println("failed load " + name + " cubemap");
        }

        // System.out.println("estimated memory " + cubeMap.getEstimatedMemorySize());

        // gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
        // gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
        // gl.glTexParameteri(GL4.GL_TEXTURE_CUBE_MAP, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_EDGE);

        return cubeMap;
    }

}