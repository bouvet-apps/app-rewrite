package com.enonic.app.rewrite;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.app.rewrite.context.ContextResolver;
import com.enonic.app.rewrite.engine.RewriteEngine;
import com.enonic.app.rewrite.engine.RewriteEngineImpl;
import com.enonic.xp.annotation.Order;
import com.enonic.xp.web.filter.OncePerRequestFilter;

@Component(immediate = true, service = Filter.class)
@Order(100)
@WebFilter("/*")
public class RewriteFilter
    extends OncePerRequestFilter
{
    private RewriteFilterConfig config;

    private Patterns excludePatterns;

    private Patterns includePatterns;

    private RewriteEngine rewriteEngine;

    public final static Logger LOG = LoggerFactory.getLogger( RewriteFilter.class );

    @Activate
    public void activate()
    {
        System.out.println( "Activating RewriteFilter" );
        this.excludePatterns = new Patterns( config.excludePatterns() );
        this.rewriteEngine = new RewriteEngineImpl();
    }

    @Override
    protected void doHandle( final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain )
        throws Exception

    {
        LOG.info( "Handling in RewriteFilter" );
        LOG.debug( "Im in debug-mode" );

        final boolean responseCommitted = doRewriteURL( req, res, chain );
        if ( !responseCommitted )
        {
            chain.doFilter( req, res );
        }
    }

    private boolean doExclude( final RequestWrapper request )
    {
        return this.excludePatterns.anyMatch( request.getRequestURI() );
    }

    private boolean doInclude( final RequestWrapper request )
    {
        //return this.includePatterns.anyMatch( request.getRequestURI() );
        return true;
    }

    private boolean doRewriteURL( HttpServletRequest hsRequest, HttpServletResponse hsResponse, FilterChain chain )
        throws Exception
    {

        LOG.info( "Checking if URL is to be rewritten" );

        if ( !this.config.enabled() )
        {
            return false;
        }

        final RequestWrapper wrappedRequest = new RequestWrapper( hsRequest );
        wrappedRequest.setContextPath( ContextResolver.resolve( hsRequest ) );

        if ( !doInclude( wrappedRequest ) || doExclude( wrappedRequest ) )
        {
            LOG.debug( "Skipped: " + hsRequest.getRequestURI() );
            return false;
        }
        final String url = rewriteEngine.process( hsRequest );
        if ( url == null )
        {
            LOG.debug( "Ignored: " + hsRequest.getRequestURI() );
            return false;
        }

        LOG.debug( "Changed from: " + hsRequest.getRequestURI() + " to: " + url );

        hsResponse.sendRedirect( url );

        return true;
    }


    @Reference
    public void setConfig( final RewriteFilterConfig config )
    {
        this.config = config;
    }

}
