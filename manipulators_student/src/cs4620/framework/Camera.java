package cs4620.framework;

import javax.swing.Icon;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public abstract class Camera {
	//Default position of the camera
	protected static final Point3f DEFAULT_EYE = new Point3f(0, 0, 10);
	protected static final Point3f DEFAULT_TARGET = new Point3f(0, 0, 0);
	protected static final Vector3f DEFAULT_UP = new Vector3f(0, 1, 0);
	protected static final Vector3f VERTICAL = new Vector3f(0,1,0);

  //All the camera parameters
	public float aspect = 1;
	public float near;
	public float far;
	public final Point3f eye = new Point3f();
	public final Point3f target = new Point3f();
	public final Vector3f up = new Vector3f();
	public final Vector3f right = new Vector3f();

	//Icon to use in the tree display
	protected transient Icon icon = null;

	/**
	 * Required by IO
	 */
	public Camera() {}

	public Camera(float newNear, float newFar) {
		this(DEFAULT_EYE, DEFAULT_TARGET, DEFAULT_UP, newNear, newFar);
	}

	/**
	 * Creates a camera from input parameters
	 */
	public Camera(Point3f newEye, Point3f newTarget, Vector3f newUp, float newNear, float newFar) {
		up.set(newUp);
		eye.set(newEye);
		target.set(newTarget);
		near = newNear;
		far = newFar;

		updateFrame();
	}

	/**
	 * Creates the camera from the current camera parameters
	 */
	public void updateFrame() {

		Vector3f negGaze = new Vector3f(eye);
		negGaze.sub(target);
		negGaze.normalize();

		up.normalize();
		right.cross(up, negGaze);
		right.normalize();
		up.cross(negGaze, right);
	}

	/**
	 * Return the eye point
	 * @return
	 */
	public Point3f getEye() {

		return eye;
	}

	/**
	 * Return the target point
	 * @return
	 */
	public Point3f getTarget() {

		return target;
	}

	/**
	 * Return the up vector
	 * @return
	 */
	public Vector3f getUp() {

		return up;
	}
	
	/**
	 * Return the view direction
	 */
	public Vector3f getViewDir() {
		Vector3f out = new Vector3f(target);
		out.sub(eye);
		return out;
	}

	/**
	 * Return the right vector
	 * @return
	 */
	public Vector3f getRight() {

		return right;
	}

	/**
	 * Returns the height of this camera's image
	 * @return
	 */
	public abstract float getHeight();

	/**
	 * Set the aspect ratio
	 * @param d
	 */
	public void setAspect(float d) {

		aspect = d;
	}

	/**
	 * Move the camera eye and target by the given vector
	 * @param delta
	 */
	public void translate(Vector3f delta) {
		eye.add(delta);
		target.add(delta);
	}

	/*
	 * Zoom the camera to distance d.
	 */
	public abstract void zoom(float d);

	/**
	 * Convert displacement delta in the view plane to world space.
	 * @param delta
	 * @param output
	 */
	public void convertMotion(Vector2f delta, Vector3f output) {
		// note: this method is approximate.  Loses accuracy away from the center of the viewport
		output.scale(-delta.x * aspect * getHeight(), right);
		output.scaleAdd(-delta.y * getHeight(), up, output);
	}

	/**
	 * Flips the camera
	 */
	public void flip() {
		Vector3f offset = new Vector3f();
		offset.sub(eye, target);
		offset.negate();
		eye.add(target, offset);
	}
	
	/**
	 * Form the projection matrix for this camera
	 */
	public abstract Matrix4f getProjection();
	
	/**
	 * Form the view matrix for this camera
	 */
	public Matrix4f getView() {
		return Transforms.lookAt3DH(eye, target, up);
	}
	
	/**
	 * Return in p and v a ray with the property that all points along the ray are
	 * projected down by this camera to the same x,y NDC coordinates specified by ndc.
	 */
	
	public void getRayNDC(Vector2f ndc, Vector3f p, Vector3f v)
	{
		//Invert (MperMcam) so that we can move from NDC to world
		Matrix4f inverseMatrix = new Matrix4f(this.getProjection());
		inverseMatrix.mul(this.getView());
		inverseMatrix.invert();
		
		//Expand our NDC coordinates, to be homogenous. Create matrix to allow for multiplication. 
		Matrix4f nearPlaneNDC = 
				 new Matrix4f(
						ndc.x,    0.0f,   0.0f, 0.0f,
		                ndc.y,    0.0f,    0.0f, 0.0f,
		                -1.0f, 0.0f, 0.0f, 0.0f,
		                1.0f, 0.0f, 0.0f, 0.0f);
				
				
		Matrix4f farPlaneNDC =  new Matrix4f(
				ndc.x,    0.0f,   0.0f, 0.0f,
                ndc.y,    0.0f,    0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 0.0f);
		
		
		Matrix4f nearTransform = new Matrix4f(inverseMatrix);
		Matrix4f farTransform = new Matrix4f(inverseMatrix); 
		
		//Transform from NDC to World by multiplying the inverse Matrix * our vectors (Which need to be afterwards extracted
		//from the 4x4 matrix. 
		Vector4f nearPlaneWorld = new Vector4f();
		Vector4f farPlaneWorld = new Vector4f(); 
		nearTransform.mul(nearPlaneNDC);
		nearTransform.getColumn(0,nearPlaneWorld);
		farTransform.mul(farPlaneNDC); 
		farTransform.getColumn(0, farPlaneWorld);
		
		//Now we must homogenize by dividing by 4th component
		nearPlaneWorld.scale(1/nearPlaneWorld.w);
		farPlaneWorld.scale(1/farPlaneWorld.w); 
		
		//We now have two points along the ray in world coordinates, so we set one to be the point, and get the 
		//direction between them as the vector. 
		p.set(nearPlaneWorld.x, nearPlaneWorld.y, nearPlaneWorld.z);
		v.set(farPlaneWorld.x - nearPlaneWorld.x, farPlaneWorld.y - nearPlaneWorld.y,
				farPlaneWorld.z - nearPlaneWorld.z);
	}
}
