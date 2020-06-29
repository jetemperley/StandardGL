
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;

class StandardGLCanvas extends GLCanvas {

    Drawable env;
    GLGraphics graphics;

    StandardGLCanvas(Drawable env, GLGraphics graphics) {
        this.env = env;
        this.graphics = graphics;
    }

    
    public void init(GLAutoDrawable arg0) {
        GL4 g = (GL4) GLContext.getCurrentGL();
        // graphics = new GLGraphics(g);
    }

    // call this.display() from a loop or timer to trigger display(drawable)
    // should occur every frame

    
    public void display(GLAutoDrawable drawable) {

        // draw things
        if (env != null)
            env.draw(graphics);
    }

}