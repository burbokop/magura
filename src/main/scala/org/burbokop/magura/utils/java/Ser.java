package org.burbokop.magura.utils.java;

import org.burbokop.magura.generators.Generator;
import play.api.libs.json.JsValue;

public interface Ser {
    JsValue apply(Generator.Options opt);
}