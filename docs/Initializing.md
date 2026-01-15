## Initializing
Unlike libGDX, CelestialGDX makes the game's lifecycle your responbility.  
To start, obtain an instance of `CelestialGdx`.
```java
CelestialGdx gdx = CelestialGdx.init(); // initializes GLFW
```
Optionally specify a custom `GdxLoggerFactory` to change the behavior of loggers used by CelestialGDX:
```java
Map<String, Level> levels = new HashMap<>();
levels.put("AssetManager", Level.DEBUG);
GdxLoggerFactory myFactory = name -> new MyLogger(levels.getOrDefault(name, Level.INFO));
CelestialGdx.init(myFactory);
```
and/or a custom `GLFWErrorCallback` which will be called when GLFW reports an error. The default is to print to `System.err`: 
```java
CelestialGdx.init(() -> GLFWErrorCallback.create((int error, long description) -> {
  String msg = GLFWErrorCallback.getDescription(description);
  myErrorLogger.log("Error: " + msg);
}));
// (will be freed when CelestialGDX terminates)
```
Use `createWindow` to configure and make a window.
```java
Window window = gdx.createWindow(config -> {
  // these are the defaults
  config.title = "game";
  config.windowWidth = 720;
  config.windowHeight = 540;
  config.windowResizable = false;
  config.debugContext = false;
  config.vsync = true;
  config.stencil = 8;
  config.listener = new WindowListener() {
  	@Override
	public void minimized(Window window, boolean isMinimized) {}
  	@Override
	public void maximized(Window window, boolean isMaximized) {}
  	@Override
	public void resized(Window window, int width, int height) {}
    @Override 
    public void closeRequested(Window window) {
      gdx.markShouldClose();
  	}
  };
});
// setup input
InputController input = window.input;
input.setInputHandler(new InputAdapter() {
  @Override
  public void onKeyEvent(int key, int scancode, int action, int mods) {
    if(key == GLFW_KEY_ESCAPE) { // static import from GLFW
  	  gdx.markShouldClose();
    }
  }
});
```
*You can make multiple windows so long as you call `Window#bind` before rendering to each one.*  
Lastly, setup your render loop.
```java
// shouldClose returns false until gdx.markShouldClose is called
while(!gdx.shouldClose()) {
  // polls GLFW for input events and calls queued runnables
  // this should be called at the start of every frame
  gdx.poll();
  // updateDeltaTime recalculates and returns seconds since the last frame
  float deltaTime = gdx.updateDeltaTime();
  // call your own render function
  render(deltaTime);
  // swap the bufferrs so that the off-screen buffer you rendered to becomes visible
  window.swapBuffers();
}
window.dispose();
gdx.terminate();
```
When working with multiple threads, you can post a runnable that will run on the main thread when `CelestialGdx#poll` is called using `postRunnable`.