import java.io.*;
import processing.core.*;
import java.util.*;
////////////////////////////////////////////////////////////////////////////
// Led positions are ment to be kept in an Array of PVector. They can be conviniently read and stored using this class
////////////////////////////////////////////////////////////////////////////
class LedPositionFile {

  ///read data from file
  public static PVector[] readFromFile(String filename) {
    ArrayList<PVector> ledPositionsList= new ArrayList<PVector> ();
    //try to read data from file
    try {
      File file = new File(filename);
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] pieces = line.split("\t");
        if (pieces.length>3) {
          ledPositionsList.add(new PVector( (Float.parseFloat(pieces[1])/4.0f), Float.parseFloat(pieces[2])/4.0f, Float.parseFloat(pieces[3])/4.0f)); //initialize Leds x,y,z,r,g,b
        }
      }
      scanner.close();
    } 
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    //convert to vanilla Java Array
    PVector[] ledPositions= new PVector[ledPositionsList.size()];
    for (int i=0; i<ledPositionsList.size(); i++) {
      ledPositions[i]=ledPositionsList.get(i);
    }
    return ledPositions;
  }

  public static void saveToFile(String filename, PVector[] ledPositions) {
    try {
      File file = new File(filename);
      PrintWriter writer = new PrintWriter(file);
      for (int i=0; i<ledPositions.length; i++) {
        writer.println(ledPositions[i].x+"\t"+ledPositions[i].y+"\t"+ledPositions[i].z);
      }
      writer.close();
    } 
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
  }
}

////////////////////////////////////////////////////////////////////////////
// maintains information about the outer dimensoions of an LED-Model 
////////////////////////////////////////////////////////////////////////////
class LedBoundingBox
 {
  // bounding box of the 3d-model
  float minX=Float.MAX_VALUE;
  float maxX=Float.MIN_VALUE;
  float minY=Float.MAX_VALUE;
  float maxY=Float.MIN_VALUE;
  float minZ=Float.MAX_VALUE;
  float maxZ=Float.MIN_VALUE;
  
  public LedBoundingBox() {
  };

  public static LedBoundingBox getForPositions(PVector[] ledPositions) {
    LedBoundingBox result=new LedBoundingBox();
    for (int i=0; i<ledPositions.length; i++) {
      PVector curLedPos=ledPositions[i];
      if (curLedPos.x<result.minX)result.minX=curLedPos.x;
      if (curLedPos.y<result.minY)result.minY=curLedPos.y;
      if (curLedPos.z<result.minZ)result.minZ=curLedPos.z;

      if (curLedPos.x>result.maxX)result.maxX=curLedPos.x;
      if (curLedPos.y>result.maxY)result.maxY=curLedPos.y;
      if (curLedPos.z>result.maxZ)result.maxZ=curLedPos.z;
    }
    return result;
  }
}

////////////////////////////////////////////////////////////////////////////
// some convenience for transformations on arrays of positions
////////////////////////////////////////////////////////////////////////////
class LedTransforms {
  // rodrigues vectors are a very conventient way to specify roration:
  // They point in the axis to rotate about, the length determines the amount of rotation
  static PMatrix3D getRotMatrixForRodrigues(PVector rodrigues ) {
    PMatrix3D R =new PMatrix3D();
    float theta=rodrigues.mag();
    if (theta<0.0001)return R;
    PVector normalized=PVector.mult(rodrigues, 1.0f/theta);


    float x = normalized.x;
    float y = normalized.y;
    float z = normalized.z;

    float c = (float)Math.cos( theta );
    float s = (float)Math.sin( theta );
    float oc = 1.0f - c;

    R.m00 = c + x * x * oc;
    R.m01 = x * y * oc - z * s;
    R.m02 = x * z * oc + y * s;

    R.m10 = y * x * oc + z * s;
    R.m11 = c + y * y * oc;
    R.m12 = y * z * oc - x * s;

    R.m20 = z * x * oc - y * s;
    R.m21 = z * y * oc + x * s;
    R.m22 = c + z * z * oc;

    return R;
  }
}