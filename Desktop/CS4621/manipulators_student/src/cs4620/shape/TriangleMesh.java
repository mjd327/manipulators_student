package cs4620.shape;

import javax.media.opengl.GL2;

import cs4620.framework.IndexBuffer;
import cs4620.framework.VertexArray;
import cs4620.framework.VertexBuffer;
import cs4620.scene.SceneProgram;

public abstract class TriangleMesh extends Mesh {
	
	// GL resources
	protected VertexArray trianglesArray;
	protected VertexArray wireframeArray;
	
	protected VertexBuffer verticesBuffer;
	protected VertexBuffer normalsBuffer;
	protected IndexBuffer triangleIndicesBuffer;
	protected IndexBuffer linesIndicesBuffer;

	public TriangleMesh(GL2 gl)
	{
		super(gl);
		
		// TODO (Scene P1): Create buffers for vertices and assign them to vertex arrays for
		// triangle and wireframe drawing. Set the buffers to be empty initially (give them 
		// length-zero arrays); when subclasses of TriangleMesh build their meshes, they
		// will update the buffers' contents using the setter methods below.
		// 
		// Assume that these vertex arrays will be drawn by the SceneProgram shader program.
	}
	
	protected void setVertices(GL2 gl, float [] vertices)
	{
		if (verticesBuffer == null) return; 
		
		if (vertices.length % 3 != 0)
			throw new Error("Vertex array's length is not a multiple of 3.");
		
		verticesBuffer.smartSetData(gl, vertices);
	}
	
	protected void setNormals(GL2 gl, float [] normals)
	{
		if (normalsBuffer == null) return;
		
		if (normals.length % 3 != 0)
			throw new Error("Normal array's length is not a multiple of 3");
		
		normalsBuffer.smartSetData(gl, normals);
	}
	
	protected void setTriangleIndices(GL2 gl, int [] triangleIndices)
	{
		if (triangleIndicesBuffer == null) return;
		
		if (triangleIndices.length % 3 != 0)
	        throw new Error("Triangle array's length is not a multiple of 3.");
		
		triangleIndicesBuffer.smartSetData(gl, triangleIndices);
	}
	
	protected void setWireframeIndices(GL2 gl, int [] wireframeIndices)
	{
		if (linesIndicesBuffer == null) return;
		
		if (wireframeIndices.length % 2 != 0)
	        throw new Error("Line array's length is not a multiple of 2.");
		
		linesIndicesBuffer.smartSetData(gl, wireframeIndices);
	}

	public final void draw(GL2 gl)
	{
		// TODO (Scene P1): Draw the triangle mesh.
	}
	
	public final void drawWireframe(GL2 gl)
	{
		// TODO (Scene P1): Draw the wireframe mesh.
	}
	
	public VertexArray getTrianglesArray()
	{
		return trianglesArray;
	}
	
	public VertexArray getWireframeArray()
	{
		return wireframeArray;
	}
}
