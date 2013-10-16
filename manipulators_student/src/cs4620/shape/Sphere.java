package cs4620.shape;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Sphere extends TriangleMesh {

	//Radius of the sphere.
	float radius = 1; 

	//Number of points in our uv grid
	int uPoints;
	int vPoints; 
	
	private static float[] sphereVertices;
	private static float[] sphereNormals;
	private static int[]   sphereTriangles;
	private static int[]   sphereLines;

	
	public Sphere(GL2 gl) {
		super(gl);
	}

	@Override
	public void buildMesh(GL2 gl, float tolerance)
	{
		//We need to decide how many point we are going to need in the u and v planes
		//so that we can make a grid. 
	    uPoints = (int) Math.ceil(2* Math.PI * radius / (.5 * tolerance)) + 1;
	    vPoints = uPoints; 
		
		//Now we will make a u,v grid.
		Point3f[][] uvGrid = new Point3f[uPoints][vPoints];
		
		//We will use polar coordinates, and convert them one at a time to cartesian.
		
		double thetaIncrement = ((2*Math.PI)/(uPoints-1));
		double phiIncrement = thetaIncrement/2;

		//360 degree theta rotation
		double theta = 0; 
		for(int u = 0; u < uPoints; u ++)
		{	
			//Phi rotation from z = 1 to  = -1
			double phi = 0; 
			for(int v = 0; v < vPoints; v++)
			{
				uvGrid[u][v] = polarToCartesian(radius,theta,phi);
				phi += phiIncrement; 
			}
			theta += thetaIncrement;
		}
		
		//We now need to make this grid one array, we will index it so that it 
		//goes from top to bottom in the phi direction and then 
		//moves to the right in the theta direction.
		sphereVertices = new float[3*uPoints*vPoints]; 
		sphereNormals = new float [3*uPoints*vPoints]; 
		for (int i = 0; i < uPoints; i++)
		{
			for(int j = 0; j < vPoints; j++)
			{
				sphereVertices[3*vPoints* i + 3*j] = uvGrid[i][j].x;
				sphereVertices[3*vPoints*i + 3*j+1] = uvGrid[i][j].y;
				sphereVertices[3*vPoints*i + 3*j+2] = uvGrid[i][j].z;
				Vector3f normalVector = new Vector3f(uvGrid[i][j]); 
				normalVector.normalize(); 
				sphereNormals[3*vPoints* i + 3*j] = normalVector.x;
				sphereNormals[3*vPoints*i + 3*j+1] = normalVector.y;
				sphereNormals[3*vPoints*i + 3*j+2] = normalVector.z;
			}
		}
		
		//Finally we make the triangles and the lines.
		
		sphereTriangles = new int[3*(uPoints-1)*(vPoints-1)*2];
		sphereLines = new int[2 * (uPoints-1)*(vPoints-1)*4]; 
		for (int i = 0; i < uPoints-1; i++)
		{
			for(int j = 0; j < vPoints-1; j++)
			{
				//We deal with one square at a time. 
				int upperLeft = uvToIndex(i,j);
				int upperRight = uvToIndex(i+1,j); 
				int lowerLeft = uvToIndex(i,j+1); 
				int lowerRight = uvToIndex(i+1,j+1);
				
				sphereTriangles[6*(vPoints-1)*i + 3*2*j] = upperLeft;
				sphereTriangles[6*(vPoints-1)*i + (3*2*j)+1] = lowerLeft;
				sphereTriangles[6*(vPoints-1)*i + (3*2*j)+2] = upperRight;
				sphereTriangles[6*(vPoints-1)*i + (3*2*j)+3] = upperRight;
			    sphereTriangles[6*(vPoints-1)*i + (3*2*j)+4] = lowerLeft;
			    sphereTriangles[6*(vPoints-1)*i + (3*2*j)+5] = lowerRight; 
			    
			    sphereLines[8*(vPoints-1)*i + 2*4*j] = upperLeft; 
			    sphereLines[8*(vPoints-1)*i + 2*4*j + 1] = upperRight; 
			    sphereLines[8*(vPoints-1)*i + 2*4*j + 2] = upperRight;
			    sphereLines[8*(vPoints-1)*i + 2*4*j + 3] = lowerRight;
			    sphereLines[8*(vPoints-1)*i + 2*4*j + 4] = lowerRight;
			    sphereLines[8*(vPoints-1)*i + 2*4*j + 5] = lowerLeft; 
			    sphereLines[8*(vPoints-1)*i + 2*4*j + 6] = lowerLeft; 
			    sphereLines[8*(vPoints-1)*i + 2*4*j + 7] = upperLeft; 
			    
			}
		}

	    //Now finally set all the arrays
		setVertices(gl, sphereVertices);
		setNormals(gl, sphereNormals);
		setTriangleIndices(gl, sphereTriangles);
		setWireframeIndices(gl, sphereLines);
		
		
		
		
	}


	//Converts three polar coordinates to Cartesian coordinates. Theta is the angle
	//on the x,y plane starting from the x-axis. 
	//Phi is between the x,y plane and the z axis, starting from the z-axis. 
	public Point3f polarToCartesian(float rho, double theta, double phi)
	{
		Point3f cartesianPoint = new Point3f(); 
		cartesianPoint.x = (float) (rho * Math.sin(phi) * Math.cos(theta)); 
		cartesianPoint.y = (float) (rho * Math.sin(phi) * Math.sin(theta)); 
		cartesianPoint.z = (float) (rho * Math.cos(phi)); 
		return cartesianPoint; 
	}
	
	//Given u,v coordinates, returns index of the vertex array.
	public int uvToIndex(int u, int v)
	{
		return uPoints*u + v;
	}
	
	
	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<Object,Object> result = new HashMap<Object, Object>();
		result.put("type", "Sphere");
		return result;
	}


}
