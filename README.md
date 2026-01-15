## CelestialGDX

A highly opinionated ABI-incompatible rewrite of libGDX to cut down on stuff and improve maintainability. 
It uses OpenGL 3.3, and only desktop is supported.

**This fork is currently unfinished, untested outside of my specific usecase, breaks ABI in more ways than I can count, and doesn't follow the upstream code style. Please don't use it.**  
Additionally, it's far from production ready as of now, though this may change is the future.

You can find docs [here](https://github.com/imthosea/celestialgdx/tree/master/docs).

### Example initialization
```java
CelestialGdx gdx = CelestialGdx.init(); // optionally specify a GdxLoggerFactory and/or GLFWErrorCallback supplier
Window window = gdx.createWindow(config -> {
  // these are all the defaults
  config.title = "game";
  config.windowResizable = false;
  config.windowWidth = 720;
  config.windowHeight = 540;
  config.vsync = true;
  config.stencil = 8;
  config.listener = new WindowListener() {
  	@Override public void minimized(Window window, boolean isMinimized) {}
  	@Override public void maximized(Window window, boolean isMaximized) {}
  	@Override public void resized(Window window, int width, int height) {}
    @Override 
    public void closeRequested(Window window) {
      window.gdx.markShouldClose();
  	}
  };
});
// if you use multiple windows, be sure to call Window.bind to change the active OpenGL context
InputController input = window.input;
input.setInputHandler(new InputAdapter() { // InputAdapter implements InputHandler
	@Override
	public void onKeyEvent(int key, int scancode, int action, int mods) {
		if(key == GLFW_KEY_ESCAPE) {
			gdx.markShouldClose();
		}
	}
});
while(!gdx.shouldClose()) {
  gdx.pollEvents();
  float deltaTime = gdx.updateDeltaTime();
  render(deltaTime);
  window.swapBuffers();
}
window.dispose();
gdx.terminate();
```

### Notes
- This isn't finished yet
- GDX GL classes are deprecated. Use GL classes from GLFW instead (`import static org.lwjgl.opengl.GL33.*;`)
- Many more classes are deprecated and will be removed but are currently here because other things need to be reworked first
- In the past, I manually tracked every change and removal from upstream libGDX (see README commit history), but this quickly became infeasible. No code using the libGDX API will work with this without major changes.

### Very Rough Roadmap
- rewrite tiled map system and renderer, then write docs for it
- finalize and document spride renderer
- finish removing all old libGDX classes
- make a controller, audio and UBO API
- another asset manager rewrite since I had a better idea
- new UI framework
- sequencing extension

### Setup

<details open>
<summary>Gradle (Kotlin)</summary>

```kotlin
maven {
	name = "teamcelestial"
	url = "https://maven.teamcelestial.org/public"
}
```

```kotlin
dependencies {
  implementation("me.thosea.celestialgdx:gdx:1.0.0")
}
```

</details>
<details>
<summary>Gradle (Groovy)</summary>

```groovy
maven {
    name "teamcelestial"
    url "https://maven.teamcelestial.org/public"
}
```

```groovy
dependencies {
  implementation "me.thosea.celestialgdx:gdx:1.0.0"
}
```

</details>
Currently, the version is 1.0.0.<br><br>

*This repo was initially made in late July 2025, but was deleted and reuploaded in November so it'd be disconnected from the fork network.*