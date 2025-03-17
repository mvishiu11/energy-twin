# Maps API Choice

## Status
**Date**: *2025-16-01*  
**Status**: Approved

## Context
To create a visualization of our digital twin, we need an external maps API. Currently, the two most popular providers are Mapbox and Google Maps API. Since our visualization will heavily rely on the map and may require extensive customization, we decided to go with Mapbox API instead of Google Maps. It's important to note that we could also choose OpenStreetMap, but it has similar limitations to Google Maps API (although it offers a bit more customization).

### Google Maps API
Google Maps API offers maps with raster tiles. This causes the map to be less responsive when scaling, and with our frontend app having to process a lot of data, we are concerned it could hinder the user experience. Despite this, the data in the API is generally more precise; however, this is not very relevant since we need just a small piece of the map. Another drawback might be the limited customizability options. Since we'll be heavily relying on the map, we fear that Google Maps might be too limiting for our needs. Additionally, the free tier pricing is less favorable than Mapbox.

### Mapbox
Mapbox offers vector tiles, unlike Google's raster tiles. This means that the map scales better, making our app more responsive. Mapbox also offers extensive customizability options out of the box, as well as the possibility to self-host map tiles, which can be useful when developing in a local environment. Furthermore, the free tier of Mapbox is substantially larger than that of Google Maps, making it the obvious choice.

### Why not OpenStreetMap with Leaflet
Although OpenStreetMap allows for customization, the available options are not as extensive as Mapbox. It also uses raster tiles and is generally less visually appealing than Mapbox and Google Maps. OSM might be a viable choice; however, it requires significantly more manual work in a project where we have little time to develop the system.
