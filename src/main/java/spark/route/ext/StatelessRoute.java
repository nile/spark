package spark.route.ext;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpSession;

import net.sourceforge.statelessfilter.backend.aescookie.AESCookieBackend;
import net.sourceforge.statelessfilter.filter.Configuration;
import net.sourceforge.statelessfilter.wrappers.StatelessRequestWrapper;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;
import spark.Spark;

public abstract class StatelessRoute extends Route{
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StatelessRoute.class);

	private static Configuration backends;
	static {
		if(Spark.isStateless()) {
			backends = new Configuration();
			AESCookieBackend backend = new AESCookieBackend();
			backends.backends.put("default", backend);
			backends.defaultBackend = "default";
			try {
				HashMap<String, String> config = new HashMap<String,String>();
				config.put("key", Spark.getSecretkey());
				config.put("iv", Spark.getSecretiv());
				backend.init(config);
			} catch (Exception e) {
				LOG.error("",e);
			}
		}
	}
	protected StatelessRoute(String path) {
		super(path);
	}
	public StatelessRoute(String path, String acceptType) {
		super(path, acceptType);
	}
	@Override
	public final Object handle(Request request, Response response) {
		copySessionFromRequest(request);
		Object statelessHandler = statelessHandler(request,response);
		copySessionToResponse(request, response);
		return statelessHandler;
	}
	private final void copySessionFromRequest(Request request) {
		StatelessRequestWrapper statelessRequestWrapper = new StatelessRequestWrapper(request.raw(), backends);
		HttpSession session = statelessRequestWrapper.getSession();
		Enumeration<String> attributeNames = session.getAttributeNames();
		while(attributeNames.hasMoreElements()) {
			String name = attributeNames.nextElement();
			request.attribute(name, session.getAttribute(name));
		}
	}
	private final void copySessionToResponse(Request request, Response response) {
		StatelessRequestWrapper statelessRequestWrapper = new StatelessRequestWrapper(request.raw(), backends);
		Session session = request.session();
		try {
			Set<String> attributes = session.attributes();
			for (String string : attributes) {
				statelessRequestWrapper.getSession().setAttribute(string, session.attribute(string));
			}
		}catch (IllegalStateException e) {
			statelessRequestWrapper.getSession().invalidate();
		}
		try {
			statelessRequestWrapper.writeSession(request.raw(), response.raw());
		} catch (IOException e) {
			LOG.error( "",e);
		} 
	}
	public final void redirect(Request request, Response response, String path) {
		copySessionToResponse(request, response);
		response.redirect(path);
	}
	public abstract Object statelessHandler(Request request, Response response);

}
