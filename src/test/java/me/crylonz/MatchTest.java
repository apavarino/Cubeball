package me.crylonz;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MatchTest {

    @Test
    void addPlayerToTeamMovesPlayerBetweenTeams() {
        Match match = new Match();
        Player player = mock(Player.class);

        assertTrue(match.addPlayerToTeam(player, Team.BLUE));
        assertTrue(match.getBlueTeam().contains(player));
        assertFalse(match.getRedTeam().contains(player));
        assertFalse(match.getSpectatorTeam().contains(player));

        assertTrue(match.addPlayerToTeam(player, Team.RED));
        assertFalse(match.getBlueTeam().contains(player));
        assertTrue(match.getRedTeam().contains(player));
        assertFalse(match.getSpectatorTeam().contains(player));

        assertTrue(match.addPlayerToTeam(player, Team.SPECTATOR));
        assertFalse(match.getBlueTeam().contains(player));
        assertFalse(match.getRedTeam().contains(player));
        assertTrue(match.getSpectatorTeam().contains(player));
    }

    @Test
    void addPlayerToTeamRejectsNullPlayer() {
        Match match = new Match();

        assertFalse(match.addPlayerToTeam(null, Team.BLUE));
    }
}
