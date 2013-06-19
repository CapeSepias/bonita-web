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
package org.bonitasoft.web.rest.server.datastore.bpm.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.connector.ArchivedConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.connector.ArchivedConnectorInstanceItem;
import org.bonitasoft.web.rest.model.bpm.connector.ConnectorInstanceItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.server.api.DatastoreHasSearch;
import org.bonitasoft.web.toolkit.server.search.ItemSearchResult;

/**
 * @author Julien Mege
 * 
 */
public class ArchivedConnectorInstanceDatastore extends CommonDatastore<ArchivedConnectorInstanceItem, ArchivedConnectorInstance> implements
        DatastoreHasSearch<ArchivedConnectorInstanceItem> {

    public ArchivedConnectorInstanceDatastore(final APISession engineSession) {
        super(engineSession);
    }

    protected ProcessAPI getProcessAPI() {
        try {
            return TenantAPIAccessor.getProcessAPI(getEngineSession());
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ItemSearchResult<ArchivedConnectorInstanceItem> search(final int page, final int resultsByPage, final String search, final String orders,
            final Map<String, String> filters) {

        SearchResult<ArchivedConnectorInstance> searchConnectorInstances;
        try {
            searchConnectorInstances = getProcessAPI().searchArchivedConnectorInstances(buildSearchOptions(page, resultsByPage, search, orders, filters));
        } catch (final Exception e) {
            throw new APIException(e);
        }

        if (searchConnectorInstances != null) {
            final List<ArchivedConnectorInstanceItem> convertedResult = convertEngineItemsIntoConsoleItems(searchConnectorInstances.getResult());
            return new ItemSearchResult<ArchivedConnectorInstanceItem>(page, convertedResult.size(), searchConnectorInstances.getCount(),
                    convertedResult);
        } else {
            throw new APIException("Search failed for the following parameters <page: " + page + " - resulsByPage: " + resultsByPage + " - search: " + search
                    + " - filters: " + filters + " - orders: " + orders + ">");
        }
    }

    /**
     * Build search option converting console filters into engine filter
     * 
     * @param page
     * @param resultsByPage
     * @param search
     * @param orders
     * @param filters
     * @return
     */
    protected SearchOptions buildSearchOptions(final int page, final int resultsByPage, final String search, final String orders,
            final Map<String, String> filters) {
        final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage, orders, search);
        addFilterToSearchBuilder(filters, builder, ConnectorInstanceItem.ATTRIBUTE_CONTAINER_ID, ConnectorInstancesSearchDescriptor.CONTAINER_ID);
        addFilterToSearchBuilder(filters, builder, ConnectorInstanceItem.ATTRIBUTE_STATE, ConnectorInstancesSearchDescriptor.STATE);
        return builder.done();
    }

    /**
     * Convert engine item into console item used by web
     * 
     * @param engineItem
     *            Item provided by engine
     */
    @Override
    protected ArchivedConnectorInstanceItem convertEngineToConsoleItem(final ArchivedConnectorInstance engineItem) {
        return new ArchivedConnectorInstanceItemWrapper(engineItem);
    }

    /**
     * 
     * @param searchResult
     * @return
     */
    protected List<ArchivedConnectorInstanceItem> convertEngineItemsIntoConsoleItems(final List<ArchivedConnectorInstance> engineItemList) {
        if (engineItemList != null) {
            final List<ArchivedConnectorInstanceItem> consoleItemList = new ArrayList<ArchivedConnectorInstanceItem>();
            for (final ArchivedConnectorInstance engineItem : engineItemList) {
                consoleItemList.add(convertEngineToConsoleItem(engineItem));
            }
            return consoleItemList;
        } else {
            throw new RuntimeException("List of engine items is null");
        }
    }

}
