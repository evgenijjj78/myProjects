package com.game.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.game.entity.Player;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> { }
