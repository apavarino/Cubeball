package me.crylonz;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CubeBallCommandTest {

    @Mock
    private Player player;

    @Mock
    private Command command;

    private CBCommandExecutor executor;
    private CBTabCompletion tabCompletion;
    private TestConfigGateway configGateway;

    @BeforeEach
    void setUp() {
        executor = new CBCommandExecutor();
        tabCompletion = new CBTabCompletion();
        configGateway = new TestConfigGateway();
        CubeBall.configGateway = configGateway;
    }

    @Test
    void reloadCommandRejectsPlayersWithoutPermission() {
        when(command.getName()).thenReturn("cb");
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("cubeball.manage")).thenReturn(false);

        executor.onCommand(player, command, "cd", new String[]{"reload"});

        verify(player).sendMessage("[Cubeball] " + ChatColor.RED + "Unknown command");
    }

    @Test
    void tabCompletionSuggestsReloadForAuthorizedPlayers() {
        when(command.getName()).thenReturn("cb");
        when(player.hasPermission("cubeball.manage")).thenReturn(true);

        List<String> completions = tabCompletion.onTabComplete(player, command, "cb", new String[]{""});

        assertEquals("reload", completions.get(0));
    }

    @Test
    void cbReloadWorksForAuthorizedPlayers() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ball.material", "GOLD_BLOCK");

        when(command.getName()).thenReturn("cb");
        when(player.isOp()).thenReturn(true);
        configGateway.config = config;

        executor.onCommand(player, command, "cb", new String[]{"reload"});

        assertEquals(1, configGateway.reloadCalls);
        verify(player).sendMessage("[Cubeball] " + ChatColor.GREEN + "Configuration reloaded.");
        assertEquals(Material.GOLD_BLOCK, CubeBall.cubeBallBlock);
    }

    private static class TestConfigGateway implements CubeBall.ConfigGateway {
        private YamlConfiguration config = new YamlConfiguration();
        private int reloadCalls = 0;

        @Override
        public void reloadConfig() {
            reloadCalls++;
        }

        @Override
        public YamlConfiguration getConfig() {
            return config;
        }
    }
}
