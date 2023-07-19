package ai.stapi.arangoaxon;

import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.objectRenderer.infrastructure.objectToJsonStringRenderer.ObjectToJSonStringOptions.RenderFeature;
import ai.stapi.test.integration.IntegrationTestCase;
import java.util.HashMap;
import java.util.Map;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommandMessageStoreTest extends IntegrationTestCase {

  @Autowired
  private ArangoCommandMessageStore commandMessageStore;


  @BeforeEach
  void clean() {
    this.commandMessageStore.wipeAll();
  }

  @Test
  void itCanStoreAndLoadPersistedCommandMessages() {
    this.commandMessageStore.storeCommand(
        new GenericCommandMessage<>(
            new ExampleCommand("Id", "someValue")
        )
    );

    this.commandMessageStore.storeCommand(
        new GenericCommandMessage<>(
            new GenericCommandMessage<>(
                new DynamicCommand(
                    new UniqueIdentifier("ExampleDynamicCommmandId"),
                    "ExampledDynamicCommandType",
                    new HashMap<>(Map.of(
                        "exampleData", "exampleValue"
                    ))
                )
            ),
            "ExampledDynamicCommandType"
        )
    );
    this.thenObjectApproved(
        this.commandMessageStore.getAll(),
        RenderFeature.HIDE_KEY_HASHCODE,
        RenderFeature.SORT_FIELDS
    );
  }
  
  private static class ExampleCommand {
    
    @TargetAggregateIdentifier
    private final String identifier;
    private final String someField;

    public ExampleCommand(String identifier, String someField) {
      this.identifier = identifier;
      this.someField = someField;
    }

    public String getIdentifier() {
      return identifier;
    }

    public String getSomeField() {
      return someField;
    }
  }
}