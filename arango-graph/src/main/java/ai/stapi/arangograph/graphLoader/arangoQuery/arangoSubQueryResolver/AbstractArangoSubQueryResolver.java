package ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver;

import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoGraphTraversalJoinable;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoKeptAttributesBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoMappedObjectBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchOptionsBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractAttributeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractNodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AllAttributesDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AttributeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.GraphElementTypeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.UuidIdentityDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.EdgeQueryDescription;
import ai.stapi.graphoperations.graphLoader.search.SearchQueryParameters;
import ai.stapi.graphoperations.objectGraphLanguage.LeafObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.ListObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectFieldDefinition;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectObjectGraphMapping;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractArangoSubQueryResolver implements ArangoSubQueryResolver {

  protected GenericSubQueryResolver genericSubQueryResolver;

  protected AbstractArangoSubQueryResolver(GenericSubQueryResolver genericSubQueryResolver) {
    this.genericSubQueryResolver = genericSubQueryResolver;
  }

  protected void resolveAttributes(
      GraphDescription graphDescription,
      ArangoKeptAttributesBuilder keptAttributesBuilder
  ) {
    var childGraphDescriptions = graphDescription.getChildGraphDescriptions();
    if (childGraphDescriptions.stream().anyMatch(UuidIdentityDescription.class::isInstance)) {
        keptAttributesBuilder.addId();
    }
    var hasAllAttributeDescription = childGraphDescriptions
        .stream()
        .anyMatch(AllAttributesDescription.class::isInstance);

    if (!hasAllAttributeDescription) {
      childGraphDescriptions.stream()
          .filter(AbstractAttributeDescription.class::isInstance)
          .map(description -> (AttributeDescriptionParameters) description.getParameters())
          .forEach(
              description -> keptAttributesBuilder.addAttribute(description.getAttributeName())
          );
    } else {
      keptAttributesBuilder.keepAllAttributes();
    }
  }

  protected void resolveSearchOptions(
      SearchQueryParameters searchParam,
      ArangoSearchOptionsBuilder searchOptionsBuilder
  ) {
    searchOptionsBuilder
        .addFilterOptions(searchParam.getFilterOptions())
        .addSortOptions(searchParam.getSortOptions())
        .setPaginationOption(searchParam.getPaginationOption());
  }

  protected void resolveSearchOptionsWithoutPagination(
      SearchQueryParameters searchParam,
      ArangoSearchOptionsBuilder searchOptionsBuilder
  ) {
    searchOptionsBuilder
        .addFilterOptions(searchParam.getFilterOptions())
        .addSortOptions(searchParam.getSortOptions());
  }

  protected void resolveGraphTraversalJoins(
      GraphDescription graphDescription,
      ArangoGraphTraversalJoinable subQueryBuilder
  ) {
    graphDescription.getChildGraphDescriptions().stream()
        .filter(AbstractEdgeDescription.class::isInstance)
        .map(AbstractEdgeDescription.class::cast)
        .forEach(edgeDescription -> {
          var joinedGraphTraversal = subQueryBuilder.joinGraphTraversal(edgeDescription);
          this.genericSubQueryResolver.resolve(joinedGraphTraversal, edgeDescription);
        });
  }

  protected void resolveMappedAttributes(
      Map<String, ObjectFieldDefinition> fields,
      ArangoMappedObjectBuilder mappedObjectBuilder
  ) {
    fields.forEach((key, value) -> {
      var objectGraphMapping = value.getFieldObjectGraphMapping();
      if (objectGraphMapping instanceof LeafObjectGraphMapping leafObjectGraphMapping) {
        this.resolveLeafOGM(key, leafObjectGraphMapping, mappedObjectBuilder);
      }
    });
  }

  protected void resolveLeafOGM(
      String fieldName,
      LeafObjectGraphMapping leafObjectGraphMapping,
      ArangoMappedObjectBuilder mappedObjectBuilder
  ) {
    var graphDescription = leafObjectGraphMapping.getGraphDescription();
    if (graphDescription instanceof AbstractAttributeDescription attributeValueDescription) {
      var param = (AttributeDescriptionParameters) attributeValueDescription.getParameters();
      mappedObjectBuilder.mapAttribute(fieldName, param.getAttributeName());
    }
    if (graphDescription instanceof UuidIdentityDescription) {
      mappedObjectBuilder.mapId(fieldName);
    }
    if (graphDescription instanceof GraphElementTypeDescription) {
      mappedObjectBuilder.mapType(fieldName);
    }
  }

  protected void resolveGraphTraversalMapping(
      Map<String, ObjectFieldDefinition> fields,
      ArangoGraphTraversalJoinable subQueryBuilder
  ) {
    fields.forEach((key, value) -> {
      var objectGraphMapping = value.getFieldObjectGraphMapping();
      if (objectGraphMapping instanceof ListObjectGraphMapping listObjectGraphMapping) {
        var childMapping = listObjectGraphMapping.getChildObjectGraphMapping();
        if (childMapping instanceof ObjectObjectGraphMapping) {
          var description = childMapping.getGraphDescription();
          if (description instanceof AbstractEdgeDescription edgeDescription) {
            if (edgeDescription instanceof EdgeQueryDescription edgeQueryDescription
                && (!edgeQueryDescription.isCompact())) {
              var traversalBuilder = subQueryBuilder.mapGraphTraversalAsConnections(
                  key,
                  edgeDescription
              );
              this.genericSubQueryResolver.resolve(traversalBuilder, childMapping);
              return;

            }
            var traversalBuilder = subQueryBuilder.mapGraphTraversal(
                key,
                edgeDescription
            );
            this.genericSubQueryResolver.resolve(traversalBuilder, childMapping);
          }
        }
      }
    });
  }

  @NotNull
  protected List<GraphDescription> getChildNodeDescriptions(
      AbstractEdgeDescription edgeDescription) {

    return edgeDescription.getChildGraphDescriptions().stream()
        .filter(AbstractNodeDescription.class::isInstance)
        .toList();
  }
}
