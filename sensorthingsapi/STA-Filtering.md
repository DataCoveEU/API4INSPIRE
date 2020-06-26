---
layout: default
title: Filtering Entities
category: STA
order: 4
---

# Filtering

In many use cases, the response should only contain objects that pertain to some specific criteria.
The “filter” request parameter makes it possible to filter the data to be returned based on the values of specific attributes of the requested data.
Thus, one could request all values above a certain threshold or measured between two specific points in time.
The following request returns all Observations where the result value is greater than 5:

```
http://.../Observations?$filter=result gt 5
```

This request provides the following response:

```
{
  "@iot.count" : 8,
  "@iot.nextLink" : "/v1.0/Observations?$filter=result gt 5&$top=4&$skip=4",
  "value" : [
    {
      "phenomenonTime" : "2016-06-22T13:21:31.144Z",
      "resultTime" : null,
      "result" : 10,
      "@iot.id" : 34,
      "@iot.selfLink" : "/FROST-Server/v1.0/Observations(34)"
    }, {
      …
    }, {
      …
    }, {
      …
    }
  ]
}
```


## Operators

The following shows operators that can be used when composing complex filter requests.


### Comparison Operators

Operator | Description | Example
--- | --- | ---
eq | Equal | /ObservedProperties?$filter=name eq 'CO2'
ne | Not equal | /ObservedProperties?$filter=name ne 'CO2'
gt | Greater than | /Observations?$filter=result gt 5
ge | Greater than or equal | /Observations?$filter=result ge 5
lt | Less than | /Observations?$filter=result lt 5
le | Less than or equal | /Observations?$filter=result le 5


### Logical Operators

Operator | Description | Example
--- | --- | ---
and | Logical and | /Observations?$filter=result le 5 and FeatureOfInterest/id eq '1'
or | Logical or | /Observations?$filter=result gt 20 or result le 3.5
not | Logical negation | /Things?$filter=not startswith(description,'test')


### Grouping Operators

Operator | Description | Example
--- | --- | ---
( ) | Precedence grouping | /Observations?$filter=(result sub 5) gt 10


## Functions

To make it easier to get the correct order of parameters in functions, remember that in general, functions can be be read as:

    parameter1 functionname parameter2

For example:

    name startswith 'room'.


### String Functions

String matches are case sensitive.

Function | Description | Example
--- | --- | ---
bool `substringof(s1, s2)` | Returns true if s1 is a substring of s2 | `Things?$filter=substringof('room', name)` matches `livingroom` and `room S01` but not `Room S01`
bool `endswith(s1, s2)` | Returns true if s1 ends with s2 | Things?$filter=endswith(name, 'room') matches `livingroom` but not `room S01` or `Room S01`
bool `startswith(s1, s2)` | Returns true if s1 starts with s2 | Things?$filter=endswith(name, 'room') matches `room S01` but not `livingroom` or `Room S01`
string `substring(s1, i1)` | Returns the substring of s1, starting at position i1 | substring(description,1) eq 'ensor Things'
string `substring(s1, i1, i2)` | Returns the substring of s1, starting at position i1, with length i2 | substring(description,2,4) eq 'nsor'
int `length(s1)` | Returns the length of string s1 | length(description) eq 13
int `indexof(s1, s2)` | Returns the index of s2 in s1 | indexof(description,'Sensor') eq 1
string `tolower(s1)` | Returns the lower case version of s1 | tolower(description) eq 'sensor things'
string `toupper(s1)` | Returns the upper case version of s1 | toupper(description) eq 'SENSOR THINGS'
string `trim(s1)` | Returns the string s1, with whitespace trimmed from start and end | trim(description) eq 'Sensor Things'
string `concat(s1, s2)` | Returns a string composed of s2 added to the end of s1 | concat(concat(unitOfMeasurement/symbol,', '), unitOfMeasurement/name) eq 'degree, Celsius'


### Mathematical Functions

Mathematical functions work on all fields that are numeric, and on numerical constants.

Function | Description | Example
--- | --- | ---
int `round(n1)` | Returns n1 rounded to the nearest integer | `round(result) eq 42` matches 41.50 to 42.49
int `floor(n1)` | Returns n1, rounded down to the nearest integer less than n1 | `floor(result) eq 42` matches 42.00 to 42.99
int `ceiling(n1)` | Returns n1, rounded up to the nearest integer larger than n1 | `ceiling(result) eq 42` matches 41.01 to 42.00


### Geospatial Functions

Geospatial functions work on all geospatial fields (Location/location and FeatureOfInterest/feature) and on geospatial constants.
Geospatial constants can be specified by using WKT enclosed in `geography'...'`, for example:

```
    geography'POINT (30 10)'
    geography'LINESTRING (30 10, 10 30, 40 40)'
    geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))'
```

Function | Description | Example
--- | --- | ---
bool `geo.intersects(g1, g2)` | Returns true if g1 intersects g2 | `geo.intersects(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
number `geo.length(g1)` | Returns the length of geometry g1  | `geo.length(location) lt 2` matches all locations that are linestrings with a length less than 2 degrees
number `geo.distance(g1, g2)` | Returns the distance between g1 and g2 in the units of the server (generally degrees) | `geo.distance(location, geography'POINT (30 10)') lt 1`
bool `st_equals(g1, g2)` | Returns true if g1 is the same as g2 | `st_equals(location, geography'POINT (30 10)')`
bool `st_disjoint(g1, g2)` | Returns true if g1 is separated from g2 | `st_disjoint(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
bool `st_touches(g1, g2)` | Returns true if g1 touches g2 | `st_touches(location, geography'LINESTRING (30 10, 10 30, 40 40)')`
bool `st_within(g1, g2)` | Returns true if g1 is within g2 | `st_within(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
bool `st_overlaps(g1, g2)` | Returns true if g1 overlaps g2 | `st_overlaps(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
bool `st_crosses(g1, g2)` | Returns true if g1 crosses g2 | `st_crosses(location, geography'LINESTRING (30 10, 10 30, 40 40)')`
bool `st_intersects(g1, g2)` | Returns true if g1 intersects g2 | `st_intersects(location, geography'LINESTRING (30 10, 10 30, 40 40)')`
bool `st_contains(g1, g2)` | Returns true if g1 contains g2 | `st_contains(location, geography'POINT (30 10)')`
bool `st_relate(g1, g2, s1)` | Returns true if g1 has a relation with g2 given the [intersecion matrix pattern](https://en.wikipedia.org/wiki/DE-9IM) s1 | `st_relate(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))', 'T********')`


### Temporal Functions

Temporal functions other than `now()` operate on the time as stored in the server.
For FROST-Server this is always in the timezone UTC, but for other servers this may also be the timezone of the original value as it was stored.

Function | Description | Example
--- | --- | ---
datetime `now()` | Returns the current time, in the timezone of the server | `phenomenonTime lt now()`
datetime `mindatetime()` | Returns the minimum time that can be stored in the server | 
datetime `maxdatetime()` | Returns the maximum time that can be stored in the server | 
date `date(t1)` | Returns the date part of time t1 | `date(resultTime) ne date(validTime)`
time `time(t1)` | Returns the time part of time t1 | `time(phenomenonTime) le time(1990-01-01T12:00:00Z)` returns all observations taken between midnight and noon
int `year(t1)` | Returns the year part of time t1 | `year(phenomenonTime) eq 2015`
int `month(t1)` | Returns the month part of time t1 | `month(phenomenonTime) eq 12`
int `day(t1)` | Returns the day part of time t1 | `day(phenomenonTime) eq 31`
int `hour(t1)` | Returns the hour part of time t1 | `hour(phenomenonTime) eq 23`
int `minute(t1)` | Returns the minute part of time t1 | `minute(phenomenonTime) eq 59`
int `second(t1)` | Returns the second part of time t1 | `second(phenomenonTime) eq 59`
double `fractionalseconds(t1)` | Returns the millisecond part of time t1 | `fractionalseconds(phenomenonTime) eq 0`
int `totaloffsetminutes(t1)` | Returns the offset part of time t1 | `totaloffsetminutes(phenomenonTime) eq 60`


