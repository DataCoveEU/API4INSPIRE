---
layout: default
title: CSV &amp; GeoJSON
category: gettingData
topCategory: STA
order: 6
---

# ResultFormats

The OGC SensorThings API specifies one additional parameter: `$resultFormat`.
This parameter can be used to request the server to return a different format than the standard JSON format.

## DataArray

The DataArray result format can format Observations in a more efficient way.
It only works on collections of Observations.


## GeoJSON

The GeoJSON result format changes the response of the server to be proper GeoJSON.
The details can be found at the [FROST-Server Documentation Site](https://fraunhoferiosb.github.io/FROST-Server/extensions/GeoJSON-ResultFormat.html).
It is a non-standard result format implemented by FROST-Server.

## CSV

The CSV result format changes the response of the server into [rfc4180](https://tools.ietf.org/html/rfc4180) conform CSV.
The details can be found at the [FROST-Server Documentation Site](https://fraunhoferiosb.github.io/FROST-Server/extensions/CSV-ResultFormat.html).
It is a non-standard result format implemented by FROST-Server.

