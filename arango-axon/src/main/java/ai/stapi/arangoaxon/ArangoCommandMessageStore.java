package ai.stapi.arangoaxon;

import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import ai.stapi.axonsystem.commandpersisting.PersistedCommandMessage;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.serde.jackson.Key;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.axonframework.commandhandling.CommandMessage;

public class ArangoCommandMessageStore implements CommandMessageStore {

  public static final String COMMAND_COLLECTION_NAME = "persistedCommandMessage";
  private final ArangoDB arangoDB;

  public ArangoCommandMessageStore(ArangoDB arangoDB) {
    this.arangoDB = arangoDB;
    var collection = this.arangoDB.db().collection(COMMAND_COLLECTION_NAME);
    if (!collection.exists()) {
      this.arangoDB.db().createCollection(COMMAND_COLLECTION_NAME);
    }
  }

  @Override
  public void storeCommand(CommandMessage<?> commandMessage) {
    var collection = this.getCommandCollection();
    if (!collection.exists()) {
      this.arangoDB.db().createCollection(COMMAND_COLLECTION_NAME);
    }
    var identifier = this.extractId(commandMessage);
    var payload = this.extractPayload(commandMessage);

    collection.insertDocument(
        new ArangoPersistedCommandMessage<>(
            identifier,
            commandMessage.getCommandName(),
            payload,
            new HashMap<>(commandMessage.getMetaData())
        )
    );
  }

  @Override
  public List<PersistedCommandMessage<?>> getAll() {
    var db = arangoDB.db();
    var cursor = db.query(
        "FOR c IN " + COMMAND_COLLECTION_NAME + " RETURN c",
        ArangoPersistedCommandMessage.class
    );
    var result = new ArrayList<PersistedCommandMessage<?>>();
    cursor.stream().forEach(result::add);

    return result;
  }

  @Override
  public void wipeAll() {
    var db = arangoDB.db();
    var collection = this.getCommandCollection();
    if (collection.exists()) {
      db.query(
          "FOR c IN " + COMMAND_COLLECTION_NAME + " REMOVE c IN " + COMMAND_COLLECTION_NAME,
          ArangoPersistedCommandMessage.class
      );
    }
  }

  private ArangoCollection getCommandCollection() {
    var db = this.arangoDB.db();
    return db.collection(COMMAND_COLLECTION_NAME);
  }

  private static class ArangoPersistedCommandMessage<T> implements PersistedCommandMessage<T> {

    @Key
    @JsonIgnore
    private String key;

    private String targetAggregateIdentifier;

    private String commandName;

    private T commandPayload;

    private Map<String, Object> commandMetaData;

    protected ArangoPersistedCommandMessage() {
    }

    public ArangoPersistedCommandMessage(
        String targetAggregateIdentifier,
        String commandName,
        T commandPayload,
        Map<String, Object> commandMetaData
    ) {
      this.targetAggregateIdentifier = targetAggregateIdentifier;
      this.commandName = commandName;
      this.commandPayload = commandPayload;
      this.commandMetaData = commandMetaData;
    }

    @JsonIgnore
    @JsonProperty("_key")
    public String getKey() {
      return key;
    }

    public T getCommandPayload() {
      return commandPayload;
    }

    public String getCommandName() {
      return commandName;
    }

    public Map<String, Object> getCommandMetaData() {
      return commandMetaData;
    }

    public String getTargetAggregateIdentifier() {
      return targetAggregateIdentifier;
    }
  }
}
