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
package org.bonitasoft.console.server.service;

import static org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n._;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.InvalidOrganizationFileFormatException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.toolkit.server.ServiceException;

/**
 * @author Séverin Moussel
 */
public class OrganizationImportService extends ConsoleService {

    public final static String TOKEN = "/organization/import";

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(OrganizationImportService.class.getName());
    
    /**
     * organization data file
     */
    private static final String FILE_UPLOAD = "organizationDataUpload";

    @Override
    public Object run() {
        final BonitaHomeFolderAccessor tenantFolder = new BonitaHomeFolderAccessor();
        try {
            final byte[] organizationContent = getOrganizationContent(tenantFolder);
            getIdentityAPI().importOrganization(new String(organizationContent));
        } catch (final InvalidSessionException e) {
            getHttpResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String message = _("Session expired. Please log in again.");
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, message, e);
            }
            throw new ServiceException(TOKEN, message, e);
        } catch (InvalidOrganizationFileFormatException e) {
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String message = _("Can't import organization. Please check that your file is well-formed.");
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, message, e);
            }
            throw new ServiceException(TOKEN, message, e);
        } catch (final Exception e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            throw new ServiceException(TOKEN, _("Can't import organization"), e);
        }
        return "";
    }

    public byte[] getOrganizationContent(final BonitaHomeFolderAccessor tenantFolder) throws IOException {
        InputStream xmlStream = null;
        try {
            File xmlFile = tenantFolder.getTempFile(getFileUploadParameter(), getTenantId());
            xmlStream = new FileInputStream(xmlFile);
            return IOUtils.toByteArray(xmlStream);
        } finally {
            if (xmlStream != null) {
                try {
                    xmlStream.close();
                } catch (final IOException e) {
                    xmlStream = null;
                }
            }
        }
    }
    
    protected IdentityAPI getIdentityAPI() throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return (IdentityAPI) TenantAPIAccessor.getIdentityAPI(getSession());
    }

    protected long getTenantId() {
        return getSession().getTenantId();
    }

    protected String getFileUploadParameter() {
        return getParameter(FILE_UPLOAD);
    }

}
