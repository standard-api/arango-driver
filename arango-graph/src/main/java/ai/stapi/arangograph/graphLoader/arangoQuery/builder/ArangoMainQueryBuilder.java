package ai.stapi.arangograph.graphLoader.arangoQuery.builder;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlObject;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlString;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import java.util.Map;

public interface ArangoMainQueryBuilder extends ArangoSubQueryBuilder {

  ArangoQuery buildAsMain(GraphLoaderReturnType... returnTypes);

  default AqlObject createMainGraphObject(AqlNode main, AqlNode nodes, AqlNode edges) {
    return new AqlObject(Map.of(
        new AqlString("mainGraphElement"), main,
        new AqlString("nodes"), nodes,
        new AqlString("edges"), edges
    ));
  }

}
