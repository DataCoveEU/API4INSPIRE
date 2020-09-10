---
title: NUTS Regions
layout: default
category: maps
order: 2
---

# NUTS Regions

Data from [Eurostat](https://ec.europa.eu/eurostat/de/web/gisco/geodata/reference-data/administrative-units-statistical-units/nuts).


<div id="mapid" style="height: 850px;"></div>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.6.0/dist/leaflet.css" integrity="sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ==" crossorigin=""/>
<script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
<script src="https://unpkg.com/leaflet@1.6.0/dist/leaflet.js" integrity="sha512-gZwIG9x3wUXg2hdXF6+rVkLF/0Vi9U8D2Ntg4Ga5I5BZpVkVxlJWbSQtXPSiUTtC0TjtGOmxa1AJPuV0CPthew==" crossorigin=""></script>
<script src='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/Leaflet.fullscreen.min.js'></script>
<link href='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/leaflet.fullscreen.css' rel='stylesheet' />
<script  src="https://unpkg.com/sta-map@1.2.0/dist/stam.min.js"></script>
<script type="text/javascript">
	var mymap = L.map('mapid').setView([51.505, 8.0], 4);
	mymap.addControl(new L.Control.Fullscreen());
	L.tileLayer('https://{s}.tile.iosb.fraunhofer.de/tiles/osmde/{z}/{x}/{y}.png', {
		attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
		maxZoom: 18
	}).addTo(mymap);
	L.stam({
		baseUrl: "https://lubw-frost.docker01.ilt-dmz.iosb.fraunhofer.de/v1.1",
		MarkerStyle: "yellow",
        clusterMin: 50,
        queryObject: [
            {
                zoomLevel: {
                    from: 0,
                    to: 5
                },
                query: {
                    count: false,
                    skip: 0,
                    entityType: 'Things',
                    filter: "properties/type eq 'NUTS' and properties/level eq 0",
                    select: ["id","name","description","properties"],
                    expand: [
                        {
                            count: false,
                            skip: 0,
                            entityType: 'Locations',
                            filter: "properties/scale eq 60",
                            select: ["id","name","description","properties","encodingType","location"],
                            expand: null,
                            top: 1
                        }
                    ]
                }
            },
            {
                zoomLevel: {
                    from: 6,
                    to: 6
                },
                query: {
                    count: false,
                    skip: 0,
                    entityType: 'Things',
                    filter: "properties/type eq 'NUTS' and properties/level eq 1",
                    select: ["id","name","description","properties"],
                    expand: [
                        {
                            count: false,
                            skip: 0,
                            entityType: 'Locations',
                            filter: "properties/scale eq 20",
                            select: ["id","name","description","properties","encodingType","location"],
                            expand: null,
                            top: 1
                        }
                    ]
                }
            },
            {
                zoomLevel: {
                    from: 7,
                    to: 8
                },
                query: {
                    count: false,
                    skip: 0,
                    entityType: 'Things',
                    filter: "properties/type eq 'NUTS' and properties/level eq 2",
                    select: ["id","name","description","properties"],
                    expand: [
                        {
                            count: false,
                            skip: 0,
                            entityType: 'Locations',
                            filter: "properties/scale eq 10",
                            select: ["id","name","description","properties","encodingType","location"],
                            expand: null,
                            top: 1
                        }
                    ]
                }
            },
            {
                zoomLevel: {
                    from: 9,
                    to: 10
                },
                query: {
                    count: false,
                    skip: 0,
                    entityType: 'Things',
                    filter: "properties/type eq 'NUTS' and properties/level eq 3",
                    select: ["id","name","description","properties"],
                    expand: [
                        {
                            count: false,
                            skip: 0,
                            entityType: 'Locations',
                            filter: "properties/scale eq 10",
                            select: ["id","name","description","properties","encodingType","location"],
                            expand: null,
                            top: 1
                        }
                    ]
                }
            },
            {
                zoomLevel: {
                    from: 11,
                    to: 13
                },
                query: {
                    count: false,
                    skip: 0,
                    entityType: 'Things',
                    filter: "properties/type eq 'NUTS' and properties/level eq 3",
                    select: ["id","name","description","properties"],
                    expand: [
                        {
                            count: false,
                            skip: 0,
                            entityType: 'Locations',
                            filter: "properties/scale eq 3",
                            select: ["id","name","description","properties","encodingType","location"],
                            expand: null,
                            top: 1
                        }
                    ]
                }
            },
            {
                zoomLevel: {
                    from: 14
                },
                query: {
                    count: false,
                    skip: 0,
                    entityType: 'Things',
                    filter: "properties/type eq 'NUTS' and properties/level eq 3",
                    select: ["id","name","description","properties"],
                    expand: [
                        {
                            count: false,
                            skip: 0,
                            entityType: 'Locations',
                            filter: "properties/scale eq 3",
                            select: ["id","name","description","properties","encodingType","location"],
                            expand: null,
                            top: 1
                        }
                    ]
                }
            }
        ]
	}).addTo(mymap);
</script>


