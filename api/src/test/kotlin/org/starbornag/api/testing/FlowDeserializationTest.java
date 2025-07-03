import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Data;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Data
@ToString // Lombok annotation for pretty printing
public class Flow {
    private String flowName;
    private java.util.List<Step> steps;
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpCallSync.class, name = "httpCallSync"),
        @JsonSubTypes.Type(value = HttpCallAsync.class, name = "httpCallAsync")
})
@Data
@ToString
public abstract class Step {
    private Validation validation;
    // Other common properties
}

@Data
@JsonTypeName("httpCallSync")
@ToString
public class HttpCallSync extends Step {
    private String uri;
    private String contenTtype;
    private String method;
    private String body;
}

@Data
@JsonTypeName("httpCallAsync")
@ToString
public class HttpCallAsync extends Step {
    private String uri;
    private String contenTtype;
    private String method;
    private String body;
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StatusValidation.class, name = "status"),
        @JsonSubTypes.Type(value = ReadQueueValidation.class, name = "readQueue")
})
@Data
@ToString
public abstract class Validation {
    // Common properties
}

@Data
@JsonTypeName("status")
@ToString
public class StatusValidation extends Validation {
    private StatusConfig status;
}

@Data
@ToString
public class StatusConfig {
    private Integer family;
}

@Data
@JsonTypeName("readQueue")
@ToString
public class ReadQueueValidation extends Validation {
    private ReadQueueConfig readQueue;
}

@Data
@ToString
public class ReadQueueConfig {
    private String name;
    private String waitFor;
    private String messageName;
    private String match;
}

public class FlowDeserializationTest {

    @Test
    public void testYamlDeserialization() throws IOException {
        String yamlContent = """
                flowName: JoshFlow
                steps:
                 - httpCallSync:
                      uri: bla
                      contenTtype: application/yaml
                      method: post
                      body: text
                      validation:
                         status:
                           family: 200
                 - httpCallAsync:
                      uri: blah
                      contenTtype: application/yaml
                      method: post
                      body: text
                      validation:
                         readQueue:
                           name: theQName
                           waitFor: 30s
                           messageName: TheMessage
                           match: "Success!"
                """;

        ObjectMapper mapper = YAMLMapper.builder().build();
        Flow flow = mapper.readValue(yamlContent, Flow.class);

        assertNotNull(flow);
        assertNotNull(flow.getSteps());
        assertTrue(flow.getSteps().size() > 0);

        System.out.println("--- YAML Output ---");
        System.out.println(flow); // Use Lombok's toString
        for (Step step : flow.getSteps()) {
            System.out.println(step);
        }
    }

    @Test
    public void testJsonDeserialization() throws IOException {
        String jsonContent = """
                {
                  "flowName": "JoshFlow",
                  "steps": [
                    {
                      "httpCallSync": {
                        "uri": "bla",
                        "contenTtype": "application/yaml",
                        "method": "post",
                        "body": "text",
                        "validation": {
                          "status": {
                            "family": 200
                          }
                        }
                      }
                    },
                    {
                      "httpCallAsync": {
                        "uri": "blah",
                        "contenTtype": "application/yaml",
                        "method": "post",
                        "body": "text",
                        "validation": {
                          "readQueue": {
                            "name": "theQName",
                            "waitFor": "30s",
                            "messageName": "TheMessage",
                            "match": "Success!"
                          }
                        }
                      }
                    }
                  ]
                }
                """;

        ObjectMapper mapper = new ObjectMapper();
        Flow flow = mapper.readValue(jsonContent, Flow.class);

        assertNotNull(flow);
        assertNotNull(flow.getSteps());
        assertTrue(flow.getSteps().size() > 0);

        System.out.println("--- JSON Output ---");
        System.out.println(flow);  // Use Lombok's toString
        for (Step step : flow.getSteps()) {
            System.out.println(step);
        }
    }
}

