package org.burbokop.utils.java;

import org.burbokop.generators.Generator;
import play.api.libs.json.JsValue;

public interface Ser {
    JsValue apply(Generator.Options opt);
}