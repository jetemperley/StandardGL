import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.math.Matrix4;

public class Camera {
    
    Matrix4 view, projection, camera, temp;
    float xoff = 0, yoff = 0, zoff = 0;
    private float[] angles;

    // GRID_SIZE is the pixel size of one world unit length on the projection screen
    // assumes 1:1 aspect ratio
    public static float CLIP_EDGE = 20, GRID_SIZE, SCREEN_ASPECT = 1, CLIP_DEPTH = 50;
    private final float MIN_CLIP_EDGE = 0.5f, MAX_CLIP_EDGE = 40;

    public Camera() {
        
        // column major identity matrix
        view = new Matrix4();
        camera = new Matrix4();
        projection = new Matrix4();
        temp = new Matrix4();

        initView();
        projection.loadIdentity();
        projection.makeOrtho(-CLIP_EDGE, CLIP_EDGE, -CLIP_EDGE, CLIP_EDGE, CLIP_DEPTH, -CLIP_DEPTH);
        
        updateCamera();

        // GRID_SIZE = IsoEngine.WIDTH/(CLIP_EDGE*2);
        GRID_SIZE = 0; // ??
        // xoff = -World.WORLD_SIZE*World.Chunk.SIZE/2;
        // yoff = -World.WORLD_SIZE*World.Chunk.SIZE/2;

    }

    public void scale(float x, float y, float z){
        view.scale(x, y, z);
    }

    // orbit camera around z
    public void rotate(float f, float x, float y, float z){
        view.rotate(f, x, y, z);
        
        
        updateCamera();
    }

    Matrix4 getCamera(){
        updateCamera();
        temp.loadIdentity();
        temp.multMatrix(camera);
        // temp.translate(xoff, yoff, zoff);
        return temp;
    }

    Matrix4 getView(){
        temp.loadIdentity();
        temp.multMatrix(view);
        temp.translate(xoff, yoff, zoff);
        return temp;
    }

    Matrix4 getPVRotationMatrix(){
        temp.loadIdentity();
        temp.multMatrix(projection);
        temp.multMatrix(view);
        return temp;
    }

    public void elevate(float radians){
        angles = Mat4Utl.getRotations(view);
        // System.out.println("x = " + angles[0] + ", y = " + angles[1] + ", z = " + angles[2]);
        rotate(radians, (float)Math.cos((double)angles[2]), (float)Math.sin((double)angles[2]), 0);
        // System.out.println("x = " + angles[0] + ", y = " + angles[1] + ", z = " + angles[2]);
        angles = Mat4Utl.getRotations(view);

        if (angles[0] < 0 && angles[0] > -1.5f){
            rotate(angles[0], (float)Math.cos((double)angles[2]), (float)Math.sin((double)angles[2]), 0);
        } else if (angles[0] <= -1.5f){
            float d = angles[0] + (float)Math.PI;
            rotate(d, (float)Math.cos((double)angles[2]), (float)Math.sin((double)angles[2]), 0);
        }

    }

    // produces the rotation of the view in radians
    float[] getAngles(){
        // angles = Mat4Utl.getRotations(view);
        return angles.clone();
    }

    float getElevation(){
        
        angles = Mat4Utl.getRotations(view);
        //System.out.println("arcsin 0.5 " + Math.asin(0.5));
        return angles[0];
    }

    public void updateCamera(){

        camera.loadIdentity();
        camera.multMatrix(projection);
        camera.multMatrix(view);
        angles = Mat4Utl.getRotations(view);
        
    }

    void updateProjection(){
        projection.loadIdentity();
        projection.makeOrtho(-CLIP_EDGE, CLIP_EDGE, -CLIP_EDGE, CLIP_EDGE, CLIP_DEPTH, -CLIP_DEPTH);
    }

    // theta is the direction to move the camera in radians
    public void move(float radians, float dist){
        
        radians = (float)(radians*Math.PI);
        angles = Mat4Utl.getRotations(view);
        radians += angles[2];

        yoff += Math.sin((double)radians)*dist;
        xoff += Math.cos((double)radians)*dist;
    }


    void offset(float x, float y, float z){
        xoff += x;
        yoff += y;
        zoff += z;
    }

    public void initView(){
        view.loadIdentity();
        rotate((float)(3*Math.PI/8), 1, 0, 0);
        rotate((float)(Math.PI), 0, 1, 0);
        rotate((float)(1*Math.PI/4), 0, 0, 1);
    }

    // takes the integer pixel screen location of the mouse
    // returns the clip space? location on the projection plane   
    public float[] screenToWorld(int x, int y){

        float xloc = (x/(float)GRID_SIZE) - CLIP_EDGE;
        float yloc = (-y/(float)GRID_SIZE) + CLIP_EDGE;
        
        float[] out = {xloc, yloc, -CLIP_EDGE, 1};
        return out;

    }

    void zoom(int zoom){
        CLIP_EDGE+=zoom*0.5;
        if (CLIP_EDGE < MIN_CLIP_EDGE){
            CLIP_EDGE = MIN_CLIP_EDGE;
        } else if (CLIP_EDGE > MAX_CLIP_EDGE){
            CLIP_EDGE = MAX_CLIP_EDGE;
        }
        GRID_SIZE = 0; // ??
        updateProjection();
        updateCamera();
    }




}