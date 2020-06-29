
import com.jogamp.opengl.math.Matrix4;

// holds the vertecies for cube faces in GL_TRIANGLE_STRIP form
// assumes z + up, and camera facing + y
public class GeoVerts {

    public static float[] getDiamondVerts() {

        Vec3 n = new Vec3(0, -1, 1);
        n.normalise();

        float[] v1 = { 0, 0, 0.5f, 1, n.x, n.y, n.z, 1,

                -0.5f, -0.5f, 0, 1, n.x, n.y, n.z, 1,

                -0.5f, 0.5f, 0, 1, n.x, n.y, n.z, 1 };

        // 4 vals * (3 verts + 3 norms) * 8 faces
        float[] v2 = new float[4 * 6 * 8];

        for (int i = 0; i < v1.length; i++) {
            v2[i] = v1[i];
        }

        // rotation for top half of shape
        Matrix4 m = new Matrix4();
        m.loadIdentity();
        m.rotate((float) Math.PI / 2, 0, 0, 1);

        // outre loop represents each face rotated for the top half
        for (int i = 0; i < 3; i++) {
            // inner loop represents each vertex
            for (int j = 0; j < 6; j++) {

                // 12 values per face (+12 norms = 24), 4 per vertex
                m.multVec(v2, (i * 24) + (j * 4), v2, ((i + 1) * 24) + (j * 4));
            }
        }

        // invert single face
        m.loadIdentity();
        m.rotate((float) Math.PI, 1, 0, 0);

        for (int i = 0; i < 6; i++) {
            m.multVec(v2, i * 4, v2, (24 * 4) + (i * 4));
        }

        m.loadIdentity();
        m.rotate((float) Math.PI / 2, 0, 0, 1);

        // outre loop represents each face rotated for the top half
        for (int i = 0; i < 3; i++) {
            // inner loop represents each vertex
            for (int j = 0; j < 6; j++) {

                // 12 values per face, 4 per vertex
                m.multVec(v2, ((i + 4) * 24) + (j * 4), v2, ((i + 5) * 24) + (j * 4));
            }
        }

        // remove 1s, 3 vals * ( 3 verts + 3 norms) * 8 faces
        float[] out = new float[3 * 6 * 8];

        int j = 0;
        for (int i = 0; i < v2.length; i += 4) {
            out[j] = v2[i];
            out[j + 1] = v2[i + 1];
            out[j + 2] = v2[i + 2];
            j += 3;
        }

        return out;
    }

    static float[] transformSet(float[] set, Matrix4 mat) {
        float[] out = new float[set.length];
        for (int i = 0; i < set.length; i+=4){
            mat.multVec(set, i, out, i);
        }

        return out;
    }

    static float[] removeEvery4th(float[] v){
        float[] out = new float[v.length*3/4];
        int j = 0;
        for (int i = 0; i < v.length; i += 4) {
            out[j] = v[i];
            out[j + 1] = v[i + 1];
            out[j + 2] = v[i + 2];
            j += 3;
        }
        return out;
    }

    // unused
    static float[] getFaceTexCoords(){

        float[] arr = {
            1, 1,
            0, 1,
            1, 0,
            0, 0

        };
        return arr;

    }

    // coord and norm, 4, 4
    private static float[] getTopFaceVerts() {

        float[] arr = { 
            -0.5f, 0.5f, 0.5f, 1,
            0, 0, 1, 1,
            0.5f, 0.5f, 0.5f, 1,
            0, 0, 1, 1,
            -0.5f, -0.5f, 0.5f, 1,
            0, 0, 1, 1,
            0.5f, -0.5f, 0.5f, 1,
            0, 0, 1, 1 
        };
        return arr;

    }

    public static float[] getBotVerts() {
        Matrix4 mat = new Matrix4();
        mat.loadIdentity();
        mat.rotate((float)Math.PI, 1, 0, 0);

        float[] arr = transformSet(getTopFaceVerts(), mat);
        arr = removeEvery4th(arr);
        float[] f = addCubeTexCoords(arr, 6);
        f = addBlankBoneWeights(f, 8);
        return f;
    }

    static float[] getTopVerts() {
        float[] arr = removeEvery4th(getTopFaceVerts());
        float[] f = addCubeTexCoords(arr, 6);
        f = addBlankBoneWeights(f, 8);
        return f;
    }

    static float[] getFrontVerts() {
        Matrix4 mat = new Matrix4();
        mat.loadIdentity();
        mat.rotate((float)Math.PI/2, 1, 0, 0);

        float[] arr = transformSet(getTopFaceVerts(), mat);
        arr = removeEvery4th(arr);
        float[] f = addCubeTexCoords(arr, 6);
        f = addBlankBoneWeights(f, 8);
        return f;
    }

    static float[] getBackVerts() {
        Matrix4 mat = new Matrix4();
        mat.loadIdentity();
        mat.rotate((float)-Math.PI/2, 1, 0, 0);

        float[] arr = transformSet(getTopFaceVerts(), mat);
        arr = removeEvery4th(arr);
        float[] f = addCubeTexCoords(arr, 6);
        f = addBlankBoneWeights(f, 8);
        return f;
    }

    static float[] getLeftVerts() {
        Matrix4 mat = new Matrix4();
        mat.loadIdentity();
        mat.rotate((float)-Math.PI/2, 0, 1, 0);

        float[] arr = transformSet(getTopFaceVerts(), mat);
        arr = removeEvery4th(arr);
        float[] f = addCubeTexCoords(arr, 6);
        f = addBlankBoneWeights(f, 8);
        return f;
    }

    static float[] getRightVerts() {
        Matrix4 mat = new Matrix4();
        mat.loadIdentity();
        mat.rotate((float)Math.PI/2, 0, 1, 0);

        float[] arr = transformSet(getTopFaceVerts(), mat);
        arr = removeEvery4th(arr);
        float[] f = addCubeTexCoords(arr, 6);
        f = addBlankBoneWeights(f, 8);
        return f;
    }

    // adds six 0s every stride (three 0s for bones and three 0s for weights)
    // TODO make each cubes first boneID 0 and weight 1
    static float[] addBlankBoneWeights(float[] data, int stride){

        int n = data.length/stride;
        float[] arr = new float[n*(stride+6)];
        int k = 0;
        for (int i = 0; i < n; i++){
            for (int j = 0; j < stride;j++){
                arr[i*stride + j + k] = data[i*stride + j];
            }
            for (int j = 0 ; j < 6; j++){
                arr[(i+1)*stride + j + k] = 0;
            }
            arr[(i+1)*stride + k + 3] = 1.0f;
            k+=6;
            
        }
        
        return arr;
    }

    static float[] interleave(float[] verts, float[] normals){

        float[] combined = new float[verts.length + normals.length];
        for (int i = 0; i < verts.length; i+=3){
            combined[i*2] = verts[i];
            combined[i*2+1] = verts[i+1];
            combined[i*2+2] = verts[i+2];

            combined[i*2+3] = normals[i];
            combined[i*2+4] = normals[i+1];
            combined[i*2+5] = normals[i+2];

        }

        return combined;
    }

    // precondition: a.length/asize == b.length/bsize == c.length/csize || each arrays elements are represented in each other array 1:1

    static float[] interleave(float[] a, int asize, float[] b, int bsize, float[] c, int csize){
        
        float[] out = new float[a.length + b.length + c.length];
        int elements = a.length/asize;
        int size = asize + bsize + csize;

        for (int i = 0; i < elements; i++){
            for (int j = 0; j < asize; j++){
                out[i*size + j] = a[i*asize + j];
            }
            for (int j = 0; j < bsize; j++){
                out[i*size + j + asize] = b[i*bsize + j];

            }
            for (int j = 0; j < csize; j++){
                out[i*size + j + asize + bsize] = c[i*csize + j];
            }
        }


        return out;
        
    }

    // creates a new array by insterting bsize elements from b into every asize elements of a
    // that is, if asize = 4, and bsize = 3
    // returns a, a, a, a, b, b, b, a, a, a, a, b, b, b, a, a, a, a...
    // requires that a.length/asize == b.length/bsize
    static float[] interleave(float[] a, int asize, float[] b, int bsize){
        // System.out.println("interleave Ratio A: " + (a.length/asize));
        // System.out.println("interleave Ratio B: " + (b.length/bsize));
        float[] arr = new float[a.length + b.length];

        int k = 0;
        for (int i = 0; i < a.length; i += asize){
            for (int j = 0; j < asize; j++){
                arr[i + j + k] = a[i + j]; 
            }
            for (int j = 0; j < bsize; j++){
                arr[i + j + k] = b[k + j];
            }
            k+=bsize;
        }
        
        return arr;
    }

    static float[] interleave2(float[] a, int asize, float[] b, int bsize){
        // System.out.println("interleave Ratio A: " + (a.length/asize));
        // System.out.println("interleave Ratio B: " + (b.length/bsize));
        float[] arr = new float[a.length + b.length];

        int n = a.length/asize;
        for (int i = 0; i < n; i++){
            for(int j = 0; j < asize; j++){
                arr[i*(asize+bsize) + j] = a[i*asize + j];
                // System.out.print(arr[i*(asize+bsize) + j] + "_");
            }
            for(int j = 0; j < bsize; j++){
                arr[i*(asize+bsize) + asize + j] = b[i*bsize + j];
            }
        }
        
        return arr;
    }


    static float[] addCubeTexCoords(float[] verts, int stride){

        // System.out.println("verts " + (verts.length/6));

        float[] v2 = new float[verts.length + 8];
        float[] tc = {
            0, 1,
            1, 1,
            0, 0,
            1, 0
        };

        int j = 0;
        for (int i = 0; i < verts.length; i+=stride){

            for (int k = 0; k < stride; k++){
                v2[i + k + j] = verts[i + k];
            }
            j+=2;
        }

        j = 0;
        for (int i = stride; i < v2.length; i +=stride+2){
            v2[i] = tc[j];
            v2[i+1] = tc[j+1];
            j+=2;
        }

        return v2;
    }
}