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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorItem.ATTRIBUTE_PROCESS_ID;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorCriterion;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessConnectorDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIFilterMandatoryException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.server.api.APIHasGet;
import org.bonitasoft.web.toolkit.server.api.APIHasSearch;
import org.bonitasoft.web.toolkit.server.api.Datastore;
import org.bonitasoft.web.toolkit.server.search.ItemSearchResult;

/**
 * @author Séverin Moussel
 */
public class APIProcessConnector extends ConsoleAPI<ProcessConnectorItem> implements
        APIHasGet<ProcessConnectorItem>,
        APIHasSearch<ProcessConnectorItem>
{

    @Override
    protected ItemDefinition defineItemDefinition() {
        return ProcessConnectorDefinition.get();
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ProcessConnectorDatastore(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ConnectorCriterion.DEFINITION_ID_ASC.name();
    }

    @Override
    public ItemSearchResult<ProcessConnectorItem> search(final int page, final int resultsByPage, final String search, final String orders,
            final Map<String, String> filters) {

        if (MapUtil.isBlank(filters, ATTRIBUTE_PROCESS_ID)) {
            throw new APIFilterMandatoryException(ATTRIBUTE_PROCESS_ID);
        }

        return super.search(page, resultsByPage, search, orders, filters);
    }

    @Override
    protected void fillDeploys(final ProcessConnectorItem item, final List<String> deploys) {

        if (isDeployable(ATTRIBUTE_PROCESS_ID, deploys, item)) {
            item.setDeploy(ATTRIBUTE_PROCESS_ID,
                    new ProcessDatastore(getEngineSession()).get(item.getProcessId()));
        }
    }
}
