package cs4620.manip;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import cs4620.scene.SceneNode;
import cs4620.scene.SceneProgram;
import cs4620.shape.Cube;
import cs4620.shape.TriangleMesh;
import cs4620.framework.Camera;
import cs4620.framework.Transforms;
import cs4620.manip.ManipUtils;
import cs4620.material.Material;
import cs4620.material.PhongMaterial;

public class TranslateManip extends Manip
{
	Cube cube = null;
	ArrowHeadMesh arrow = null;
	PhongMaterial xMaterial;
	PhongMaterial yMaterial;
	PhongMaterial zMaterial;
	PhongMaterial centerMaterial;
	boolean resourcesInitialized = false;
	
	private void initResourcesGL(GL2 gl)
	{
		if (!resourcesInitialized)
		{
			cube = new Cube(gl);
			cube.buildMesh(gl, 1.0f);
			arrow = new ArrowHeadMesh(gl);
			arrow.buildMesh(gl, 1.0f);
			
			xMaterial = new PhongMaterial();
			xMaterial.setAmbient(0.0f, 0.0f, 0.0f);
			xMaterial.setDiffuse(0.8f, 0.0f, 0.0f);
			xMaterial.setSpecular(0.0f, 0.0f, 0.0f);
			
			yMaterial = new PhongMaterial();
			yMaterial.setAmbient(0.0f, 0.0f, 0.0f);
			yMaterial.setDiffuse(0.0f, 0.8f, 0.0f);
			yMaterial.setSpecular(0.0f, 0.0f, 0.0f);
			
			zMaterial = new PhongMaterial();
			zMaterial.setAmbient(0.0f, 0.0f, 0.0f);
			zMaterial.setDiffuse(0.0f, 0.0f, 0.8f);
			zMaterial.setSpecular(0.0f, 0.0f, 0.0f);
			
			centerMaterial = new PhongMaterial();
			centerMaterial.setAmbient(0.0f, 0.0f, 0.0f);
			centerMaterial.setDiffuse(0.8f, 0.8f, 0.0f);
			centerMaterial.setSpecular(0.0f, 0.0f, 0.0f);
			resourcesInitialized = true;
		}
	}
	
	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{	
		if(axisMode == PICK_CENTER) translateOnCenter(mousePosition,mouseDelta);
		else translateOnAxis(mousePosition, mouseDelta);
		
	}
	
	/*Translation for when the mouse has clicked on the center cube. Calculates the 
	 *translation and updates translation matrix accordingly*/
	private void translateOnCenter(Vector2f mousePosition, Vector2f mouseDelta)
	{
		
		Matrix4f toWorldTransform = sceneNode.toWorld(); 
		//Fourth column of world transform corresponds to origin in world coordinates,
		//because worldTransform is homogenous and identiy matrix 4th column is (0,0,0,1)
		//Together these define the plane parallel to the camera's view plane in which the node lies
		Vector3f originInWorld = new Vector3f(toWorldTransform.getElement(0, 3), toWorldTransform.getElement(1,3), 
						toWorldTransform.getElement(2,3));
		Vector3f planeNormal = camera.getViewDir(); 
		
		//Create two eye rays, for the points before and after the mouse drag.
		//Then put it into world coordinates. 
		Vector3f eyeRayP0 = new Vector3f(); 
		Vector3f eyeRayV0 = new Vector3f(); 
		Vector3f eyeRayP1 = new Vector3f(); 
		Vector3f eyeRayV1 = new Vector3f(); 
		Vector2f prevMouse = (new Vector2f(mousePosition.x - mouseDelta.x, mousePosition.y - mouseDelta.y)); 
		camera.getRayNDC(prevMouse,eyeRayP0,eyeRayV0);
		camera.getRayNDC(mousePosition,eyeRayP1,eyeRayV1); 
		
		//Intersect eye rays with plane containing node's origin, and find the
		//intersection points. 
		float t0 = ManipUtils.intersectRayPlane(eyeRayP0, eyeRayV0, originInWorld, planeNormal);
		float t1 = ManipUtils.intersectRayPlane(eyeRayP1, eyeRayV1, originInWorld, planeNormal);
		
		//Point on Plane = eye Origin + t * viewDir
		Vector3f pOld = new Vector3f(eyeRayV0);
		pOld.scaleAdd(t0, eyeRayP0);
		Vector3f pNew = new Vector3f(eyeRayV1);
		pNew.scaleAdd(t1, eyeRayP1);
		
		Vector3f translationDisplacement = new Vector3f(pNew.x - pOld.x, 
				pNew.y - pOld.y, pNew.z - pOld.z); 
		
		//We need to transform this from world to local coordinates. 
		Matrix4f fromWorldTransform = new Matrix4f(); 
		fromWorldTransform.invert(toWorldTransform); 
		fromWorldTransform.transform(translationDisplacement); 
		
		//Now we update the translation matrix. 
		 sceneNode.setTranslation(sceneNode.translation.x + translationDisplacement.x, 
					sceneNode.translation.y + translationDisplacement.y, 
					sceneNode.translation.z + translationDisplacement.z);
		
	}

	/*Translation for when the mouse has clicked on one of the axes. Calculates the 
	 *translation and updates translation matrix accordingly*/
	private void translateOnAxis(Vector2f mousePosition, Vector2f mouseDelta) {
		//Create two eye rays, for the points before and after the mouse drag.
		//Then put it into world coordinates. 
		Vector3f eyeRayP0 = new Vector3f(); 
		Vector3f eyeRayV0 = new Vector3f(); 
		Vector3f eyeRayP1 = new Vector3f(); 
		Vector3f eyeRayV1 = new Vector3f(); 
		Vector2f prevMouse = (new Vector2f(mousePosition.x - mouseDelta.x, mousePosition.y - mouseDelta.y)); 
		camera.getRayNDC(prevMouse,eyeRayP0,eyeRayV0);
		camera.getRayNDC(mousePosition,eyeRayP1,eyeRayV1); 
		
		//Now we move the object position and axis into world coordinates. 
		//Something to note is that the identity matrix has columns that perfectly
		//correspond to our vectors. The first is the x-axis, the second is the y-axis, the third is the z-axis, and the
		//fourth is the origin. We can take transformed vectors directly from the worldTransform matrix. 
		Matrix4f worldTransform = sceneNode.toWorld(); 
		
		Vector3f origin = new Vector3f(worldTransform.getElement(0, 3), worldTransform.getElement(1,3), 
				worldTransform.getElement(2,3));
		
		Vector3f axis = new Vector3f(); 
		if(axisMode == PICK_X)
		{
			axis = new Vector3f(worldTransform.getElement(0, 0), worldTransform.getElement(1,0), 
					worldTransform.getElement(2,0));
		}
		if(axisMode == PICK_Y)
		{
			axis = new Vector3f(worldTransform.getElement(0, 1), worldTransform.getElement(1,1), 
					worldTransform.getElement(2,1));
		}
		if(axisMode == PICK_Z)
		{
			axis = new Vector3f(worldTransform.getElement(0, 2), worldTransform.getElement(1,2), 
					worldTransform.getElement(2,2));
		}
		
		//Now we have origin and axis in world coordinates and can finally get our two t values. 
		float t0 = ManipUtils.timeClosestToRay(origin,axis,eyeRayP0,eyeRayV0);
		float t1 = ManipUtils.timeClosestToRay(origin, axis, eyeRayP1, eyeRayV1);
		
		float translationOffset = t1 - t0; 

		//Now we update the translation 
		if(this.axisMode == PICK_X) sceneNode.setTranslation(sceneNode.translation.x + translationOffset, 
				sceneNode.translation.y, sceneNode.translation.z);
		if(this.axisMode == PICK_Y) sceneNode.setTranslation(sceneNode.translation.x, 
				sceneNode.translation.y + translationOffset, sceneNode.translation.z);
		if(this.axisMode == PICK_Z) sceneNode.setTranslation(sceneNode.translation.x, 
				sceneNode.translation.y, sceneNode.translation.z + translationOffset);
	}

	@Override
	public void glRender(GL2 gl, SceneProgram program, Matrix4f modelView, double scale)
	{
		initResourcesGL(gl);
		
		SceneNode parent = sceneNode.getSceneNodeParent();
		Matrix4f parentModelView;
		if (parent != null)
		{
			parentModelView = parent.toEye(modelView);
		}
		else
		{
			parentModelView = new Matrix4f(modelView);
		}
		
		// get eye-space vectors for x,y,z axes of parent
		Vector3f eyeX = new Vector3f();
		Vector3f eyeY = new Vector3f();
		Vector3f eyeZ = new Vector3f();
		Transforms.toColumns3DH(parentModelView, eyeX, eyeY, eyeZ, null);
		
		// get eye-space position of translated origin of sceneNode
		Vector3f eyeOrigin = new Vector3f(sceneNode.translation);
		ManipUtils.transformPosition(parentModelView, eyeOrigin);
		
		// translation to eye-space translated origin of sceneNode
		Matrix4f translateOrigin = Transforms.translate3DH(eyeOrigin);
		Matrix4f scaleMatrix = Transforms.scale3DH((float) scale);
		
		Matrix4f nextModelView = new Matrix4f();
		
		// x axis
		nextModelView.set(translateOrigin);
		nextModelView.mul(ManipUtils.rotateZTo(eyeX));
		nextModelView.mul(scaleMatrix);
		setIdIfPicking(gl, program, Manip.PICK_X);
		glRenderArrow(gl, program, nextModelView, 0);
		
		// y axis
		nextModelView.set(translateOrigin);
		nextModelView.mul(ManipUtils.rotateZTo(eyeY));
		nextModelView.mul(scaleMatrix);
		setIdIfPicking(gl, program, Manip.PICK_Y);
		glRenderArrow(gl, program, nextModelView, 1);
		
		// z axis
		nextModelView.set(translateOrigin);
		nextModelView.mul(ManipUtils.rotateZTo(eyeZ));
		nextModelView.mul(scaleMatrix);
		setIdIfPicking(gl, program, Manip.PICK_Z);
		glRenderArrow(gl, program, nextModelView, 2);
		
		float boxRadius = 0.1f;
		// center cube
		nextModelView.set(translateOrigin);
		nextModelView.mul(scaleMatrix);
		nextModelView.mul(Transforms.scale3DH(boxRadius));
		program.setMaterial(gl, centerMaterial);
		program.setModelView(gl, nextModelView);
		setIdIfPicking(gl, program, Manip.PICK_CENTER);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		cube.draw(gl);  // cube should draw over other parts of manipulator
		gl.glEnable(GL2.GL_DEPTH_TEST);
	}

	public void glRenderArrow(GL2 gl, SceneProgram program, Matrix4f modelView, int axis) {
		Material axisMaterial = zMaterial;
		if (axis == 0) // x
		{
			axisMaterial = xMaterial;
		}
		else if (axis == 1) // y
		{
			axisMaterial = yMaterial;
		}
		
		float radiusTail = 0.075f;
		float radiusHead = 0.15f;
		float lengthTail = 1.7f;
		float lengthHead = 0.3f;
		
		program.setMaterial(gl, axisMaterial);
		
		// tail
		Matrix4f nextModelView = new Matrix4f(modelView);
		nextModelView.mul(Transforms.scale3DH(radiusTail, radiusTail, -lengthTail / 2.0f));
		nextModelView.mul(Transforms.translate3DH(0.0f, 0.0f, -1.0f));
		program.setModelView(gl, nextModelView);
		arrow.draw(gl);
		
		// tail
		nextModelView.set(modelView);
		nextModelView.mul(Transforms.translate3DH(0.0f, 0.0f, lengthTail));
		nextModelView.mul(Transforms.scale3DH(radiusHead, radiusHead, lengthHead / 2.0f));
		nextModelView.mul(Transforms.translate3DH(0.0f, 0.0f, 1.0f));
		program.setModelView(gl, nextModelView);
		arrow.draw(gl);
		
	}
}

class ArrowHeadMesh extends TriangleMesh
{
	
	public static final int CIRCLE_DIVS = 32;

	public ArrowHeadMesh(GL2 gl) {
		super(gl);
	}
	
	float cos(float ang)
	{
		return (float) Math.cos(ang);
	}
	
	float sin(float ang)
	{
		return (float) Math.sin(ang);
	}

	@Override
	public void buildMesh(GL2 gl, float tolerance) {
		// build the arrowhead!
		float [] vertices = new float [3 * CIRCLE_DIVS * 2];
		float [] normals = new float [3 * CIRCLE_DIVS * 2];
		
		int [] triangles = new int [3 * CIRCLE_DIVS];
		
		float pi = (float) Math.PI;
		float xyCoeff = 2.0f / (float) Math.sqrt(5.0f);
		float zCoeff = 1.0f / (float) Math.sqrt(5.0f);
		// form vertices and normals
		for(int i = 0; i < CIRCLE_DIVS; i++)
		{
			float ang = i * 2.0f * pi / CIRCLE_DIVS;
			float angBetween = (2 * i + 1) * 2.0f * pi / (2 * CIRCLE_DIVS);
			
			vertices[6*i + 0] = cos(ang);
			vertices[6*i + 1] = sin(ang);
			vertices[6*i + 2] = -1.0f;
			normals[6*i + 0] = cos(ang) * xyCoeff;
			normals[6*i + 1] = sin(ang) * xyCoeff;
			normals[6*i + 2] = zCoeff;
			
			vertices[6*i + 3] = 0.0f;
			vertices[6*i + 4] = 0.0f;
			vertices[6*i + 5] = 1.0f;
			normals[6*i + 3] = cos(angBetween) * xyCoeff;
			normals[6*i + 4] = sin(angBetween) * xyCoeff;
			normals[6*i + 5] = zCoeff;
		}
		
		// assemble triangles
		for(int i = 0; i < CIRCLE_DIVS; i++)
		{
			triangles[3 * i + 0] = 2*i;
			triangles[3 * i + 1] = (2*i + 2) % (2 * CIRCLE_DIVS);
			triangles[3 * i + 2] = 2*i+1;
		}
		
		this.setVertices(gl, vertices);
		this.setNormals(gl, normals);
		this.setTriangleIndices(gl, triangles);
		// we won't need wireframe here
		this.setWireframeIndices(gl, new int [0]);
	}

	@Override
	public Object getYamlObjectRepresentation() {
		return null;
	}
	
}
