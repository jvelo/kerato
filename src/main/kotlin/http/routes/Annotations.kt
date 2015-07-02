package http.routes

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @version $Id$
 */
@Retention(RetentionPolicy.RUNTIME)
annotation class Get (val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class Post (val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class Delete (val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class Put (val path : String = "" )

@Retention(RetentionPolicy.RUNTIME)
annotation class At (val path : String = "" )