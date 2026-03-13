/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.model;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IOCTypeTests {

    private final ImmutableList<String> invalidTypes = ImmutableList.of(
            "domain_name", // not a supported type
            "", // type cannot be empty
            " " // type cannot be blank
    );

    private final ImmutableList<String> invalidFormatTypes = new ImmutableList.Builder<String>()
            .addAll(invalidTypes)
            .addAll(IOCType.types.stream().map((type) ->
                    // Should be lowercase to match the supported types
                    type.toUpperCase(Locale.ROOT)).collect(Collectors.toList())
            )
            .build();

    @Test
    public void test_constructor_withValidInput() {
        IOCType.types.forEach((type) -> assertEquals(type, new IOCType(type).toString()));
    }

    @Test
    public void test_constructor_withInvalidInput() {
        invalidTypes.forEach((type) -> assertThrows(IllegalArgumentException.class, () -> new IOCType(type)));
    }

    @Test
    public void test_constructor_withNullInput() {
        assertThrows(IllegalArgumentException.class, () -> new IOCType(null));
    }

    // types
    @Test
    public void test_types_returnsExpectedList() {
        final List<String> expectedTypes = List.of(
                IOCType.DOMAIN_NAME_TYPE,
                IOCType.HASHES_TYPE,
                IOCType.IPV4_TYPE,
                IOCType.IPV6_TYPE
        );

        final List<String> output = IOCType.types;

        assertEquals(expectedTypes.size(), output.size());
        expectedTypes.forEach((type) -> assertTrue(output.contains(type)));
    }

    @Test
    public void test_fromString_withValidInput() {
        IOCType.types.forEach((type) -> assertEquals(type, IOCType.fromString(type)));
    }

    @Test
    public void test_fromString_withInvalidInput() {
        invalidTypes.forEach((type) -> assertThrows(IllegalArgumentException.class, () -> IOCType.fromString(type)));
    }

    @Test
    public void test_fromString_withNullInput() {
        assertThrows(IllegalArgumentException.class, () -> IOCType.fromString(null));
    }

    @Test
    public void test_supportedType_withValidInput() {
        IOCType.types.forEach((type) -> assertTrue(IOCType.supportedType(type)));
    }

    @Test
    public void test_supportedType_withInvalidInput() {
        invalidFormatTypes.forEach((type) -> assertFalse(IOCType.supportedType(type)));
    }

    @Test
    public void test_supportedType_withNullInput() {
        assertFalse(IOCType.supportedType(null));
    }
}
