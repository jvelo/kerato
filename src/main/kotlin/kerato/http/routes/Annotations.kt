package kerato.http.routes

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

Retention(RetentionPolicy.RUNTIME)
annotation class at(val path: String = "")

Retention(RetentionPolicy.RUNTIME)
Target(ElementType.METHOD)
annotation class get(val path: String = "")

Retention(RetentionPolicy.RUNTIME)
Target(ElementType.METHOD)
annotation class post(val path: String = "")

Retention(RetentionPolicy.RUNTIME)
Target(ElementType.METHOD)
annotation class delete(val path: String = "")

Retention(RetentionPolicy.RUNTIME)
Target(ElementType.METHOD)
annotation class put(val path: String = "")

Retention(RetentionPolicy.RUNTIME)
Target(ElementType.METHOD)
annotation class options(val path: String = "")

Retention(RetentionPolicy.RUNTIME)
Target(ElementType.METHOD)
annotation class patch(val path: String = "")
