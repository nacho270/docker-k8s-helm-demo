package com.nacho.dockerk8shelm.demo.repository;

public interface CounterRepository {
    Integer increment();
    Integer getCurrent();
}
