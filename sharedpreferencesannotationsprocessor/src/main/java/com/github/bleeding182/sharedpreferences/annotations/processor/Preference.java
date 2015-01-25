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

package com.github.bleeding182.sharedpreferences.annotations.processor;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import com.github.bleeding182.sharedpreferences.PreferenceType;
import com.github.bleeding182.sharedpreferences.annotations.DefaultValue;
import com.github.bleeding182.sharedpreferences.annotations.Type;

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

    private static final String PARAM_DEFAULT_VALUE = "defaultValue";
    private static final String VALUE = "value";

    private final VariableElement mElement;
    private final PreferenceType mType;
    private final String mPreferenceName;
    private final String mPreferenceId;
    private final String mBooleanPrefix;
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

    public Preference(String preferenceName, String preferenceId, VariableElement element, PreferenceType defaultType) {
        mPreferenceName = preferenceName;
        mPreferenceId = preferenceId;
        mElement = element;
        Type type = element.getAnnotation(Type.class);
        if (type == null) {
            mType = defaultType != null ? defaultType : PreferenceType.STRING;
            mBooleanPrefix = "is";
        } else {
            mType = type.value();
            mBooleanPrefix = type.booleanPrefix();
        }

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
        final String prefix = mType == PreferenceType.BOOLEAN ? mBooleanPrefix : "get";

        if (hasDefaultValue) {
            writer.emitEmptyLine().emitJavadoc("gets '%s' from the preferences, <b>%s</b> if not yet set.", mPreferenceId, mDefaultValue)
                    .beginMethod(mType.getReturnType(), prefix + getPreferenceNameUpperFirst(), setPublic)
                    .emitStatement("return get%1$s(\"%2$s\", %3$s)", mType.getFullName(), mPreferenceId, mDefaultValue).endMethod();
        }

        writer.emitEmptyLine().emitJavadoc("gets '%s' from the preferences.\n@param %s the default value to use", mPreferenceId, PARAM_DEFAULT_VALUE)
                .beginMethod(mType.getReturnType(), prefix + getPreferenceNameUpperFirst(), setPublic, mType.getReturnType(), PARAM_DEFAULT_VALUE)
                .emitStatement("return get%1$s(\"%2$s\", %3$s)", mType.getFullName(), mPreferenceId, PARAM_DEFAULT_VALUE).endMethod();
    }

    public String getPreferenceNameUpperFirst() {
        return Character.toUpperCase(mPreferenceName.charAt(0)) + mPreferenceName.substring(1);
    }

    public void writeSetter(JavaWriter writer) throws IOException {
        writer.emitEmptyLine().emitJavadoc("sets '%1$s' in the preferences.\n@param %2$s the new value for %1$s", mPreferenceId, VALUE)
                .beginMethod("void", "set" + getPreferenceNameUpperFirst(), setPublic, mType.getReturnType(), VALUE)
                .emitStatement("edit().put%1$s(\"%2$s\", %3$s).apply()", mType.getFullName(), mPreferenceName, VALUE)
                .endMethod();
    }

    public void writeChainSetter(JavaWriter writer, String editorType, String editor) throws IOException {
        writer.emitEmptyLine().emitJavadoc("sets '%1$s' in the preferences.\n@param %2$s the new value for %1$s", mPreferenceId, VALUE)
                .beginMethod(editorType, "set" + getPreferenceNameUpperFirst(), setPublic, mType.getReturnType(), VALUE)
                .emitStatement("%1$s.put%2$s(\"%3$s\", %4$s)", editor, mType.getFullName(), mPreferenceName, VALUE)
                .emitStatement("return this")
                .endMethod();
    }
}
