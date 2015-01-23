/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 David Medenjak
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
 */

package at.bleeding182.sharedpreferences.annotations.processor;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import at.bleeding182.sharedpreferences.PreferenceType;
import at.bleeding182.sharedpreferences.annotations.DefaultValue;
import at.bleeding182.sharedpreferences.annotations.Type;

/**
 * @author David Medenjak
 * @version 1.0
 */
public class Preference {
    private static final Set<Modifier> setPublic;

    static {
        setPublic = new HashSet<>();
        setPublic.add(Modifier.PUBLIC);
    }

    private static final String DEFAULT_VALUE = "defaultValue";

    private final VariableElement mElement;
    private final PreferenceType mType;
    private final String mPreferenceName;
    private final boolean hasDefaultValue;
    private final String mDefaultValue;

    public static String camelCaseName(String name) {
        String[] split = name.toLowerCase().split("_");
        String ret = split[0];
        for (int i = 1; i < split.length; i++) {
            ret += Character.toUpperCase(split[i].charAt(0)) + split[i].substring(1);
        }
        return ret;
    }

    public Preference(String preferenceName, VariableElement element, PreferenceType defaultType) {
        mPreferenceName = preferenceName;
        mElement = element;
        Type type = element.getAnnotation(Type.class);
        if (type == null)
            mType = defaultType != null ? defaultType : PreferenceType.STRING;
        else
            mType = type.value();

        DefaultValue defValue = element.getAnnotation(DefaultValue.class);
        if (defValue != null) {
            hasDefaultValue = true;
            mDefaultValue = defValue.value();
        } else {
            hasDefaultValue = false;
            mDefaultValue = null;
        }
    }

    public VariableElement getElement() {
        return mElement;
    }

    public void writeGetter(JavaWriter writer) throws IOException {
        writer.emitJavadoc("gets the " + mPreferenceName + " from the preferences.");

        if (hasDefaultValue)
            writer.beginMethod(mType.getReturnType(), "get" + getPreferenceNameUpperFirst(), setPublic)
                    .emitStatement("return get%1$s(\"%2$s\", %3$s)", mType.getFullName(), mPreferenceName, mDefaultValue);
        else
            writer.beginMethod(mType.getReturnType(), "get" + getPreferenceNameUpperFirst(), setPublic, mType.getReturnType(), DEFAULT_VALUE)
                    .emitStatement("return get%1$s(\"%2$s\", %3$s)", mType.getFullName(), mPreferenceName, DEFAULT_VALUE);

        writer.endMethod();
    }

    public String getPreferenceNameUpperFirst() {
        return Character.toUpperCase(mPreferenceName.charAt(0)) + mPreferenceName.substring(1);
    }
}
