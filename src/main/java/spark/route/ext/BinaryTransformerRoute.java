package spark.route.ext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import javax.servlet.ServletOutputStream;

import org.apache.commons.codec.net.URLCodec;

import spark.Request;
import spark.Response;
import spark.ResponseTransformerRoute;

 public abstract class BinaryTransformerRoute extends ResponseTransformerRoute {

    protected BinaryTransformerRoute(String path) {
		super(path,"application/json");
	}
    @Override
    public String render(Object model) {
	   return null;
    }
   
    public static Object handleBinary(Request request, Response response, String name, String contentType, InputStream is) {
    	try {
    	response.raw().setContentType(contentType);
    	
    	String dispositionType;
		boolean inline = false;
		if(inline ) {
            dispositionType = INLINE_DISPOSITION_TYPE;
        } else {
            dispositionType = ATTACHMENT_DISPOSITION_TYPE;
        }
		if(canAsciiEncode(name)) {
            String contentDisposition = "%s; filename=\"%s\"";
            response.raw().setHeader("Content-Disposition", String.format(contentDisposition, dispositionType, name));
        } else {
            final String encoding = "utf-8";
            String contentDisposition = "%1$s; filename*="+encoding+"''%2$s; filename=\"%2$s\"";
            response.raw().setHeader("Content-Disposition", String.format(contentDisposition, dispositionType, encoder.encode(name, encoding)));
        }
		ServletOutputStream out = response.raw().getOutputStream();
		byte[] buffer = new byte[8092];
        int count = 0;
        while ((count = is.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        is.close();
        out.flush();
		
		return null;
		} catch (IOException e) {
			return e;
		}
    }
    private static final String INLINE_DISPOSITION_TYPE = "inline";
	 private static final String ATTACHMENT_DISPOSITION_TYPE = "attachment";

	private static boolean canAsciiEncode(String string) {
      CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
      return asciiEncoder.canEncode(string);
	}
	private static URLCodec encoder = new URLCodec();
 }
 