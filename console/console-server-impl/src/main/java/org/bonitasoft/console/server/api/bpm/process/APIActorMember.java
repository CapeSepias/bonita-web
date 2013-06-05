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
package org.bonitasoft.console.server.api.bpm.process;

import static org.bonitasoft.console.client.model.bpm.process.ActorMemberItem.ATTRIBUTE_ACTOR_ID;
import static org.bonitasoft.console.client.model.portal.profile.AbstractMemberItem.FILTER_MEMBER_TYPE;

import java.util.List;
import java.util.Map;

import org.bonitasoft.console.client.model.bpm.process.ActorMemberDefinition;
import org.bonitasoft.console.client.model.bpm.process.ActorMemberItem;
import org.bonitasoft.console.server.api.userXP.profile.AbstractAPIMember;
import org.bonitasoft.console.server.datastore.bpm.process.ActorDatastore;
import org.bonitasoft.console.server.datastore.bpm.process.ActorMemberDatastore;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIFilterEmptyException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIFilterMandatoryException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.server.api.Datastore;
import org.bonitasoft.web.toolkit.server.search.ItemSearchResult;

/**
 * @author Julien Mege
 * @author Séverin Moussel
 */
public class APIActorMember extends AbstractAPIMember<ActorMemberItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return ActorMemberDefinition.get();
    }

    @Override
    public String defineDefaultSearchOrder() {
        return "";
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ActorMemberDatastore(getEngineSession());
    }

    @Override
    public ItemSearchResult<ActorMemberItem> search(final int page, final int resultsByPage, final String search, final String orders,
            final Map<String, String> filters) {
        if (MapUtil.isBlank(filters, ATTRIBUTE_ACTOR_ID)) {
            throw new APIFilterMandatoryException(ATTRIBUTE_ACTOR_ID);
        }

        if (filters.containsKey(FILTER_MEMBER_TYPE) && MapUtil.isBlank(filters, FILTER_MEMBER_TYPE)) {
            throw new APIFilterEmptyException(FILTER_MEMBER_TYPE);
        }

        return super.search(page, resultsByPage, search, orders, filters);
    }

    @Override
    protected void fillDeploys(final ActorMemberItem item, final List<String> deploys) {
        if (isDeployable(ActorMemberItem.ATTRIBUTE_ACTOR_ID, deploys, item)) {
            item.setDeploy(ActorMemberItem.ATTRIBUTE_ACTOR_ID, new ActorDatastore(getEngineSession()).get(item.getActorId()));
        }

        super.fillDeploys(item, deploys);
    }

}
