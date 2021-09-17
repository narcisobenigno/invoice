package invoice.common.es;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Version {
    private final int value;

    public Version(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
