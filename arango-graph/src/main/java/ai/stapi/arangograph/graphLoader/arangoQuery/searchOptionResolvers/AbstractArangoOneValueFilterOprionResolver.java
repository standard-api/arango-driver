package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperatorStatement;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlRootNode;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlVariable;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.highLevelOperations.AqlQuestionMarkOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AbstractOneValueFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.FilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.arangodb.internal.DocumentFields;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractArangoOneValueFilterOprionResolver<F extends FilterOption<?>>
    extends AbstractArangoSearchOptionResolver<F> {

    protected AbstractArangoOneValueFilterOprionResolver(
        ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
        StructureSchemaFinder structureSchemaFinder,
        GenericSubQueryResolver genericSubQueryResolver,
        GraphLoaderOgmFactory graphLoaderOGMFactory
    ) {
        super(
            arangoGenericSearchOptionResolver,
            structureSchemaFinder,
            genericSubQueryResolver,
            graphLoaderOGMFactory
        );
    }

    @NotNull
    protected ArangoQuery getArangoQuery(
        AbstractOneValueFilterOption<?> option,
        ArangoSearchResolvingContext context,
        Object decoratedValue,
        AqlOperator aqlOperator
    ) {
        var attributeTypePlaceholder = this.createAttributeNamePlaceholder(
            context,
            option.getOptionType()
        );
        var attributeValuePlaceholder = this.createAttributeValuePlaceholder(
            context,
            option.getOptionType()
        );
        
        var bindParameters = new HashMap<String, Object>();
        bindParameters.put(attributeValuePlaceholder, decoratedValue);
        var rightExpression = new AqlVariable(attributeValuePlaceholder).markAsBindParameter();
        if (option.isLeaf()) {
            if (option.isDescribingAttribute()) {
                var attributeName = option.getParameters().getAttributeName();
                bindParameters.put(attributeTypePlaceholder, attributeName);
                var filter = new AqlOperatorStatement(
                    this.getAttributeValue(context, attributeTypePlaceholder, attributeName),
                    aqlOperator,
                    rightExpression
                );
                return new ArangoQuery(filter, bindParameters);
            } else {
                var filter = new AqlOperatorStatement(
                    context.getDocumentName().getField(DocumentFields.KEY),
                    aqlOperator,
                    rightExpression
                );
                return new ArangoQuery(filter, bindParameters);
            }
        }
        var searchOptionSubQueryPostfix = this.createSearchOptionSubQueryPostfix(
            context, 
            option.getOptionType()
        );
        var searchOptionSubQuery = this.createSearchOptionSubQuery(
            option.getParameters().getAttributeNamePath(),
            context,
            searchOptionSubQueryPostfix,
            GraphLoaderReturnType.FILTER_OPTION
        );
        bindParameters.putAll(searchOptionSubQuery.getBindParameters());
        var searchOptionSubQueryAqlNode = searchOptionSubQuery.getAqlNode();
        if (searchOptionSubQueryAqlNode instanceof AqlRootNode rootNode) {
            var questionMarkOperator = rootNode.getAqlNodeList().stream()
                .filter(AqlQuestionMarkOperator.class::isInstance)
                .map(AqlQuestionMarkOperator.class::cast)
                .findFirst();
            if (questionMarkOperator.isPresent()) {
                questionMarkOperator.get().setDeepestRightHandExpression(
                    new AqlRootNode(
                        aqlOperator,
                        rightExpression
                    )
                );
                return new ArangoQuery(
                    searchOptionSubQueryAqlNode,
                    bindParameters
                );
            }
        }
        return new ArangoQuery(
            new AqlOperatorStatement(
                searchOptionSubQuery.getAqlNode(),
                aqlOperator,
                rightExpression
            ),
            bindParameters
        );
    }
}
