/* Copyright 2026 Carl Stainton. SPDX-License-Identifier: Apache-2.0 */
package io.instanto.lienzo.demo;

import com.ait.lienzo.client.core.shape.*;
import com.ait.lienzo.client.widget.panel.impl.LienzoFixedPanel;
import com.ait.lienzo.client.widget.DragContext;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;

/** The same application source runs under both GWT and TeaVM. */
public final class Showcase {
  private Showcase() {}
  public static void start(String runtime) {
    LienzoFixedPanel panel = LienzoFixedPanel.newPanel(880, 480);
    panel.getElement().id = "lienzo-panel";
    DomGlobal.document.getElementById("canvas-host").appendChild(panel.getElement());
    Layer layer = new Layer();
    panel.add(layer);
    Rectangle rectangle = new Rectangle(160, 100).setX(90).setY(110)
        .setFillColor("#dbeafe").setStrokeColor("#2563eb").setStrokeWidth(3).setDraggable(true);
    Circle circle = new Circle(55).setX(410).setY(180)
        .setFillColor("#dcfce7").setStrokeColor("#15803d").setStrokeWidth(3).setDraggable(true);
    Text label = new Text("Drag a shape", "sans-serif", 22).setX(70).setY(50).setFillColor("#172033");
    layer.add(rectangle); layer.add(circle); layer.add(label);
    HTMLElement status = (HTMLElement) DomGlobal.document.getElementById("status");
    Runnable update = () -> {
      status.textContent = runtime + " · Rectangle: " + (int) rectangle.getX() + ", "
          + (int) rectangle.getY() + " · Circle: " + (int) circle.getX() + ", " + (int) circle.getY();
      status.setAttribute("data-x", String.valueOf((int) rectangle.getX()));
      status.setAttribute("data-y", String.valueOf((int) rectangle.getY()));
    };
    DragContext[] lastDrag = new DragContext[1];
    rectangle.addNodeDragEndHandler(event -> { lastDrag[0] = event.getDragContext(); update.run(); });
    circle.addNodeDragEndHandler(event -> { lastDrag[0] = event.getDragContext(); update.run(); });
    DomGlobal.document.getElementById("restore-drag").addEventListener("click", event -> {
      if (lastDrag[0] != null) { lastDrag[0].reset(); lastDrag[0] = null; layer.batch(); update.run(); }
    });
    rectangle.addNodeMouseClickHandler(event -> {
      rectangle.setStrokeColor("#b45309"); layer.batch();
      status.setAttribute("data-selected", "rectangle");
    });
    DomGlobal.document.getElementById("reset").addEventListener("click", event -> {
      lastDrag[0] = null;
      rectangle.setX(90).setY(110).setStrokeColor("#2563eb");
      circle.setX(410).setY(180); layer.batch(); update.run();
    });
    panel.draw(); update.run();
    DomGlobal.document.documentElement.setAttribute("data-ready", "true");
  }
}
