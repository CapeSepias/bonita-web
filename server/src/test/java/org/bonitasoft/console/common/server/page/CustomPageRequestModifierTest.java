package org.bonitasoft.console.common.server.page;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomPageRequestModifierTest {
    
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;
    
    @Mock
    private RequestDispatcher requestDispatcher;
    
    @Test
    public void redirect_with_trailing_slash_should_not_encode_parameter() throws Exception {
        when(request.getContextPath()).thenReturn("bonita/");
        when(request.getServletPath()).thenReturn("apps/");
        when(request.getPathInfo()).thenReturn("myapp/mypage");
        when(request.getQueryString()).thenReturn("time=12:00");
        when(response.encodeRedirectURL("bonita/apps/myapp/mypage/?time=12:00")).thenReturn("bonita/apps/myapp/mypage/?time=12:00");

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.redirectToValidPageUrl(request, response);

        verify(response).sendRedirect("bonita/apps/myapp/mypage/?time=12:00");
    }

    @Test
    public void redirect_with_trailing_slash_should_not_add_question_mark() throws Exception {
        when(request.getContextPath()).thenReturn("bonita/");
        when(request.getServletPath()).thenReturn("apps/");
        when(request.getPathInfo()).thenReturn("myapp/mypage");
        when(response.encodeRedirectURL("bonita/apps/myapp/mypage/")).thenReturn("bonita/apps/myapp/mypage/");

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.redirectToValidPageUrl(request, response);

        verify(response).sendRedirect("bonita/apps/myapp/mypage/");
    }
    
    @Test
    public void check_should_not_authorize_requests_to_other_paths() throws Exception {
        String apiPath = "/API/living/../../WEB-INF/web.xml";

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.forwardIfRequestIsAuthorized(request, response, "/API", apiPath);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "attempt to access unauthorized path " + apiPath);
        verify(request, never()).getRequestDispatcher(anyString());
    }
    
    @Test
    public void check_should_authorize_valid_requests() throws Exception {
        String apiPath = "/API/living/0";
        when(request.getRequestDispatcher(apiPath)).thenReturn(requestDispatcher);

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.forwardIfRequestIsAuthorized(request, response, "/API", apiPath);

        verify(request).getRequestDispatcher(apiPath);
        verify(response, never()).sendError(anyInt(), anyString());
    }

}