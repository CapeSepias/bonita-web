package org.bonitasoft.console.common.server.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Locale;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class PageContextHelperTest {

    public static final String MY_PROFILE = "myProfile";
    PageContextHelper pageContextHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession httpSession;


    private Locale locale;

    @Mock
    private APISession apiSession;


    @Before
    public void before() throws Exception {
        locale = Locale.FRANCE;
}

    @Test
    public void should_return_CurrentProfile() throws Exception {
        //given
        doReturn(MY_PROFILE).when(request).getParameter(PageContextHelper.PROFILE_PARAM);
        pageContextHelper = new PageContextHelper(request);

        //when
        final String currentProfile = pageContextHelper.getCurrentProfile();

        //then
        assertThat(currentProfile).isEqualTo(MY_PROFILE);

    }

    @Test
    public void should_getCurrentLocale_return_request_parameter() throws Exception {
        //given
        doReturn(locale.toString()).when(request).getParameter(LocaleUtils.LOCALE_PARAM);
        pageContextHelper = new PageContextHelper(request);

        //when
        final Locale returnedLocale = pageContextHelper.getCurrentLocale();

        //then
        assertThat(returnedLocale.getLanguage()).isEqualTo(locale.getLanguage());
        assertThat(returnedLocale.getCountry()).isEmpty();
    }

    @Test
    public void should_getCurrentLocale_return_cookie_locale() throws Exception {
        //given
        Cookie[] cookieList = new Cookie[1];
        cookieList[0] = new Cookie(LocaleUtils.LOCALE_COOKIE_NAME, locale.getLanguage());

        doReturn(null).when(request).getParameter(LocaleUtils.LOCALE_PARAM);
        doReturn(cookieList).when(request).getCookies();
        pageContextHelper = new PageContextHelper(request);

        //when
        final Locale returnedLocale = pageContextHelper.getCurrentLocale();

        //then
        assertThat(returnedLocale.getLanguage()).isEqualTo(locale.getLanguage());
        assertThat(returnedLocale.getCountry()).isEmpty();

    }

    @Test
    public void should_getCurrentLocale_return_default_locale() throws Exception {
        //given
        Cookie[] cookieList = new Cookie[1];
        cookieList[0] = new Cookie("otherCookie", "otherValue");

        doReturn(null).when(request).getParameter(LocaleUtils.LOCALE_PARAM);
        doReturn(cookieList).when(request).getCookies();
        pageContextHelper = new PageContextHelper(request);

        //when
        final Locale returnedLocale = pageContextHelper.getCurrentLocale();

        //then
        assertThat(returnedLocale.toString()).isEqualTo(LocaleUtils.DEFAULT_LOCALE);
    }


    @Test
    public void should_return_ApiSession() throws Exception {
        //given
        doReturn(httpSession).when(request).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(pageContextHelper.ATTRIBUTE_API_SESSION);
        pageContextHelper = new PageContextHelper(request);

        //when
        final APISession returnedApiSession = pageContextHelper.getApiSession();

        //then
        assertThat(returnedApiSession).isEqualTo(apiSession);
    }
}