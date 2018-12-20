import processing.core.*;

public class ledDirectionDrawer {
	static void drawDirection(PVector[] ledNormals,LedColor[] _ledColors,  PVector _angle, float _size, LedColor color,LedColor.LedAlphaMode blendMode, float _blendOut){
	    
	    LedColor black=new LedColor(0,0,0,0);
	    LedColor pixelColor;
	    for(int i=0;i<_ledColors.length;i++){
          if(ledNormals[i].x>_angle.x&&ledNormals[i].x<_angle.x+_size&&ledNormals[i].y>_angle.y&&ledNormals[i].y<_angle.y+_size&&ledNormals[i].z>_angle.z&&ledNormals[i].z<_angle.z+_size){

                	  _ledColors[i].mixWithAlpha(color,blendMode,_blendOut); 	  

    	  }
	      else _ledColors[i].mixWithAlpha(black,LedColor.LedAlphaMode.MULTIPLY,_blendOut);
	    }
	  }
}
