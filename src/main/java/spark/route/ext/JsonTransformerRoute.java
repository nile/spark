package spark.route.ext;
import spark.ResponseTransformerRoute;

import com.google.gson.Gson;

 public abstract class JsonTransformerRoute extends ResponseTransformerRoute {

    protected JsonTransformerRoute(String path) {
		super(path,"application/json");
	}

	private Gson gson = new Gson();
	
    @Override
    public String render(Object model) {
	   return gson.toJson(model);
    }
  
 }
 