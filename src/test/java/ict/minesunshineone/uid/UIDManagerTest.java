package ict.minesunshineone.uid;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ict.minesunshineone.uid.manager.DatabaseManager;
import ict.minesunshineone.uid.manager.UIDManager;

@ExtendWith(MockitoExtension.class)
public class UIDManagerTest {

    @Mock
    private UIDPlugin plugin;

    @Mock
    private DatabaseManager dbManager;

    @Mock
    private Player player;

    private UIDManager uidManager;

    @BeforeEach
    void setUp() {
        when(plugin.getDatabaseManager()).thenReturn(dbManager);
        uidManager = new UIDManager(plugin);
    }

    @Test
    void generateUID_ShouldCreateNewUID() {
        // Arrange
        UUID playerUUID = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUUID);
        doNothing().when(dbManager).saveUID(any(UUID.class), anyLong());

        // Act
        CompletableFuture<Long> futureUID = uidManager.generateUID(player);

        // Assert
        assertNotNull(futureUID);
        Long uid = futureUID.join();
        assertNotNull(uid);
        verify(dbManager).saveUID(eq(playerUUID), eq(uid));
    }

    @Test
    void getUID_ShouldReturnCachedUID() {
        // Arrange
        UUID playerUUID = UUID.randomUUID();
        long expectedUID = 12345L;
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(dbManager.getUID(playerUUID)).thenReturn(expectedUID);

        // Act
        CompletableFuture<Optional<Long>> futureResult = uidManager.getUID(playerUUID);
        Optional<Long> result = futureResult.join();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUID, result.get());
    }
}
