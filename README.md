# Wolftrap Visuals

A Clojure implementation for running OpenGL shaders using LWJGL interop, and connecting incoming MIDI signals from various hardware to uniforms in the shaders in order to generate complex visuals controlled by real time musical performances.

## Running

``` sh
bb run
```

## Development

Running and starting an embedded nREPL:

``` sh
bb repl
```

And then connect to the nREPL port 7888 from your editor.

### Hot reloading shaders

If you want to hot reload shaders and reset the VAOs, call this function from the REPL:

``` clojure
(require 'dev)
(dev/start-shader-watcher!)
```

Or more easily when starting the REPL using  `--watch-shaders`:

``` sh
bb repl --watch-shaders
```

When there is an issue with startup, or you want to develop something without the energy drain of render loop, you can start up a REPL without initializing OpenGL:

### Debugging startup issues

When you can't start up the program because of some issues with OpenGL calls or window creation, you can still start up a REPL with the correct paths using `--no-window` in order to connect and debug. Note that on MacOS  in order to regain a window you should stop the process and restart without the flag since the window creation calls need to be started on the main thread.

``` sh
bb repl --no-window
```

## Contributions

It's not a released engine (yet?), but if you get this to work on another infrastructure than Apple Intel, please submit a PR with your changes. I could collaborate to integrate the changes.
