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

When there is an issue with startup, or you want to develop something without the energy drain of render loop, you can start up a REPL without initializing OpenGL:

``` sh
bb repl --no-window
```
