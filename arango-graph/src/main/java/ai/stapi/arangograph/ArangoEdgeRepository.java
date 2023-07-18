package ai.stapi.arangograph;

import ai.stapi.graph.EdgeRepository;
import ai.stapi.graph.EdgeTypeInfo;
import ai.stapi.graph.NodeIdAndType;
import ai.stapi.graph.exceptions.EdgeNotFound;
import ai.stapi.graph.exceptions.EdgeWithSameIdAndTypeAlreadyExists;
import ai.stapi.graph.exceptions.OneOrBothNodesOnEdgeDoesNotExist;
import ai.stapi.graph.graphElementForRemoval.EdgeForRemoval;
import ai.stapi.graph.graphelements.Edge;
import ai.stapi.graph.traversableGraphElements.TraversableEdge;
import ai.stapi.identity.UniqueIdentifier;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.CollectionCreateOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ArangoEdgeRepository implements EdgeRepository {

  private static final String EDGE_TYPES_COLLECTION_NAME = "edge_types";
  private static final String ATTRIBUTE_COUNT = "count";
  private final ArangoDB arangoDb;
  private final ArangoNodeRepository nodeRepository;

  public ArangoEdgeRepository(
      ArangoDB arangoDb,
      ArangoNodeRepository nodeRepository
  ) {
    this.arangoDb = arangoDb;
    this.nodeRepository = nodeRepository;
  }

  @Override
  public void save(Edge edge) {
    if (!this.nodeRepository.nodeExists(
        edge.getNodeFromId(),
        edge.getNodeFromType()
    ) || !this.nodeRepository.nodeExists(
        edge.getNodeToId(),
        edge.getNodeToType()
    )) {
      throw new OneOrBothNodesOnEdgeDoesNotExist(edge);
    }

    ArangoCollection collection = this.getEdgeCollection(edge.getType());

    if (this.edgeExists(edge.getId(), edge.getType())) {
      throw new EdgeWithSameIdAndTypeAlreadyExists(edge.getId().getId(), edge.getType());
    }
    collection.insertDocument(edge);

    ArangoCollection edgeTypesCollection = this.getEdgeTypesCollection();
    var typeRecord = arangoDb.db()
        .collection(EDGE_TYPES_COLLECTION_NAME)
        .getDocument(edge.getType(), BaseDocument.class);

    if (typeRecord != null) {
      typeRecord.updateAttribute(ATTRIBUTE_COUNT,
          Integer.parseInt(typeRecord.getAttribute(ATTRIBUTE_COUNT).toString()) + 1
      );
      edgeTypesCollection.replaceDocument(edge.getType(), typeRecord);
    } else {
      typeRecord = new BaseDocument(edge.getType());

      typeRecord.addAttribute(ATTRIBUTE_COUNT, 1);
      edgeTypesCollection.insertDocument(typeRecord);
    }
  }

  @Override
  public TraversableEdge loadEdge(
      UniqueIdentifier id,
      String type
  ) {
    var baseDocument = this.arangoDb.db()
        .collection(type)
        .getDocument(id.toString(), Edge.class);
    if (baseDocument == null) {
      throw new EdgeNotFound(id, type);
    }
    return TraversableEdge.from(baseDocument, this.nodeRepository);
  }

  @Override
  public boolean edgeExists(
      UniqueIdentifier id,
      String type
  ) {
    var baseDocument = this.arangoDb.db()
        .collection(type)
        .getDocument(id.toString(), Edge.class);
    return baseDocument != null;
  }

  @Override
  public void replace(Edge edge) {
    ArangoCollection collection = this.getEdgeCollection(edge.getType());

    collection.deleteDocument(edge.getId().getId());
    collection.insertDocument(edge);
  }

  @Override
  public void removeEdge(
      UniqueIdentifier edgeId,
      String edgeType
  ) {
    ArangoCollection collection = this.getEdgeCollection(edgeType);
    if (!collection.documentExists(edgeId.toString())) {
      return;
    }
    collection.deleteDocument(edgeId.toString());
  }

  @Override
  public void removeEdge(EdgeForRemoval edgeForRemoval) {
    this.removeEdge(edgeForRemoval.getGraphElementId(), edgeForRemoval.getGraphElementType());
  }

  @Override
  public List<EdgeTypeInfo> getEdgeTypeInfos() {
    var bindParameters = new HashMap<String, Object>();
    bindParameters.put("@collection", EDGE_TYPES_COLLECTION_NAME);
    var query = "FOR doc IN @@collection\n   RETURN doc\n";

    ArangoCursor<BaseDocument> arangoCursorIterator;
    try {
      arangoCursorIterator = this.arangoDb.db().query(query, BaseDocument.class, bindParameters);
    } catch (ArangoDBException e) {
      return new ArrayList<>();
    }

    return arangoCursorIterator.stream()
        .map(baseDocument -> new EdgeTypeInfo(baseDocument.getKey(),
            Long.parseLong(baseDocument.getAttribute(ATTRIBUTE_COUNT).toString())
        ))
        .collect(Collectors.toList());

  }

  @Override
  public Set<TraversableEdge> findInAndOutEdgesForNode(
      UniqueIdentifier nodeId,
      String nodeType
  ) {
    var edges = new HashSet<TraversableEdge>();
    this.getEdgeTypeInfos().forEach(edgeTypeInfo -> {
      var collection = this.arangoDb.db().collection(edgeTypeInfo.getType());
      if (!collection.exists()) {
        return;
      }

      var bindParameters = new HashMap<String, Object>();
      bindParameters.put("@collection", edgeTypeInfo.getType());
      bindParameters.put("node", nodeType + "/" + nodeId.toString());
      var query = "FOR e IN @@collection\nFILTER e._from == @node || e._to == @node\nRETURN e";

      var arangoCursorIterator = this.arangoDb.db()
          .query(query, BaseEdgeDocument.class, bindParameters);

      while (arangoCursorIterator.hasNext()) {
        var document = arangoCursorIterator.next();
        edges.add(this.loadEdge(new UniqueIdentifier(document.getKey()), edgeTypeInfo.getType()));
      }
    });
    return edges;
  }

  @Override
  public TraversableEdge findEdgeByTypeAndNodes(
      String edgeType,
      NodeIdAndType nodeFrom,
      NodeIdAndType nodeTo
  ) {
    var collection = this.arangoDb.db().collection(edgeType);
    if (!collection.exists()) {
      return null;
    }

    var bindParameters = new HashMap<String, Object>();
    bindParameters.put("@collection", edgeType);
    bindParameters.put("nodeFrom", nodeFrom.getType() + "/" + nodeFrom.getId().toString());
    bindParameters.put("nodeTo", nodeTo.getType() + "/" + nodeTo.getId().toString());

    var query = "FOR e IN @@collection\nFILTER e._from == @nodeFrom && e._to == @nodeTo\nRETURN e";

    var arangoCursorIterator = this.arangoDb.db().query(query, BaseDocument.class, bindParameters);

    if (arangoCursorIterator.hasNext()) {
      var document = arangoCursorIterator.next();
      return this.loadEdge(new UniqueIdentifier(document.getKey()), edgeType);
    }
    return null;
  }

  @NotNull
  private ArangoCollection getEdgeCollection(String edgeType) {
    var collection = this.arangoDb.db().collection(edgeType);
    if (!collection.exists()) {
      this.arangoDb.db()
          .createCollection(edgeType, new CollectionCreateOptions().type(CollectionType.EDGES));
    }
    return collection;
  }

  private ArangoCollection getEdgeTypesCollection() {
    var collection = this.arangoDb.db().collection(EDGE_TYPES_COLLECTION_NAME);
    if (!collection.exists()) {
      this.arangoDb.db().createCollection(EDGE_TYPES_COLLECTION_NAME);
    }
    return collection;
  }
}
