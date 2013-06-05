/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.console.server.datastore.bpm.process.helper;

import java.util.Map;

import org.bonitasoft.console.client.model.bpm.process.ProcessItem;
import org.bonitasoft.console.server.datastore.SearchOptionsCreator;
import org.bonitasoft.console.server.datastore.Sorts;
import org.bonitasoft.console.server.datastore.converter.ItemSearchResultConverter;
import org.bonitasoft.console.server.datastore.filter.Filters;
import org.bonitasoft.console.server.engineclient.ProcessEngineClient;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.server.api.DatastoreHasSearch;
import org.bonitasoft.web.toolkit.server.search.ItemSearchResult;

/**
 * @author Vincent Elcrin
 * 
 */
public class SearchProcessHelper implements DatastoreHasSearch<ProcessItem> {

    private ProcessEngineClient engineClient;

    public SearchProcessHelper(ProcessEngineClient engineClient) {
        this.engineClient = engineClient;
    }

    @Override
    public ItemSearchResult<ProcessItem> search(int page, int resultsByPage, String search, String orders, Map<String, String> filters) {
        SearchOptionsCreator searchOptions = new SearchOptionsCreator(page, resultsByPage, search,
                new Sorts(orders, new ProcessSearchDescriptorConverter()),
                new Filters(filters, new SearchProcessFilterCreator()));
        final SearchResult<ProcessDeploymentInfo> result = runSearch(filters, searchOptions.create());
        return convertResult(page, resultsByPage, result);
    }

    private SearchResult<ProcessDeploymentInfo> runSearch(Map<String, String> filters, SearchOptions searchOptions) {

        if (isFilteringOn(filters, ProcessItem.FILTER_USER_ID, ProcessItem.FILTER_RECENT_PROCESSES)) {
            return engineClient.searchRecentlyStartedProcessDefinitions(getApiId(filters, ProcessItem.FILTER_USER_ID), searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_USER_ID, ProcessItem.FILTER_CATEGORY_ID)
                && filters.get(ProcessItem.FILTER_CATEGORY_ID) == null) {
            return engineClient.searchUncategorizedProcessDefinitionsUserCanStart(getApiId(filters, ProcessItem.FILTER_USER_ID), searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_USER_ID)) {
            return engineClient.searchProcessDeploymentInfos(getApiId(filters, ProcessItem.FILTER_USER_ID), searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_SUPERVISOR_ID, ProcessItem.FILTER_CATEGORY_ID)) {
            return engineClient.searchProcessDefinitionsSupervisedBy(getApiId(filters, ProcessItem.FILTER_SUPERVISOR_ID), searchOptions);

        } else if (isFilteringOn(filters, ProcessItem.FILTER_SUPERVISOR_ID)) {
            return engineClient.searchUncategorizedProcessDefinitionsSupervisedBy(getApiId(filters, ProcessItem.FILTER_SUPERVISOR_ID), searchOptions);

        } else {
            return engineClient.searchProcessDefinitions(searchOptions);
        }
    }

    private boolean isFilteringOn(Map<String, String> filters, String... attributes) {
        for (String attribute : attributes) {
            if (!filters.containsKey(attribute)) {
                return false;
            }
        }
        return true;
    }

    private Long getApiId(Map<String, String> filters, String attribute) {
        return APIID.makeAPIID(filters.get(attribute)).toLong();
    }

    private ItemSearchResult<ProcessItem> convertResult(int page, int nbResultsByPage, final SearchResult<ProcessDeploymentInfo> result) {
        return new ItemSearchResultConverter<ProcessItem, ProcessDeploymentInfo>(page, nbResultsByPage, result, new ProcessItemConverter()).toItemSearchResult();
    }
}
