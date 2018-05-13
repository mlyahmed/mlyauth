package com.primasolutions.idp;

public interface IDomainMapper<E, B> {

    B toBean(E entity);

    E toEntity(B bean);

}
