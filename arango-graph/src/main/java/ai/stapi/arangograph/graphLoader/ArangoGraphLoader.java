package ai.stapi.arangograph.graphLoader;

import ai.stapi.arangograph.ArangoEdgeRepository;
import ai.stapi.arangograph.ArangoNodeRepository;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects.ArangoEdgeFindDocument;
import ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects.ArangoEdgeGetDocument;
import ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects.ArangoNodeFindDocument;
import ai.stapi.arangograph.graphLoader.arangoQuery.bindingObjects.ArangoNodeGetDocument;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoEdgeCollectionSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoEdgeGetSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoQueryBuilderProvider;
import ai.stapi.graph.RepositoryEdgeLoader;
import ai.stapi.graph.graphelements.Edge;
import ai.stapi.graph.graphelements.Node;
import ai.stapi.graph.inMemoryGraph.InMemoryGraphRepository;
import ai.stapi.graph.traversableGraphElements.TraversableEdge;
import ai.stapi.graph.traversableGraphElements.TraversableGraphElement;
import ai.stapi.graph.traversableGraphElements.TraversableNode;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractNodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.EdgeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.GraphElementQueryDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.NodeQueryGraphDescription;
import ai.stapi.graphoperations.graphLoader.GraphLoader;
import ai.stapi.graphoperations.graphLoader.GraphLoaderFindAsObjectOutput;
import ai.stapi.graphoperations.graphLoader.GraphLoaderGetAsObjectOutput;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.graphoperations.graphLoader.exceptions.GraphLoaderException;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.identity.UniqueIdentifier;
import com.arangodb.ArangoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public class ArangoGraphLoader implements GraphLoader {

  private final ArangoDB arangoDb;
  private final ArangoEdgeRepository arangoEdgeRepository;
  private final ArangoNodeRepository arangoNodeRepository;
  private final ArangoQueryBuilderProvider arangoQueryBuilderProvider;
  private final GenericSubQueryResolver genericSubQueryResolver;
  private final ObjectMapper objectMapper;
  private final GraphLoaderOgmFactory graphLoaderOgmFactory;

  public ArangoGraphLoader(
      ArangoDB arangoDb,
      ArangoEdgeRepository arangoEdgeRepository,
      ArangoNodeRepository arangoNodeRepository,
      ArangoQueryBuilderProvider arangoQueryBuilderProvider,
      GenericSubQueryResolver genericSubQueryResolver,
      ObjectMapper objectMapper,
      GraphLoaderOgmFactory graphLoaderOgmFactory
  ) {
    this.arangoDb = arangoDb;
    this.arangoEdgeRepository = arangoEdgeRepository;
    this.arangoNodeRepository = arangoNodeRepository;
    this.arangoQueryBuilderProvider = arangoQueryBuilderProvider;
    this.genericSubQueryResolver = genericSubQueryResolver;
    this.objectMapper = objectMapper;
    this.graphLoaderOgmFactory = graphLoaderOgmFactory;
  }

  @Override
  public List<TraversableGraphElement> findAsTraversable(GraphElementQueryDescription graphDescription) {
    var output = this.find(
        graphDescription,
        Object.class,
        GraphLoaderReturnType.GRAPH
    ).getGraphLoaderFindAsGraphOutput();
    String elementType;
    if (graphDescription instanceof NodeQueryGraphDescription nodeQueryGraphDescription) {
      elementType = ((NodeDescriptionParameters) nodeQueryGraphDescription.getParameters()).getNodeType();
    } else {
      elementType = ((EdgeDescriptionParameters) graphDescription.getParameters()).getEdgeType();
    }
    InMemoryGraphRepository outputRepository = output.getGraph();
    return output.getFoundGraphElementIds()
        .stream()
        .map(id -> outputRepository.loadGraphElement(id, elementType))
        .toList();
  }

  @Override
  public TraversableGraphElement getAsTraversable(
      UniqueIdentifier elementId,
      GraphElementQueryDescription graphDescription
  ) {
    var graph = this.get(
        elementId,
        graphDescription,
        Object.class,
        GraphLoaderReturnType.GRAPH
    ).getGraph();
    
    String elementType;
    if (graphDescription instanceof NodeQueryGraphDescription nodeQueryGraphDescription) {
      elementType = ((NodeDescriptionParameters) nodeQueryGraphDescription.getParameters()).getNodeType();
    } else {
      elementType = ((EdgeDescriptionParameters) graphDescription.getParameters()).getEdgeType();
    }
    
    return graph.loadGraphElement(elementId, elementType);
  }

  @Override
  public <T> GraphLoaderFindAsObjectOutput<T> find(
      GraphElementQueryDescription graphDescription,
      Class<T> objectClass,
      GraphLoaderReturnType... returnTypes
  ) {
    var collectionType = this.getCollectionType(graphDescription);
    var collection = this.arangoDb.db().collection(collectionType);
    if (!collection.exists()) {
      return new GraphLoaderFindAsObjectOutput<>();
    }
    var arangoQueryBuilder = this.arangoQueryBuilderProvider.provide();

    var finalReturnTypes = returnTypes.length == 0
        ? new GraphLoaderReturnType[] {GraphLoaderReturnType.OBJECT}
        : returnTypes;
    if (graphDescription instanceof AbstractNodeDescription) {
      return resolveFindNodesQuery(
          collectionType,
          graphDescription,
          arangoQueryBuilder,
          objectClass,
          finalReturnTypes
      );
    }
    if (graphDescription instanceof AbstractEdgeDescription edgeDescription) {
      return resolveFindEdgesQuery(
          collectionType,
          edgeDescription,
          arangoQueryBuilder,
          objectClass,
          finalReturnTypes
      );
    }

    throw new NotImplementedException(
        "There should not be any other Query graph description, than node and edge");
  }

  @Override
  public <T> GraphLoaderGetAsObjectOutput<T> get(
      UniqueIdentifier elementId,
      GraphElementQueryDescription graphDescription,
      Class<T> objectClass,
      GraphLoaderReturnType... returnTypes
  ) {
    var collectionType = this.getCollectionType(graphDescription);
    var arangoQueryBuilder = this.arangoQueryBuilderProvider.provide();

    GraphLoaderReturnType[] finalReturnTypes = returnTypes.length == 0
        ? new GraphLoaderReturnType[] {GraphLoaderReturnType.OBJECT}
        : returnTypes;
    if (graphDescription instanceof AbstractNodeDescription) {
      if (!this.arangoNodeRepository.nodeExists(elementId, collectionType)) {
        throw GraphLoaderException.becauseThereIsNoGraphElementWithProvidedUuid(
            elementId,
            graphDescription
        );
      }
      return resolveGetNodeQuery(
          elementId,
          collectionType,
          graphDescription,
          arangoQueryBuilder,
          objectClass,
          finalReturnTypes
      );
    }

    if (graphDescription instanceof AbstractEdgeDescription edgeDescription) {
      if (!this.arangoEdgeRepository.edgeExists(elementId, collectionType)) {
        throw GraphLoaderException.becauseThereIsNoGraphElementWithProvidedUuid(
            elementId,
            graphDescription
        );
      }
      return resolveGetEdgeQuery(
          elementId,
          collectionType,
          edgeDescription,
          arangoQueryBuilder,
          objectClass,
          finalReturnTypes
      );
    }

    throw new NotImplementedException(
        "There should not be any other Query graph description, than node and edge");
  }

  @Deprecated
  public List<TraversableNode> findAsTraversableNodesWithOriginalRepository(
      NodeQueryGraphDescription graphDescription
  ) {
    var output = this.find(
        graphDescription,
        Object.class,
        GraphLoaderReturnType.GRAPH
    ).getGraphLoaderFindAsGraphOutput();
    
    var nodeParam = (NodeDescriptionParameters) graphDescription.getParameters();
    return output
        .getFoundGraphElementIds()
        .stream()
        .map(uuid -> output.getGraph().loadNode(uuid, nodeParam.getNodeType()))
        .map(oldNode -> new TraversableNode(
            oldNode.getId(),
            oldNode.getType(),
            oldNode.getVersionedAttributes(),
            new RepositoryEdgeLoader(this.arangoEdgeRepository)
        )).toList();
  }

  @NotNull
  private <T> GraphLoaderGetAsObjectOutput<T> resolveGetNodeQuery(
      UniqueIdentifier elementId,
      String elementType,
      GraphElementQueryDescription graphDescription,
      ArangoQueryBuilder arangoQueryBuilder,
      Class<T> objectClass,
      GraphLoaderReturnType[] returnTypes
  ) {
    var mainQueryBuilder = arangoQueryBuilder.setGetNodeMainQuery(elementType, elementId);

    if (Arrays.asList(returnTypes).contains(GraphLoaderReturnType.OBJECT)) {
      var generatedOgm = this.graphLoaderOgmFactory.create(graphDescription);
      this.genericSubQueryResolver.resolve(mainQueryBuilder, generatedOgm);
    } else {
      this.genericSubQueryResolver.resolve(mainQueryBuilder, graphDescription);
    }

    var query = arangoQueryBuilder.build(returnTypes);
    var getQueryArangoDocuments = this.arangoDb.db()
        .query(query.getQueryString(), ArangoNodeGetDocument.class, query.getBindParameters())
        .stream()
        .toList();

    return this.mapNodeToInMemoryGraph(getQueryArangoDocuments, objectClass);
  }

  @NotNull
  private <T> GraphLoaderGetAsObjectOutput<T> resolveGetEdgeQuery(
      UniqueIdentifier elementId,
      String elementType,
      AbstractEdgeDescription edgeDescription,
      ArangoQueryBuilder arangoQueryBuilder,
      Class<T> objectClass,
      GraphLoaderReturnType[] returnTypes
  ) {
    ArangoEdgeGetSubQueryBuilder mainQueryBuilder;
    if (edgeDescription.isOutgoing()) {
      mainQueryBuilder = arangoQueryBuilder.setGetOutgoingEdgeMainQuery(elementType, elementId);
    } else {
      mainQueryBuilder = arangoQueryBuilder.setGetIngoingEdgeMainQuery(elementType, elementId);
    }

    if (Arrays.asList(returnTypes).contains(GraphLoaderReturnType.OBJECT)) {
      var generatedOgm = this.graphLoaderOgmFactory.create(edgeDescription);
      this.genericSubQueryResolver.resolve(mainQueryBuilder, generatedOgm);
    } else {
      this.genericSubQueryResolver.resolve(mainQueryBuilder, edgeDescription);
    }

    var query = arangoQueryBuilder.build(returnTypes);
//    var pretty = new AqlFormatter().format(query.getQueryString());

    var getQueryArangoDocuments = this.arangoDb.db()
        .query(query.getQueryString(), ArangoEdgeGetDocument.class, query.getBindParameters())
        .stream()
        .toList();

    return this.mapEdgeToInMemoryGraph(getQueryArangoDocuments, objectClass);
  }

  @NotNull
  private <T> GraphLoaderFindAsObjectOutput<T> resolveFindNodesQuery(
      String elementType,
      GraphElementQueryDescription graphDescription,
      ArangoQueryBuilder arangoQueryBuilder,
      Class<T> objectClass,
      GraphLoaderReturnType[] returnTypes
  ) {
    var mainQueryBuilder = arangoQueryBuilder.setFindNodesMainQuery(elementType);

    if (Arrays.asList(returnTypes).contains(GraphLoaderReturnType.OBJECT)) {
      var generatedOgm = this.graphLoaderOgmFactory.create(graphDescription);
      this.genericSubQueryResolver.resolve(mainQueryBuilder, generatedOgm);
    } else {
      this.genericSubQueryResolver.resolve(mainQueryBuilder, graphDescription);
    }

    var query = arangoQueryBuilder.build(returnTypes);
//    var pretty = new AqlFormatter().format(query.getQueryString());

    var findQueryArangoDocuments = this.arangoDb.db()
        .query(query.getQueryString(), ArangoNodeFindDocument.class, query.getBindParameters())
        .stream()
        .toList();

    return this.mapNodesToGraphLoaderOutput(findQueryArangoDocuments, objectClass);
  }

  @NotNull
  private <T> GraphLoaderFindAsObjectOutput<T> resolveFindEdgesQuery(
      String elementType,
      AbstractEdgeDescription edgeDescription,
      ArangoQueryBuilder arangoQueryBuilder,
      Class<T> objectClass,
      GraphLoaderReturnType[] returnTypes
  ) {
    ArangoEdgeCollectionSubQueryBuilder mainQueryBuilder;
    if (edgeDescription.isOutgoing()) {
      mainQueryBuilder = arangoQueryBuilder.setFindOutgoingEdgeMainQuery(elementType);
    } else {
      mainQueryBuilder = arangoQueryBuilder.setFindIngoingEdgeMainQuery(elementType);
    }

    if (Arrays.asList(returnTypes).contains(GraphLoaderReturnType.OBJECT)) {
      var generatedOgm = this.graphLoaderOgmFactory.create(edgeDescription);
      this.genericSubQueryResolver.resolve(mainQueryBuilder, generatedOgm);
    } else {
      this.genericSubQueryResolver.resolve(mainQueryBuilder, edgeDescription);
    }

    var query = arangoQueryBuilder.build(returnTypes);
//    var pretty = new AqlFormatter().format(query.getQueryString());

    var findQueryArangoDocuments = this.arangoDb.db()
        .query(query.getQueryString(), ArangoEdgeFindDocument.class, query.getBindParameters())
        .stream()
        .toList();

    return this.mapEdgesToGraphLoaderOutput(findQueryArangoDocuments, objectClass);
  }

  @NotNull
  private <T> GraphLoaderFindAsObjectOutput<T> mapNodesToGraphLoaderOutput(
      List<ArangoNodeFindDocument> results,
      Class<T> objectClass
  ) {

    final InMemoryGraphRepository graph = new InMemoryGraphRepository();

    var originUuids = new ArrayList<UniqueIdentifier>();

    results.forEach(result -> {
      if (result.getGraphResponse().getMainGraphElements() == null) {
        return;
      }
      result.getGraphResponse().getMainGraphElements()
          .forEach(baseDocument -> {
            var traversableNode = TraversableNode.from(baseDocument, graph);
            graph.save(new Node(traversableNode));
            originUuids.add(traversableNode.getId());
          });
      result.getGraphResponse().getNodes()
          .forEach(graph::save);
      result.getGraphResponse().getEdges()
          .forEach(graph::save);
    });

    var finalData = new ArrayList<T>();
    results.forEach(
        result -> finalData.addAll(
            result.getData().stream().map(data -> this.objectMapper.convertValue(data, objectClass))
                .toList()
        )
    );

    return new GraphLoaderFindAsObjectOutput<>(
        finalData,
        originUuids,
        graph
    );
  }

  @NotNull
  private <T> GraphLoaderFindAsObjectOutput<T> mapEdgesToGraphLoaderOutput(
      List<ArangoEdgeFindDocument> results,
      Class<T> objectClass
  ) {
    final InMemoryGraphRepository graph = new InMemoryGraphRepository();

    var originUUIDs = new ArrayList<UniqueIdentifier>();

    results.forEach(result -> {
      if (result.getGraphResponse().getMainGraphElements() == null) {
        return;
      }
      result.getGraphResponse()
          .getNodes()
          .forEach(graph::save);
      result.getGraphResponse().getMainGraphElements()
          .forEach(baseDocument -> {
            var traversableEdge = TraversableEdge.from(baseDocument, graph);
            graph.save(new Edge(traversableEdge));
            originUUIDs.add(traversableEdge.getId());
          });
      result.getGraphResponse().getEdges()
          .forEach(graph::save);
    });

    var finalData = new ArrayList<T>();
    results.forEach(
        result -> finalData.addAll(
            result.getData().stream().map(data -> this.objectMapper.convertValue(data, objectClass))
                .toList()
        )
    );

    return new GraphLoaderFindAsObjectOutput<>(
        finalData,
        originUUIDs,
        graph
    );
  }

  @NotNull
  private <T> GraphLoaderGetAsObjectOutput<T> mapNodeToInMemoryGraph(
      List<ArangoNodeGetDocument> results,
      Class<T> objectClass
  ) {

    var graph = new InMemoryGraphRepository();

    results.forEach(result -> {
      if (result.getGraphResponse().getMainGraphElement() == null) {
        return;
      }
      var main = result.getGraphResponse().getMainGraphElement();
      graph.save(main);

      result.getGraphResponse().getNodes()
          .forEach(graph::save);
      result.getGraphResponse().getEdges()
          .forEach(graph::save);
    });

    var data = results.get(0).getData();
    return new GraphLoaderGetAsObjectOutput<>(
        this.objectMapper.convertValue(data, objectClass),
        graph
    );
  }

  @NotNull
  private <T> GraphLoaderGetAsObjectOutput<T> mapEdgeToInMemoryGraph(
      List<ArangoEdgeGetDocument> results,
      Class<T> objectClass
  ) {
    var graph = new InMemoryGraphRepository();
    results.forEach(result -> {
      if (result.getGraphResponse().getMainGraphElement() == null) {
        return;
      }
      result.getGraphResponse()
          .getNodes()
          .forEach(graph::save);

      var main = result.getGraphResponse().getMainGraphElement();
      var traversableEdge = TraversableEdge.from(main, graph);
      graph.save(new Edge(traversableEdge));

      result.getGraphResponse()
          .getEdges()
          .forEach(graph::save);
    });

    var data = results.get(0).getData();
    return new GraphLoaderGetAsObjectOutput<>(
        this.objectMapper.convertValue(data, objectClass),
        graph
    );
  }

  private String getCollectionType(GraphDescription graphDescription) {
    if (graphDescription instanceof AbstractNodeDescription) {
      var params = (NodeDescriptionParameters) graphDescription.getParameters();
      return params.getNodeType();
    }

    if (graphDescription instanceof AbstractEdgeDescription) {
      var params = (EdgeDescriptionParameters) graphDescription.getParameters();
      return params.getEdgeType();
    }
    throw new NotImplementedException("There should never be any other graph descriptions");
  }
}

