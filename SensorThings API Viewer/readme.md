# SensorThings API Viewer

While APIs are cool, and provide access to a wealth of data, a picture is still worth more than 1000 words. 
Thus, while having access to a SensorThings API endpoint is cool, being able to visualize it is even cooler!

Here you find a very primitive but effective SensorThings API Viewer, courtesy of Tomas Kliment @klimeto at [Klimeto](https://klimeto.com/) as well as the DanubeHack.

Basic viewer specification was as follows (Keep in mind, this was a last minute save at a hackathon! 4 hours til the end, and nothing to show):
* Dots on Map
* Klick Dots
* Get Numbers & Graphs 
* GO!!!

In the 4 hours required to put some meteorological data online, Tomas did the viewer (it's been polished a bit since, but not much), not bad for a Sunday morning. The only change we did for the current Ad-hoc Austrian Air-Quality API Viewer is to modify the SensorThings API endpoint: **[Ad-hoc Austrian Air-Quality API Viewer](http://service.datacove.eu/AT_AIR/)**

Now for the geeky details. The following files comprise the viewer:

* index.html: main file, sets up the HTML framework, includes the js files
* map.js: this is the main programatic part:
  * displays the map
  * accesses the SensorThings API for data on measurement station
  * puts the dots (measurement stations) on the map
  * displays parameters observed at a station clicked
* table.js: displays the individual measurement values in table form
* plot.js: displays the individual measurement values as a plot utilizing plotly


