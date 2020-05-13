package com.enonic.app.rewrite.provider.repo;

import java.util.List;

import com.google.common.collect.Lists;

import com.enonic.app.rewrite.redirect.RedirectType;
import com.enonic.app.rewrite.rewrite.RewriteContextKey;
import com.enonic.app.rewrite.rewrite.RewriteMapping;
import com.enonic.app.rewrite.rewrite.RewriteRule;
import com.enonic.app.rewrite.rewrite.RewriteRules;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeEditor;

public class RewriteMappingSerializer
{
    private static final String RULE_FROM_KEY = "from";

    private static final String RULE_TARGET_KEY = "to";

    private static final String RULE_ORDER_KEY = "order";

    private static final String RULE_TYPE_KEY = "type";

    private static final String CONTEXT_KEY = "rewriteContextKey";

    private static final String RULES_KEY = "rewriteRules";

    static RewriteMapping fromNode( final Node node )
    {
        final PropertyTree data = node.data();

        return RewriteMapping.create().
            contextKey( new RewriteContextKey( data.getString( CONTEXT_KEY ) ) ).
            rewriteRules( createRewriteRules( node ) ).
            build();
    }

    private static RewriteRules createRewriteRules( final Node node )
    {
        final RewriteRules.Builder builder = RewriteRules.create();

        final Iterable<PropertySet> ruleSets = node.data().getSets( RULES_KEY );

        if ( ruleSets != null )
        {
            ruleSets.forEach( ( ruleSet ) -> {
                builder.addRule( RewriteRule.create().
                    from( ruleSet.getString( RULE_FROM_KEY ) ).
                    target( ruleSet.getString( RULE_TARGET_KEY ) ).
                    order( ruleSet.getDouble( RULE_ORDER_KEY ).intValue() ).
                    type( RedirectType.valueOf( ruleSet.getString( RULE_TYPE_KEY ) ) ).
                    build() );
            } );
        }

        return builder.build();
    }

    static PropertyTree toCreateNodeData( final RewriteMapping rewriteMapping )
    {
        final PropertyTree propertyTree = new PropertyTree();
        final PropertySet data = propertyTree.getRoot();

        if ( rewriteMapping.getRewriteRules() != null )
        {
            data.addSets( RULES_KEY, createRules( rewriteMapping.getRewriteRules() ) );
        }

        data.setString( CONTEXT_KEY, rewriteMapping.getContextKey().toString() );
        return propertyTree;
    }

    static NodeEditor toUpdateNodeData( final RewriteMapping rewriteMapping )
    {
        return toBeEdited -> {
            toBeEdited.data = toCreateNodeData( rewriteMapping );
        };
    }

    private static PropertySet[] createRules( final RewriteRules rewriteRules )
    {
        final List<PropertySet> setList = Lists.newArrayList();

        rewriteRules.forEach( rule -> {
            final PropertySet ruleData = new PropertySet();
            ruleData.addString( RULE_FROM_KEY, rule.getFrom() );
            ruleData.addString( RULE_TARGET_KEY, rule.getTarget().path() );
            ruleData.addDouble( RULE_ORDER_KEY, toDouble( rule.getOrder() ) );
            ruleData.addString( RULE_TYPE_KEY, rule.getType().name() );
            setList.add( ruleData );
        } );

        return setList.toArray( new PropertySet[setList.size()] );
    }

    private static Double toDouble( final Integer value )
    {
        return value != null ? value.doubleValue() : null;
    }


}
