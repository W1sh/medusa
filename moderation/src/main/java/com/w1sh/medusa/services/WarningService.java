package com.w1sh.medusa.services;

import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.repos.WarningRepository;
import com.w1sh.medusa.services.cache.MemoryCache;
import com.w1sh.medusa.services.cache.MemoryCacheBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarningService {

    private final WarningRepository repository;
    private final MemoryCache<Integer, List<Warning>> cache;

    public WarningService(WarningRepository repository) {
        this.repository = repository;
        this.cache = new MemoryCacheBuilder<Integer, List<Warning>>()
                .fetch(userId -> repository.findAllByUser(userId)
                        .collectList())
                .build();
    }
}
