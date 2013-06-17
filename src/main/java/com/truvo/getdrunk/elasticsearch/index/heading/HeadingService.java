package com.truvo.getdrunk.elasticsearch.index.heading;

import java.net.URISyntaxException;

import com.truvo.getdrunk.elasticsearch.index.heading.domain.Heading;

public interface HeadingService {
	Heading getHeadingByCode(String code);

	Heading getHeadingByNumber(String number);

	void loadHeadingMaps() throws URISyntaxException;
}
