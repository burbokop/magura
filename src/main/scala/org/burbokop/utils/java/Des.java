package org.burbokop.utils.java;

import org.burbokop.generators.Generator;
import play.api.libs.json.JsValue;

public interface Des {
    Generator.Options apply(JsValue val);
}