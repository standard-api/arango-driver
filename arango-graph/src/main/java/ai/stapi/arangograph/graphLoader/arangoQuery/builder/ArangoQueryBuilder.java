package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlList;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlString;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlLet;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlReturn;
import ai.stapi.arangograph.graphLoader.arangoQuery.exceptions.CannotBuildArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers.ArangoGenericSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ArangoQueryBuilder {

  private final ArangoGenericSearchOptionResolver searchOptionResolver;
  private final StructureSchemaFinder structureSchemaFinder;
  private ArangoMainQueryBuilder mainQueryBuilder;
  private boolean isMainCollection = false;

  public ArangoQueryBuilder(
      ArangoGenericSearchOptionResolver searchOptionResolver,
      StructureSchemaFinder structureSchemaFinder
  ) {
    this.searchOptionResolver = searchOptionResolver;
    this.structureSchemaFinder = structureSchemaFinder;
  }

  public ArangoNodeCollectionSubQueryBuilder setFindNodesMainQuery(String graphElementType) {
    var collectionSubQueryBuilder = new ArangoNodeCollectionSubQueryBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        graphElementType,
        ""
    );
    this.mainQueryBuilder = collectionSubQueryBuilder;
    this.isMainCollection = true;

    return collectionSubQueryBuilder;
  }

  public ArangoEdgeCollectionSubQueryBuilder setFindOutgoingEdgeMainQuery(String graphElementType) {
    var collectionSubQueryBuilder = ArangoEdgeCollectionSubQueryBuilder.outgoing(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        graphElementType,
        ""
    );
    this.mainQueryBuilder = collectionSubQueryBuilder;
    this.isMainCollection = true;

    return collectionSubQueryBuilder;
  }

  public ArangoEdgeCollectionSubQueryBuilder setFindIngoingEdgeMainQuery(String graphElementType) {
    var collectionSubQueryBuilder = ArangoEdgeCollectionSubQueryBuilder.ingoing(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        graphElementType,
        ""
    );
    this.mainQueryBuilder = collectionSubQueryBuilder;
    this.isMainCollection = true;

    return collectionSubQueryBuilder;
  }

  public ArangoNodeGetSubQueryBuilder setGetNodeMainQuery(
      String graphElementType,
      UniqueIdentifier mainNodeUuid
  ) {
    var getSubQueryBuilder = new ArangoNodeGetSubQueryBuilder(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        graphElementType,
        "",
        mainNodeUuid
    );
    this.mainQueryBuilder = getSubQueryBuilder;

    return getSubQueryBuilder;
  }

  public ArangoEdgeGetSubQueryBuilder setGetOutgoingEdgeMainQuery(
      String graphElementType,
      UniqueIdentifier mainEdgeUuid
  ) {
    var subQueryBuilder = ArangoEdgeGetSubQueryBuilder.outgoing(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        mainEdgeUuid,
        graphElementType,
        ""
    );
    this.mainQueryBuilder = subQueryBuilder;

    return subQueryBuilder;
  }

  public ArangoEdgeGetSubQueryBuilder setGetIngoingEdgeMainQuery(
      String graphElementType,
      UniqueIdentifier mainEdgeUuid
  ) {
    var subQueryBuilder = ArangoEdgeGetSubQueryBuilder.ingoing(
        this.searchOptionResolver,
        this.structureSchemaFinder,
        mainEdgeUuid,
        graphElementType,
        ""
    );
    this.mainQueryBuilder = subQueryBuilder;

    return subQueryBuilder;
  }

  public ArangoQuery build(GraphLoaderReturnType... returnTypes) {
    if (this.mainQueryBuilder == null) {
      throw CannotBuildArangoQuery.becauseNoMainQuerySet();
    }
    if (this.isMainCollection) {
      var findQuery = this.mainQueryBuilder.buildAsMain(returnTypes);

      var letNode = new AqlLet(
          new AqlVariable("mainSubQuery"),
          findQuery.getAqlNode()
      );

      var rootNode = new AqlRootNode(
          letNode,
          this.buildFindMainQueryReturnStatement(letNode.getVariable(), returnTypes)
      );
      return new ArangoQuery(
          rootNode,
          findQuery.getBindParameters()
      );
    }

    return this.mainQueryBuilder.buildAsMain(returnTypes);
  }

  private AqlReturn buildFindMainQueryReturnStatement(
      AqlVariable mainQueryVariable,
      GraphLoaderReturnType... returnTypes
  ) {
    var set = Arrays.stream(returnTypes).collect(Collectors.toSet());

    var graphObject = new AqlObject(Map.of(
        new AqlString("mainGraphElements"), mainQueryVariable
            .getAllItems()
            .getField(ArangoSubQueryBuilder.GRAPH_RESPONSE)
            .getField("mainGraphElement"),

        new AqlString("edges"), mainQueryVariable
            .getAllItems()
            .getField(ArangoSubQueryBuilder.GRAPH_RESPONSE)
            .getField("edges")
            .getFlattenItems(),

        new AqlString("nodes"), mainQueryVariable
            .getAllItems()
            .getField(ArangoSubQueryBuilder.GRAPH_RESPONSE)
            .getField("nodes")
            .getFlattenItems()
    ));

    var dataVariable = set.contains(GraphLoaderReturnType.OBJECT)
        ? mainQueryVariable.getAllItems().getField("data").getFlattenItems()
        : new AqlList();

    var graphVariable =
        set.contains(GraphLoaderReturnType.GRAPH) ? graphObject : new AqlObject(Map.of());

    var mainObject = new AqlObject(Map.of(
        new AqlString("data"), dataVariable,
        new AqlString(ArangoSubQueryBuilder.GRAPH_RESPONSE), graphVariable
    ));

    return new AqlReturn(mainObject);
  }
}
