package ru.practicum.mapper.location;

import ru.practicum.model.location.Location;
import ru.practicum.model.location.dtos.LocationDto;

public class LocationMapper {
    public static Location mapToLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());

        return location;
    }

    public static LocationDto mapToLocationDto(Location location) {
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());

        return locationDto;
    }

}
