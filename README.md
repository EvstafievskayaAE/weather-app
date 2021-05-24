# weather-app
Weather-app is client-server application for displaying weather in current city or city, selected from the list. 
### Technologies used:
- OpenWeatherMap API - to get weather data in real time acording to the specified parameters;
- OkHttp - to create server requests;
- Glide - to work with images;
- SQLiteDatabase - to cache data;
- FusedLocationProviderClient - to get and update current location coordinates;
- ConnectivityManager - to check network capabilities on a mobile device.
### Functionality:
- Request permissions to determine the location;
- Determine the current location and add the name of the found city to the list;
- Ability to select a city from the list;
- Displaying weather data for current or chosen city;
- Caching the received data;
- Automatic Internet connection check;
- Loading the spinner while waiting for a response from the server;
- Displaying cached data when there is no internet connection;
- Displaying messages and warnings when there is no internet connection or required permissions.
