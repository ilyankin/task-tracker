package web.servers.typeAdapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

public class ExceptionAdapter extends TypeAdapter<Exception> {
    @Override
    public void write(JsonWriter jsonWriter, Exception e) throws IOException {
        jsonWriter.beginObject()
                .name("exception")
                    .beginObject()
                    .name("timestamp").value(String.valueOf(Timestamp.from(Instant.now())))
                    .name("message").value(e.getMessage())
                    .endObject()
                .endObject();
    }

    @Override
    public Exception read(JsonReader jsonReader) {
        return null;
    }
}
