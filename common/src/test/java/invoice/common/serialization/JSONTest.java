package invoice.common.serialization;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONTest {
    @Test
    void it_retrieves_string_value() {
        final var json = new JSON.Object(Map.of("Field1", "Value1", "Field2", "Value2"));
        assertEquals("Value2", json.stringValue("Field2"));
    }

    @Test
    void it_marshals_string_value() {
        final var json = new JSON.Object(Map.of("Field1", "Value1", "Field2", "Value2"));
        final var sameJson = new JSON.Object("{\"Field1\":  \"Value1\", \"Field2\":  \"Value2\"}");
        final var notEqualJson = new JSON.Object(Map.of("AnotherField1", "AnotherValue1", "AnotherField2", "AnotherValue2"));

        assertAll(
                "properties",
                () -> JSONAssert.assertEquals(
                        json.marshelled(),
                        sameJson.marshelled(),
                        JSONCompareMode.STRICT
                ),
                () -> JSONAssert.assertNotEquals(
                        json.marshelled(),
                        notEqualJson.marshelled(),
                        JSONCompareMode.STRICT
                )
        );
    }
}