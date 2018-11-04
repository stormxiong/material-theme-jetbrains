/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Chris Magnussen and Elior Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.chrisrm.idea.icons;

import com.chrisrm.idea.MTConfig;
import com.chrisrm.idea.MTThemeFacade;
import com.chrisrm.idea.listeners.AccentsListener;
import com.chrisrm.idea.listeners.MTTopics;
import com.chrisrm.idea.listeners.ThemeListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.ui.ColorUtil;
import com.intellij.util.SVGLoader;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;

public final class MTTintedIconsComponent implements BaseComponent {
  private TintedColorPatcher colorPatcher;
  private MessageBusConnection connect;

  @Override
  public void initComponent() {
    colorPatcher = new TintedColorPatcher();
    SVGLoader.setColorPatcher(colorPatcher);

    // Listen for changes on the settings
    connect = ApplicationManager.getApplication().getMessageBus().connect();
    connect.subscribe(MTTopics.ACCENTS, new AccentsListener() {
      @Override
      public void accentChanged(@NotNull final Color accentColor) {
        SVGLoader.setColorPatcher(null);
        SVGLoader.setColorPatcher(colorPatcher);
        TintedColorPatcher.refreshAccentColor(accentColor);
      }
    });

    connect.subscribe(MTTopics.THEMES, new ThemeListener() {
      @Override
      public void themeChanged(@NotNull final MTThemeFacade theme) {
        SVGLoader.setColorPatcher(null);
        SVGLoader.setColorPatcher(colorPatcher);
        TintedColorPatcher.refreshThemeColor(theme);
      }

    });

  }

  @Override
  public void disposeComponent() {
    connect.disconnect();
  }

  @Override
  @NotNull
  public String getComponentName() {
    return "MTTintedIconsComponent";
  }

  private static class TintedColorPatcher implements SVGLoader.SvgColorPatcher {
    private static String accentColor;
    private static String themedColor;

    TintedColorPatcher() {
      refreshColors();
    }

    static void refreshAccentColor(final Color accentColor) {
      TintedColorPatcher.accentColor = ColorUtil.toHex(accentColor);
    }

    static void refreshThemeColor(final MTThemeFacade theme) {
      TintedColorPatcher.themedColor = ColorUtil.toHex(theme.getTheme().getPrimaryColor());
    }

    private static void refreshColors() {
      accentColor = MTConfig.getInstance().getAccentColor();
      themedColor = ColorUtil.toHex(MTConfig.getInstance().getSelectedTheme().getTheme().getPrimaryColor());
    }

    @Override
    public void patchColors(final Element svg) {
      final String tint = svg.getAttribute("tint");
      final String themed = svg.getAttribute("themed");

      if (tint.equals("true") || tint.equals("fill")) {
        svg.setAttribute("fill", "#" + accentColor);
      } else if (tint.equals("stroke")) {
        svg.setAttribute("stroke", "#" + accentColor);
      } else if (themed.equals("true") || themed.equals("fill")) {
        svg.setAttribute("fill", "#" + themedColor);
      } else if (themed.equals("stroke")) {
        svg.setAttribute("stroke", "#" + themedColor);
      }

      final NodeList nodes = svg.getChildNodes();
      final int length = nodes.getLength();
      for (int i = 0; i < length; i++) {
        final Node item = nodes.item(i);
        if (item instanceof Element) {
          patchColors((Element) item);
        }
      }
    }
  }
}