package ai.stapi.arangograph.graphLoader.arangoQuery.searchOptionResolvers;

import ai.stapi.arangograph.graphLoader.arangoQuery.ArangoQuery;
import ai.stapi.arangograph.graphLoader.arangoQuery.arangoSubQueryResolver.GenericSubQueryResolver;
import ai.stapi.arangograph.graphLoader.arangoQuery.ast.AqlOperator;
import ai.stapi.arangograph.graphLoader.arangoQuery.builder.ArangoSearchResolvingContext;
import ai.stapi.graphoperations.graphLoader.graphLoaderOGMFactory.GraphLoaderOgmFactory;
import ai.stapi.graphoperations.graphLoader.search.SearchOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.LowerThanFilterOption;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

public class ArangoLowerThanFilterOptionResolver
    extends AbstractArangoOneValueFilterOprionResolver<LowerThanFilterOption<?>> {

    public ArangoLowerThanFilterOptionResolver(
        ArangoGenericSearchOptionResolver arangoGenericSearchOptionResolver,
        StructureSchemaFinder structureSchemaFinder, 
        GenericSubQueryResolver genericSubQueryResolver,
        GraphLoaderOgmFactory graphLoaderOgmFactory
    ) {
        super(
            arangoGenericSearchOptionResolver, 
            structureSchemaFinder, 
            genericSubQueryResolver,
            graphLoaderOgmFactory
        );
    }

    @Override
    public boolean supports(SearchOption<?> option) {
        return option instanceof LowerThanFilterOption<?>;
    }

    @Override
    public ArangoQuery resolveTyped(
        LowerThanFilterOption<?> option,
        ArangoSearchResolvingContext context
    ) {
        var aqlOperator = AqlOperator.lessThan();
        var decoratedValue = option.getParameters().getAttributeValue();
        return this.getArangoQuery(option, context, decoratedValue, aqlOperator);
    }
}
