/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.console.server.datastore.bpm.cases;

import java.util.Map;

import org.bonitasoft.console.client.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.console.common.server.datastore.CommonDatastore;
import org.bonitasoft.console.common.server.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.server.api.DatastoreHasGet;
import org.bonitasoft.web.toolkit.server.api.DatastoreHasSearch;
import org.bonitasoft.web.toolkit.server.search.ItemSearchResult;

/**
 * @author Séverin Moussel
 */
public class ArchivedCaseDatastore extends CommonDatastore<ArchivedCaseItem, ArchivedProcessInstance> implements DatastoreHasGet<ArchivedCaseItem>,
        DatastoreHasSearch<ArchivedCaseItem> {

    public ArchivedCaseDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected ArchivedCaseItem convertEngineToConsoleItem(final ArchivedProcessInstance item) {

        final ArchivedCaseItem result = new ArchivedCaseItem();

        result.setId(item.getId());
        result.setLastUpdateDate(item.getLastUpdate());
        result.setState(item.getState());
        result.setStartDate(item.getStartDate());
        result.setStartedByUserId(item.getStartedBy());
        result.setEndDate(item.getEndDate());
        result.setProcessId(item.getProcessDefinitionId());
        result.setArchivedDate(item.getArchiveDate());
        result.setSourceObjectId(item.getSourceObjectId());

        return result;
    }

    @Override
    public ItemSearchResult<ArchivedCaseItem> search(final int page, final int resultsByPage, final String search, final String orders,
            final Map<String, String> filters) {

        // Build search
        final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage, orders, search);

        addFilterToSearchBuilder(filters, builder, ArchivedCaseItem.ATTRIBUTE_PROCESS_ID, ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID);
        addFilterToSearchBuilder(filters, builder, ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID, ProcessInstanceSearchDescriptor.STARTED_BY);

        // Run search depending on filters passed
        final SearchResult<ArchivedProcessInstance> searchResult = runSearch(filters, builder);

        // Convert to ConsoleItems
        return new ItemSearchResult<ArchivedCaseItem>(
                page,
                resultsByPage,
                searchResult.getCount(),
                convertEngineToConsoleItemsList(searchResult.getResult()));
    }

    private SearchResult<ArchivedProcessInstance> runSearch(final Map<String, String> filters, final SearchOptionsBuilder builder) {

        try {
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(getEngineSession());

            if (filters.containsKey(ArchivedCaseItem.FILTER_USER_ID)) {
                return processAPI.searchArchivedProcessInstancesInvolvingUser(MapUtil.getValueAsLong(filters, ArchivedCaseItem.FILTER_USER_ID),
                        builder.done());
            }

            if (filters.containsKey(ArchivedCaseItem.FILTER_SUPERVISOR_ID)) {
                return processAPI
                        .searchArchivedProcessInstancesSupervisedBy(MapUtil.getValueAsLong(filters, ArchivedCaseItem.FILTER_SUPERVISOR_ID), builder.done());
            }

            return processAPI.searchArchivedProcessInstances(builder.done());
        } catch (final Exception e) {
            throw new APIException(e);
        }

    }

    @Override
    public ArchivedCaseItem get(final APIID id) {
        try {
            return convertEngineToConsoleItem(TenantAPIAccessor.getProcessAPI(getEngineSession())
                    .getArchivedProcessInstance(id.toLong()));
            // .getFinalArchivedProcessInstance(id.toLong()));
        } catch (final Exception e) {
            throw new APIException(e);
        }

    }
}
