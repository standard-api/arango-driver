package ai.stapi.arangograph;

import ai.stapi.graph.NodeInfo;
import ai.stapi.graph.NodeRepository;
import ai.stapi.graph.NodeTypeInfo;
import ai.stapi.graph.exceptions.NodeNotFound;
import ai.stapi.graph.exceptions.NodeWithSameIdAndTypeAlreadyExists;
import ai.stapi.graph.graphElementForRemoval.NodeForRemoval;
import ai.stapi.graph.graphelements.Node;
import ai.stapi.graph.traversableGraphElements.TraversableNode;
import ai.stapi.identity.UniqueIdentifier;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ArangoNodeRepository implements NodeRepository {

  public static final String NODE_TYPES_COLLECTION_NAME = "node_types";
  public static final String ATTRIBUTE_COUNT = "count";
  private final ArangoDB arangoDb;
  private final ArangoEdgeRepository edgeRepository;

  public ArangoNodeRepository(
      ArangoDB arangoDb,
      ArangoEdgeRepository edgeRepository
  ) {
    this.arangoDb = arangoDb;
    this.edgeRepository = edgeRepository;
  }

  private static void insertDocumentToCollection(
      Node node,
      ArangoCollection nodeTypesCollection
  ) {
    try {
      nodeTypesCollection.insertDocument(node);
    } catch (Exception exception) {
      throw new RuntimeException(
          String.format(
              "Unable to insert document:\nnode type:\n%s\nid:\n%s",
              node.getType(),
              node.getId()
          ),
          exception
      );
    }
  }

  @Override
  public void save(Node node) {
    ArangoCollection collection = this.getNodeCollection(node.getType());

    if (this.nodeExists(node.getId(), node.getType())) {
      throw new NodeWithSameIdAndTypeAlreadyExists(node.getId(), node.getType());
    }
    this.insertDocumentToCollection(
        node,
        collection
    );

    ArangoCollection nodeTypesCollection = this.getNodeTypesCollection();
    var typeRecord = arangoDb.db()
        .collection(NODE_TYPES_COLLECTION_NAME)
        .getDocument(node.getType(), BaseDocument.class);

    if (typeRecord != null) {
      typeRecord.updateAttribute(
          ATTRIBUTE_COUNT,
          Integer.parseInt(typeRecord.getAttribute(ATTRIBUTE_COUNT).toString()) + 1
      );
      nodeTypesCollection.replaceDocument(node.getType(), typeRecord);
    } else {
      typeRecord = new BaseDocument(node.getType());

      typeRecord.addAttribute(ATTRIBUTE_COUNT, 1);
      nodeTypesCollection.insertDocument(typeRecord);
    }
  }

  @Override
  public void replace(Node node) {
    ArangoCollection collection = getNodeCollection(node.getType());

    collection.deleteDocument(node.getId().getId());
    collection.insertDocument(node);
  }

  @Override
  public void removeNode(
      UniqueIdentifier id,
      String nodeType
  ) {
    ArangoCollection collection = getNodeCollection(nodeType);
    if (!collection.documentExists(id.toString())) {
      return;
    }
    edgeRepository.findInAndOutEdgesForNode(id, nodeType)
        .forEach(traversableEdge -> edgeRepository.removeEdge(
            traversableEdge.getId(),
            traversableEdge.getType()
        ));
    collection.deleteDocument(id.toString());
    ArangoCollection nodeTypesCollection = this.getNodeTypesCollection();
    var typeRecord =
        arangoDb.db()
            .collection(NODE_TYPES_COLLECTION_NAME)
            .getDocument(nodeType, BaseDocument.class);

    if (typeRecord != null) {
      typeRecord.updateAttribute(ATTRIBUTE_COUNT,
          Integer.parseInt(typeRecord.getAttribute("count").toString()) - 1
      );
      nodeTypesCollection.replaceDocument(nodeType, typeRecord);
    }
  }

  @Override
  public void removeNode(NodeForRemoval nodeForRemoval) {
    this.removeNode(nodeForRemoval.getGraphElementId(), nodeForRemoval.getGraphElementType());
  }

  @Override
  public boolean nodeExists(
      UniqueIdentifier id,
      String nodeType
  ) {
    var baseDocument =
        this.arangoDb.db()
            .collection(nodeType)
            .getDocument( id.toString(), BaseDocument.class);

    return baseDocument != null;
  }

  @Override
  public TraversableNode loadNode(
      UniqueIdentifier uuid,
      String nodeType
  ) {
    var baseDocument = this.arangoDb.db()
        .collection(nodeType)
        .getDocument(uuid.toString(), Node.class);

    if (baseDocument == null) {
      throw new NodeNotFound(uuid, nodeType);
    }

    return TraversableNode.from(baseDocument, edgeRepository);
  }

  @Override
  public List<NodeTypeInfo> getNodeTypeInfos() {
    var bindParameters = new HashMap<String, Object>();
    bindParameters.put("@collection", NODE_TYPES_COLLECTION_NAME);
    var query = "FOR doc IN @@collection\n" + "   RETURN doc\n";

    var arangoCursorIterator = this.arangoDb.db().query(
        query, 
        BaseDocument.class, 
        bindParameters
    );

    return arangoCursorIterator.stream().map(baseDocument -> new NodeTypeInfo(
        baseDocument.getKey(),
        Long.parseLong(baseDocument.getAttribute(ATTRIBUTE_COUNT).toString())
    )).collect(Collectors.toList());

  }

  @Override
  public List<NodeInfo> getNodeInfosBy(String nodeType) {
    var bindParameters = new HashMap<String, Object>();
    bindParameters.put("@collection", nodeType);
    var query = "FOR doc IN @@collection\n" + "   RETURN doc\n";

    var arangoCursorIterator = this.arangoDb.db().query(
        query,
        Node.class,
        bindParameters
    );

    return arangoCursorIterator.stream().map(baseDocument -> {
      var node = TraversableNode.from(baseDocument, this.edgeRepository);
      var name = node.getSortingNameWithNodeTypeFallback();
      return new NodeInfo(new UniqueIdentifier(baseDocument.getId().getId()), nodeType, name);
    }).sorted(Comparator.comparing(NodeInfo::getName)).collect(Collectors.toList());
  }

  public int getNodeHashCodeWithoutEdges(
      UUID uuid,
      String nodeType
  ) {
    // TODO
    return 0;
  }

  @NotNull
  private ArangoCollection getNodeCollection(String nodeType) {
    var collection = this.arangoDb.db().collection(nodeType);
    if (!collection.exists()) {
      this.arangoDb.db().createCollection(nodeType);
    }
    return collection;
  }

  @NotNull
  private ArangoCollection getNodeTypesCollection() {
    return this.getNodeTypesCollection(5);
  }

  @NotNull
  private ArangoCollection getNodeTypesCollection(int retries) {
    var collection = this.arangoDb.db().collection(NODE_TYPES_COLLECTION_NAME);
    if (!collection.exists()) {
      try {
        this.arangoDb.db().createCollection(NODE_TYPES_COLLECTION_NAME);
        collection = this.arangoDb.db().collection(NODE_TYPES_COLLECTION_NAME);
      } catch (ArangoDBException e) {
        if (retries < 1) {
          throw new ArangoDBException("Arango cannot initialize collection. Is it running?");
        }
        return this.getNodeTypesCollection(retries - 1);
      }
    }
    return collection;
  }

}
