package cs4620.material;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import cs4620.scene.SceneProgram;

public abstract class Material
{
	protected static ArrayList<Material> instances = new ArrayList<Material>();
	
	public abstract void applyTo(GL2 gl, SceneProgram program);

	public Material()
	{
		synchronized(instances) {
			instances.add(this);
		}
	}

	public abstract Object getYamlObjectRepresentation();
}
