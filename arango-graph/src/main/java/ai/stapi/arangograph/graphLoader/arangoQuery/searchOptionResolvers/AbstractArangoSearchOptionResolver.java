package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQueryType;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlParentheses;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlQuestionMarkOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoGraphTraversalSubQueryBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoQueryByNodeTypeBuilder;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.arangograph.graphLoader.arangoQuery.exceptions.CannotBuildArangoQuery;
import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.AbstractEdgeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.AbstractSearchOptionResolver;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.serializableGraph.GraphElementKeys;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractArangoSearchOptionResolver<S extends SearchOption<?>>
    extends AbstractSearchOptionResolver<S, ArangoSearchResolvingContext, ArangoQuery> {

  protected final ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver;
  protected final GenericSubQueryResolver genericSubQueryResolver;
  protected final GraphLoaderOgmFactory graphLoaderOGMFactory;

  protected AbstractArangoSearchOptionResolver(
      ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
      StructureSchemaFinder structureSchemaFinder,
      GenericSubQueryResolver genericSubQueryResolver,
      GraphLoaderOgmFactory graphLoaderOGMFactory
  ) {
    super(structureSchemaFinder);
    this.arangoGenericSearchOptionResolver = arangoGenericSearchOptionResolver;
    this.genericSubQueryResolver = genericSubQueryResolver;
    this.graphLoaderOGMFactory = graphLoaderOGMFactory;
  }

  @NotNull
  protected String createAttributeNamePlaceholder(ArangoSearchResolvingContext context,
      String optionType) {
    if (context.getSubQueryPostfix().isBlank()) {
      return String.format("%sAttributeNamePlaceholder_%s", optionType,
          context.getPlaceholderPostfix());
    }
    return String.format(
        "%sAttributeNamePlaceholder_%s__%s",
        optionType,
        context.getPlaceholderPostfix(),
        context.getSubQueryPostfix()
    );
  }

  @NotNull
  protected String createSearchOptionSubQueryPostfix(ArangoSearchResolvingContext context,
      String optionType) {
    if (context.getSubQueryPostfix().isBlank()) {
      return String.format("%sOption_%s", optionType, context.getPlaceholderPostfix());
    }
    return String.format(
        "%sOption_%s__%s",
        optionType,
        context.getPlaceholderPostfix(),
        context.getSubQueryPostfix()
    );
  }

  protected String createAttributeValuePlaceholder(ArangoSearchResolvingContext context,
      String optionType) {
    if (context.getSubQueryPostfix().isBlank()) {
      return String.format("%sAttributeValuePlaceholder_%s", optionType,
          context.getPlaceholderPostfix());
    }
    return String.format(
        "%sAttributeValuePlaceholder_%s__%s",
        optionType,
        context.getPlaceholderPostfix(),
        context.getSubQueryPostfix()
    );
  }

  protected AqlVariable getFirstAttributeValue(
      ArangoSearchResolvingContext context,
      String attributeTypePlaceholder
  ) {
    return context.getDocumentName()
        .getField(GraphElementKeys.ATTRIBUTES)
        .getField("@" + attributeTypePlaceholder)
        .getItem(0)
        .getField("values")
        .getItem(0)
        .getField("value");
  }

  protected AqlNode getAttributeValue(
      ArangoSearchResolvingContext context,
      String attributeTypePlaceholder,
      String attributeName
  ) {
    var values = context.getDocumentName()
        .getField(GraphElementKeys.ATTRIBUTES)
        .getField("@" + attributeTypePlaceholder)
        .getItem(0)
        .getField("values");

    var fieldDefinition = this.structureSchemaFinder.getFieldDefinitionOrFallback(
        context.getGraphElementType(),
        attributeName
    );
    if (fieldDefinition.isList()) {
      return new AqlParentheses(values.getAllItems().getField("value"));
    } else {
      return values.getItem(0).getField("value");
    }
  }

  protected ArangoQuery createSearchOptionSubQuery(
      GraphDescription attributeNamePath,
      ArangoSearchResolvingContext context,
      String searchOptionSubQueryPostfix,
      GraphLoaderReturnType returnType
  ) {
    var originQueryType = context.getOriginQueryType();
    var relationshipStructureSchema = this.createRelationshipStructureSchema(
        attributeNamePath,
        context.getGraphElementType()
    );
    if (originQueryType.equals(ArangoQueryType.NODE)) {
      var graphTraversalBuilder = ArangoGraphTraversalSubQueryBuilder.asConnections(
          this.arangoGenericSearchOptionResolver,
          this.structureSchemaFinder,
          (AbstractEdgeDescription) attributeNamePath,
          searchOptionSubQueryPostfix,
          context.getDocumentName()
      );
      this.genericSubQueryResolver.resolve(graphTraversalBuilder, attributeNamePath);
      var subQuery = graphTraversalBuilder.build(returnType);
      var aqlParentheses = new AqlParentheses(subQuery.getAqlNode());
      if (returnType.equals(GraphLoaderReturnType.SORT_OPTION)) {
        return new ArangoQuery(
            aqlParentheses.getItem(0),
            subQuery.getBindParameters()
        );
      } else {
        AqlQuestionMarkOperator aqlQuestionMarkOperator = null;
        for (var relationshipSchema : relationshipStructureSchema) {
          if (relationshipSchema.fieldDefinition().isList()) {
            if (aqlQuestionMarkOperator != null) {
              aqlQuestionMarkOperator.setDeepestRightHandExpression(
                  new AqlQuestionMarkOperator(
                      null,
                      new AqlOperator(relationshipSchema.operator().name())
                  )
              );
            } else {
              aqlQuestionMarkOperator = new AqlQuestionMarkOperator(
                  null,
                  new AqlOperator(relationshipSchema.operator().name())
              );
            }
          }
        }
        AqlNode finalParentheses = aqlParentheses;
        if (!relationshipStructureSchema.isEmpty()) {
          var first = relationshipStructureSchema.get(0);
          if (!first.fieldDefinition().isList()) {
            finalParentheses = finalParentheses.getItem(0);
          }
        }
        return new ArangoQuery(
            aqlQuestionMarkOperator == null ? finalParentheses
                : new AqlRootNode(finalParentheses, aqlQuestionMarkOperator),
            subQuery.getBindParameters()
        );
      }
    }
    if (
        List.of(
            ArangoQueryType.OUTGOING_EDGE,
            ArangoQueryType.INGOING_EDGE
        ).contains(originQueryType)
    ) {
      var nodeDocument = originQueryType.equals(ArangoQueryType.OUTGOING_EDGE) ?
          context.getDocumentName().getField("_to") :
          context.getDocumentName().getField("_from");
      var queryByNodeByTypeBuilder = new ArangoQueryByNodeTypeBuilder(
          this.arangoGenericSearchOptionResolver,
          this.structureSchemaFinder,
          searchOptionSubQueryPostfix,
          nodeDocument
      );
      var nodeDescriptionParameters = (NodeDescriptionParameters) attributeNamePath.getParameters();
      var nodeGetOption = queryByNodeByTypeBuilder.addGetNodeOption(
          nodeDescriptionParameters.getNodeType()
      );
      this.genericSubQueryResolver.resolve(nodeGetOption, attributeNamePath);
      var subQuery = queryByNodeByTypeBuilder.build(returnType);
      var aqlParentheses = new AqlParentheses(subQuery.getAqlNode()).getItem(0);
      if (returnType.equals(GraphLoaderReturnType.SORT_OPTION)) {
        return new ArangoQuery(
            aqlParentheses,
            subQuery.getBindParameters()
        );
      } else {
        AqlQuestionMarkOperator aqlQuestionMarkOperator = null;
        for (var relationshipSchema : relationshipStructureSchema) {
          if (relationshipSchema.fieldDefinition().isList()) {
            if (aqlQuestionMarkOperator != null) {
              aqlQuestionMarkOperator.setDeepestRightHandExpression(
                  new AqlQuestionMarkOperator(
                      null,
                      new AqlOperator(relationshipSchema.operator().name())
                  )
              );
            } else {
              aqlQuestionMarkOperator = new AqlQuestionMarkOperator(
                  null,
                  new AqlOperator(relationshipSchema.operator().name())
              );
            }
          }
        }
        return new ArangoQuery(
            aqlQuestionMarkOperator == null ? aqlParentheses
                : new AqlRootNode(aqlParentheses, aqlQuestionMarkOperator),
            subQuery.getBindParameters()
        );
      }

    }
    if (originQueryType.equals(ArangoQueryType.GRAPH_TRAVERSAL)) {
      var graphTraversalBuilder = ArangoGraphTraversalSubQueryBuilder.asConnections(
          this.arangoGenericSearchOptionResolver,
          this.structureSchemaFinder,
          (AbstractEdgeDescription) attributeNamePath,
          searchOptionSubQueryPostfix,
          context.getDocumentName()
      );
      this.genericSubQueryResolver.resolve(graphTraversalBuilder, attributeNamePath);
      var subQuery = graphTraversalBuilder.build(returnType);
      var aqlParentheses = new AqlParentheses(subQuery.getAqlNode());
      if (returnType.equals(GraphLoaderReturnType.SORT_OPTION)) {
        return new ArangoQuery(
            aqlParentheses.getItem(0),
            subQuery.getBindParameters()
        );
      } else {
        AqlQuestionMarkOperator aqlQuestionMarkOperator = null;
        for (var relationshipSchema : relationshipStructureSchema) {
          if (relationshipSchema.fieldDefinition().isList()) {
            if (aqlQuestionMarkOperator != null) {
              aqlQuestionMarkOperator.setDeepestRightHandExpression(
                  new AqlQuestionMarkOperator(
                      null,
                      new AqlOperator(relationshipSchema.operator().name())
                  )
              );
            } else {
              aqlQuestionMarkOperator = new AqlQuestionMarkOperator(
                  null,
                  new AqlOperator(relationshipSchema.operator().name())
              );
            }
          }
        }
        AqlNode finalParentheses = aqlParentheses;
        if (!relationshipStructureSchema.isEmpty()) {
          var first = relationshipStructureSchema.get(0);
          if (!first.fieldDefinition().isList()) {
            finalParentheses = finalParentheses.getItem(0);
          }
        }
        return new ArangoQuery(
            aqlQuestionMarkOperator == null ? finalParentheses
                : new AqlRootNode(finalParentheses, aqlQuestionMarkOperator),
            subQuery.getBindParameters()
        );
      }
    }

    throw CannotBuildArangoQuery.becauseEncounteredNonExisitingOriginQueryType(originQueryType);
  }

}
