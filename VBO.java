import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.awt.*;

public class VBO{

    // start: the location first INDEX in the buffer
    // length: the number of VERTECIES SPECIFIED in the sequence 
    // vbo: a reference to the opengl vbo ID that stores the data  
    // NOTE cuurently using dublets
    final int start, length, vertexPattern;

    VBO(int s, int l){
        this(s, l, GL4.GL_TRIANGLES);
    }

    VBO(int s, int l, int vertexPattern){
        start = s;
        length = l;
        this.vertexPattern = vertexPattern;
    }

}