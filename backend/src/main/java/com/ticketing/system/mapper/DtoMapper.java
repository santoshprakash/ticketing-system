package com.ticketing.system.mapper;

public interface DtoMapper<E, R> {

    R toResponse(E entity);
}
