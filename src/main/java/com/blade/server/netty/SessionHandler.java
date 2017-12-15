package com.blade.server.netty;

import com.blade.Blade;
import com.blade.event.EventManager;
import com.blade.event.EventType;
import com.blade.kit.ReflectKit;
import com.blade.kit.UUID;
import com.blade.mvc.SessionManager;
import com.blade.mvc.WebContext;
import com.blade.mvc.http.Cookie;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.Session;

import java.time.Instant;
import java.util.Optional;

import static com.blade.mvc.Const.ENV_KEY_SESSION_KEY;
import static com.blade.mvc.Const.ENV_KEY_SESSION_TIMEOUT;

/**
 * session handler
 *
 * @author biezhi
 * 2017/6/3
 */
public class SessionHandler {

    private final Blade          blade;
    private final SessionManager sessionManager;
    private final EventManager   eventManager;
    private final String         sessionKey;
    private final int            timeout;

    SessionHandler(Blade blade) {
        this.blade = blade;
        this.sessionManager = blade.sessionManager();
        this.eventManager = blade.eventManager();
        this.sessionKey = blade.environment().get(ENV_KEY_SESSION_KEY, HttpConst.DEFAULT_SESSION_KEY);
        this.timeout = blade.environment().getInt(ENV_KEY_SESSION_TIMEOUT, 1800);
    }

    public Session createSession(Request request) {
        Session  session  = getSession(request);
        Response response = WebContext.response();
        if (null == session) {
            return createSession(request, response);
        } else {
            if (session.expired() < Instant.now().getEpochSecond()) {
                removeSession(session);
            }
        }
        return session;
    }

    private Session createSession(Request request, Response response) {

        long now     = Instant.now().getEpochSecond();
        long expired = now + timeout;

        String sessionId = UUID.UU32();
        Cookie cookie    = new Cookie();
        cookie.name(sessionKey);
        cookie.value(sessionId);
        cookie.httpOnly(true);

        Session session = ReflectKit.newInstance(blade.sessionType());
        session.id(sessionId);
        session.created(now);
        session.expired(expired);
        sessionManager.addSession(session);

        request.cookie(cookie);
        response.cookie(cookie);

        eventManager.fireEvent(EventType.SESSION_CREATED, blade);

        return session;
    }

    private void removeSession(Session session) {
        session.attributes().clear();
        sessionManager.remove(session);
        eventManager.fireEvent(EventType.SESSION_DESTROY, blade);
    }

    private Session getSession(Request request) {
        Optional<String> cookieHeader = request.cookie(sessionKey);
        if (!cookieHeader.isPresent()) {
            return null;
        }
        return sessionManager.getSession(cookieHeader.get());
    }

}
