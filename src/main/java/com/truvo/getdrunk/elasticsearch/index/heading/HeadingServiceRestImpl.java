package com.truvo.getdrunk.elasticsearch.index.heading;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.client.RestTemplate;

import com.truvo.getdrunk.elasticsearch.index.heading.domain.Heading;
import com.truvo.schema.csm.heading._0.HeadingInternalRefWithBasicDetailsType;
import com.truvo.schema.csm.heading._0.HeadingTranslationType;
import com.truvo.schema.csm.heading._0.Headings;

public class HeadingServiceRestImpl implements HeadingService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(HeadingServiceRestImpl.class);

	private static final String HEADING_RESOURCE_URI = "/headings";

	private RestTemplate restTemplate;
	private String csmServiceRoot; // http://csm.truvo.net.tst.lab.truvo.net/api/v1/BE

	private Map<String, Heading> headingsByCode = new HashMap<String, Heading>();
	private Map<String, Heading> headingsByNumber = new HashMap<String, Heading>();

	@Override
	public Heading getHeadingByCode(String code) {

		if (StringUtils.isBlank(code)) {
			return null;
		}

		Heading cached = headingsByCode.get(code);
		if (cached != null) {
			return cached;
		}

		try {
			logger.info("code {} not found in heading map. call CSM", code);
			URI uri = buildURI("headingCode", code);
			Headings headingsResponse = restTemplate.getForObject(uri, Headings.class);
			final List<HeadingInternalRefWithBasicDetailsType> headings = headingsResponse.getHeading();
			if (headings.size() != 1) {
				logger.error("Heading service returned " + headings.size() + " results for headingcode {" + code + "} expected 1.");
				return null;
			}

			final Heading headingDto = toDto(headings.get(0));
			headingsByCode.put(headingDto.getCode(), headingDto);
			headingsByNumber.put(headingDto.getNumber(), headingDto);

			return headingDto;

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Heading getHeadingByNumber(String number) {
		if (StringUtils.isBlank(number)) {
			return null;
		}

		Heading cached = headingsByNumber.get(number);
		if (cached != null) {
			return cached;
		}

		try {
			logger.info("number {} not found in heading map. call CSM", number);
			URI uri = buildURI("headingNumber", number);
			Headings headingsResponse = restTemplate.getForObject(uri, Headings.class);
			final List<HeadingInternalRefWithBasicDetailsType> headings = headingsResponse.getHeading();
			if (headings.size() != 1) {
				logger.error("Heading service returned " + headings.size() + " results for headingnumber {" + number + "} expected 1.");
				return null;
			}

			final Heading headingDto = toDto(headings.get(0));
			headingsByCode.put(headingDto.getCode(), headingDto);
			headingsByNumber.put(headingDto.getNumber(), headingDto);

			return headingDto;

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	private Heading toDto(HeadingInternalRefWithBasicDetailsType headingInternal) {
		Heading result = new Heading();
		result.setCode(headingInternal.getCode());
		result.setNumber(headingInternal.getExternalRef().getHeadingNumber());

		for (HeadingTranslationType headingTranslationType : headingInternal.getTranslation()) {
			result.addOfficialName(headingTranslationType.getLanguageIso().value(), headingTranslationType.getOfficialName());
		}

		return result;
	}

	private URI buildURI(String paramName, String paramValue) throws URISyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append(csmServiceRoot).append(HEADING_RESOURCE_URI).append("?").append(paramName).append("=").append(paramValue);
		return new URI(sb.toString());
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getCsmServiceRoot() {
		return csmServiceRoot;
	}

	public void setCsmServiceRoot(String csmServiceRoot) {
		this.csmServiceRoot = csmServiceRoot;
	}

	@Override
	public void afterPropertiesSet() {
		try {
			loadHeadingMaps();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadHeadingMaps() throws URISyntaxException {
		logger.debug("start loading headings in memory");
		headingsByCode.clear();
		headingsByNumber.clear();

		URI uri = new URI(csmServiceRoot + HEADING_RESOURCE_URI);
		Headings headingsResponse = restTemplate.getForObject(uri, Headings.class);
		for (HeadingInternalRefWithBasicDetailsType headingRef : headingsResponse.getHeading()) {
			final Heading dto = toDto(headingRef);
			headingsByCode.put(dto.getCode(), dto);
			headingsByNumber.put(dto.getNumber(), dto);
		}
		logger.debug("finished loading headings in memory");
	}
}
