

public class Vec3{
    float x, y, z;
    
    public Vec3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vec3(float[] f){
        this(f[0], f[1], f[2]);
    }

    Vec3(){
        this(0, 0, 0);
    }

    Vec3(Vec3 vec){
        this(vec.x, vec.y, vec.z);
    }

    void add(Vec3 vec){
        x += vec.x;
        y += vec.y;
        z += vec.z;
    }

    float dot(Vec3 vec){
        float sum = (x*vec.x) +(y*vec.y) + (z*vec.z);
        return sum;        
    }

    float mag(){
        float mag = (float)Math.sqrt(x*x + y*y + z*z);
        return mag;
    }

    

    // this might be wrong ******
    void mult(float scalar){
        x=x*scalar;
        y=y*scalar;
        z=z*scalar;

    }

    float[] getFloat(){
        float[] f = new float[3];
        f[0] = x;
        f[1] = y;
        f[2] = z;
        return f;
    }

    void normalise(){
        float mag = mag();
        x = x/mag;
        y = y/mag; 
        z = z/mag;
    }

    static Vec3 mult(float scalar, Vec3 vec){
        Vec3 out = new Vec3(vec);
        out.mult(scalar);
        return out;
    }

    static Vec3 normalise(Vec3 vec){
        float mag = vec.mag();
        Vec3 unit = new Vec3(vec.x/mag, vec.y/mag, vec.z/mag);
        return unit;
    }

    static Vec3 scale(Vec3 vec, float t){
        Vec3 scaled = Vec3.normalise(vec);
        scaled.mult(t);
        return scaled;
    }

    static float dist(Vec3 a, Vec3 b){
        return (float)Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y) + (a.z-b.z)*(a.z-b.z));
    }

    static float simpleDist(Vec3 a, Vec3 b){
        return ((a.x-b.x)*(a.x-b.x)) + ((a.y-b.y)*(a.y-b.y)) + ((a.z-b.z)*(a.z-b.z));
    }

    static float simpleDist(float ax, float ay, float az, Vec3 b){
        return ((ax-b.x)*(ax-b.x)) + ((ay-b.y)*(ay-b.y)) + ((az-b.z)*(az-b.z));
    }

    static float simpleDist(float ax, float ay, float az, float bx, float by, float bz){
        return ((ax-bx)*(ax-bx)) + ((ay-by)*(ay-by)) + ((az-bz)*(az-bz));
    }

    static float dist(float ax, float ay, float az, float bx, float by, float bz){
        return (float)Math.sqrt(((ax-bx)*(ax-bx)) + ((ay-by)*(ay-by)) + ((az-bz)*(az-bz)));
    }
    

    

}