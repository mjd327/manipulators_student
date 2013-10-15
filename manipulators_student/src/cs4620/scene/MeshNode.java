package cs4620.scene;

import java.util.Map;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix4f;

import cs4620.material.Material;
import cs4620.material.PhongMaterial;
import cs4620.shape.Mesh;
import cs4620.shape.Sphere;
import cs4620.util.Util;

public class MeshNode extends SceneNode
{
	private static final long	serialVersionUID	= 1L;

	private Mesh mesh;
	private Material material;

	/**
	 * Required for I/O
	 */
	public MeshNode()
	{
		this("", null, null);
	}

	public MeshNode(GL2 gl, String name)
	{
		this(name, new Sphere(gl), new PhongMaterial());
	}

	public MeshNode(String name, Mesh mesh)
	{
		this(name, mesh, new PhongMaterial());
	}

	public MeshNode(String name, Mesh mesh, Material material)
	{
		super(name);
		this.mesh = mesh;
		this.material = material;
	}

	public Mesh getMesh()
	{
		return mesh;
	}

	public void setMesh(Mesh mesh)
	{
		this.mesh = mesh;
	}

	public Material getMaterial()
	{
		return material;
	}
	
	public void draw(GL2 gl, SceneProgram program, Matrix4f modelView)
	{
		getMaterial().applyTo(gl, program);
		program.setModelView(gl, modelView);
		getMesh().draw(gl);
	}
	
	public void drawWireframe(GL2 gl, SceneProgram program, Matrix4f modelView)
	{
		getMaterial().applyTo(gl, program);
		program.setModelView(gl, modelView);
		getMesh().drawWireframe(gl);
	}
	
	public void drawForPicking(GL2 gl)
	{
		gl.glLoadName(getMesh().getId());
		getMesh().draw(gl);
	}

	public void setMaterial(Material material)
	{
		this.material = material;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<String, Object> result = (Map<String, Object>)super.getYamlObjectRepresentation();
		result.put("type", "MeshNode");
		result.put("mesh", mesh.getYamlObjectRepresentation());
		result.put("material", material.getYamlObjectRepresentation());
		return result;
	}

	public void extractMeshFromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;

		mesh = Mesh.fromYamlObject(gl, yamlMap.get("mesh"));
	}

	public void extractMaterialFromYamlObject(Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;

		if (!(yamlMap.get("material") instanceof Map))
			throw new RuntimeException("material field not a Map");
		Map<?, ?> materialMap = (Map<?, ?>)yamlMap.get("material");

		if (!materialMap.get("type").equals("GLPhongMaterial"))
			throw new RuntimeException("material other than GLPhongMaterial is not supported");

		PhongMaterial glMaterial = new PhongMaterial();
		Util.assign4ElementArrayFromYamlObject(glMaterial.ambient, materialMap.get("ambient"));
		Util.assign4ElementArrayFromYamlObject(glMaterial.diffuse, materialMap.get("diffuse"));
		Util.assign4ElementArrayFromYamlObject(glMaterial.specular, materialMap.get("specular"));
		glMaterial.shininess = Float.valueOf(materialMap.get("shininess").toString());

		material = glMaterial;
	}

	public static SceneNode fromYamlObject(GL2 gl, Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;

		MeshNode result = new MeshNode();
		result.setName((String)yamlMap.get("name"));
		result.extractTransformationFromYamlObject(yamlObject);
		result.addChildrenFromYamlObject(gl, yamlObject);
		result.extractMeshFromYamlObject(gl, yamlObject);
		result.extractMaterialFromYamlObject(yamlObject);

		return result;
	}
}
