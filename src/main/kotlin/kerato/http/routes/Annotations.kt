package kerato.http.routes

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @version $Id$
 */
@Retention(RetentionPolicy.RUNTIME)
annotation class get(val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class post(val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class delete(val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class put(val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class options(val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class patch(val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class at(val path : String = "" )