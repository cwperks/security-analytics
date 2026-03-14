package org.opensearch.securityanalytics.commons.util;

import org.opensearch.securityanalytics.commons.model.IOC;
import org.opensearch.securityanalytics.commons.model.IOCType;
import org.opensearch.securityanalytics.commons.model.STIX2;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class STIX2Generator {
    public static List<IOC> generateSTIX2(final int count) {
        return generateSTIX2(count, i -> randomSTIX2());
    }

    public static List<IOC> generateSTIX2(final int count, final Function<Integer, STIX2> generatorFunction) {
        return IntStream.range(0, count)
                .mapToObj(generatorFunction::apply)
                .collect(Collectors.toList());
    }

    public static STIX2 randomSTIX2() {
        return randomSTIX2(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static STIX2 randomSTIX2(
            String id,
            String name,
            String type,
            String value,
            String severity,
            Instant created,
            Instant modified,
            String description,
            List<String> labels,
            String specVersion,
            String feedId,
            String feedName
    ) {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (name == null) {
            name = UUID.randomUUID().toString();
        }
        if (type == null) {
            type = IOCType.types.get(new Random().nextInt(IOCType.types.size() - 1));
        }
        if (value == null) {
            value = UUID.randomUUID().toString();
        }
        if (severity == null) {
            severity = UUID.randomUUID().toString();
        }
        if (created == null) {
            created = Instant.now();
        }
        if (modified == null) {
            modified = Instant.now().plusSeconds(3600); // 1 hour
        }
        if (description == null) {
            description = UUID.randomUUID().toString();
        }
        if (labels == null) {
            labels = IntStream.range(0, new Random().nextInt(5))
                    .mapToObj(i -> UUID.randomUUID().toString())
                    .collect(Collectors.toList());
        }
        if (specVersion == null) {
            specVersion = UUID.randomUUID().toString();
        }
        if (feedId == null) {
            feedId = UUID.randomUUID().toString();
        }
        if (feedName == null) {
            feedName = UUID.randomUUID().toString();
        }
        return new STIX2(
                id,
                name,
                type,
                value,
                severity,
                created,
                modified,
                description,
                labels,
                specVersion,
                feedId,
                feedName
        );
    }
}
