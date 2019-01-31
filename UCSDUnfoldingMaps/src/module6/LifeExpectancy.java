package module6;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import de.fhpotsdam.unfolding.providers.*;
import de.fhpotsdam.unfolding.providers.Google.*;

import java.util.List;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;

import java.util.HashMap;


import de.fhpotsdam.unfolding.marker.Marker;

/**
 * Visualizes life expectancy in different countries. 
 * 
 * It loads the country shapes from a GeoJSON file via a data reader, and loads the population density values from
 * another CSV file (provided by the World Bank). The data value is encoded to transparency via a simplistic linear
 * mapping.
 */
public class LifeExpectancy extends PApplet {

	UnfoldingMap map;
	HashMap<String, Float> lifeExpMap;
	List<Feature> countries;
	List<Marker> countryMarkers;
	Marker lastClicked = null;

	public void setup() {
		size(800, 600, OPENGL);
		map = new UnfoldingMap(this, 50, 50, 700, 500, new Google.GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);

		// Load lifeExpectancy data
		lifeExpMap = ParseFeed.loadLifeExpectancyFromCSV(this,"LifeExpectancyWorldBank.csv");
		

		// Load country polygons and adds them as markers
		countries = GeoJSONReader.loadData(this, "countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		map.addMarkers(countryMarkers);
		System.out.println(countryMarkers.get(0).getId());


		shadeCountries();

	}

	public void draw() {
		// Draw map tiles and country markers
		map.draw();
		addBox();
	}

	//Draws the Box and the Life Expectancy
	private void addBox() {
		fill (255, 250, 250);

		int xbase = 15;
		int ybase = 50;

		rect(xbase, ybase, 115, 60);


		if (lastClicked != null) {
			String countryId = lastClicked.getId();
			if (lifeExpMap.containsKey(countryId)) {
				float lifeExp = lifeExpMap.get(countryId);

				fill(0);
				textAlign(LEFT, CENTER);
				textSize(12);
				text(countryId + ": " + lifeExp, xbase + 5, ybase + 25);
			} else {
				fill(0);
				textAlign(LEFT, CENTER);
				textSize(12);
				text(countryId + ": N/A", xbase + 5, ybase + 25);
			}
		}
	}

	//Helper method to color each country based on life expectancy
	//Red-orange indicates low (near 40)
	//Blue indicates high (near 100)
	private void shadeCountries() {
		for (Marker marker : countryMarkers) {
			// Find data for country of the current marker
			String countryId = marker.getId();
			//System.out.println(lifeExpMap.containsKey(countryId));
			if (lifeExpMap.containsKey(countryId)) {
				float lifeExp = lifeExpMap.get(countryId);
				// Encode value as brightness (values range: 40-90)
				int colorLevel = (int) map(lifeExp, 40, 90, 10, 255);
				marker.setColor(color(255-colorLevel, 100, colorLevel));
			}
			else {
				marker.setColor(color(150,150,150));
			}
		}
	}

	//On mouse click, cycles through all countries (markers).
	//If the mouse click was in one, turn all other countries gray and
	//display the life expectancy of the selected country in the top left box.
	//Else clear the box and shade all the countries as normal.
	@Override
	public void mouseClicked() {
		lastClicked = null;  //Reset the click
		shadeCountries();  //Shade all countries

		//Cycle through countries and see if one was clicked
		for (Marker marker : countryMarkers) {
			if (marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;  //This will let the box in the top left know which country to display
				String countryId = marker.getId();

				//Turn all countries grey
				for (Marker marker2 : countryMarkers) {
					marker2.setColor(color(150,150,150));
				}

				//If the selected country has a life expectancy, shade just it appropriately
				if (lifeExpMap.containsKey(countryId)) {
					float lifeExp = lifeExpMap.get(countryId);
					// Encode value as brightness (values range: 40-90)
					int colorLevel = (int) map(lifeExp, 40, 90, 10, 255);
					marker.setColor(color(255-colorLevel, 100, colorLevel));
				}
			}
		}
	}

}
