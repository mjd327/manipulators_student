package cs4620.shape;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Cylinder extends TriangleMesh
{
	float radius = 1; 
	float height = 2; 
	

	private static float[] cylinderVertices;
	private static float[] cylinderNormals;
	private static int[]   cylinderTriangles;
	private static int[]   cylinderLines;

	
	public Cylinder(GL2 gl)
	{
		super(gl);
	}
	
	@Override
	public void buildMesh(GL2 gl, float tolerance)
	{
		// TODO (Scene P2): Implement mesh generation for Cylinder. Your code should
		// fill arrays of vertex positions/normals and vertex indices for triangles/lines
		// and put this information in the GL buffers using the
		//   set*()
		// methods from TriangleMesh.

		//We need to decide how many point we are going to need around the top and bottom
	    int thetaPoints = (int) Math.ceil(2* Math.PI * radius / (.5 * tolerance)) + 1;
		double thetaIncrement = ((2*Math.PI)/(thetaPoints-1));
		
		//Array of vertex points and normals. They are stored as follows: 
		//Top point, bottom point, top ring (for cap), top ring (for side),
		//bottom ring(for side), bottom ring(for cap)
		Point3f[] vertices = new Point3f[thetaPoints * 4 + 2];
		Vector3f[] normals = new Vector3f[thetaPoints * 4 + 2];
		
		vertices[0] = new Point3f(0,1,0);
		vertices[1] = new Point3f(0,-1,0);
		
		normals[0] = new Vector3f(0,1,0);
		normals[1] = new Vector3f(0,-1,0);
		
		double theta = 0; 
		for(int i = 1; i <= thetaPoints; i++)
		{
			int topCapIndex = 1+i;
			int topSideIndex = 1+i+thetaPoints;
			int bottomSideIndex = 1+i+ 2*thetaPoints;
			int bottomCapIndex = 1+i+ 3*thetaPoints; 
			
			Point3f topPoint = polarToCartesian(theta);
			Point3f bottomPoint = new Point3f(topPoint.x, -topPoint.y, topPoint.z); 
			
			vertices[topCapIndex] = topPoint;
			vertices[topSideIndex] = topPoint;
			normals[topCapIndex] = new Vector3f(0,1,0);
			normals[topSideIndex] = new Vector3f(topPoint.x,0,topPoint.z); 
					
			vertices[bottomSideIndex] = bottomPoint;
			vertices[bottomCapIndex] = bottomPoint;
			normals[bottomSideIndex] = new Vector3f(bottomPoint.x,0,bottomPoint.z);
			normals[bottomCapIndex] = new Vector3f(0,-1,0); 
					
			theta += thetaIncrement; 
		}

		//Now we will use these point indexes, to make triangles and lines.
		
		//3 vertices per triangle, 4 * (thetaPoints - 1) triangles.
		//We will do top triangles, bottom triangles, and then side triangles in that order. 
		cylinderTriangles = new int[3 * 4*(thetaPoints-1)];
		
		//2 faces of (thetaPoints-1) triangles, 6 lines per triangle + 
		//8 lines per box, (thetaPoints-1) boxes.
		//We will do top lines, bottom lines, and then side lines in that order. 
		cylinderLines = new int[2 * 6 * (thetaPoints -1) + 8 * (thetaPoints-1)];
		
		//First compute triangles and lines for the caps. 
		for (int i = 0; i < thetaPoints-1; i++)
		{
			//A triangle on the top cap is the top point, 
			//and two consectuive points along the edge.
			int topPoint1 = 0;
			int topPoint2 = 2 + i;
			int topPoint3 = 2 + i+ 1;
			
			int bottomPoint1 = 1; 
			int bottomPoint2 = 2 + (3 * thetaPoints) + i; 
			int bottomPoint3 = 2 + (3 * thetaPoints) + i + 1; 
			
			//Add top triangles. 
			cylinderTriangles[3 * i] = topPoint1;
			cylinderTriangles[3 * i + 1] = topPoint2;
			cylinderTriangles[3 * i + 2] = topPoint3; 
			//Add bottom triangles
			cylinderTriangles[3 * i + 3 * (thetaPoints - 1)] = bottomPoint3; 
			cylinderTriangles[3 * i + 3 * (thetaPoints - 1) + 1] = bottomPoint2; 
			cylinderTriangles[3 * i + 3 * (thetaPoints -1) + 2] = bottomPoint1; 
			//Add top lines
			cylinderLines[6*i] = topPoint1;
			cylinderLines[6*i+1] = topPoint2;
			cylinderLines[6*i+2] = topPoint2;
			cylinderLines[6*i+3] = topPoint3;
			cylinderLines[6*i+4] = topPoint3;
			cylinderLines[6*i+5] = topPoint1;
			//Add bottom lines 
			cylinderLines[6*i + 6*(thetaPoints-1)] = bottomPoint1;
			cylinderLines[6*i + 6*(thetaPoints-1) + 1] = bottomPoint2; 
			cylinderLines[6*i + 6*(thetaPoints-1) + 2] = bottomPoint2; 
			cylinderLines[6*i + 6*(thetaPoints-1) + 3] = bottomPoint3; 
			cylinderLines[6*i + 6*(thetaPoints-1) + 4] = bottomPoint3; 
			cylinderLines[6*i + 6*(thetaPoints-1) + 5] = bottomPoint1; 
		}
		

		//Initial offset based off of triangles already created for side triangles. 
		int triangleOffset = 3 * 2 * (thetaPoints-1); 
		//Same offset for lines. 
		int lineOffset = 6 * 2 * (thetaPoints-1); 
		
		//Now compute triangles and lines for the sides.
		for (int i = 0; i < thetaPoints-1; i++)
		{
			int upperLeft = 2 + thetaPoints + i;
			int upperRight = 2 + thetaPoints + i + 1; 
			int bottomLeft = 2 + (2 * thetaPoints) + i; 
			int bottomRight = 2 + (2 * thetaPoints) + i + 1; 
			 
			//Add triangles for sides
			cylinderTriangles[triangleOffset + 6 * i] = upperLeft;
			cylinderTriangles[triangleOffset + 6 * i + 1] = bottomLeft;
			cylinderTriangles[triangleOffset + 6 * i + 2] = upperRight;
			cylinderTriangles[triangleOffset + 6 * i + 3] = upperRight;
			cylinderTriangles[triangleOffset + 6 * i + 4] = bottomLeft;
			cylinderTriangles[triangleOffset + 6 * i + 5] = bottomRight; 
			
			//Add triangles for lines
			cylinderLines[lineOffset + 8 * i] = upperLeft;
			cylinderLines[lineOffset + 8 * i + 1] = bottomLeft;
			cylinderLines[lineOffset + 8 * i + 2] = bottomLeft;
			cylinderLines[lineOffset + 8 * i + 3] = bottomRight; 
			cylinderLines[lineOffset + 8 * i + 4] = bottomRight; 
			cylinderLines[lineOffset + 8 * i + 5] = upperRight; 
			cylinderLines[lineOffset + 8 * i + 6] = upperRight; 
			cylinderLines[lineOffset + 8 * i + 7] = upperLeft; 
		}
		
		//Now we need to split up the points in the cylinderVertices and cylinderNormal arrays. 
		
		cylinderVertices = new float[3 * vertices.length];
		cylinderNormals = new float[3 * normals.length];
		for (int i = 0; i < vertices.length; i++)
		{
			cylinderVertices[3*i] = vertices[i].x; 
			cylinderVertices[3*i + 1] = vertices[i].y; 
			cylinderVertices[3*i + 2] = vertices[i].z; 
		}

		for (int i = 0; i < normals.length; i++)
		{
			cylinderNormals[3*i] = normals[i].x; 
			cylinderNormals[3*i + 1] = normals[i].y; 
			cylinderNormals[3*i + 2] = normals[i].z; 
		}

	    //Now finally set all the arrays
		setVertices(gl, cylinderVertices);
		setNormals(gl, cylinderNormals);
		setTriangleIndices(gl, cylinderTriangles);
		setWireframeIndices(gl, cylinderLines);
		
		
		
		
	}

	//Given a theta value, returns the Cartesian coordinates of a point along the top face.
	//theta = 0 starts at z axis, theta = pi/2 points to postive x axis

	public Point3f polarToCartesian(double theta)
	{
		float x = (float) Math.sin(theta); 
		float y = 1; 
		float z = (float) Math.cos(theta); 
		
		return new Point3f(x,y,z);
	}
	
	
	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<Object,Object> result = new HashMap<Object, Object>();
		result.put("type", "Cylinder");
		return result;
	}
}
