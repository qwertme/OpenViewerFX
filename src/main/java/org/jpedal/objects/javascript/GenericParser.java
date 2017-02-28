/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2015 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * GenericParser.java
 * ---------------
 */
package org.jpedal.objects.javascript;


import org.jpedal.objects.Javascript;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.javascript.jsobjects.JSConsole;
import org.jpedal.objects.javascript.jsobjects.JSDoc;
import org.jpedal.objects.javascript.jsobjects.JSField;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.utils.LogWriter;

import javax.script.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GenericParser implements ExpressionEngine {

    private static final String[] engineOrder = {"nashorn", "rhino"};
    private AcroRenderer acroRenderer;
    private ScriptEngine engine;
    private final ScriptContext context;
    private JSDoc docObject;
    private final Javascript jsObject;
    // <end-demo>

    private static final boolean debugEngine = false;
    private static ArrayList<String> erroredCode;

    public GenericParser(final Javascript jsObject) throws Exception {
        this.jsObject = jsObject;
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        int i = 0;
        while (engine == null && i < engineOrder.length) {
            engine = engineManager.getEngineByName(engineOrder[i]);
            i++;
        }
        if (engine == null) {
            throw new Exception("Could not load a suitable ScriptEngine for parsing JavaScript, are you using a fully fledged JVM?");
        } else {
            if (debugEngine) {
                final ScriptEngineFactory factory = engine.getFactory();
                System.out.println("Using JavaScript Engine: " + factory.getEngineName());
                System.out.println("Engine Version:" + factory.getEngineVersion());
                System.out.println("Language Version: " + factory.getLanguageVersion());
            }
            context = engine.getContext();
        }
    }

    // <end-demo>

    public void setupPDFObjects(final Javascript jsObject) {
        // Insert code for setting up PDF objects here
        try {
            if (debugEngine) {
                System.out.println("Setting up Java bindings of objects.");
            }
            docObject = new JSDoc();
            docObject.setAcroRenderer(acroRenderer);
            context.setAttribute("JSDoc", docObject, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("app", new JSApp(), ScriptContext.ENGINE_SCOPE);
//			context.setAttribute("event", new JSEvent(), ScriptContext.ENGINE_SCOPE);
            final JSConsole console = new JSConsole();
            context.setAttribute("console", console, ScriptContext.ENGINE_SCOPE);
            // <end-demo>
            if (debugEngine) {
                System.out.println("Parsing aform.js");
            }

            final BufferedReader JSObjectsReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org/jpedal/objects/javascript/jsobjects/JSObjects.js")));
            engine.eval(JSObjectsReader);
            engine.eval("var event = new Event();");

            final BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org/jpedal/objects/javascript/aform.js")));
            engine.eval(br);
            final String onLoadJS = preParseJS(jsObject.getJavaScript(null), true);
            if (onLoadJS != null && !onLoadJS.isEmpty()) {
                if (debugEngine) {
                    System.out.println(onLoadJS);
                }
                try {
                    engine.eval(onLoadJS);
                } catch (final ScriptException e) {
//					debugLog("setup Code: " + onLoadJS + "\r\n" + e.getMessage() + "\r\n");
                    e.printStackTrace();
                }
            }
        } catch (final ScriptException e) {
//			debugLog("aforms error:\r\n" + e.getMessage() + "\r\n");
            e.printStackTrace();
        }
    }

    @Override
    public int execute(final FormObject ref, final int type, Object js, final int eventType, final char keyPressed) {

        // ignore unknown keypresses
        if (keyPressed == 65535) {
            return 0;
        }

        if (debugEngine) {
            System.out.println("execute(FormObject ref, int type, Object js, int eventType, char keyPressed)");
            System.out.println("execute(" + '[' + ref.getObjectRefAsString() + ']' + ", " + PdfDictionary.showAsConstant(type) + ", \"" + js + "\", "  + ", " + keyPressed + ')');
        }

        if (js instanceof String) {
            js = preParseJS((String) js, false);
            try {
                // The following line causes issues in Java 8, It was not needed anyway as we change what "event" refers to which should tell the gc to delete the object.
//			context.removeAttribute("event", ScriptContext.ENGINE_SCOPE); // remove the event
//			JSEvent event = new JSEvent(type);
                engine.eval("var event = new Event(" + type + ");");
                engine.eval("event.target = JSDoc.getFieldByRef('" + ref.getObjectRefAsString() + "');");
                engine.eval("event.value = '" + ref.getValue() + "';");
            } catch (final ScriptException ex) {
                Logger.getLogger(GenericParser.class.getName()).log(Level.SEVERE, null, ex);
            }
//			context.setAttribute("event", event, ScriptContext.ENGINE_SCOPE); // create a new event object for each event
            //docObject.setField(ref.getObjectRefAsString(), ref);
//			event.target = docObject.getFieldByRef(ref.getObjectRefAsString());
//			event.value = String.valueOf(ref.getValue());
//			System.out.println("HI:=" + ref.getValue());
            final Object returnObject;
            try {
//				engine.eval("app.alert(\"This is a test\");"); // basic alert test, works!
//				engine.eval("app.alert(\"This is a test\", 1, 0);"); // advanced alert test, works!
//				engine.eval("var a = new Array(\"RGB\", 0.5, 0.5, 1); console.log(a); var s = ColorConvert(a, \"CMYK\"); console.log(s);"); // Colorspace test, works!
                returnObject = engine.eval((String) js, context);
                final Object eventTarget = engine.eval("event.target");
                final Object eventValue = engine.eval("event.value");
                if (eventTarget != null && eventType == ActionHandler.FOCUS_EVENT) {
                    final JSField field = (JSField) eventTarget;
                    field.value = eventValue;
                    final boolean isSelected = false;
                    field.syncToGUI(isSelected);
                }
                if (returnObject != null) {
                    // Do stuff with the result?
                    if (debugEngine) {
                        System.out.println("returnObject=" + returnObject);
                    }
                }
                final Object event = engine.eval("event");
                final Object eventName = engine.eval("event.name");
                if (event != null && eventName != null && eventName.equals("Format")) {
                    calcualteEvent();
                }
            } catch (final ScriptException e) {
//				debugLog("execute Code: " + (String) js + "\r\n" + e.getMessage() + "\r\n");
                e.printStackTrace();
            }

        }

        return 0;
    }

    @Override
    public void closeFile() {
        flush();
    }

    @Override
    public boolean reportError(final int code, final Object[] args) {
        // This Method doesn't appear to be used in the DefaultParser or RhinoParser
        if (debugEngine) {
            System.out.println("reportError(int code, Object[] args)");
            System.out.println("reportError(" + code + ", " + Arrays.toString(args) + ')');
        }
        return false;
    }

    @Override
    public int addCode(String value) {
        value = preParseJS(value, true);
        if (debugEngine) {
            System.out.println("addCode(String value)");
            System.out.println("value={\n" + value + "\n}");
        }
        final String finalValue = value;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    engine.eval(finalValue, context); // attempt to evaluate given code
                } catch (final ScriptException e) {
//					debugLog("addCode Code: " + finalValue + "\r\n" + e.getMessage() + "\r\n");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };
        SwingUtilities.invokeLater(r);
        return 0;
    }

    @Override
    public void executeFunctions(final String jsCode, final FormObject formObject) {
        if (debugEngine) {
            System.out.println("executeFunctions(String jsCode, FormObject formObject)");
            System.out.println("executeFunctions(\"" + jsCode + "\", [" + formObject.getObjectRefAsString() + "])");
        }
        //does nothing in default
    }

    @Override
    public void dispose() {
        if (debugEngine) {
            System.out.println("dispose()");
        }
    }

    @Override
    public void setAcroRenderer(final AcroRenderer acro) {
        acroRenderer = acro;
        docObject.setAcroRenderer(acro);
    }

    private void flush() {
        if (debugEngine) {
            System.out.println("flush()");
        }
        // This Method doesn't appear to be used in the DefaultParser but does in the RhinoParser
        docObject.flush();
    }

    private void calcualteEvent() {
//		System.out.println("CALC");
        final FormObject[] formObjects = docObject.getFormObjects();
        for (final FormObject formObject : formObjects) {
            final String ref = formObject.getObjectRefAsString();
            final String name = formObject.getTextStreamValue(PdfDictionary.T);
            String command = (String) jsObject.getJavascriptCommand((name != null ? name : ref), PdfDictionary.C2);
//            System.out.println(command);
            if (command != null) {
                command = preParseJS(command, false);
//				System.out.println("execute calc=" + command);
                //JSEvent event = new JSEvent(PdfDictionary.C2);
                //context.setAttribute("event", event, ScriptContext.ENGINE_SCOPE); // create a new event object for each event
                //event.target = docObject.getFieldByRef(ref);
                try {
                    engine.eval("var event = new Event(" + PdfDictionary.C2 + ");", context);
                    engine.eval("event.target = JSDoc.getFieldByRef('" + ref + "');", context);

                    engine.eval(command, context);

                    final JSField field = (JSField) engine.eval("event.target", context);
                    final Boolean rc = (Boolean) engine.eval("event.rc", context);
//                    System.out.println(field);
                    if (field != null && rc) {
                        final Object value = engine.eval("event.value", context);
                        if (value != null) {
                            field.value = value.toString();
                        } else {
                            field.value = null;
                        }
                        final boolean isSelected = false;
                        field.syncToGUI(isSelected);
                    }
                } catch (final ScriptException e) {
//					debugLog("Calculate Code: " + command + "\r\n" + e.getMessage() + "\r\n");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        }
    }

    private static String preParseJS(String js, final boolean isDocumentLevel) {
        js = addMethodstoObject(makeGlobalVars(js));
        js = fixGetFields(js);
        if (isDocumentLevel) {
//			String testSolution = "this.getField = function(f) {return Doc.getField(f);}\n";
//			js = js.replace("this", "Doc"); // temp
//			js = fixCallsToThisVars(js);
            js = "(function() {" + js + "}).call(Doc);";
        }
        // Page 709 of JavaScript for Acrobat API Reference for details on what `this` should refer to
        // In most cases it should refer to the current Doc object, hence the current code

        return js;
    }

//	private String fixCallsToThisVars(String js) {
//
//		String lines[] = js.split("\\n");
//		HashMap<String, Integer> defined = new HashMap<String, Integer>();
//		HashMap<String, Integer> definedToLine = new HashMap<String, Integer>();
//		int bracketCount = 0;
//		int i = 0;
//		for(String line : lines) {
//			i ++;
//			if(bracketCount == 0) {
////				Pattern pat = Pattern.compile("this.\\w+\\s?=\\s?");
////				Pattern pat = Pattern.compile("this\\.");
////				Matcher mat = pat.matcher(line);System.out.println("call me " +mat.groupCount());
////				if(mat.groupCount() >= 1) {
////					String s = mat.group();
////					s = s.replace("this.", "");
////					s = s.substring(0, s.indexOf("="));
////					System.out.println("F OFF " + s);
////					defined.put(bracketCount, s);
////				}
//				int thisPos = line.indexOf("this.");
//				int equalsPs = line.indexOf("=");
//				if(thisPos != -1 && equalsPs != -1) {
//					String s = line.substring(thisPos, equalsPs);
//					if(s.endsWith(" ")) {
//						s = s.substring(0, s.lastIndexOf(" "));
//					}
//					if(defined.containsKey(s) && defined.get(s) >= bracketCount) {
//						lines[i] = line.replace(s, "var " + )
//					}
//					defined.put(s, bracketCount);
//					definedToLine.put(s, i);
//				}
//			}
//			if(line.endsWith("{")) {
//				bracketCount ++;
//			}
//			else if(bracketCount > 0 && line.startsWith("}")) {
//				bracketCount --;
//			}
//			"this.\\w+\\s?=\\s?"
//		}
//		return js;  //To change body of created methods use File | Settings | File Templates.
//	}

    /**
     * changes references to this.getField to Doc.getField and also turns references to getField to Doc.getField
     *
     * @param js
     * @return
     */
    private static String fixGetFields(String js) {
        final Pattern pat = Pattern.compile("[^.]getField\\(");
        final Matcher mat = pat.matcher(js);
        while (mat.find()) {
            final String s = mat.group();
            js = js.replace(s, s.charAt(0) + "Doc.getField(");
        }
        js = js.replace("this.getField(", "Doc.getField(");
        return js;
    }

    /**
     * Turns function declarations like function thisIsAFunction(param) {...} into:
     * this.thisIsAFunction = function(param) {...}
     *
     * @param js
     * @return
     */
    private static String addMethodstoObject(final String js) {
        final Pattern pat = Pattern.compile("function\\s\\w+\\((\\w+)?\\)");
        final Matcher mat = pat.matcher(js);
        final HashMap<String, String> mapping = new HashMap<String, String>();
        while (mat.find()) {
            final String s = mat.group();
            final String methodSig = js.substring(mat.start() + 9, mat.end()); // showCurrent() or augment(digit)
            final int firstBracket = methodSig.indexOf('(');
            final String methodName = methodSig.substring(0, firstBracket);
            final String newMethodSig = "this." + methodName + " = " + "function" + methodSig.substring(firstBracket);
            mapping.put(s, newMethodSig);
        }

        String newJs = js;
        for (final String s : mapping.keySet()) {
            newJs = newJs.replace(s, mapping.get(s));
        }

        return newJs;
    }

    private static String makeGlobalVars(final String js) {
        final String trimmed = trimToGlobal(js);
        final Pattern pat = Pattern.compile("var\\s\\w+\\s?=\\s?");
        final Matcher mat = pat.matcher(trimmed);
        final HashMap<String, String> mapping = new HashMap<String, String>();
        while (mat.find()) {
            final String s = mat.group();
            final String sig = trimmed.substring(mat.start() + 4, mat.end());
            int nameEndPos = 0;
            while (sig.charAt(nameEndPos) != ' ' && sig.charAt(nameEndPos) != '=' && nameEndPos < sig.length()) {
                nameEndPos++;
            }
            final String name = sig.substring(0, nameEndPos);
            mapping.put(s.substring(0, 4 + name.length()), "this." + name);
        }

        String newJs = js;
        for (final String s : mapping.keySet()) {
            newJs = newJs.replace(s, mapping.get(s));
        }
        if (debugEngine) {
            System.out.println(newJs);
        }
        return newJs;
    }

    private static String trimToGlobal(String js) {
        // only show the text that is not within code blocks or quotes
        final StringBuilder sb = new StringBuilder();
        boolean inCurlyBrackets = false;
        boolean inBrackets = false;
        boolean inQuotes = false;
        boolean inDblQuotes = false;
        int index = 0;
        while (index < js.length()) {
            final char c = js.charAt(index);

            if (!inDblQuotes) {
                if (c == '\"') {
                    inDblQuotes = true;
                } else {
                    if (!inQuotes) {
                        if (c == '\'') {
                            inQuotes = true;
                        } else {
                            if (!inCurlyBrackets) {
                                if (c == '{') {
                                    inCurlyBrackets = true;
                                } else {
                                    if (!inBrackets) {
                                        if (c == '(') {
                                            inBrackets = true;
                                        } else if (c != ')' && c != '}') {
                                            sb.append(c);
                                        }
                                    } else {
                                        if (c == ')') {
                                            inBrackets = false;
                                        }
                                    }
                                }
                            } else {
                                if (c == '}') {
                                    inCurlyBrackets = false;
                                }
                            }
                        }
                    } else {
                        if (c == '\'') {
                            inQuotes = false;
                        }
                    }
                }
            } else {
                if (c == '\"') {
                    inDblQuotes = false;
                }
            }
            index++;
        }
        js = sb.toString();
//		js = js.replaceAll("var\\s", "this.");
        return js;
    }

    public static void debugLog(final String log) {
        final File logfile = new File("JSErrorLog.txt");
        try {
            if (logfile.createNewFile()) {
                System.err.println("Javascript error log file created: " + logfile.getAbsolutePath());
            }
            if (erroredCode == null) {
                erroredCode = new ArrayList<String>();
            }
            if (erroredCode.contains(log)) {
                return;
            }
            final BufferedWriter out = new BufferedWriter(new FileWriter(logfile, true));
            out.write(log);
            out.close();
            erroredCode.add(log);
        } catch (final IOException e) {
            if(LogWriter.isOutput()) {
                LogWriter.writeLog("Exception: " + e.getMessage());
            }
            //
        }
    }

}
