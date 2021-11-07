package org.burbokop.magura.utils.java;

import org.burbokop.magura.generators.Generator;
import play.api.libs.json.JsValue;

public interface Des {
    Generator.Options apply(JsValue val);
}