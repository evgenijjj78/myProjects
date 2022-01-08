package com.game.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;

@Service
public class PlayerServiceImpl implements PlayerService {
	
	@Autowired
	private PlayerRepository repository;
	private List<Player> players;
	private List<Player> filteredPlayers;
	private PlayerOrder order;
	private Integer pageSize;
	private String name, title;
	private Race race;
	private Profession profession;
	private Long after, before;
	private Boolean banned;
	private Integer minExperience, maxExperience, minLevel, maxLevel;

	@Override
	public List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned
			, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber
			, Integer pageSize) {
		
		if (players == null) fillPlayers();
		
		if (this.name != name || this.title != title || this.race != race || this.profession != profession || this.after != after 
				|| this.before != before || this.banned != banned || this.minExperience != minExperience || this.maxExperience != maxExperience
				|| this.minLevel != minLevel || this.maxLevel != maxLevel) 
			filteredPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
		
		order = order != null ? order : PlayerOrder.ID;
		if (pageSize != null) this.pageSize = pageSize;
		if (this.pageSize == null) this.pageSize = 3;
		if (this.order != order) sortedPlayers(order);
		int playersCount = filteredPlayers.size();
		int fromIndex = pageNumber != null ? pageNumber * this.pageSize : 0;
		fromIndex = fromIndex <= playersCount ? fromIndex : playersCount;
		int toIndex = fromIndex + this.pageSize;
		toIndex = toIndex > playersCount ? playersCount : toIndex;
		return filteredPlayers.subList(fromIndex, toIndex);
	}

	@Override
	public int getCount(String name, String title, Race race, Profession profession
			, Long after, Long before, Boolean banned, Integer minExperience
			, Integer maxExperience, Integer minLevel, Integer maxLevel) {
		
		if (this.name != name || this.title != title || this.race != race || this.profession != profession || this.after != after 
				|| this.before != before || this.banned != banned || this.minExperience != minExperience || this.maxExperience != maxExperience
				|| this.minLevel != minLevel || this.maxLevel != maxLevel) 
			filteredPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
		return filteredPlayers.size();
	}
	
	@Override
	public Player getPlayerById(long id) throws PlayerNotFoundException {
		for (Player player : players) {
			if (player.getId() == id) return player;
		}
		throw new PlayerNotFoundException();
	}
	
	@Override
	public Player create(Player player) throws PlayerValidationException {
		validatePlayer(player);
		player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
		player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());
		Player createdPlayer = repository.save(player);
		fillPlayers();
		return createdPlayer;
	}
	
	@Override
	public Player update(Player player, long id) throws PlayerValidationException, PlayerNotFoundException {
		Player updatedPlayer = getPlayerById(id);
		if (player.getName() != null) updatedPlayer.setName(player.getName());
		if (player.getTitle() != null) updatedPlayer.setTitle(player.getTitle());
		if (player.getRace() != null) updatedPlayer.setRace(player.getRace());
		if (player.getProfession() != null) updatedPlayer.setProfession(player.getProfession());
		if (player.getBirthday() != null) updatedPlayer.setBirthday(player.getBirthday());
		if (player.getBanned() != null) updatedPlayer.setBanned(player.getBanned());
		if (player.getExperience() != null && player.getExperience() != updatedPlayer.getExperience()) {
			updatedPlayer.setExperience(player.getExperience());
			updatedPlayer.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
			updatedPlayer.setUntilNextLevel(50 * (updatedPlayer.getLevel() + 1) * (updatedPlayer.getLevel() + 2) - player.getExperience());
		}
		validatePlayer(player);
		repository.save(updatedPlayer);
		fillPlayers();
		return updatedPlayer;
	}
	
	@Override
	public void delete(long id) throws PlayerNotFoundException {
		repository.delete(getPlayerById(id));
		fillPlayers();
	}
	
	private void validatePlayer(Player player) throws PlayerValidationException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		if (player.getName().isEmpty() || player.getName().length() > 12 || player.getTitle().isEmpty() || player.getTitle().length() > 30 
				|| player.getBirthday() == null || Integer.parseInt(sdf.format(player.getBirthday())) < 2000 
				|| Integer.parseInt(sdf.format(player.getBirthday())) > 3000 || player.getExperience() == null || player.getExperience() < 0 
				|| player.getExperience() > 10000000) throw new PlayerValidationException();
	}
	
	private void fillPlayers() {
		players = new ArrayList<>();
		filteredPlayers = new ArrayList<>();
		players.addAll((List<Player>) repository.findAll());
		filteredPlayers.addAll(players);
		order = PlayerOrder.ID;
	}
	
	private void filteredPlayers(String name, String title, Race race, Profession profession
			, Long after, Long before, Boolean banned, Integer minExperience
			, Integer maxExperience, Integer minLevel, Integer maxLevel) {
		filteredPlayers = new ArrayList<>();
		if(name != null || title != null || race != null || profession != null || after != null || before != null 
				|| minExperience != null || maxExperience != null || minLevel != null || maxLevel != null) {
			for (Player player : players) {
				if (name != null && !player.getName().contains(name)) continue;
				if (title != null && !player.getTitle().contains(title)) continue;
				if (race != null && player.getRace() != race) continue;
				if (profession != null && player.getProfession() != profession) continue;
				if (after != null && player.getBirthday().getTime() <= after) continue;
				if (before != null && player.getBirthday().getTime() >= before) continue;
				if (banned != null && player.getBanned() != banned) continue;
				if (minExperience != null && player.getExperience() < minExperience) continue;
				if (maxExperience != null && player.getExperience() > maxExperience) continue;
				if (minLevel != null && player.getLevel() < minLevel) continue;
				if (maxLevel != null && player.getLevel() > maxLevel) continue;
				filteredPlayers.add(player);
			}
		} else filteredPlayers.addAll(players);
		this.name = name; this.title = title; this.race = race; this.profession = profession;
		this.after = after; this.before = before; this.banned = banned; this.minExperience = minExperience;
		this.maxExperience = maxExperience; this.minLevel = minLevel; this.maxLevel = maxLevel;
	}
	
	private void sortedPlayers(PlayerOrder order) {
		if (this.order != order) { 
			Collections.sort(filteredPlayers, new Comparator<Player>() {
				@Override 
				public int compare(Player player1, Player player2) {
					switch (order) {
						case NAME:
							return player1.getName().compareTo(player2.getName());
						case EXPERIENCE:
							return player1.getExperience().compareTo(player2.getExperience());
						case BIRTHDAY:
							return player1.getBirthday().compareTo(player2.getBirthday());
						default:
							return player1.getId().compareTo(player2.getId());
					}
				}
			});
			this.order = order;
		}
	}
}
