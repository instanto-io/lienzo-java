# Lienzo Java

Lienzo Java is an **alpha port** of Lienzo's Java canvas API for modern GWT and TeaVM.
It preserves the `com.ait.lienzo` packages and keeps one library source tree. The GWT build
uses GWT and Elemental2; the TeaVM build uses Instanto's GWT, Elemental2 and JsInterop
compatibility libraries. It has no Sarto runtime dependency.

Lienzo draws shapes onto HTML canvas. Its layers, shapes, transforms, hit detection and input
handlers let a Java application build an interactive drawing surface. Its Wires classes add
connectors, magnets and diagram editing controls. This repository imports the full core source,
but compilation is not a guarantee that every feature behaves correctly under TeaVM.

## Current coverage

The first shared showcase exercises canvas creation, layers, rectangles, circles, text, click
selection, dragging and restoration of a drag. The same Java application is compiled separately
by GWT and TeaVM. Tests inspect rendered pixels and use browser input, rather than checking only
that a canvas exists.

The broader Wires surface, animation, image filters, video, export, high-DPI rendering and lifecycle
behaviour still need dedicated browser coverage. Touch is not yet part of the verified baseline.
The JavaScript-facing `JsCanvas` integration retains upstream global state; see
[upstream changes and limits](UPSTREAM.md) before using multiple independent canvases through it.

## Choose a compiler stream

For GWT, use `io.instanto:lienzo-gwt:0.1.0-SNAPSHOT` and inherit the existing module:

```xml
<inherits name="com.ait.lienzo.Lienzo"/>
```

For TeaVM, use:

```xml
<dependency>
  <groupId>io.instanto</groupId>
  <artifactId>lienzo-teavm</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The two artifacts define the same Java packages. Choose one for each application build; do not
put both on the same classpath. The TeaVM artifact brings in the compatibility runtime. GWT
applications use upstream GWT and Elemental2 dependencies as shown in the GWT showcase POM.
These alpha coordinates describe the local build; they are not yet a Maven Central release.

## Put a drawing surface in your application

A panel owns the canvas viewport. Add a layer, put shapes on it, and attach the panel's element to
your application's DOM. This example expects an element with the ID `drawing` to exist:

```java
LienzoFixedPanel panel = LienzoFixedPanel.newPanel(800, 500);
DomGlobal.document.getElementById("drawing").appendChild(panel.getElement());
Layer layer = new Layer();
panel.add(layer);
Rectangle box = new Rectangle(160, 90)
    .setX(80).setY(100)
    .setFillColor("#dbeafe")
    .setStrokeColor("#2563eb")
    .setDraggable(true);
layer.add(box);
panel.draw();
```

`LienzoFixedPanel` is in `com.ait.lienzo.client.widget.panel.impl`; `Layer` and `Rectangle` are in
`com.ait.lienzo.client.core.shape`; `DomGlobal` is in `elemental2.dom`. The browser supplies canvas
itself. There is no separate Lienzo JavaScript library to download: your compiler includes the
reachable Java implementation in the application output.

## Respond to editing

Register a handler on a shape to receive interaction events. For example,
`box.addNodeDragEndHandler(event -> savePosition(box.getX(), box.getY()))` lets an application
persist a completed move. `savePosition` is application code. Call `layer.batch()` after
programmatic changes to request a redraw.

Lienzo core does **not** provide a general undo/redo stack. Its existing `DragContext.reset()` API
restores the node's position before a particular drag. The showcase retains the context from
`event.getDragContext()` and demonstrates that API with **Restore last drag**. It does not imply
multi-step undo or redo. KIE Stunner's command history is a separate framework above Lienzo and
is not included. An optional history integration can be added without replacing these APIs.

## Build and run the showcase

Use JDK 21. The build uses GWT 2.13.1 and TeaVM 0.16.0. It also needs the current
[teavm-compat](https://github.com/instanto-io/teavm-compat) artifacts, including the generic native
result conversion and `ImageResourcePrototype` support introduced for this port. CI pins compatibility revision
`94146a0a9860123693229ebb654cbc01ca61df6e` so its builds can be reproduced.

From the repository root:

```shell
./mvnw clean install -DskipTests
./mvnw -f lienzo-showcase-gwt/pom.xml \
  org.codehaus.mojo:exec-maven-plugin:3.6.3:java \
  -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium webkit"
./mvnw verify
```

The browser installation is needed once per matching Playwright version. `verify` runs the
upstream Java tests against both library artifacts and the showcase tests in Chromium and WebKit.
Screenshots and browser reports are under each showcase module's `target` directory. SpotBugs
reports are also generated; the narrowly documented compatibility exceptions are described in
[UPSTREAM.md](UPSTREAM.md). A production build uses `./mvnw -Pproduction verify`.

Serve a compiled showcase to try it locally:

```shell
python3 -m http.server 8080 --directory lienzo-showcase-teavm/target/site
```

Open [localhost:8080](http://localhost:8080/). To try GWT instead, serve
`lienzo-showcase-gwt/target/site`. The examples use the same application Java source.

## Credits and funding

Lienzo was created by Ahome' Innovation Technologies and Dean S. Jones, and developed further by
KIE contributors. This port is based on the [Apache KIE source pinned in UPSTREAM.md](UPSTREAM.md),
with the original API, source headers and applicable notices retained. It is an independent port,
not an official Apache release. The source is distributed under [Apache License 2.0](LICENSE);
see [NOTICE](NOTICE) for attribution.

To support upstream Apache projects, [donate to the Apache Software Foundation](https://www.apache.org/foundation/contributing.html).
This is foundation-wide funding, not a dedicated Lienzo sponsorship programme. You can also
[sponsor Instanto](https://github.com/sponsors/instanto-io) and
[sponsor TeaVM](https://github.com/sponsors/konsoletyper).
