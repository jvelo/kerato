package kerato.conversions

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

public class ConversionTests {

    val _thrown: ExpectedException = ExpectedException.none()

    @Rule
    public fun getThrown(): ExpectedException = _thrown

    enum class Enum {
        FOO,
        BAR
    }

    data class WithStringConstructor(val string: String)

    @Test
    fun test_conversion_from_string() {
        Assert.assertEquals(46, Conversions.fromString("46", Int::class))
        Assert.assertEquals(3.14, Conversions.fromString("3.14", Double::class), 0.0001)
        Assert.assertEquals(Enum.FOO, Conversions.fromString("FOO", Enum::class))
        Assert.assertEquals(WithStringConstructor("The Moops"),
                Conversions.fromString("The Moops", WithStringConstructor::class))
    }

    @Test
    fun test_conversion_from_string_to_java_class() {
        Assert.assertEquals(46, Conversions.fromString("46", javaClass<Int>()))
        Assert.assertEquals(3.14, Conversions.fromString("3.14", javaClass<Double>()), 0.0001)
        Assert.assertEquals(Enum.FOO, Conversions.fromString("FOO", javaClass<Enum>()))
        Assert.assertEquals(WithStringConstructor("The Moops"),
                Conversions.fromString("The Moops", javaClass<WithStringConstructor>()))
    }

    @Test
    fun test_conversion_of_invalid_enum_value() {
        _thrown.expect(javaClass<java.lang.IllegalArgumentException>())
        Conversions.fromString("INVALID", Enum::class)
    }

}