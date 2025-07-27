## CelestialGDX

An esoteric fork of LibGDX to cut down on stuff and improve performance.

**This fork is completely undocumented, untested outside of my specific usecase, breaks ABI in more ways than I can count, and doesn't follow the upstream code style. Please don't use it.**

### Changes
- Nuke everything except desktop LWJGL3 backend
- Remove tests
- ANGLE is now always included with the LWJGL module
- LOTS of code cleanup
- Update to Java 21
- Lwjgl3Application addLifecycleListener/removeLifecycleListener is no longer thread safe
- Many uses of reflection were removed
- Made some fields in Lwjgl3Application public for ease of use
- Lwjgl3Application's constructor has been changed to a supplier to allow final constants in the application class
- Lwjgl3Application can no longer be constructed multiple times
- InputProcessor.scrolled now takes doubles and is no longer flipped
- FileHandle is now abstract and isn't bound to system files
    - FileType has been removed. Gdx.files methods will still work (except the FileType one), but they now return instances of SystemFileHandle or ClasspathFileHandle
    - WritableFileHandle is another abstraction that contains a write() method
    - GdxRuntimeException has been replaced with throws IOException
- Window icons now take Pixmaps instead of files
- Default HdpiMode is now Pixels
- AssetManager is no longer backed by nested maps
- AssetManager now gets the loader by class; per-file-extension loaders cannot be set anymore. This may change
- TEMP: 3D model loading by AssetManager will not work
- GLFW events are now polled before rendering
- ApplicationListener.resize is no longer called on frame 1

### Removed
- FileHandle:
    - file, type, listFiles, exists, isDirectory, lastModified
    - all write-related methods
- FileHandleStream
- FileType
- Box2D / Bullet
- Multi-window support from LWJGL backend and subsequently a ton of syncrhonization code
- Automated tests
- Non-continous rendering (along with ApplicationListener/LifecycleListener's pause and resume)
- ApplicationListener.create - do stuff in your constructor instead
- glfw_async, on Mac, you must call with -XstartOnFirstThread
- LwjglCanvas
- The entire reflection util package
- gdx-tools - use upstream
- ReflectionPool
- LWJGL initialBackgroundColor
- Audio - use gdx-miniaudio instead
- Netcode - use stdlib instead
- Preferences - use your own solution
- InputEventQueue
- Keys.ANY_KEY
- These methods in InputProcessor / Input:
- touchDragged, getTextInput, vibrate, setKeyboardHeightObserver, isPeripheralAvailable, getRotation, getNativeOrientation has been removed, getNativeOrientation, getDeltaX, getDeltaY, resetPollingStates
- FirstPersonCameraController / CameraController
- TextInputWrapper
- GestureDetector
- ActorGestureListener
- ScrollPane flickScroll
- AtlasTmxMapLoader

### Notes
- For gdx-controllers, bypass Controllers and create JamepadControllerManager directly instead
- The Gdx class remains for compatibility, but please don't use them - mutable static constants are questionable for maintainability. Use the function constructor in Lwjgl3Application and get the window to initialize your constants
- GDX GL classes are deprecated. Use GL classes from GLFW instead

### TODO maybe
- rewrite Sync (fps cap) implementation
- possibly remove 3D
- remove use of deprecated code
- remove reflection entirely
- remove Json class (it is a MESS) in favor of Jankson
- remove XML parser in favor of stdlib
- unspaghetify TMX reader and TextureAtlas
- remove more useless stdlib replication classes
- make TextureRegion and its subclasses immutable
- make AssetManager concurrent and maybe use virtual threads for it

This isn't a full code cleanup because I want to make my game. I'm only modifying stuff that affects me.

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
configurations.all { 
  val version = "1.0.0-SNAPSHOT"
  // if using version catalogue:
  // val version = libs.versions.celestialgdx.get()
    
  resolutionStrategy.dependencySubstitution {
    fun sub(artifact: String) = substitute(
      module("com.badlogicgames.gdx:$artifact")
    ).using(
      module("me.thosea.celestialgdx:$artifact:$version")
    ).because("fork")

    sub("gdx")
    sub("gdx-freetype")
    sub("gdx-backend-lwjgl3")
    sub("gdx-lwjgl3-angle")
  }
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
configurations.all {
  val version = "1.0.0-SNAPSHOT"
  resolutionStrategy.dependencySubstitution {
    def sub = { String artifact ->
        substitute module("com.badlogicgames.gdx:$artifact") using module("me.thosea.celestialgdx:$artifact:$version") because "fork"
    }
    sub("gdx")
    sub("gdx-freetype")
    sub("gdx-backend-lwjgl3")
    sub("gdx-lwjgl3-angle")
  }
}
```

</details>
Currently, the version is 1.0.0-SNAPSHOT.
