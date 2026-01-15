## Shaders
To make a shader, subclass the `Shader` class and make instance fields for each uniform.
```java
public final class MyShader extends Shader {
  public final Mat4fUniform projection = uMat4f("u_projection");
  public final IntUniform texture = uInt("u_texture");
  public final FloatUniform value = uFloat("u_value");
}
```
For the constructor, you can either hardcode the shader code in the class file:
```java
public MyShader() {
  super("""
    #version 330 core
    layout (location = 0) in vec4 a_position;
    uniform mat4 u_projection;
    void main() {
      gl_Position = u_projection * a_position;
    }
    """,
    """
    #version 330 core
    uniform sampler2D u_texture;
    uniform float value;
    out vec4 FragColor;
    void main() {
      FragColor = someCalculation;
    }
    """);
}
MyShader shader = new MyShader();
```
or take parameters, so you can read from another file, for example:
```java
public MyShader(String vert, String frag) {
  super(vert, frag);
}
// (keep in mind that shaders can only be created on the main thread!)
MyShader shader = new MyShader(readFile("shader.vert"), readFile("shader.frag"));
```

To set uniforms, bind the shader and use the instance fields.
```java
MyShader shader = ...;
Matrix4f matrix = ...;
Texture myTexture = ...;

shader.bind();
shader.projcetion.set(matrix, /*transpose*/ false);
myTexture.bindTexture(/*slot*/ 0);
shader.texture.set(0);
shader.value.set(1.0f);
```
When you're done, call `dispose` to delete the shader off the GPU.
```java
shader.dispose();
```

### Shader compiling and recompiling
Shaders are compiled on construction and can be recompiled with `compile`.
If compilation fails, or a required uniform is missing, an exception will be thrown. In the case of recompilation, the state will be reverted as well. This means that a `Shader` can never represent an invalid or uncompiled shader unless it is disposed.

### Automatic shader reloading
When debugging, it can be useful to have your shaders automatically reload when you change them, similar to https://thebookofshaders.com/edit.php. 
To set this up, you can use `ShaderReloader` to listen to the shader files and automatically recompile the shaders when they change.
```java
CelestialGdx gdx = ...;
Path vertexPath = Paths.get("assets/Shaders/test.vert");
Path fragmentPath = Paths.get("assets/Shaders/test.frag");
MyShader shader = ...;

ShaderReloader reloader = ShaderReloader.create(gdx);
reloader.listen(
	"test shader", // a custom name that will be used in logs
	shader, vertexPath, fragmentPath
);
reloader.start();
```
After start is called, you can't add any more shaders to be listened to.
Every time a change in the files is detected, it'll auto-recompile the shaders and log a message:
```
[ShaderReloader] Shader test shader recompiled
```
If it fails to recompile, it'll log a different message with the error:
```
[ShaderReloader] Error recompiling shader test shader
Failed to compile fragment shader
0(10) : error C0000: syntax error, unexpected '=', expecting "::" at token "="
```

You can shut it down by interupting the thread.
```java
reloader.interupt();
```