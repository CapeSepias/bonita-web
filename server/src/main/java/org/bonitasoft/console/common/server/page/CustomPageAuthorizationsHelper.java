package org.bonitasoft.console.common.server.page;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor;
import org.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;

import org.bonitasoft.livingapps.ApplicationModel;
import org.bonitasoft.livingapps.ApplicationModelFactory;

/**
 * @author Julien Mege
 */
public class CustomPageAuthorizationsHelper {

    private final ApplicationAPI applicationAPI;
    private final PageAPI pageApi;
    private final APISession apiSession;
    private final ApplicationModelFactory applicationFactory;

    public CustomPageAuthorizationsHelper(final APISession apiSession, final ApplicationAPI applicationAPI, final PageAPI pageApi, final ApplicationModelFactory applicationModelFactory) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        this.applicationAPI = applicationAPI;
        this.pageApi = pageApi;
        this.apiSession = apiSession;
        this.applicationFactory = applicationModelFactory;
    }

    public boolean isPageAuthorized(final String applicationToken, final String pageToken) {
        try {
            Long applicationId = getApplicationId(applicationToken);
            final ApplicationModel application = applicationFactory.createApplicationModel(applicationToken);

            if (applicationId == null || !application.authorize(apiSession)) {
                return false;
            }

            return applicationAPI.searchApplicationPages(new SearchOptionsBuilder(0, 0)
                    .filter(ApplicationPageSearchDescriptor.APPLICATION_ID, applicationId)
                    .filter(ApplicationPageSearchDescriptor.PAGE_ID, pageApi.getPageByName(pageToken).getId())
                    .done()).getCount() > 0;
        } catch (final Exception e) {
            return false;
        }
    }

    private Long getApplicationId(String applicationToken) throws BonitaException {
        SearchResult<Application> applicationSResult = applicationAPI.searchApplications(new SearchOptionsBuilder(0, 1)
                .filter(ApplicationSearchDescriptor.TOKEN, applicationToken).done());

        if (applicationSResult.getResult().isEmpty()) {
            return null;
        }

        return applicationSResult.getResult().get(0).getId();
    }

}
