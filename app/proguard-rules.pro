# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# required in LogFactory#getLog
-keep class android.org.apache.commons.logging.LogFactory { *; }
-keep class android.org.apache.commons.logging.impl.LogFactoryImpl { *; }

# required in LogFactoryImpl#discoverLogImplementation / LogFactoryImpl#createLogFromClass
-keep class android.org.apache.commons.logging.impl.Jdk14Logger { *; }

# required by EwsServiceXmlWriter
-keep class com.sun.xml.stream.ZephyrWriterFactory { *; }
# the below classes might be required as well
-keep class com.sun.xml.stream.ZephyrParserFactory { *; }
-keep class com.sun.xml.stream.events.ZephyrEventFactory { *; }