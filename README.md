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
- Lwjgl3Application's constructor is now protected 
  - Use the static "create" method (or extend it) and pass an ApplicationCreator that makes an instance of your game listener
  - This is so your game can have final constants for the window, graphics etc
- Lwjgl3Application can no longer be created multiple times
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
- TEMP: TMJ map loading was removed work because I went insane
- GLFW events are now polled before rendering
- ApplicationListener.resize is no longer called on frame 1
- AssetManager loads assets in parallel now
    - Each asset is loaded on a virtual thread
    - Obtain a dependency by AssetLoadingContext.dependOn
    - If you need to do something on the main thread, use AssetLoadingContext.awaitMainThread
    - If you need to do CPU-bound work, use AssetLoadingContext.awaitWork to submit to a separate thread executor (and avoid blocking the carrier threads)
    - All asset loaders need to be rewritten for this!
- Tiled map loaders now use one MapLoader with a different load handler for each type
- Tiled maps no longer "own" resources (and thus aren't Disposable anymore), they are handled by asset dependencies now
- AssetManager's default log level is now ERROR instead of NONE
- The default SpriteBatch shader is no longer LOWP and no longer uses deprecated GLSL features
  - Also it only works on GLES 3, don't use GL 2
- The layout attribute a_texCoord0 was renamed to a_texCoord

### Removed
- SynchronousLoader / AsynchronousLoader
- FileHandle:
    - file, type, listFiles, exists, isDirectory, lastModified
    - all write-related methods
- FileHandleStream
- "Managed" resources, since Application is now singleton
- FileType
- Lwjgl3Window doesn't handle sync callbacks anymore, post to Lwjgl3Application instead
- Box2D / Bullet
- Multi-window support from LWJGL backend and subsequently a ton of syncrhonization code
- Automated tests
- Non-continuous rendering (along with ApplicationListener/LifecycleListener's pause and resume)
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
- InputEventQueue - it calls InputProcessor instantly now
- Keys.ANY_KEY
- These methods in InputProcessor / Input:
    - touchDragged, getTextInput, vibrate, setKeyboardHeightObserver, isPeripheralAvailable, getRotation, getNativeOrientation, getDeltaX, getDeltaY, resetPollingStates
- FirstPersonCameraController / CameraController
- TextInputWrapper
- GestureDetector
- ActorGestureListener
- ScrollPane flickScroll
- AtlasTmxMapLoader / AtlasTmjMapLoadHandler. Use a custom ImageResolver instead
- SpriteBatch.maxSpritesInBatch

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
- unspaghetify TextureAtlas
- remove more useless stdlib replication classes
- make TextureRegion and its subclasses immutable
- rid of tmx map parser entirely and use the official library instead (https://github.com/mapeditor/tiled/tree/master/util/java/libtiled-java)

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