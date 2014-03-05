/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.web.rest.server.api.organization.password.validator;

import static org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n._;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.AbstractStringValidator;


/**
 * @author Paul AMAR
 * This class is only adding error if the password is not (at least) 10 chars long
 * All the other checks are with default value : 0 
 */
public class RobustnessPasswordValidator extends AbstractStringValidator {

    @Override
    protected void _check(String password) {
        String regex = "";
        
        LOCALE Locale = AbstractI18n.stringToLocale(locale);

        // Check number of digits        
        regex = "[0-9]";
        int numberMinOccurences = 3;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(_("Password does not contain enough numbers", Locale));
        }
        
        // Check number of lower case chars
        regex = "[a-z]";
        numberMinOccurences = 2;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(_("Password does not contain enough lower case chars", Locale));
        }
        
        // Check number of upper case chars
        regex = "[A-Z]";
        numberMinOccurences = 2;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(_("Password does not contain enough upper case chars", Locale));
        }
        
        // Check number of special chars
        regex = "[~@#\\^\\$&\\*\\(\\)-_\\+=\\[\\]\\{\\}\\|\\,\\.\\?]";
        numberMinOccurences = 2;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(_("Password does not contain enough special chars", Locale));
        }
        
        // Check number of length
        int minimalLength = 10;
        if (password.length() < minimalLength) {
            addError(_("Password is not long enough", Locale));
        }
    }
    
    private int numberOfOccurenceOfRegex(String regex, String password) {
        Pattern pattern = Pattern.compile(regex);
        Matcher  matcher = pattern.matcher(password);
        
        int count = 0;
        while (matcher.find())
            count++;

        return count;
    }
    
    
}
