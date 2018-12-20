import processing.core.*;

class LedSphereDrawer{
  static void drawSphere(PVector[] ledPositions,LedColor[] _ledColors,  PVector center,float outerRadius, float innerRadius, LedColor color,LedColor.LedAlphaMode blendMode, float _blendOut){
    float maxDistSquared=outerRadius*outerRadius;
    float minDistSquared=innerRadius*innerRadius;
    
    LedColor black=new LedColor(0,0,0,0);
    LedColor pixelColor;
    for(int i=0;i<_ledColors.length;i++){
      float distSquared=PVector.sub(ledPositions[i],center).magSq();
      if(distSquared<=maxDistSquared&&distSquared>=minDistSquared){
          pixelColor=color;
         // pixelColor.setAlpha(PApplet.sq(((distSquared/maxDistSquared)-1)));
          
         // pixelColor.setAlpha(-((distSquared/maxDistSquared)-1));
    	  _ledColors[i].mixWithAlpha(color,blendMode,_blendOut); 
      }
      else _ledColors[i].mixWithAlpha(black,LedColor.LedAlphaMode.MULTIPLY,_blendOut);
    }
  }
}