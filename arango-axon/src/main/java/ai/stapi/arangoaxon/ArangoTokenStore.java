package ai.stapi.arangoaxon;

import com.arangodb.ArangoDB;
import com.arangodb.serde.jackson.Key;
import java.lang.management.ManagementFactory;
import java.util.Map;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.SimpleSerializedType;
import org.jetbrains.annotations.NotNull;

public class ArangoTokenStore implements TokenStore {

  public static final String TOKEN_COLLECTION_NAME = "axonTrackingToken";
  private final ArangoDB arangoDB;
  private final Serializer serializer;

  public ArangoTokenStore(ArangoDB arangoDB, Serializer serializer) {
    this.arangoDB = arangoDB;
    this.serializer = serializer;
    var collection = this.arangoDB.db().collection(TOKEN_COLLECTION_NAME);
    if (!collection.exists()) {
      this.arangoDB.db().createCollection(TOKEN_COLLECTION_NAME);
    }
  }

  private static String getCurrentOwnerName() {
    return ManagementFactory.getRuntimeMXBean().getName();
  }

  @NotNull
  private static SimpleSerializedObject<byte[]> createSimpleSerializedObject(
      byte[] serializedToken) {
    return new SimpleSerializedObject<>(
        serializedToken, byte[].class,
        new SimpleSerializedType(
            TrackingToken.class.getCanonicalName(),
            null
        )
    );
  }

  @Override
  public void storeToken(TrackingToken token, String processorName, int segment) {
    // Use the arangoDB Java driver to store the token in the database
    var db = arangoDB.db();
    var key = this.createKey(processorName, segment);
    byte[] serializedToken = null;
    if (token != null) {
      serializedToken = this.serializer.serialize(token, byte[].class).getData();
    }
    var arangoToken = new Token(
        key,
        serializedToken,
        processorName,
        segment,
        getCurrentOwnerName()
    );
    if (db.collection(TOKEN_COLLECTION_NAME).documentExists(key)) {
      db.collection(TOKEN_COLLECTION_NAME).replaceDocument(key, arangoToken);
      return;
    }
    db.collection(TOKEN_COLLECTION_NAME).insertDocument(arangoToken);
  }

  @Override
  public TrackingToken fetchToken(String processorName, int segment) {
    // Use the arangoDB Java driver to fetch the token from the database
    var db = arangoDB.db();
    var key = this.createKey(processorName, segment);
    var token = db.collection(TOKEN_COLLECTION_NAME).getDocument(
        key,
        Token.class
    );
    if (token != null) {
      token.setOwner(getCurrentOwnerName());
      db.collection(TOKEN_COLLECTION_NAME).replaceDocument(key, token);
      var serializedToken = token.getSerializedToken();
      return serializedToken == null ? null
          : this.serializer.deserialize(createSimpleSerializedObject(serializedToken));
    }
    return null;
  }

  @Override
  public void releaseClaim(String processorName, int segment) {
    // Use the arangoDB Java driver to release the claim for the token in the database
    var db = arangoDB.db();
    var key = this.createKey(processorName, segment);
    var token = db.collection(TOKEN_COLLECTION_NAME).getDocument(
        key,
        Token.class
    );
    token.setOwner(null);
    db.collection(TOKEN_COLLECTION_NAME).replaceDocument(token.getKey(), token);
  }

  @Override
  public int[] fetchSegments(String processorName) {
    var db = arangoDB.db();
    var cursor = db.query(
        "FOR t IN " + TOKEN_COLLECTION_NAME
            + " FILTER t.processorName == @processorName RETURN t.segment",
        Integer.class,
        Map.of("processorName", processorName)
    );
    return cursor.stream().mapToInt(segment -> segment).toArray();
  }

  private String createKey(String processorName, int segment) {
    return processorName + "_" + segment;
  }

  private static class Token {


    @Key
    protected String key;
    private byte[] serializedToken;
    private String processorName;
    private Integer segment;

    private String owner;

    protected Token() {
    }

    public Token(String key, byte[] serializedToken, String processorName, Integer segment,
        String owner) {
      this.key = key;
      this.serializedToken = serializedToken;
      this.processorName = processorName;
      this.segment = segment;
      this.owner = owner;
    }

    public String getProcessorName() {
      return processorName;
    }

    public void setProcessorName(String processorName) {
      this.processorName = processorName;
    }

    public Integer getSegment() {
      return segment;
    }

    public void setSegment(Integer segment) {
      this.segment = segment;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getOwner() {
      return owner;
    }

    public void setOwner(String owner) {
      this.owner = owner;
    }

    public byte[] getSerializedToken() {
      return serializedToken;
    }

    public void setSerializedToken(byte[] serializedToken) {
      this.serializedToken = serializedToken;
    }
  }
}