package com.truvo.getdrunk.web;

public class Coordinate {

	private double lat;
	private double lon;

	public Coordinate() {}
	
	public Coordinate(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	@Override
	public String toString() {
		return "Coordinate [lat=" + lat + ", lon=" + lon + "]";
	}
}
