package com.game.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.game.service.exceptions.PlayerNotFoundException;
import com.game.service.exceptions.PlayerValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;

@Service
public class PlayerServiceImpl implements PlayerService {

	private final PlayerRepository repository;
	private List<Player> filteredPlayers;
	String name, title;
	Race race; Profession profession; PlayerOrder order;
	Integer minExperience, maxExperience, minLevel, maxLevel, pageSize;
	Long after, before;
	Boolean banned;

	@Autowired
	public PlayerServiceImpl(PlayerRepository repository) {
		this.repository = repository;
		filteredPlayers = (List<Player>) repository.findAll();
		order = PlayerOrder.ID;
		pageSize = 3;
		banned = false;
	}

	@Override
	public List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned
			, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber
			, Integer pageSize) {
		int playersCount = getCount(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
		order = order != null ? order : this.order;
		if (this.order != order) sortedPlayers(order);
		this.order = order;
		pageSize = pageSize != null ? pageSize : this.pageSize;
		int fromIndex = pageNumber != null ? pageNumber * pageSize : 0;
		fromIndex = Math.min(fromIndex, playersCount);
		int toIndex = fromIndex + pageSize;
		toIndex = Math.min(toIndex, playersCount);
		return filteredPlayers.subList(fromIndex, toIndex);
	}

	@Override
	public int getCount(String name, String title, Race race, Profession profession
			, Long after, Long before, Boolean banned, Integer minExperience
			, Integer maxExperience, Integer minLevel, Integer maxLevel) {
		filteredPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order);
		return filteredPlayers.size();
	}
	
	@Override
	public Player getPlayerById(Long id) throws PlayerNotFoundException, PlayerValidationException {
		if (id == null || id == 0) throw new PlayerValidationException();
		for (Player player : repository.findAll()) {
			if (player.getId().longValue() == id) return player;
		}
		throw new PlayerNotFoundException();
	}
	
	@Override
	public Player create(Player player) throws PlayerValidationException {
		validatePlayer(player);
		player.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
		player.setUntilNextLevel(50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience());
		Player createdPlayer = repository.save(player);
		filteredPlayers.add(createdPlayer);
		sortedPlayers(order);
		return createdPlayer;
	}
	
	@Override
	public Player update(Player player, Long id) throws PlayerValidationException, PlayerNotFoundException {
		Player updatedPlayer = getPlayerById(id);
		if (player.getName() != null) updatedPlayer.setName(player.getName());
		if (player.getTitle() != null) updatedPlayer.setTitle(player.getTitle());
		if (player.getRace() != null) updatedPlayer.setRace(player.getRace());
		if (player.getProfession() != null) updatedPlayer.setProfession(player.getProfession());
		if (player.getBirthday() != null) updatedPlayer.setBirthday(player.getBirthday());
		if (player.getBanned() != null) updatedPlayer.setBanned(player.getBanned());
		if (player.getExperience() != null && !player.getExperience().equals(updatedPlayer.getExperience())) {
			updatedPlayer.setExperience(player.getExperience());
			updatedPlayer.setLevel((int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100));
			updatedPlayer.setUntilNextLevel(50 * (updatedPlayer.getLevel() + 1) * (updatedPlayer.getLevel() + 2) - player.getExperience());
			validatePlayer(updatedPlayer);
			repository.save(updatedPlayer);
		}
		return updatedPlayer;
	}
	
	@Override
	public void delete(long id) throws PlayerNotFoundException, PlayerValidationException {
		getPlayerById(id);
		repository.deleteById(id);
	}
	
	private void validatePlayer(Player player) throws PlayerValidationException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		if (player.getName() == null || player.getName().isEmpty() || player.getName().length() > 12 || player.getTitle() == null 
				|| player.getTitle().isEmpty() || player.getTitle().length() > 30 || player.getBirthday() == null 
				|| Integer.parseInt(sdf.format(player.getBirthday())) < 2000 || Integer.parseInt(sdf.format(player.getBirthday())) > 3000 
				|| player.getExperience() == null || player.getExperience() < 0 || player.getExperience() > 10000000) 
			throw new PlayerValidationException();
	}
	
	private void filteredPlayers(String name, String title, Race race, Profession profession
			, Long after, Long before, Boolean banned, Integer minExperience
			, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order) {
		if (!Objects.equals(this.name, name) || !Objects.equals(this.title, title) || this.race != race || this.profession != profession || !Objects.equals(this.after, after)
				|| !Objects.equals(this.before, before) || this.banned != banned || !Objects.equals(this.minExperience, minExperience)
				|| !Objects.equals(this.maxExperience, maxExperience) || !Objects.equals(this.minLevel, minLevel) || !Objects.equals(this.maxLevel, maxLevel)) {
			filteredPlayers = new ArrayList<>();
			for (Player player : repository.findAll()) {
				if (name != null && !player.getName().contains(name)) continue;
				if (title != null && !player.getTitle().contains(title)) continue;
				if (race != null && player.getRace() != race) continue;
				if (profession != null && player.getProfession() != profession) continue;
				if (after != null && player.getBirthday().getTime() <= after) continue;
				if (before != null && player.getBirthday().getTime() >= before) continue;
				if (banned != null && !player.getBanned().equals(banned)) continue;
				if (minExperience != null && player.getExperience() < minExperience) continue;
				if (maxExperience != null && player.getExperience() > maxExperience) continue;
				if (minLevel != null && player.getLevel() < minLevel) continue;
				if (maxLevel != null && player.getLevel() > maxLevel) continue;
				filteredPlayers.add(player);
			}
			this.name = name; this.title = title; this.race = race; this.profession = profession; this.after = after; this.before = before;
			this.banned = banned; this.minExperience = minExperience; this.maxExperience = maxExperience; this.minLevel = minLevel; this.maxLevel = maxLevel;
		}
		if (order != PlayerOrder.ID) sortedPlayers(order);
	}
	
	private void sortedPlayers(PlayerOrder order) {
		filteredPlayers.sort((Player player1, Player player2) -> {
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
		});
	}
}
