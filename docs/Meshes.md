## Meshes
To make a mesh, start by creating a [VBO](https://wikis.khronos.org/opengl/Vertex_Specification#Vertex_Buffer_Object):
```java
Vbo vbo = Vbo.create(BufferUsage.STATIC);
float[] vertices = {
  0.5f, 0.5f, 0f / 255f, 0f / 255f, 255 / 255f,
  0.5f, -0.5f, 173f / 255f, 19 / 255f, 130 / 255f,
  -0.5f, -0.5f, 117 / 255f, 186 / 255f, 20 / 255f,
  -0.5f, 0.5f, 184 / 255f, 76 / 255f, 29 / 255f
};
// using off-heap buffers (i.e. from MemoryUtil.memAllocFloat)
// has significantly faster transfer speed than Java arrays,
// but in this case it's only uploaded once so it doesn't really matter
vbo.uploadVertices(vertices);

// to modify the data later, you must bind the object first
vbo.bind();
vbo.uploadVertices(data);
```
Optionally, make an [EBO](https://wikis.khronos.org/opengl/Vertex_Specification#Index_buffers) to allow the GPU to reuse transformed vertices:
```java
short[] indices = {
  0, 1, 3,
  1, 2, 3
};
Ebo ebo = Ebo.create(BufferUsage.STATIC);
ebo.uploadIndices(indices);
```
Lastly, create a [VAO](https://wikis.khronos.org/opengl/Vertex_Specification#Vertex_Array_Object), and in your render loop, call `bind` then `render` to draw it.
```java
import static org.lwjgl.opengl.GL33.*;

// (it's called Mesh instead of Vao to avoid mixing it up with Vbo)
Mesh mesh = Mesh.create(ebo);
mesh.setEbo(ebo); // this will bind the EBO
mesh.setAttributes(
  /*autoPosition*/ true,
  VxAttrib.of(vbo, 2, GL_FLOAT), // vec2
  VxAttrib.of(vbo, 3, GL_FLOAT) // vec3
);
myShader.bind();
mesh.bind();
mesh.render(GL_TRIANGLES, /*count*/ 6);
```

All of these objects hold native GPU resources and should be `dispose`d of when you're done.
```java
vbo.dispose();
ebo.dispose();
mesh.dispose();
```

### Attributes
Vertex attributes can be sourced from one, multiple or even zero vertex buffers.

If all your attributes are sourced from one buffer, you can set `autoPosition` to true to allow
the vertex attributes' [stride and pointer](https://wikis.khronos.org/opengl/Vertex_Specification#Vertex_buffer_offset_and_stride) to be calculated automatically.
```java
Vbo vbo = ...;
Mesh mesh = ...;
mesh.setAttributes(
  /*autoPosition*/ true,
  VxAttrib.of(vbo, 2, GL_FLOAT), // vec2
  VxAttrib.of(vbo, 3, GL_FLOAT) // vec3
);
// since autoPosition is true and we didn't specify any stride/pointer,
// it will be calculated automatically based on the sizes.
// in this case, both with have a stride of 2*4+3*4=20,
// the first attribute will have a position of 0, and the second will have a position of 2*4=8
```
If your attributes are from multiple buffers (which is common in instanced rendering), `autoPosition` must be false and you must specify the stride/position yourself.
```java
// TODO: better example
Vbo positionVbo = ...;
Vbo offsetsVbo = ...;
Mesh mesh = ...;
mesh.setAttributes(
  /*autoPosition*/ false,
  VxAttrib.of(positionVbo, 3, GL_FLOAT), // vec3 pos
  VxAttrib.of(positionVbo, 3, GL_FLOAT), // vec3 normal
  // these will be called with glVertexAttribDivisor
  // so they'll only be sent once per instance
  // https://registry.khronos.org/OpenGL-Refpages/gl4/html/glVertexAttribDivisor.xhtml
  VxAttrib.of(offsetsVbo, 2, GL_FLOAT).divsior(1), // vec2 offset
  VxAttrib.of(offsetsVbo, 1, GL_FLOAT).divisor(1) // float
);

mesh.bind();
mesh.renderInstanced(GL_TRIANGLES, /*count*/ 6, /*instances*/ 3);
```