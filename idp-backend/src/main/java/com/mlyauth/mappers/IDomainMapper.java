package com.mlyauth.mappers;

public interface IDomainMapper<E, B> {

    B toBean(E entity);

    E toEntity(B bean);

}
