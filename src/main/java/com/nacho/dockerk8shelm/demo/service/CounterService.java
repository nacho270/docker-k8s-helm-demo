package com.nacho.dockerk8shelm.demo.service;

import com.nacho.dockerk8shelm.demo.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepository counterRepository;

    public Integer increment() {
        log.info("Incrementing...");
        return counterRepository.increment();
    }

    public Integer getCurrent() {
        log.info("Getting current...");
        return counterRepository.getCurrent();
    }
}
