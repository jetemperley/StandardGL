import javax.swing.JFrame;
import javax.swing.OverlayLayout;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

public class StandardWindow{

    JFrame frame;
    StandardGLCanvas canvas;

    StandardWindow(){
        this("window", 200, 200);
    }

    StandardWindow(String title){

        this(title, 200, 200);
    }

    StandardWindow(int x, int y){
        this("window", 200, 200);
    }

    StandardWindow(String name, int x, int y){
        frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(x, y);
        frame.getContentPane().setLayout(new OverlayLayout(frame.getContentPane()));
    }

    public void setCanvas(StandardGLCanvas canvas){
        this.canvas = canvas;
        frame.getContentPane().add(canvas);
    }


}