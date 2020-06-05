package com.w1sh.medusa.mappers;

public interface Mapper<T, K> {

    K map(T source);

    K map(T source, K destination);
}
