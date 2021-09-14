package invoice.common.serialization;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface JSON {
    String stringValue(String name);

    String string();

    class Object implements JSON {
        private final JSONObject content;

        public Object(String content) {
            this(new JSONObject(content));
        }

        public Object(Map<String, java.lang.Object> content) {
            this(new JSONObject(content));
        }

        private Object(JSONObject content) {
            this.content = content;
        }

        @Override
        public String stringValue(String name) {
            return this.content.getString(name);
        }

        @Override
        public String string() {
            var out = new ByteArrayOutputStream();
            try (var output = new OutputStreamWriter(out)) {
                this.content.write(output);
            } catch (IOException e) {
                throw new IllegalStateException("error to create byte array output stream", e);
            }

            return out.toString(StandardCharsets.UTF_8);
        }
    }
}
