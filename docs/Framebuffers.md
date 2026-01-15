## Framebuffers
To use a framebuffer, make one, add attachments, check that's complete then bind it when needed.
```java
Framebuffer framebuffer = Framebuffer.create();
Texture texture = Texture.create2D();
texture.allocate(PixelFormat.RGBA, /*width*/ 720, /*height*/ 540);
framebuffer.attachColor2D(texture, /*slot*/ 0);
// optionally specify a slot if you're using multiple render targets

// we want to use the stencil buffer, but don't need to sample it,
// so we can use a renderbuffer instead
// https://wikis.khronos.org/opengl/Renderbuffer_Object
Renderbuffer renderbuffer = Renderbuffer.create();
// for supported formats, see https://wikis.khronos.org/opengl/Image_Format
renderbuffer.allocate(GL_STENCIL_INDEX8, /*width*/ 720, /*height*/ 540);
framebuffer.attachStencil(renderbuffer);

// now we check if the framebuffer is complete and ready to use.
// if not, an exception will be thrown with a message on why not (i.e. incomplete attachments)
// https://wikis.khronos.org/opengl/Framebuffer_Object#Completeness_Rules
framebuffer.checkComplete();

framebuffer.bind(); // now we're drawing to our custom framebuffer
```
Framebuffers and renderbuffers use GPU resources so you should call `dispose` when you're done.
```java
framebuffer.dispose();
renderbuffer.dispose();
```

### Default framebuffer
The default framebuffer points to the window you created and cannot be disposed manually.
```java
Framebuffer buffer = Framebuffer.DEFAULT;
buffer.bind(); // now we're drawing to the window's framebuffer
buffer.dispose(); // will throw an exception. the default framebuffer cannot be disposed since it's disposed when the window is closed
```

### Blitting
To copy one framebuffer to another, call `bindForRead` on your source and `bindForDraw` on your target, then use `Framebuffer.blit`:
```java
// https://wikis.khronos.org/opengl/Framebuffer#Blitting
Framebuffer source = myCustomFramebuffer;
Framebuffer target = Framebuffer.DEFAULT;
source.bindForRead();
target.bindForDraw();
Framebuffer.blit(
  source, target,
  /*srcX0*/ 0, /*srcY0*/ 0,
  /*srcX1*/ width, /*srcY1*/ height,
  /*targetX0*/ 0, /*targetY0*/ 0,
  /*targetX1*/ width, /*targetY1*/ height,
  /*mask*/ GL_COLOR_BUFFER_BIT,
  // you can also copy the stencil buffer by replacing that with GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT
  /*filter*/ BlitFilter.LINEAR 
);
```