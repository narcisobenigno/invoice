package invoice.common.jdbi;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UniqueConstraint extends Constraint.Base {
    public UniqueConstraint(String name, String... fields) {
        super(
                name,
                String.format(
                        "UNIQUE (%s)",
                        Arrays.stream(fields)
                                .map(f -> String.format("\"%s\"", f))
                                .collect(Collectors.joining(","))
                )
        );
    }
}
