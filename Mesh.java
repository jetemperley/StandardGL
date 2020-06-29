
import java.util.*;

import com.jogamp.opengl.math.*;

class Mesh {

    float[] verts;
    // ArrayList<Animation> anims;
    Animation anim;
    Skeleton skel;

    Mesh(String fileName) {
        initData(XmlParser.loadXmlFile(fileName));
    }

    void initData(XmlNode mainNode) {

        float[] verts = extractVerticiesFromXML(mainNode);
        float[] norms = extractTrianglesFromXML(mainNode, "NORMAL");
        float[] texs = extractTrianglesFromXML(mainNode, "TEXCOORD");
        int[] idx = extractIndecies(mainNode);

        int tris = Integer.parseInt(mainNode.getChild("library_geometries").getChild("geometry").getChild("mesh")
                .getChild("triangles").getAttribute("count"));

        // 3 floats *3 points per triangle
        // 2 tcs * 3 points per tri
        float[] expVerts = new float[tris * 9];
        float[] expNorms = new float[tris * 9];
        float[] expTexs = new float[tris * 6];
        int j = 0;
        int t = 0;
        for (int i = 0; i < idx.length; i += 3) {
            for (int k = 0; k < 3; k++) {
                expVerts[j] = verts[(idx[i] * 3) + k];
                expNorms[j] = norms[(idx[i + 1] * 3) + k];
                j++;
            }
            for (int k = 0; k < 2; k++) {
                expTexs[t] = texs[idx[i + 2] * 2 + k];
                t++;
            }
        }

        float[] expanded = GeoVerts.interleave(expVerts, 3, expNorms, 3, expTexs, 2);

        if (mainNode.getChild("library_controllers") != null) {

            // Mat4Utl.writeMat4(new Matrix4());

            float[] v, vcount, weights, boneWeights;
            // vcount points to the number of bone ids & weights per positions in v
            weights = extractVertexWeights(mainNode);
            // System.out.println(weights);
            vcount = extractVCount(mainNode);
            v = extractVs(mainNode);
            boneWeights = extractBonesAndWeights(vcount, v, weights);

            float[] expBw = new float[tris * 6 * 3];
            int w = 0;
            for (int i = 0; i < idx.length; i += 3) {
                for (int q = 0; q < 6; q++) {
                    expBw[w] = boneWeights[(idx[i] * 6) + q];
                    // System.out.println(expBw[w] + " ");
                    w++;
                }
            }
            expanded = GeoVerts.interleave2(expanded, 8, expBw, 6);

            skel = new Skeleton(mainNode);

            if (mainNode.getChild("library_animations") != null) {
                // add aminations to list
            }
            anim = new Animation(skel.boneNames.length);

        } else {
            expanded = GeoVerts.addBlankBoneWeights(expanded, 8);
        }

        this.verts = expanded;
    }

    static float[] extractVerticiesFromXML(XmlNode mainNode) {
        String objectID = mainNode.getChild("library_geometries").getChild("geometry").getChild("mesh")
                .getChild("vertices").getChild("input").getAttribute("source").substring(1);

        String[] tempPos = mainNode.getChild("library_geometries").getChild("geometry").getChild("mesh")
                .getChildWithAttribute("source", "id", objectID).getChild("float_array").getData().split(" ");

        float[] pos = new float[tempPos.length];
        for (int i = 0; i < tempPos.length; i++) {
            pos[i] = Float.parseFloat(tempPos[i]);
            // System.out.print(pos[i] + " ");
        }
        // System.out.println();
        return pos;

    }

    static float[] extractTrianglesFromXML(XmlNode mainNode, String semantic) {
        String objectID = mainNode.getChild("library_geometries").getChild("geometry").getChild("mesh")
                .getChild("triangles").getChildWithAttribute("input", "semantic", semantic).getAttribute("source")
                .substring(1);

        String[] stringData = mainNode.getChild("library_geometries").getChild("geometry").getChild("mesh")
                .getChildWithAttribute("source", "id", objectID).getChild("float_array").getData().split(" ");

        float[] data = new float[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            data[i] = Float.parseFloat(stringData[i]);
            // System.out.print(pos[i] + " ");
        }
        // System.out.println();
        return data;

    }

    static float[] extractVCount(XmlNode mainNode) {
        String[] raw = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                .getChild("vertex_weights").getChild("vcount").getData().split(" ");
        float[] counts = new float[raw.length];
        for (int i = 0; i < raw.length; i++) {
            counts[i] = Float.parseFloat(raw[i]);
        }
        return counts;
    }

    // effective joints
    static float[] extractVs(XmlNode mainNode) {

        String[] rawData = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                .getChild("vertex_weights").getChild("v").getData().split(" ");
        float[] counts = new float[rawData.length];
        for (int i = 0; i < rawData.length; i++) {
            counts[i] = Float.parseFloat(rawData[i]);
        }
        return counts;
    }

    static int[] extractIndecies(XmlNode mainNode) {
        String[] idxString = mainNode.getChild("library_geometries").getChild("geometry").getChild("mesh")
                .getChild("triangles").getChild("p").getData().split(" ");
        int[] idx = new int[idxString.length];
        for (int i = 0; i < idx.length; i++) {
            idx[i] = Integer.parseInt(idxString[i]);
        }
        return idx;
    }

    static float[] extractVertexWeights(XmlNode mainNode) {
        String weightsID = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                .getChild("vertex_weights").getChildWithAttribute("input", "semantic", "WEIGHT").getAttribute("source")
                .substring(1);
        String[] raw = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                .getChildWithAttribute("source", "id", weightsID).getChild("float_array").getData().split(" ");
        float[] weights = new float[raw.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = Float.parseFloat(raw[i]);
            // System.out.println(weights[i] + " ");
        }
        return weights;
    }

    // TODO i dont know what i was thinking here but YOU NEED TO FIX THIS WHOLE
    // FUNCTION
    // vcount is the number of bones/weights that effect a verticy
    static float[] extractBonesAndWeights(float[] vcount, float[] v, float[] weights) {
        // bones is a jagged 2d array of boneIDs per position
        float[][] bones;
        bones = new float[vcount.length][];
        // wts is a jagged array of 0.0 - 1.0 bone weights that pairs with bones
        float[][] wts;
        wts = new float[vcount.length][];

        // loop through vi[] and make each boneID count a new array
        int k = 0;
        for (int i = 0; i < vcount.length; i++) {

            // add the jagged array section
            bones[i] = new float[(int) vcount[i]];
            wts[i] = new float[(int) vcount[i]];

            // assign elements from v into bones sequentially
            // assign the indexed weight using v & weights
            for (int j = 0; j < bones[i].length; j++) {
                bones[i][j] = v[k];
                wts[i][j] = weights[(int) v[k + 1]];
                k += 2;
            }
        }
        // System.out.println("v parsed " + k);

        // process the bone weights/boneIDs by
        // making sure they add to 1 if there are more than 3 bones
        for (int i = 0; i < wts.length; i++) {

            // if there are more influences than max bones (3)
            if (wts[i].length > 3) {

                // sort the weights
                boolean done = false;
                float n = wts[i].length;
                float tempf;
                float tempi;

                // System.out.println("weight IN");
                // for (int j = 0; j < wts[i].length; j++){
                // System.out.print(wts[i][j] + " ");
                // }
                // System.out.println();

                // sort
                while (!done) {

                    done = true;
                    for (int j = 0; j < n - 1; j++) {
                        if (wts[i][j] < wts[i][j + 1]) {

                            tempf = wts[i][j + 1];
                            wts[i][j + 1] = wts[i][j];
                            wts[i][j] = tempf;

                            tempi = bones[i][j + 1];
                            bones[i][j + 1] = bones[i][j];
                            bones[i][j] = tempi;

                            done = false;
                        }
                    }
                    n--;

                }

                // System.out.println("weight OUT");
                // for (int j = 0; j < wts[i].length; j++){
                // System.out.print(wts[i][j] + " ");
                // }
                // System.out.println();

                // cut off excess bones
                float[] tempWeight = new float[3];
                float[] tempBone = new float[3];
                float totalWeight = 0.0f;

                for (int j = 0; j < tempWeight.length; j++) {
                    tempWeight[j] = wts[i][j];
                    tempBone[j] = bones[i][j];
                    totalWeight += tempWeight[j];
                }

                // normalise remaining weights
                for (int j = 0; j < tempWeight.length; j++) {
                    tempWeight[j] = tempWeight[j] / totalWeight;
                }

                wts[i] = tempWeight;
                bones[i] = tempBone;

            } else if (wts[i].length < 3) {
                // if there are less than 3 bones, make the remaining 0.0
                float[] w = { 1.0f, 0.0f, 0.0f };
                float[] b = { 0.0f, 0.0f, 0.0f };

                for (int j = 0; j < wts[i].length; j++) {
                    w[j] = wts[i][j];
                    b[j] = bones[i][j];
                }

                wts[i] = w;
                bones[i] = b;
            }
            
            // for (int j = 0; j < wts[i].length; j++) {
            //     System.out.println("weight: " + wts[i][j] + " bone: " + bones[i][j]);
            // }
            // System.out.println();

        }

        // converts 2d array to a linear array
        float[] exb = new float[bones.length * 3];
        float[] exw = new float[wts.length * 3];
        for (int i = 0; i < bones.length; i++) {
            for (int j = 0; j < 3; j++) {

                exb[i * 3 + j] = bones[i][j];
                // `System.out.print(((int)exb[i * 3 + j]) + " ");
                exw[i * 3 + j] = wts[i][j];
                // System.out.print((exw[i * 3 + j]) + " ");

            }
            // System.out.println();
        }
        // interleave

        float[] arr = GeoVerts.interleave2(exb, 3, exw, 3);

        return arr;
    }

    class Animation {

        // the timestamp for each keyframe
        float[] frameTimes;
        // keyFrame[timeIndex][boneID] holds all the pose data for one animation
        // matricies are in model space, that is the matrix that transforms in
        // relation to the parent joint to the pose position
        Matrix4[][] keyFrames;

        Animation(int boneN) {

            // set-up generic equidistant timestamps & keyframes
            frameTimes = new float[] { 0, 0.5f, 1.0f, 1.5f, 2.0f };
            keyFrames = new Matrix4[frameTimes.length][boneN];

            // apply identity matrix to all keyframes (blank animation)
            for (int i = 0; i < keyFrames.length; i++) {
                for (int j = 0; j < keyFrames[i].length; j++) {

                    keyFrames[i][j] = new Matrix4();
                    keyFrames[i][j].multMatrix(skel.binds[j]);

                }
            }

            // 45 degree rotations around the x axis
            Matrix4 rotX = new Matrix4();
            Matrix4 rotNX = new Matrix4();
            rotX.rotate((float) Math.PI / 4, 1, 0, 0);
            rotNX.rotate(-(float) Math.PI / 4, 1, 0, 0);

            int legL = skel.getBoneID("Leg_L");
            int legR = skel.getBoneID("Leg_R");

            // apply roations to animation
            keyFrames[1][legL].multMatrix(rotNX);
            keyFrames[1][legR].multMatrix(rotX);

            keyFrames[3][legL].multMatrix(rotX);
            keyFrames[3][legR].multMatrix(rotNX);

        }

        

        // gets the pose based on the proportion of time passed
        Matrix4[] getPose(float proportion) {

            // get the two poses closest to the proportion
            // simple frame calculation based on NUMBER of frames but not length of frames
            // works only with animatioons whos frames are equal lengths, but a fast
            // calculation
            // UPDATE FOR FRAME LENGTH: proportions calculation need to take account of the
            // frameTime[]
            float frame = (proportion * (frameTimes.length - 1));
            // System.out.println(frame);

            // if the propotrion IS a keyframe
            // if (frame % 1.0f == 0) {
            if (true) {

                Matrix4[] bones = new Matrix4[skel.boneNames.length];
                for (int i = 0; i < bones.length; i++) {
                    bones[i] = new Matrix4();
                    // bones[i].multMatrix(skel.binds[i]);
                    bones[i].multMatrix(keyFrames[(int)frame][i]);
                }

                skel.bindsToModel(bones, 0);
                return bones;

            } else {
                // the proportion is between frames

                // work of the next and previous frames
                int pframe = (int)frame;
                int nframe = pframe+1;
                if (nframe >= keyFrames.length){
                    nframe = 0;
                }

                Matrix4[] keyFrame1, keyFrame2;
                keyFrame1 = keyFrames[pframe];
                keyFrame2 = keyFrames[nframe];

                // blend is proportion of time passed between the two closest frames (UPDATE FOR
                // FRAME LENGTH: use frameTime[])
                float frame1prop = (frame) / (float) (frameTimes.length - 1);
                float frame2prop = (frame + 1) / (float) (frameTimes.length - 1);
                float blend = (frame - frame1prop) / (frame2prop - frame1prop);

                Matrix4[] interBones = new Matrix4[keyFrame1.length];
                // for each of the bones in both poses:
                for (int i = 0; i < keyFrame1.length; i++) {
                    // interpolate
                    interBones[i] = Mat4Utl.interpolate(keyFrame1[i], blend, keyFrame2[i]);
                }

                // alter the interpolated pose from bind space to model space
                // by multiplying the parent transform and inverse binds
                // modeBind = parentBind * childBind * inverseBind
                skel.bindsToModel(interBones, 0);

                return interBones;
            }

        }

        // given a set of bind bones, runs through the skels boneGraph and
        // recursively calcs the model transforms
        // initialise with the root joint (0)
        // maybe pos = inv * parent * child??
        // joint * inverseBind * vertex

    }

    class Skeleton {

        String[] boneNames;
        // boneGraph[boneID][childrenID] is a jagged 2d array
        int[][] boneGraph;
        // parallel invBind[boneID] containing the inverse bind matricies,
        // relative to the models origin
        Matrix4[] invBind;
        Matrix4[] binds;

        Skeleton(XmlNode mainNode) {

            // get the id for inverse bind matrixs
            String bindMatsID = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                    .getChild("joints").getChildWithAttribute("input", "semantic", "INV_BIND_MATRIX")
                    .getAttribute("source").substring(1);
            // System.out.println(bindMatsID);
            // use the id to get the matrix data
            String[] raw = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                    .getChildWithAttribute("source", "id", bindMatsID).getChild("float_array").getData().split(" ");

            // turn matrix strings into floats
            float[] data = new float[raw.length];
            for (int i = 0; i < data.length; i++) {
                data[i] = Float.parseFloat(raw[i]);
            }

            // turn floats into matrix4
            float[] transposed = new float[data.length];
            int n = data.length / 16;
            invBind = new Matrix4[n];

            for (int i = 0; i < n; i++) {

                // FloatUtil.transposeMatrix(data, i * 16, transposed, i * 16);
                invBind[i] = new Matrix4();
                // invBind[i].multMatrix(transposed, i * 16);

            }

            // init the bone heirarcy and bind data
            String jointLocID = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                    .getChild("joints").getChildWithAttribute("input", "semantic", "JOINT").getAttribute("source")
                    .substring(1);

            this.boneNames = mainNode.getChild("library_controllers").getChild("controller").getChild("skin")
                    .getChildWithAttribute("source", "id", jointLocID).getChild("Name_array").getData().split(" ");
            
            for (int i = 0; i < boneNames.length; i++){
                // System.out.print(boneNames[i] + " ");
            }
            // System.out.println();

            XmlNode joints = mainNode.getChild("library_visual_scenes").getChild("visual_scene").getChild("node")
                    .getChildWithAttribute("node", "name", boneNames[0]);

            this.boneGraph = new int[boneNames.length][];
            this.binds = new Matrix4[boneNames.length];
            initBones(joints);

            // extra part for manually calculating invBinds
            Matrix4[] bindCopy = new Matrix4[boneNames.length];
            for (int i = 0; i < bindCopy.length; i++) {
                bindCopy[i] = new Matrix4();
                bindCopy[i].multMatrix(binds[i]);
            }
            bindsToModel(bindCopy, 0);
            for (int i = 0; i < bindCopy.length; i++) {
                bindCopy[i].invert();
                // Mat4Utl.writeMat4(bindCopy[i]);
            }
            invBind = bindCopy;
        }

        // initialise the boneGrapgh based on rootJoint and its kids
        // root joint is a <node> from
        // library_visual_scenes -> visual_scene -> armature
        void initBones(XmlNode rootJoint) {

            // int[][] boneGraph;
            // work out which jointID we are working with
            int boneID = getBoneID(rootJoint.getAttribute("sid"));

            // save its bind matrix
            String[] raw = rootJoint.getChild("matrix").getData().split(" ");
            float[] bind = new float[raw.length];

            for (int i = 0; i < bind.length; i++) {
                bind[i] = Float.parseFloat(raw[i]);
            }

            float[] tbind = new float[bind.length];
            FloatUtil.transposeMatrix(bind, tbind);

            this.binds[boneID] = new Matrix4();
            this.binds[boneID].multMatrix(tbind);
            // Mat4Utl.writeMat4(this.binds[boneID]);

            // get the children
            List<XmlNode> kids = rootJoint.getChildren("node");
            // System.out.println("inited bone " + boneID + ", kids = " + kids.size());

            // work out the IDs of each child and add the children to the graph
            this.boneGraph[boneID] = new int[kids.size()];
            for (int i = 0; i < kids.size(); i++) {
                this.boneGraph[boneID][i] = getBoneID(kids.get(i).getAttribute("sid"));

            }
            for (XmlNode kid : kids) {
                // initJoint() for each of the other children
                initBones(kid);
            }

            // function will recursively initilise the boneGraph

        }

        int getBoneID(String name) {
            for (int i = 0; i < boneNames.length; i++) {
                if (name.equals(boneNames[i])) {
                    return i;
                }
            }
            return -1;
        }

        void bindsToModel(Matrix4[] bones, int currentBone) {
            
            // if the bone has children
            for (int i = 0; i < this.boneGraph[currentBone].length; i++) {

                /*
                 * child = current * child
                 */
                bones[this.boneGraph[currentBone][i]] = Mat4Utl.mult(bones[currentBone],
                        bones[this.boneGraph[currentBone][i]]);

                // recursively run through tthis bones children
                bindsToModel(bones, this.boneGraph[currentBone][i]);
            }

            // append the inverse bind to the lefthand side
            bones[currentBone] = Mat4Utl.mult(this.invBind[currentBone], bones[currentBone]);
            // System.out.println("bone " + currentBone);
            // Mat4Utl.writeMat4(bones[currentBone]);

        }

    }

}