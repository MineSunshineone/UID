package ict.minesunshineone.uid.util;

import org.bukkit.configuration.file.FileConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ict.minesunshineone.uid.UIDPlugin;
import ict.minesunshineone.uid.exception.UIDGenerationException;

@ExtendWith(MockitoExtension.class)
class UIDGeneratorTest {

    @Mock
    private UIDPlugin plugin;

    @Mock
    private FileConfiguration config;

    private UIDGenerator generator;

    @BeforeEach
    void setUp() {
        when(plugin.getConfig()).thenReturn(config);
        when(config.getInt("uid.random-allocation-count", 100)).thenReturn(10);
        when(config.getInt("uid.random-min", 1)).thenReturn(1);
        when(config.getInt("uid.random-max", 9999)).thenReturn(100);
        when(config.getInt("uid.sequential-start", 10000)).thenReturn(101);
        when(config.getInt("uid.format.digits", 4)).thenReturn(4);
        when(config.getString("uid.format.pad-char", "0")).thenReturn("0");

        generator = new UIDGenerator(plugin);
    }

    @Test
    void testRandomUIDGeneration() throws UIDGenerationException {
        long uid = generator.generateUID(5);
        assertTrue(uid >= 1 && uid <= 100, "随机UID应该在配置的范围内");
    }

    @Test
    void testSequentialUIDGeneration() throws UIDGenerationException {
        long uid = generator.generateUID(10);
        assertEquals(101, uid, "顺序UID应该从配置的起始值开始");
    }
}
