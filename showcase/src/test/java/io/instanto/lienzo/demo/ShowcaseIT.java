/* Copyright 2026 Carl Stainton. SPDX-License-Identifier: Apache-2.0 */
package io.instanto.lienzo.demo;

import static org.junit.Assert.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ShowcaseIT {
  @Parameterized.Parameters(name="{0}")
  public static Object[][] engines() { return new Object[][]{{"chromium"}, {"webkit"}}; }
  @Parameterized.Parameter public String engine;
  private HttpServer server;
  private Playwright playwright;
  private Browser browser;
  private Page page;
  private final List<String> errors = new ArrayList<>();

  @Before public void start() throws Exception {
    Path root = Path.of("target/site").toAbsolutePath().normalize();
    server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/", exchange -> {
      String name = exchange.getRequestURI().getPath();
      Path file = root.resolve(name.equals("/") ? "index.html" : name.substring(1)).normalize();
      if (!file.startsWith(root) || !Files.isRegularFile(file)) { exchange.sendResponseHeaders(404,-1); exchange.close(); return; }
      byte[] body = Files.readAllBytes(file);
      exchange.getResponseHeaders().set("Content-Type",name.endsWith(".js") ? "application/javascript" : "text/html");
      exchange.sendResponseHeaders(200,body.length);
      try(var out=exchange.getResponseBody()){out.write(body);}
    });
    server.start();
    playwright = Playwright.create();
    browser = (engine.equals("chromium") ? playwright.chromium() : playwright.webkit()).launch();
    page=browser.newPage(new Browser.NewPageOptions().setViewportSize(1100,900));
    page.setDefaultTimeout(8000);
    page.onPageError(errors::add);
    page.navigate("http://127.0.0.1:"+server.getAddress().getPort()+"/");
    page.waitForSelector("html[data-ready=true]");
  }
  @After public void stop() {
    if(!errors.isEmpty()) System.err.println(errors);
    if(browser!=null) browser.close();
    if(playwright!=null) playwright.close();
    if(server!=null) server.stop(0);
  }
  @Test public void rendersSelectsDragsAndResets() {
    BoundingBox b=page.locator("#lienzo-panel").boundingBox();
    assertTrue(page.locator("#lienzo-panel canvas").count()>0);
    // Check pixels from the visible layer, not just Java state or a created canvas.
    Object painted=page.locator("#lienzo-panel").evaluate("el => [...el.querySelectorAll('canvas')].some(c => { const d=c.getContext('2d').getImageData(110,130,1,1).data; return d[3]>0; })");
    assertEquals(true,painted);
    page.mouse().click(b.x+130,b.y+150);
    page.waitForSelector("#status[data-selected=rectangle]");
    page.mouse().move(b.x+130,b.y+150);
    page.mouse().down();
    page.mouse().move(b.x+210,b.y+210,new Mouse.MoveOptions().setSteps(12));
    page.mouse().up();
    // Lienzo begins a drag after its movement threshold, rather than at pointer-down.
    page.waitForFunction("() => { const s=document.querySelector('#status'); const x=+s.dataset.x, y=+s.dataset.y; return x>=150 && x<=180 && y>=150 && y<=180; }");
    page.locator("#restore-drag").click();
    page.waitForSelector("#status[data-x='90'][data-y='110']");
    page.locator("#reset").click();
    page.waitForSelector("#status[data-x='90'][data-y='110']");
    assertTrue("Browser errors: "+errors, errors.isEmpty());
    page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/showcase-"+engine+".png")).setFullPage(true));
  }
}
