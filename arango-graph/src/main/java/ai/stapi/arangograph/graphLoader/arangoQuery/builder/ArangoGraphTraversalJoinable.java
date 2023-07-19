package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;

public interface ArangoGraphTraversalJoinable {

  ArangoGraphTraversalSubQueryBuilder joinGraphTraversal(AbstractEdgeDescription edgeDescription);

  ArangoGraphTraversalSubQueryBuilder mapGraphTraversal(
      String fieldName,
      AbstractEdgeDescription edgeDescription
  );

  ArangoGraphTraversalSubQueryBuilder mapGraphTraversalAsConnections(
      String fieldName,
      AbstractEdgeDescription edgeDescription
  );

}
