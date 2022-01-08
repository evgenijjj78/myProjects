package com.game.service;

import java.util.List;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

public interface PlayerService {
	List<Player> getPlayers(String name, String title, Race race, Profession profession
			, Long after, Long before, Boolean banned, Integer minExperience
			, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order
			, Integer pageNumber, Integer pageSize);
	int getCount(String name, String title, Race race, Profession profession
			, Long after, Long before, Boolean banned, Integer minExperience
			, Integer maxExperience, Integer minLevel, Integer maxLevel);
	Player getPlayerById(long id) throws PlayerNotFoundException;
	Player create(Player player) throws PlayerValidationException;
	Player update(Player player, long id) throws PlayerValidationException, PlayerNotFoundException;
	void delete(long id) throws PlayerNotFoundException;
}