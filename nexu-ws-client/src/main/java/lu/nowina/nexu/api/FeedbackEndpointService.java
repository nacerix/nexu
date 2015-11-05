
package lu.nowina.nexu.api;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.2.9-b14002 Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "FeedbackEndpointService", targetNamespace = "http://api.nexu.nowina.lu/", wsdlLocation = "file:/C:/Users/david.naramski/git/nexu/nexu-ws-client/src/wsdl/feedback.wsdl")
public class FeedbackEndpointService extends Service {

	private final static URL FEEDBACKENDPOINTSERVICE_WSDL_LOCATION;
	private final static WebServiceException FEEDBACKENDPOINTSERVICE_EXCEPTION;
	private final static QName FEEDBACKENDPOINTSERVICE_QNAME = new QName("http://api.nexu.nowina.lu/", "FeedbackEndpointService");

	static {
		URL url = null;
		WebServiceException e = null;
		try {
			url = new URL("file:/C:/Users/david.naramski/git/nexu/nexu-ws-client/src/wsdl/feedback.wsdl");
		} catch (MalformedURLException ex) {
			e = new WebServiceException(ex);
		}
		FEEDBACKENDPOINTSERVICE_WSDL_LOCATION = url;
		FEEDBACKENDPOINTSERVICE_EXCEPTION = e;
	}

	public FeedbackEndpointService() {
		super(__getWsdlLocation(), FEEDBACKENDPOINTSERVICE_QNAME);
	}

	public FeedbackEndpointService(WebServiceFeature... features) {
		super(__getWsdlLocation(), FEEDBACKENDPOINTSERVICE_QNAME, features);
	}

	public FeedbackEndpointService(URL wsdlLocation) {
		super(wsdlLocation, FEEDBACKENDPOINTSERVICE_QNAME);
	}

	public FeedbackEndpointService(URL wsdlLocation, WebServiceFeature... features) {
		super(wsdlLocation, FEEDBACKENDPOINTSERVICE_QNAME, features);
	}

	public FeedbackEndpointService(URL wsdlLocation, QName serviceName) {
		super(wsdlLocation, serviceName);
	}

	public FeedbackEndpointService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
		super(wsdlLocation, serviceName, features);
	}

	/**
	 * 
	 * @return returns FeedbackEndpoint
	 */
	@WebEndpoint(name = "FeedbackEndpointPort")
	public FeedbackEndpoint getFeedbackEndpointPort() {
		return super.getPort(new QName("http://api.nexu.nowina.lu/", "FeedbackEndpointPort"), FeedbackEndpoint.class);
	}

	/**
	 * 
	 * @param features
	 *            A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy. Supported features not in the <code>features</code> parameter will
	 *            have their default values.
	 * @return returns FeedbackEndpoint
	 */
	@WebEndpoint(name = "FeedbackEndpointPort")
	public FeedbackEndpoint getFeedbackEndpointPort(WebServiceFeature... features) {
		return super.getPort(new QName("http://api.nexu.nowina.lu/", "FeedbackEndpointPort"), FeedbackEndpoint.class, features);
	}

	private static URL __getWsdlLocation() {
		if (FEEDBACKENDPOINTSERVICE_EXCEPTION != null) {
			throw FEEDBACKENDPOINTSERVICE_EXCEPTION;
		}
		return FEEDBACKENDPOINTSERVICE_WSDL_LOCATION;
	}

}
