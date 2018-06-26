/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.console.common.server.login.filter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerFactory;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerNotFoundException;
import org.bonitasoft.console.common.server.filter.ExcludingPatternFilter;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.TenantIdAccessor;
import org.bonitasoft.console.common.server.login.TenantIdAccessorFactory;
import org.bonitasoft.console.common.server.login.localization.LoginUrl;
import org.bonitasoft.console.common.server.login.localization.LoginUrlException;
import org.bonitasoft.console.common.server.login.localization.RedirectUrl;
import org.bonitasoft.console.common.server.login.localization.RedirectUrlBuilder;
import org.bonitasoft.console.common.server.utils.SessionUtil;

/**
 * @author Vincent Elcrin
 * @author Anthony Birembaut
 */
public class AuthenticationFilter extends ExcludingPatternFilter {

    protected static final String AUTHENTICATION_FILTER_EXCLUDED_PAGES_PATTERN = "^/(bonita/)?(portal/themeResource$)|(theme/)|(portal/scripts)|(portal/formsService)|(apps/.+/API/)|(portal/resource/.+/API/)";

    protected static final String MAINTENANCE_JSP = "/maintenance.jsp";
    
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    private final LinkedList<AuthenticationRule> rules = new LinkedList<AuthenticationRule>();

    public AuthenticationFilter() {
        addRules();
    }

    protected void addRules() {
        addRule(new AlreadyLoggedInRule());
        addRule(new AutoLoginRule());
    }

    protected void addRule(final AuthenticationRule rule) {
        rules.add(rule);
    }

    public String getDefaultExcludedPages() {
        return AUTHENTICATION_FILTER_EXCLUDED_PAGES_PATTERN;
    }

    @Override
    public void proceedWithFiltering(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws ServletException, IOException {
        final HttpServletRequestAccessor requestAccessor = new HttpServletRequestAccessor((HttpServletRequest) request);
        doAuthenticationFiltering(requestAccessor, (HttpServletResponse) response, getTenantAccessor(requestAccessor), chain);
    }

    protected TenantIdAccessor getTenantAccessor(final HttpServletRequestAccessor requestAccessor) throws ServletException {
        return TenantIdAccessorFactory.getTenantIdAccessor(requestAccessor);
    }

    protected void doAuthenticationFiltering(final HttpServletRequestAccessor requestAccessor,
            final HttpServletResponse response,
            final TenantIdAccessor tenantIdAccessor,
            final FilterChain chain) throws ServletException, IOException {

        if (!isAuthorized(requestAccessor, response, tenantIdAccessor, chain)) {
            cleanHttpSession(requestAccessor.getHttpSession());
            if (requestAccessor.asHttpServletRequest().getMethod().equals("GET")) {
                response.sendRedirect(createLoginUrl(requestAccessor, tenantIdAccessor).getLocation());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }

    /**
     * @return true if one of the rules pass false otherwise
     */
    protected boolean isAuthorized(final HttpServletRequestAccessor requestAccessor,
            final HttpServletResponse response,
            final TenantIdAccessor tenantIdAccessor,
            final FilterChain chain) throws ServletException, IOException {

        for (final AuthenticationRule rule : getRules()) {
            try {
                if (rule.doAuthorize(requestAccessor, response, tenantIdAccessor)) {
                    chain.doFilter(requestAccessor.asHttpServletRequest(), response);
                    return true;
                }
            } catch (final ServletException e) {
                if (e.getCause() instanceof TenantIsPausedRedirectionToMaintenancePageException) {
                    return handleTenantPausedException(requestAccessor, response, e);
                } else {
                    throw e;
                }
            }
        }
        return false;
    }

    protected boolean handleTenantPausedException(final HttpServletRequestAccessor requestAccessor, final HttpServletResponse response,
            final ServletException e) throws ServletException {
        final TenantIsPausedRedirectionToMaintenancePageException tenantIsPausedException = (TenantIsPausedRedirectionToMaintenancePageException) e.getCause();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "redirection to maintenance page : " + e.getMessage(), e);
        }
        redirectToMaintenance(requestAccessor, response, tenantIsPausedException.getTenantId());
        return false;
    }

    /**
     * manage redirection to maintenance page
     *  @param request
     * @param response
     */
    protected void redirectToMaintenance(final HttpServletRequestAccessor request, final HttpServletResponse response, final long tenantId)
            throws ServletException {
        try {
            response.sendRedirect(request.asHttpServletRequest().getContextPath() + MAINTENANCE_JSP);
        } catch (final IOException e) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, e.getMessage());
            }
        }
    }

    protected LinkedList<AuthenticationRule> getRules() {
        return rules;
    }

    @Override
    public void destroy() {
    }

    // protected for test stubbing
    protected AuthenticationManager getAuthenticationManager(final TenantIdAccessor tenantIdAccessor) throws ServletException {
        try {
            return AuthenticationManagerFactory.getAuthenticationManager(tenantIdAccessor.ensureTenantId());
        } catch (final AuthenticationManagerNotFoundException e) {
            throw new ServletException(e);
        }
    }

    protected RedirectUrl makeRedirectUrl(final HttpServletRequestAccessor httpRequest) {
        final RedirectUrlBuilder builder = new RedirectUrlBuilder(httpRequest.getRequestedUri());
        builder.appendParameters(httpRequest.getParameterMap());
        return builder.build();
    }

    protected LoginUrl createLoginUrl(final HttpServletRequestAccessor requestAccessor, final TenantIdAccessor tenantIdAccessor) throws ServletException {
        try {
            return new LoginUrl(getAuthenticationManager(tenantIdAccessor),
                    makeRedirectUrl(requestAccessor).getUrl(), requestAccessor);
        } catch (final LoginUrlException e) {
            throw new ServletException(e);
        }
    }

    protected void cleanHttpSession(final HttpSession session) {
        SessionUtil.sessionLogout(session);
    }
}
