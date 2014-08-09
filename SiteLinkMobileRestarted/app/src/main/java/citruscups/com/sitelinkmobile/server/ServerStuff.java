package citruscups.com.sitelinkmobile.server;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import citruscups.com.sitelinkmobile.ResponseParserHandler;
import citruscups.com.sitelinkmobile.DataStructures.DataSet;


/**
 * Created by Jakkyl on 8/9/2014.
 */
public class ServerStuff
{
    private static final String NAMESPACE = "http://tempuri.org/CallCenterWs/CallCenterWs";
    private static final String URL = "https://api.smdservers.net/CCWs_3.5/CallCenterWs.asmx?WSDL";

    public static DataSet callSoapMethod(String methodName, Hashtable<String, Object> params)
    {
        /**
         * Attempt authentication against SiteLink API
         */
        String soapAction = NAMESPACE + "/" + methodName;

        HttpParams params1 = new BasicHttpParams();

        try
        {
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            int timeoutConnection = 15000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 35000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setHeader("soapaction", soapAction);
            httpPost.setHeader("Content-Type", "text/xml; charset=utf-8");

            Log.i("callSoapMethod", "executing request" + httpPost.getRequestLine());

            final StringBuffer soap = new StringBuffer();
            soap.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            soap.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
            soap.append("<soap:Body>\n");
            soap.append("<" + methodName + " xmlns=\"" + NAMESPACE + "\">\n");

            for (Map.Entry<String, Object> entry : params.entrySet())
            {
                soap.append("<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">\n");
            }

            soap.append("</" + methodName + ">\n");
            soap.append("</soap:Body>\n");
            soap.append("</soap:Envelope>\n");
            Log.i("SOAP: ", soap.toString());
            HttpEntity entity = new StringEntity(soap.toString(), HTTP.UTF_8);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost, localContext);
            HttpEntity r_entity = response.getEntity();  //get response
            Log.i("Reponse Header", "Begin...");          // response headers
            Log.i("Reponse Header", "StatusLine:" + response.getStatusLine());
            Header[] headers = response.getAllHeaders();
            for (Header h : headers)
            {
                Log.i("Reponse Header", h.getName() + ": " + h.getValue());
            }
            Log.i("Reponse Header", "END...");
            byte[] result = null;
            if (r_entity != null)
            {
                result = new byte[(int) r_entity.getContentLength()];
                if (r_entity.isStreaming())
                {
                    DataInputStream is = new DataInputStream(
                            r_entity.getContent());
                    is.readFully(result);
                }
            }
            String str = new String(result, "UTF-8");
            Log.i("Result: ", str);

            ByteArrayInputStream bais = new ByteArrayInputStream(result);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            ResponseParserHandler myXMLHandler = new ResponseParserHandler();
            xr.setContentHandler(myXMLHandler);
            xr.parse(new InputSource(bais));

            DataSet ds = myXMLHandler.getDataSet();
            Log.i("DS", ds.toString());
            return ds;
            //Toast.makeText(getApplicationContext(), objectResult.toString(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
