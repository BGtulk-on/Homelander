package com.uktc.schoolInventory.config;

import com.uktc.schoolInventory.models.ActiveUserStore;
import com.uktc.schoolInventory.models.LoggedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ActiveUserStore activeUserStore;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication auth) throws IOException {
        LoggedUser user = new LoggedUser(auth.getName(), activeUserStore);
        request.getSession().setAttribute("user", user);

        // Това казва на сървъра просто да върне 200 OK вместо да пренасочва към страница
        response.setStatus(HttpServletResponse.SC_OK);
    }
}