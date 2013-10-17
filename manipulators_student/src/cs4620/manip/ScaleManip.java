package cs4620.manip;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4620.scene.SceneNode;
import cs4620.scene.SceneProgram;
import cs4620.shape.Cube;
import cs4620.framework.PickingManager;
import cs4620.framework.Transforms;
import cs4620.manip.ManipUtils;
import cs4620.material.Material;
import cs4620.material.PhongMaterial;

public class ScaleManip extends Manip {
	private Vector3f xManipBasis = new Vector3f();
	private Vector3f yManipBasis = new Vector3f();
	private Vector3f zManipBasis = new Vector3f();
	private Vector3f manipOrigin = new Vector3f();
	
	Cube cube = null;
	PhongMaterial stickMaterial;
	PhongMaterial xMaterial;
	PhongMaterial yMaterial;
	PhongMaterial zMaterial;
	PhongMaterial centerMaterial;
	boolean resourcesInitialized = false;
	
	float stickRadius = 0.02f;
	float stickLength = 2.0f;
	float boxRadius = 0.1f;
	
	private void initResourcesGL(GL2 gl)
	{
		if (!resourcesInitialized)
		{
			cube = new Cube(gl);
			cube.buildMesh(gl, 1.0f);
			
			stickMaterial = new PhongMaterial();
			stickMaterial.setAmbient(0.0f, 0.0f, 0.0f);
			stickMaterial.setDiffuse(1.0f, 1.0f, 1.0f);
			stickMaterial.setSpecular(0.0f, 0.0f, 0.0f);
			
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

	private void initManipBasis()
	{
		Matrix4f toWorld = sceneNode.toWorld();
		
		// and apply this rotation, since scale happens before rotation
		xManipBasis.set(eX);
		yManipBasis.set(eY);
		zManipBasis.set(eZ);
		manipOrigin.set(e0);
		
		// origin can be transformed directly to world
		ManipUtils.transformPosition(toWorld, manipOrigin);
		
		// for axes, first account for this node's transform minus scaling
		ManipUtils.applyNodeRotation(sceneNode, xManipBasis);
		ManipUtils.applyNodeRotation(sceneNode, yManipBasis);
		ManipUtils.applyNodeRotation(sceneNode, zManipBasis);
		
		// then, apply parent-to-world if parent exists
		SceneNode parentNode = sceneNode.getSceneNodeParent();
		if (parentNode != null)
		{
			Matrix4f parentToWorld = parentNode.toWorld();
			
			ManipUtils.transformVector(parentToWorld, xManipBasis);
			ManipUtils.transformVector(parentToWorld, yManipBasis);
			ManipUtils.transformVector(parentToWorld, zManipBasis);
		}
		
		// normalize basis
		xManipBasis.normalize();
		yManipBasis.normalize();
		zManipBasis.normalize();
	}


	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{	
		if(axisMode == PICK_CENTER) scaleOnCenter(mousePosition,mouseDelta);
		else scaleOnAxis(mousePosition, mouseDelta);
		
	}
	
	/*Translation for when the mouse has clicked on the center cube. Calculates the 
	 *translation and updates translation matrix accordingly*/
	private void scaleOnCenter(Vector2f mousePosition, Vector2f mouseDelta)
	{
		float verticalOffset = mouseDelta.y;
		float scaleFactor = (float) Math.pow(2, verticalOffset);
		sceneNode.scaling.set(scaleFactor*sceneNode.scaling.x, scaleFactor*sceneNode.scaling.y,
				scaleFactor*sceneNode.scaling.z);
	}

	/*Scaling for when the mouse has clicked on one of the axes. Calculates the 
	 *scale and updates scale matrix accordingly*/
	private void scaleOnAxis(Vector2f mousePosition, Vector2f mouseDelta) {
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
		float t0 = ManipUtils.timeClosestToRay(origin, axis, eyeRayP0, eyeRayV0);
		float t1 = ManipUtils.timeClosestToRay(origin, axis, eyeRayP1, eyeRayV1);
		
		float scaleOffset = t1 - t0; 

		//Now we update the scaling 
		if(this.axisMode == PICK_X) sceneNode.setScaling(sceneNode.scaling.x + scaleOffset, 
				sceneNode.scaling.y, sceneNode.scaling.z);
		if(this.axisMode == PICK_Y) sceneNode.setScaling(sceneNode.scaling.x, 
				sceneNode.scaling.y + scaleOffset, sceneNode.scaling.z);
		if(this.axisMode == PICK_Z) sceneNode.setScaling(sceneNode.scaling.x, 
				sceneNode.scaling.y, sceneNode.scaling.z + scaleOffset);
	}

	@Override
	public void glRender(GL2 gl, SceneProgram program, Matrix4f modelView, double scale)
	{
		initManipBasis();
		
		Matrix4f axisOriginToWorld = Transforms.translate3DH(manipOrigin);
		Matrix4f scaleMatrix = Transforms.scale3DH((float) scale);
		Matrix4f nextModelView;
		
		nextModelView = new Matrix4f(modelView);
		nextModelView.mul(axisOriginToWorld);
		nextModelView.mul(ManipUtils.rotateZTo(xManipBasis));
		nextModelView.mul(scaleMatrix);
		glRenderBoxOnAStick(gl, program, nextModelView, 0);
		
		nextModelView = new Matrix4f(modelView);
		nextModelView.mul(axisOriginToWorld);
		nextModelView.mul(ManipUtils.rotateZTo(yManipBasis));
		nextModelView.mul(scaleMatrix);
		glRenderBoxOnAStick(gl, program, nextModelView, 1);
		
		nextModelView = new Matrix4f(modelView);
		nextModelView.mul(axisOriginToWorld);
		nextModelView.mul(ManipUtils.rotateZTo(zManipBasis));
		nextModelView.mul(scaleMatrix);
		glRenderBoxOnAStick(gl, program, nextModelView, 2);
		
		nextModelView = new Matrix4f(modelView);
		nextModelView.mul(axisOriginToWorld);
		nextModelView.mul(scaleMatrix);
		nextModelView.mul(Transforms.scale3DH(boxRadius));
		program.setMaterial(gl, centerMaterial);
		program.setModelView(gl, nextModelView);
		setIdIfPicking(gl, program, Manip.PICK_CENTER);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		cube.draw(gl); // cube should draw over other parts of manipulator
		gl.glEnable(GL2.GL_DEPTH_TEST);
	}

	private void glRenderBoxOnAStick(GL2 gl, SceneProgram program, Matrix4f modelView, int axis) {
		initResourcesGL(gl);
		
		// render stick
		Matrix4f modelViewComponent = new Matrix4f(modelView);
		modelViewComponent.mul(Transforms.translate3DH(0.0f, 0.0f, stickLength / 2.0f + boxRadius));
		modelViewComponent.mul(Transforms.scale3DH(stickRadius, stickRadius, (stickLength - 2 * boxRadius) / 2.0f));
		
		program.setMaterial(gl, stickMaterial);
		setIdIfPicking(gl, program, PickingManager.UNSELECTED_ID);
		program.setModelView(gl, modelViewComponent);
		cube.draw(gl);
		
		Material axisMaterial;
		if (axis == 0) // x
		{
			axisMaterial = xMaterial;
			setIdIfPicking(gl, program, Manip.PICK_X);
		}
		else if (axis == 1) // y
		{
			axisMaterial = yMaterial;
			setIdIfPicking(gl, program, Manip.PICK_Y);
		}
		else
		{
			axisMaterial = zMaterial;
			setIdIfPicking(gl, program, Manip.PICK_Z);
		}
		
		// render cube at end
		modelViewComponent.set(modelView);
		modelViewComponent.mul(Transforms.translate3DH(0.0f, 0.0f, stickLength));
		modelViewComponent.mul(Transforms.scale3DH(boxRadius));
		
		program.setMaterial(gl, axisMaterial);
		program.setModelView(gl, modelViewComponent);
		cube.draw(gl);
	}
}
