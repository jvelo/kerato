package kerato.conversions

import kotlin.reflect.KClass
import kotlin.reflect.jvm.java as joe

object Conversions {

    fun <T> fromString(value: String, type: KClass<T>): T {
        return fromString(value, type.joe)
    }

    suppress("UNCHECKED_CAST")
    fun <T> fromString(value: String, javaClass: Class<T>): T {

        if (javaClass.isPrimitive()) {
            return javaPrimitiveFromString(value, javaClass)
        }

        val valueOfMethod = try {
            javaClass.getMethod("valueOf", javaClass<String>())
        } catch (e: NoSuchMethodException) {
            null
        }
        if (valueOfMethod != null) {
            try {
                return valueOfMethod.invoke(null, value) as T
            } catch (e: java.lang.reflect.InvocationTargetException) {
                throw e.getTargetException()
            }
        }

        val stringConstructor = try {
            javaClass.getConstructor(javaClass<String>())
        } catch (e: NoSuchMethodException) {
            null
        }
        if (stringConstructor != null) {
            try {
                return stringConstructor.newInstance(value)
            } catch (e: java.lang.reflect.InvocationTargetException) {
                throw e.getTargetException()
            }
        }

        return null;
    }

    suppress("UNCHECKED_CAST")
    private fun <T> javaPrimitiveFromString(value: String, javaClass: Class<T>): T {
        return when {
            javaClass<Short>().isAssignableFrom(javaClass) -> java.lang.Short.valueOf(value) as T
            javaClass<Int>().isAssignableFrom(javaClass) -> java.lang.Integer.valueOf(value) as T
            javaClass<Long>().isAssignableFrom(javaClass) -> java.lang.Long.valueOf(value) as T
            javaClass<Char>().isAssignableFrom(javaClass) -> value.charAt(0) as T
            javaClass<Float>().isAssignableFrom(javaClass) -> java.lang.Float.valueOf(value) as T
            javaClass<Double>().isAssignableFrom(javaClass) -> java.lang.Double.valueOf(value) as T
            javaClass<Boolean>().isAssignableFrom(javaClass) -> java.lang.Boolean.valueOf(value) as T
            else -> null
        }
    }

}
