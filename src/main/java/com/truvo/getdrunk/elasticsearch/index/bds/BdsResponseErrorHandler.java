package com.truvo.getdrunk.elasticsearch.index.bds;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import com.truvo.schema.basedata.ResponseStatusDetailType;
import com.truvo.schema.basedata.StandardErrorResponse;

public class BdsResponseErrorHandler implements ResponseErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(BdsResponseErrorHandler.class);

	private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();
	private Jaxb2Marshaller errorMessageUnmarshaller;

	public BdsResponseErrorHandler() {
		errorMessageUnmarshaller = new Jaxb2Marshaller();
		errorMessageUnmarshaller.setClassesToBeBound(StandardErrorResponse.class);
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return errorHandler.hasError(response);
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		try {
			StandardErrorResponse standardError = (StandardErrorResponse) errorMessageUnmarshaller.unmarshal(new StreamSource(response.getBody()));
			for (ResponseStatusDetailType err : standardError.getResponseStatus().getStatusDetail()) {
				logger.error("error received from bds: " + err.getStatusDetailCode() + " : " + err.getStatusDetailText());
			}

			try {
				errorHandler.handleError(response);
			} catch (RestClientException e) {
				// handled by this
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
