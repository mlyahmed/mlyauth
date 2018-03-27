package com.mlyauth;

public interface IDomainMapper<E, B> {

    B toBean(E entity);

    E toEntity(B bean);

}
