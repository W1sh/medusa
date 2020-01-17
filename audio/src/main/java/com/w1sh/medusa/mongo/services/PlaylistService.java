package com.w1sh.medusa.mongo.services;

import com.w1sh.medusa.mongo.entities.Playlist;
import com.w1sh.medusa.mongo.repos.PlaylistRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlaylistService {

    private final PlaylistRepo playlistRepo;

    public PlaylistService(PlaylistRepo playlistRepo) {
        this.playlistRepo = playlistRepo;
    }

    public Flux<Playlist> findAll(){
        return playlistRepo.findAll();
    }

    public Mono<Playlist> save(Playlist playlistMono){
        return playlistRepo.save(playlistMono);
    }
}
